//
// $Id$

package coreen.model;

/**
 * Extends {@link Def} with signature and documentation.
 */
public class DefInfo extends Def
{
    /** The maximum number of characters allowed for a single def's signature. */
    public static final int MAX_SIG_LENGTH = 1024;

    /** The maximum number of characters allowed for a single def's documentation. */
    public static final int MAX_DOC_LENGTH = 32768;

    /** This def's signature. */
    public String sig;

    /** Minimal info on the defs that occur in the signature. */
    public SigDef[] sigDefs;

    /** The uses that occur in the signature. */
    public Use[] sigUses;

    /** This def's documentation. */
    public String doc;

    /** The uses that occur in the documentation. */
    public Use[] docUses;
}
