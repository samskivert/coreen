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

    /** This def's signature. */
    public String sig;

    /** This def's documentation. */
    public String doc;
}
