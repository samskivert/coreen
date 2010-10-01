//
// $Id$

package coreen.model;

import java.io.Serializable;

/**
 * Provides detailed information for a single compilation unit.
 */
public class CompUnitDetail
    implements Serializable
{
    /** The metadata for this unit. */
    public CompUnit unit;

    /** The text of the compilation unit. */
    public String text;

    /** The defs that occur in this compilation unit (sorted in occurrence order). */
    public Def[] defs;

    /** The uses that occur in this compilation unit (sorted in occurrence order). */
    public Use[] uses;
}
