//
// $Id$

package coreen.model;

/**
 * Contains detail information for a type and basic information for its members.
 */
public class TypeSummary extends DefDetail
{
    /** Type members of this type. */
    public DefInfo[] types;

    /** Function members of this type. */
    public DefInfo[] funcs;

    /** Term members of this type. */
    public DefInfo[] terms;
}
