//
// $Id$

package coreen.project

import java.io.{File, StringReader}
import java.net.URI
import java.util.concurrent.Callable

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.xml.{XML, Elem}

import org.squeryl.PrimitiveTypeMode._

import coreen.nml.SourceModel
import coreen.nml.SourceModel._
import coreen.persist.{DB, Project, CompUnit}
import coreen.server.{Log, Exec, Dirs}

/** Provides project updating services. */
trait Updater {
  this :Log with Exec with DB with Dirs =>

  /** Handles updating projects. */
  object _updater {
    /**
     * (Re)imports the contents of the specified project. This includes:
     * <ul>
     *  <li>scanning the root path of the supplied project for compilation units</li>
     *  <li>grouping them by language</li>
     *  <li>running the appropriate readers to convert them to name-resolved form</li>
     *  <li>clearing the current project contents from the database</li>
     *  <li>loading the name-resolved metadata into the database</li>
     * </ul>
     * This is very disk and compute intensive and should be done on a background thread.
     *
     * @param log a callback function which will be passed log messages to communicate status.
     */
    def update (p :Project, ulog :String=>Unit = noop => ()) {
      ulog("Finding compilation units...")

      // first figure out what sort of source files we see in the project
      val types = collectFileTypes(new File(p.rootPath))

      // fire up readers to handle all types of files we find in the project
      val readers = Map() ++ (types flatMap(t => readerForType(t) map(r => (t -> r))))
      ulog("Processing compilation units of type " + readers.keySet.mkString(", ") + "...")
      readers.values map(_.invoke(p, ulog))
    }

    abstract class Reader {
      def invoke (p :Project, ulog :String=>Unit) {
        _log.info("Invoking reader: " + (args :+ p.rootPath).mkString(" "))
        val proc = Runtime.getRuntime.exec((args :+ p.rootPath).toArray)

        // read stderr on a separate thread so that we can ensure that stdout and stderr are both
        // actively drained, preventing the process from blocking
        val errLines = _exec.submit(new Callable[Array[String]] {
          def call = Source.fromInputStream(proc.getErrorStream).getLines.toArray
        })

        // consume stdout from the reader, accumulating <compunit ...>...</compunit> into a buffer
        // and processing each complete unit that we receive; anything in between compunit elements
        // is reported verbatim to the status log
        val cus = parseCompUnits(p, ulog, Source.fromInputStream(proc.getInputStream).getLines)

        // now that we've totally drained stdout, we can wait for stderr output and log it
        errLines.get.foreach(ulog)

        // report any error status code (TODO: we probably don't really need to do this)
        val ecode = proc.waitFor
        if (ecode != 0) {
          ulog("Reader exited with status: " + ecode)
          return // leave the project as is; TODO: maybe not if this is the first import...
          }

        // determine which CUs we knew about before
        val oldCUs = transaction { _db.compunits where(cu => cu.projectId === p.id) toList }

        // update compunit data, and construct a mapping from compunit path to id
        val newPaths = Set() ++ (cus map(_.src))
        val toDelete = oldCUs filterNot(cu => newPaths(cu.path)) map(_.id) toSet
        val toAdd = newPaths -- (oldCUs map(_.path))
        val toUpdate = oldCUs filterNot(cu => toDelete(cu.id)) map(_.id) toSet
        val cuIds = collection.mutable.Map[String,Long]()
        transaction {
          if (!toDelete.isEmpty) {
            _db.compunits.deleteWhere(cu => cu.id in toDelete)
            ulog("Removed " + toDelete.size + " obsolete compunits.")
          }
          val now = System.currentTimeMillis
          if (!toAdd.isEmpty) {
            val added = toAdd.map(CompUnit(p.id, _, now))
            _db.compunits.insert(added)
            // add the ids of the newly inserted units to our (path -> id) mapping
            added foreach { cu => cuIds += (cu.path -> cu.id) }
            ulog("Added " + toAdd.size + " new compunits.")
          }
          if (!toUpdate.isEmpty) {
            _db.compunits.update(cu =>
              where(cu.id in toUpdate) set(cu.lastUpdated := now))
            // add the ids of the updated units to our (path -> id) mapping
            oldCUs filter(cu => toUpdate(cu.id)) foreach { cu => cuIds += (cu.path -> cu.id) }
            ulog("Updated " + toUpdate.size + " compunits.")
          }
        }

        // process each compunit individually
        for (cu <- cus) {
          processCompUnit(cuIds(cu.src), cu)
        }

        // // update def and use data: first resolve the def/use graph
        // cus foreach { cu =>
        //   println("Source: " + cu.src)
        //   cu.defs map dumpDef("")
        // }

      }

