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

    /** The id of the project to which this def belongs, or -1 if it's an unknown def. */
    public long projectId;

    /** The id of the compunit to which this def belongs, or -1 if it's an unknown def. */
    public long unitId;

    /** This def's signature. */
    public String sig;

    /** This def's documentation. */
    public String doc;
}
