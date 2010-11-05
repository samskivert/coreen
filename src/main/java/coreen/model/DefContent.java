//
// $Id$

package coreen.model;

/**
 * Contains metadata, source text and nested defs and uses for a single definition.
 */
public class DefContent extends DefDetail
{
    /** The source text for this def. */
    public String text;

    /** All defs that are children of this def (and their children). */
    public DefId[] defs;

    /** All uses that occur with this def (or its children). */
    public Use[] uses;
}
