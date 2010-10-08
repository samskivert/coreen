//
// $Id$

package coreen.ui;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;

/**
 * Does something extraordinary.
 */
public class WindowFX
{
    /**
     * Smooth scrolls the window such that the target widget is at the top of the display.
     */
    public static void scrollTo (Widget target)
    {
        scrollToPos(target.getElement().getAbsoluteTop());
    }

    /**
     * Smooth scrolls the window to the specified scroll offset.
     */
    public static void scrollToPos (int vpos)
    {
        _targetPos = vpos;
        _prevPos = -1;
        if (_timer == null) {
            _timer = new Timer() {
                public void run () {
                    updateScrollPos();
                }
            };
            _timer.scheduleRepeating(10);
        }
    }

    protected static void updateScrollPos ()
    {
        int curpos = Window.getScrollTop();
        if (curpos == _prevPos) {
            _timer.cancel();
            _timer = null;
        } else {
            _prevPos = curpos;
            if (_targetPos < curpos) {
                curpos = Math.max(_targetPos, curpos - DELTA);
            } else {
                curpos = Math.min(_targetPos, curpos + DELTA);
            }
            Window.scrollTo(Window.getScrollLeft(), curpos);
        }
    }

    protected static int _targetPos;
    protected static int _prevPos;
    protected static Timer _timer;

    protected static final int DELTA = 25;
}
