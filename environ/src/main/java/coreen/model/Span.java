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

    public Span (int start, int length)
    {
        this.start = start;
        this.length = length;
    }

    // used when unserializing
    public Span () {}

    @Override // from Object
    public String toString ()
    {
        return start + ":" + length;
    }
}
