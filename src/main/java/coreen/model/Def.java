//
// $Id$

package coreen.model;

/**
 * Extends the basic {@link DefId} with parent id and the position in the source text.
 */
public class Def extends DefId
{
    /** A flag distinguishing defs with public access versus those with non-public. */
    public static final int PUBLIC = 0x1 << 0;

    /** The kind of this def. */
    public Kind kind;

    /** Flags associated with this def. */
    public int flags;

    /** A unique identifier for this definition's enclosing def (or 0 if they have none). */
    public long outerId;

    /** The id of this def's primary super def (or 0 if they have none). */
    public long superId;

    /** The character offset in the source file at which this def starts. */
    public int start;

    /**
     * Returns true if this def has public access, false otherwise.
     */
    public boolean isPublic ()
    {
        return (flags & PUBLIC) != 0;
    }
}
