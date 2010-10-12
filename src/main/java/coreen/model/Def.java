//
// $Id$

package coreen.model;

/**
 * Extends the basic {@link DefId} with parent id and the position in the source text.
 */
public class Def extends DefId
{
    /** A unique identifier for this definition's enclosing def (or 0 if they have none). */
    public long parentId;

    /** The character offset in the source file at which this def starts. */
    public int start;
}
