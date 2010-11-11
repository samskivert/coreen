//
// $Id$

package coreen.model;

/**
 * Extends {@link DefInfo} with info on compunit and ids of all enclosing defs.
 */
public class DefDetail extends DefInfo
{
    /** The compunit to which this def belongs. */
    public CompUnit unit;

    /** The path to this def from most outer type to immediately enclosing. */
    public DefId[] path;

    /** Returns the id of outermost type (not module) that encloses this def. */
    public long outerTypeId ()
    {
        for (DefId tid : path) {
            if (tid.kind == Kind.TYPE) {
                return tid.id;
            }
        }
        return id; // we're our own outermost type
    }

    /** Returns the id of the outermost member that encloses this def or 0L if this def is itself a
     * top-level type. */
    public long outerMemberId ()
    {
        long outerTypeId = outerTypeId();
        if (outerTypeId == id) {
            return 0L; // we're an outermost type, so we have no enclosing member
        }
        boolean returnNext = false;
        for (DefId tid : path) {
            if (returnNext) {
                return tid.id;
            } else if (tid.id == outerTypeId) {
                returnNext = true;
            }
        }
        return id; // we're our own outermost member
    }
}
