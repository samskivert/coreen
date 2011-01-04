//
// $Id$

package coreen.model;

/**
 * Contains detail information for a def as well as info on all of its members.
 */
public class MemberInfo extends DefDetail
{
    /** Info on the members of this def (sorted by flavor, name). */
    public DefInfo[] members;
}
