//
// $Id$

package coreen.model;

import java.io.Serializable;

/**
 * Identifies a span of characters in a source file.
 */
public class Span
    implements Serializable
{
    /** The character offset in the source file at which this span starts. */
    public int start;

    /** The length of this span in characters. */
    public int length;

    /** Creates and initializes this instance. */
    public Span (int start, int length)
    {
        this.start = start;
        this.length = length;
    }

    /** Used when unserializing. */
    public Span () {}

    /** * Offsets this span by the specified number of characters. */
    public void adjust (int delta)
    {
        start += delta;
    }

    @Override // from Object
    public String toString ()
    {
        return start + ":" + length;
    }
}
