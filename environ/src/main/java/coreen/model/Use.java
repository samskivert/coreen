//
// $Id$

package coreen.model;

/**
 * Identifies the use of a definition that exists in a source file.
 */
public class Use extends Span
{
    /** A unique identifier for this use (1 or higher). */
    public final int id;

    /** The immediately enclosing definition in which this use occurs. */
    public final Def owner;

    /** The definition of the referent of this use. */
    public final Def referent;

    public Use (int id, Def owner, Def referent, int start, int length) {
        super(start, length);
        this.id = id;
        this.owner = owner;
        this.referent = referent;
    }
}
