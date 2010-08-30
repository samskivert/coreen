//
// $Id$

package coreen.model;

import java.io.Serializable;

/**
 * Identifies a span of characters in a source file.
 */
public abstract class Span
    implements Serializable
{
    /** The character offset in the source file at which this span starts. */
    public final int start;

    /** The length of this span in characters. */
    public final int length;

    @Override // from Object
    public String toString () {
        return toString(new StringBuffer("[")).append("]").toString();
    }

    protected Span (int start, int length) {
        this.start = start;
        this.length = length;
    }

    protected StringBuffer toString (StringBuffer buf) {
        return buf.append("start=").append(start).append(", length=").append(length);
    }
}
