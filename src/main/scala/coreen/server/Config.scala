//
// $Id$

package coreen.server

import scala.collection.mutable.{Map => MMap}

import org.squeryl.PrimitiveTypeMode._

import coreen.model.ConfigData
import coreen.persist.{DB, Setting}

/** Provides server configuration. */
trait Config
{
  this :Dirs =>

  /** Defines the actions that can be taken on server configuration. */
  trait ConfigService {
    /** Returns the hostname to which the server should bind its HTTP socket. */
    def getHttpHostname = get(ConfigData.HTTP_HOSTNAME, ConfigData.DEFAULT_HTTP_HOSTNAME)

    /** Returns the port to which the server should bind its HTTP socket. */
    def getHttpPort = if (!_appdir.isDefined) 8081 // if we're running in dev mode, use 8081
                      else get(ConfigData.HTTP_PORT, ConfigData.DEFAULT_HTTP_PORT)

    /** Returns a boolean configuration value. */
    def get (key :String, defval :Boolean) :Boolean =
      get(key, String.valueOf(defval)).toBoolean

    /** Returns an integer configuration value. */
    def get (key :String, defval :Int) :Int =
      get(key, String.valueOf(defval)).toInt

    /** Returns a long configuration value. */
    def get (key :String, defval :Long) :Long =
      get(key, String.valueOf(defval)).toLong

    /** Returns a string configuration value. */
    def get (key :String, defval :String) :String

    /** Updates a primitive configuration value. */
    def update[T <: AnyVal] (key :String, value :T) {
      update(key, String.valueOf(value))
    }

    /** Updates a string configuration value. */
    def update (key :String, value :String) :Unit

    /** Returns a snapshot of all configuration settings. */
    def getSnapshot :Map[String, String]
  }

  /** Provides server configuration. */
  val _config :ConfigService
}

/** A concrete implementation of {@link Log}. */
trait ConfigComponent extends Component with Config {
  this :DB with Dirs =>

  val _config = new ConfigService {
    def get (key :String, defval :String) :String = _cdata.getOrElse(key, defval)
    def update (key :String, value :String) {
      // update our local cache
      _cdata.update(key, value)
      // and update the database
      transaction {
        if (_db.settings.update(s => where(s.key === key) set(s.value := value)) == 0) {
          _db.settings.insert(Setting(key, value));
        }
      }
    }
    def getSnapshot = Map() ++ _cdata
  }

  override protected def initComponents {
    super.initComponents
    transaction {
      // read in all existing configuration values
      _cdata ++= _db.settings map(s => (s.key, s.value))
    }
  }

  private val _cdata = MMap[String,String]()
}
