//
// $Id$

package coreen.model;

/**
 * Provides details on a type.
 */
public class TypeDetail extends DefDetail
{
    /** Type members of this type. */
    public Def[] types;

    /** Function members of this type. */
    public Def[] funcs;

    /** Term members of this type. */
    public Def[] terms;
}
