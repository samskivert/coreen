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
}
