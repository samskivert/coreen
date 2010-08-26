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

    protected Span (int start, int length) {
        this.start = start;
        this.length = length;
    }
}
