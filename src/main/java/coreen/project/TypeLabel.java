//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;

import coreen.model.Def;
import coreen.model.TypedId;
import coreen.util.DefMap;

/**
 * Displays the name of a type including its enclosing types, which are linked.
 */
public class TypeLabel extends FlowPanel
{
    public TypeLabel (TypedId[] parents, Def def, UsePopup.Linker linker, DefMap defmap)
    {
        addStyleName(_rsrc.styles().typeLabel());
        for (TypedId encl : parents) {
            if (encl.type != Def.Type.MODULE) {
                Widget plabel = Widgets.newInlineLabel(encl.name);
                new UsePopup.Popper(encl.id, plabel, linker, defmap);
                add(plabel);
                add(Widgets.newInlineLabel(".")); // TODO: customizable path separator?
            }
        }
        add(Widgets.newInlineLabel(def.name));
    }

    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
