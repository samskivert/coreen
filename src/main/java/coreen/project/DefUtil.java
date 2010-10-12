//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;

import com.threerings.gwt.ui.Widgets;

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
        new UsePopup.Popper(def.id, label, linker, defmap, false);
        panel.add(TypeLabel.adornDef(def, label));
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

    /**
     * Returns the appropriate style for a def of the specified type.
     */
    public static String getStyle (Def.Type type)
    {
        switch (type) {
        case MODULE: return _rsrc.styles().defModule();
        case TYPE: return _rsrc.styles().defType();
        case FUNC: return _rsrc.styles().defFunc();
        case TERM: return _rsrc.styles().defTerm();
        default: return _rsrc.styles().defUnknown();
        }
    }

    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
