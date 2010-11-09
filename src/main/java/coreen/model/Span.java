//
// $Id$

package coreen.model;

/**
 * An interface implemented by any model element that refers to some span of source text.
 */
public interface Span
{
    /** Returns the id of the def to which this span refers. */
    public long getId ();

    /** Returns the kind of the id that is referred to by this span. */
    public Kind getKind ();

    /** Returns the character offset into the source text at which this span starts. */
    public int getStart ();

    /** Returns the length of this span in characters. */
    public int getLength ();
}
