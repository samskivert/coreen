//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.WindowUtil;

import coreen.model.Def;
import coreen.util.DefMap;

/**
 * Displays a list of defs in a flowed left-to-right panel.
 */
public class DefUtil
{
    /**
     * Adds a label for a def to a flow panel, along with all the appropriate accouterments.
     */
    public static Label addDef (FlowPanel panel, final Def def, UsePopup.Linker linker,
                                final DefMap defmap)
    {
        InlineLabel label = new InlineLabel(def.name);
        label.addMouseOverHandler(new MouseOverHandler() {
            public void onMouseOver (MouseOverEvent event) {
                // if this def is already onscreen, just highlight it
                Widget dw = defmap.get(def.id);
                if (dw != null && WindowUtil.isScrolledIntoView(dw)) {
                    dw.addStyleName(_rsrc.styles().highlight());
                }
            }
        });
        label.addMouseOutHandler(new MouseOutHandler() {
            public void onMouseOut (MouseOutEvent event) {
                // if we've highlighted our onscreen def, unhighlight it
                Widget dw = defmap.get(def.id);
                if (dw != null) {
                    dw.removeStyleName(_rsrc.styles().highlight());
                }
            }
        });

        new UsePopup.Popper(def.id, label, linker, defmap);
        panel.add(Widgets.newFlowPanel(_rsrc.styles().defLabel(),
                                       TypeLabel.iconForDef(def.type), label));

        return label;
    }

    /**
     * Adds a clearing div to the supplied panel, which presumably has had numerous {@link #addDef}
     * calls on it. Only needed if additional elements are to be added beyond the defs.
     */
    public static void addClear (FlowPanel panel)
    {
        panel.add(Widgets.newLabel(" ", _rsrc.styles().defClear()));
    }

    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
