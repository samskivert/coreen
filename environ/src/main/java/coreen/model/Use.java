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

    /** The definition id of the referent of this use. */
    public final int referent;

    public Span (int id, int referent, int start, int length) {
        super(start, length);
        this.id = id;
        this.referent = referent;
    }
}
