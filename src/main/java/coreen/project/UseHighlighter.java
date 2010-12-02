//
// $Id$

package coreen.project;

import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.util.WindowUtil;

import coreen.util.DefMap;

/**
 * Wires up mouse listeners that highlight definitions when hovering over a use.
 */
public class UseHighlighter
{
    public static void bind (final long referentId, final SpanWidget target,
                             final DefMap defmap)
    {
        if (target instanceof HasMouseOverHandlers) {
            ((HasMouseOverHandlers)target).addMouseOverHandler(new MouseOverHandler() {
                public void onMouseOver (MouseOverEvent event) {
                    highlightTarget(defmap, referentId, target, true);
                }
            });
            ((HasMouseOutHandlers)target).addMouseOutHandler(new MouseOutHandler() {
                public void onMouseOut (MouseOutEvent event) {
                    clearTarget(defmap, referentId);
                }
            });
        }
    }

    public static boolean highlightTarget (DefMap defmap, long defId, Widget except,
                                           boolean evenIfNoDef)
    {
        boolean highedDef = highlightTarget(defmap.get(defId), except);
        if (highedDef || evenIfNoDef) {
            for (SpanWidget w : defmap.getUses(defId)) {
                highlightTarget(w, except);
            }
        }
        return highedDef;
    }

    public static boolean clearTarget (DefMap defmap, long defId)
    {
        boolean clearedDef = clearTarget(defmap.get(defId));
        for (SpanWidget w : defmap.getUses(defId)) {
            clearTarget(w);
        }
        return clearedDef;
    }

    protected static boolean highlightTarget (SpanWidget target, Widget except)
    {
        if (target != null && WindowUtil.isScrolledIntoView(target)) {
            if (target != except) {
                target.setHighlighted(true);
            }
            return true;
        }
        return false;
    }

    protected static boolean clearTarget (SpanWidget target)
    {
        if (target != null) {
            target.setHighlighted(false);
            return true;
        }
        return false;
    }
}
