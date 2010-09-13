//
// $Id$

package coreen.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Models a project (a collection of source files as far as Coreen is concerned).
 */
public class Project
    implements Serializable
{
    /** A unique identifier for this project (1 or higher). */
    public long id;

    /** The (human readable) name of this project. */
    public String name;

    /** The path to the root of this project. */
    public String rootPath;

    /** A string identifying the imported version of this project. */
    public String version;

    /** When this project was imported into the library. */
    public Date imported;

    /** When this project was last updated. */
    public Date lastUpdated;

    /** Creates and initializes this instance. */
    public Project (long id, String name, String rootPath, String version,
                    Date imported, Date lastUpdated)
    {
        this.id = id;
        this.name = name;
        this.rootPath = rootPath;
        this.version = version;
        this.imported = imported;
        this.lastUpdated = lastUpdated;
    }

    /** Used when unserializing. */
    public Project () {}

    @Override // from Object
    public String toString ()
    {
        return new StringBuffer("[id=").append(id).
            append(", name=").append(name).
            append(", rootPath=").append(rootPath).
            append(", version=").append(version).
            append(", imported=").append(imported).
            append(", lastUpdated=").append(lastUpdated).
            append("]").toString();
    }
}
