//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
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
    public static void bind (final long referentId, Widget target, final DefMap defmap)
    {
        if (target instanceof HasMouseOverHandlers) {
            ((HasMouseOverHandlers)target).addMouseOverHandler(new MouseOverHandler() {
                public void onMouseOver (MouseOverEvent event) {
                    highlightTarget(defmap, referentId);
                }
            });
            ((HasMouseOutHandlers)target).addMouseOutHandler(new MouseOutHandler() {
                public void onMouseOut (MouseOutEvent event) {
                    clearTarget(defmap, referentId);
                }
            });
        }
    }

    public static boolean highlightTarget (DefMap defmap, long defId)
    {
        Widget def = defmap.get(defId);
        if (def != null && WindowUtil.isScrolledIntoView(def)) {
            def.addStyleName(_rsrc.styles().highlight());
            return true;
        }
        return false;
    }

    public static boolean clearTarget (DefMap defmap, long defId)
    {
        Widget def = defmap.get(defId);
        if (def != null) {
            def.removeStyleName(_rsrc.styles().highlight());
            return true;
        }
        return false;
    }

    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
