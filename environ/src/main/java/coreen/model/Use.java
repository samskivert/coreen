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

    /** The id of the immediately enclosing definition in which this use occurs. */
    public final int ownerId;

    /** The id of the definition of the referent of this use. */
    public final int referentId;

    public Use (int id, int ownerId, int referentId, int start, int length) {
        super(start, length);
        this.id = id;
        this.ownerId = ownerId;
        this.referentId = referentId;
    }
}
