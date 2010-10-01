//
// $Id$

package coreen.util;

import com.threerings.gwt.util.StringUtil;

/**
 * Represents some text to be inserted into some other text.
 */
public class Edit implements Comparable<Edit>
{
    /** The offset at which to insert the text. */
    public final int offset;

    /** The text to be inserted. */
    public final String text;

    /**
     * Applies the edits to the supplied text. Escapes any HTML in the intervening text, but the
     * edits will be left unescaped.
     */
    public static String applyEdits (Iterable<Edit> edits, String text)
    {
        StringBuilder buf = new StringBuilder();
        int offset = 0;
        for (Edit edit : edits) {
            buf.append(StringUtil.escapeAttribute(text.substring(offset, edit.offset)));
            buf.append(edit.text);
            offset = edit.offset;
        }
        buf.append(StringUtil.escapeAttribute(text.substring(offset)));
        return buf.toString();
    }

    /** Creates a new edit. */
    public Edit (int offset, String text)
    {
        this.offset = offset;
        this.text = text;
    }

    // from interface Comparable<Edit>
    public int compareTo (Edit other)
    {
        return offset - other.offset;
    }

    @Override // from Object
    public String toString ()
    {
        return text + ":" + offset;
    }
}
