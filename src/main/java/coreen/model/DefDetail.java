//
// $Id$

package coreen.model;

import java.io.Serializable;

/**
 * Contains detailed information on a particular definition.
 */
public class DefDetail
    implements Serializable
{
    /** The standard information about this def. */
    public Def def;

    /** The compunit to which this def belongs. */
    public CompUnit unit;

    /** The path to this def from most outer type to immediately enclosing. */
    public TypedId[] path;

    /** This def's signature. */
    public String sig;

    /** This def's documentation. */
    public String doc;

    /** Returns the id of outermost type (not module) that encloses this def. */
    public long outerTypeId ()
    {
        for (TypedId tid : path) {
            if (tid.type == Def.Type.TYPE) {
                return tid.id;
            }
        }
        return def.id; // we're our own outermost type
    }

    /** Returns the id of the outermost member that encloses this def or 0L if this def is itself a
     * top-level type. */
    public long outerMemberId ()
    {
        long outerTypeId = outerTypeId();
        if (outerTypeId == def.id) {
            return 0L; // we're an outermost type, so we have no enclosing member
        }
        boolean returnNext = false;
        for (TypedId tid : path) {
            if (returnNext) {
                return tid.id;
            } else if (tid.id == outerTypeId) {
                returnNext = true;
            }
        }
        return def.id; // we're our own outermost member
    }
}
