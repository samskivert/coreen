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

    /** A flag indicating whether a def is inherited by subtypes or not. */
    public static final int INHERITED = 0x1 << 1;

    /**
     * Returns true if the flags indicate public access, false otherwise.
     */
    public static boolean isPublic (int flags)
    {
        return (flags & PUBLIC) != 0;
    }

    /**
     * Returns true if the flags indicate inheritedness by subtypes, false otherwise.
     */
    public static boolean isInherited (int flags)
    {
        return (flags & INHERITED) != 0;
    }

    /** The flavor of this def. */
    public Flavor flavor;

    /** Flags associated with this def. */
    public int flags;

    /** A unique identifier for this definition's enclosing def (or 0 if they have none). */
    public long outerId;

    /** The id of this def's primary super def (or 0 if they have none). */
    public long superId;

    /**
     * Returns true if this def has public access, false otherwise.
     */
    public boolean isPublic ()
    {
        return isPublic(flags);
    }

    /**
     * Returns true if this def is inherited by subtypes, false otherwise.
     */
    public boolean isInherited ()
    {
        return isInherited(flags);
    }
}
