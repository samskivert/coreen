//
// $Id$

package coreen.ui;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Various UI related utilities.
 */
public class UIUtil
{
    /**
     * Shows the supplied popup above the specified target widget.
     */
    public static void showAbove (PopupPanel popup, Widget target)
    {
        popup.setVisible(false);
        popup.show();
        int left = target.getAbsoluteLeft();
        int top = Math.max(target.getAbsoluteTop() - popup.getOffsetHeight(), 0);
        if (left + popup.getOffsetWidth() > Window.getClientWidth()) {
            left = Math.max(0, Window.getClientWidth() - popup.getOffsetWidth());
        }
        popup.setPopupPosition(left, top);
        popup.setVisible(true);
    }

    /**
     * Sets the browser title.
     *
     * @param parts a list of parts that will be combined with dashes to form the title.
     */
    public static void setWindowTitle (String... parts)
    {
        StringBuilder buf = new StringBuilder("Coreen"); // TODO: maybe nix Coreen?
        for (String part : parts) {
            if (buf.length() > 0) {
                buf.append(" - ");
            }
            buf.append(part);
        }
        Window.setTitle(buf.toString());
    }
}
