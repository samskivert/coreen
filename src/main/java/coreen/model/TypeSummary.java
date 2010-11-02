//
// $Id$

package coreen.model;

/**
 * Contains detail information for a type and basic information for its members.
 */
public class TypeSummary extends DefDetail
{
    /** This type's primary supertypes, ordered from nearest to furthest. */
    public Def[] supers;

    /** Members of this type. */
    public DefInfo[] members;
}
