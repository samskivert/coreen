//
// $Id$

package coreen.model;

/**
 * Models a project (a collection of source files as far as Coreen is concerned).
 */
public class Project
{
    /** A unique identifier for this project (1 or higher). */
    public final long id;

    /** The (human readable) name of this project. */
    public final String name;

    /** The path to the root of this project. */
    public final String rootPath;

    public Project (long id, String name, String rootPath) {
        this.id = id;
        this.name = name;
        this.rootPath = rootPath;
    }

    @Override // from Object
    public String toString () {
        return new StringBuffer("[id=").append(id).
            append(", name=").append(name).
            append(", rootPath=").append(rootPath).
            append("]").toString();
    }
}