      def dumpDef (prefix :String)(df :DefElem) {
        println(prefix + df.name + " " + df.typ)
        df.defs map dumpDef(prefix + df.name + ".")
      }

      def parseCompUnits (p :Project, ulog :String=>Unit, lines :Iterator[String]) = {
        // obtain a sane prefix we can use to relativize the comp unit source URIs
        val uriRoot = new File(p.rootPath).getCanonicalFile.toURI.getPath
        assert(uriRoot.endsWith("/"))

        var accmode = false
        var accum = new StringBuilder
        val cubuf = ArrayBuffer[CompUnitElem]()
        for (line <- lines) {
          accmode = accmode || line.trim.startsWith("<compunit");
          if (!accmode) ulog(line)
          else {
            accum.append(line)
            accmode = !line.trim.startsWith("</compunit>")
            if (!accmode) {
              try {
                val cu = SourceModel.parse(XML.load(new StringReader(accum.toString)))
                val curi = new URI(cu.src)
                if (curi.getPath.startsWith(uriRoot))
                  cu.src = curi.getPath.substring(uriRoot.length)
                cubuf += cu
              } catch {
                case e => ulog("Error parsing reader output [" + e + "]: " +
                               truncate(accum.toString, 100))
              }
              accum.setLength(0)
            }
          }
        }
        cubuf.toList
      }

      def args :List[String]
    }

    def processCompUnit (cuId :Long, cu :CompUnitElem) {
      println("Processing " + cuId + " -> " + cu)
    }

    class JavaReader (
      classname :String, classpath :List[File], javaArgs :List[String]
    ) extends Reader {
      val javabin = mkFile(new File(System.getProperty("java.home")), "bin", "java")
      def args = (javabin.getCanonicalPath :: "-classpath" ::
                  classpath.map(_.getAbsolutePath).mkString(File.pathSeparator) ::
                  classname :: javaArgs)
    }

    def mkFile (root :File, path :String*) = (root /: path)(new File(_, _))

    def getToolsJar = {
      val jhome = new File(System.getProperty("java.home"))
      val tools = mkFile(jhome.getParentFile, "lib", "tools.jar")
      val classes = mkFile(jhome.getParentFile, "Classes", "classes.jar")
      if (tools.exists) tools
      else if (classes.exists) classes
      else error("Can't find tools.jar or classes.jar")
    }

    def createJavaJavaReader = _appdir match {
      case Some(appdir) => new JavaReader(
        "coreen.java.Main",
        List(getToolsJar, mkFile(appdir, "code", "coreen-java-reader.jar")),
        List())
      case None => new JavaReader(
        "coreen.java.Main",
        List(getToolsJar,
             mkFile(new File("java-reader"), "target", "scala_2.8.0",
                    "coreen-java-reader_2.8.0-0.1.min.jar")),
        List())
    }

    def readerForType (typ :String) :Option[Reader] = typ match {
      case "java" => Some(createJavaJavaReader)
      case _ => None
    }

    def collectFileTypes (file :File) :Set[String] = {
      def suffix (name :String) = name.substring(name.lastIndexOf(".")+1)
      if (file.isDirectory) file.listFiles.toSet flatMap(collectFileTypes)
      else Set(suffix(file.getName))
    }

    def truncate (text :String, length :Int) =
      if (text.length <= length) text
      else text.substring(0, length) + "..."
  }
}
