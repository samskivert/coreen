//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Bindings;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Value;

import coreen.model.Def;
import coreen.model.DefId;
import coreen.model.Type;
import coreen.util.DefMap;

/**
 * Displays the name of a type including its enclosing types, which are linked.
 */
public class TypeLabel extends FlowPanel
{
    public TypeLabel (DefId[] parents, Def def, UsePopup.Linker linker, DefMap defmap, String docs)
    {
        addStyleName(_rsrc.styles().typeLabel());
        add(DefUtil.iconForDef(def));
        for (DefId encl : parents) {
            Widget plabel = Widgets.newInlineLabel(encl.name);
            if (encl.type != Type.MODULE) {
                new UsePopup.Popper(encl.id, plabel, linker, defmap, true);
            }
            add(plabel);
            add(Widgets.newInlineLabel(".")); // TODO: customizable path separator?
        }
        Widget dlabel = createDefLabel(def);
        dlabel.addStyleName("inline");
        add(dlabel);

        Label supers = Widgets.newInlineLabel(" ↑ ", _rsrc.styles().actionable());
        add(supers);
        Label subs = Widgets.newInlineLabel(" ↓ ", _rsrc.styles().actionable());
        add(subs);

        SuperTypesPanel spanel = new SuperTypesPanel(def, linker, defmap);
        Value<Boolean> showSupers = Value.create(false);
        supers.addClickHandler(Bindings.makeToggler(showSupers));
        Bindings.bindVisible(showSupers, spanel);
        add(spanel);

        if (docs != null) {
            add(new DocLabel(docs));
        }
    }

    protected Widget createDefLabel (Def def)
    {
        return Widgets.newLabel(def.name, _rsrc.styles().Type());
    }

    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
