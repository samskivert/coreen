//
// $Id$

package coreen.model;

/**
 * Extends {@link Def} with signature and documentation.
 */
public class DefInfo extends Def
{
    /** This def's signature. */
    public String sig;

    /** The uses that occur in the signature. */
    public Use[] sigUses;

    /** This def's documentation. */
    public String doc;
}
