//
// $Id$

package coreen.model;

/**
 * Contains detail information for a type and basic information for its members.
 */
public class TypeSummary extends DefDetail
{
    /** Contains information for a single member. */
    public static class Member extends Def {
        /** The signature of this def. */
        public String sig;
        /** The documentation for this def. */
        public String doc;
    }

    /** Type members of this type. */
    public Member[] types;

    /** Function members of this type. */
    public Member[] funcs;

    /** Term members of this type. */
    public Member[] terms;
}
