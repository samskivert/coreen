//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Bindings;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Value;

import coreen.model.Def;
import coreen.model.DefDetail;
import coreen.model.DefId;
import coreen.model.Type;
import coreen.model.TypeSummary;
import coreen.util.DefMap;

/**
 * Displays the name of a type including its enclosing types, which are linked.
 */
public class TypeLabel extends FlowPanel
{
    public TypeLabel (DefDetail deet, UsePopup.Linker linker, DefMap defmap)
    {
        this(deet, null, linker, defmap);
    }

    public TypeLabel (TypeSummary sum, UsePopup.Linker linker, DefMap defmap)
    {
        this(sum, sum.supers, linker, defmap);
    }

    public TypeLabel (DefDetail deet, Def[] supers, UsePopup.Linker linker, DefMap defmap)
    {
        addStyleName(_rsrc.styles().typeLabel());

        // header
        FlowPanel header = Widgets.newFlowPanel(_rsrc.styles().typeLabelHeader());
        add(header);
        header.add(DefUtil.iconForDef(deet));
        for (DefId encl : deet.path) {
            Widget plabel = Widgets.newLabel(encl.name);
            if (encl.type != Type.MODULE) {
                new UsePopup.Popper(encl.id, plabel, linker, defmap, true);
            }
            header.add(plabel);
            header.add(Widgets.newLabel(".")); // TODO: customizable path separator?
        }
        header.add(createDefLabel(deet));

        for (int ii = 0, ll = (supers == null) ? 0 : supers.length; ii < ll; ii++) {
            header.add(Widgets.newLabel((ii == 0) ? " ← " : ", "));
            Widget suplab = createSuperLabel(supers[ii]);
            new UsePopup.Popper(supers[ii].id, suplab, linker, defmap, false);
            header.add(suplab);
        }

        // TODO: tidy this up
        Hyperlink src = UsePopup.SOURCE.makeLink(deet);
        src.setText("src");
        src.addStyleName(_rsrc.styles().code());
        header.add(Widgets.newLabel(" ["));
        header.add(src);
        header.add(Widgets.newLabel("]"));

        Label supHier = Widgets.newLabel(" ↑ ", _rsrc.styles().actionable());
        header.add(supHier);
        Label subs = Widgets.newLabel(" ↓ ", _rsrc.styles().actionable());
        header.add(subs);
        // END TODO

        // stuff below the header
        SuperTypesPanel spanel = new SuperTypesPanel(deet, linker, defmap);
        Value<Boolean> showSupers = Value.create(false);
        supHier.addClickHandler(Bindings.makeToggler(showSupers));
        Bindings.bindVisible(showSupers, spanel);
        add(spanel);

        if (deet.doc != null) {
            add(new DocLabel(deet.doc));
        }
    }

    protected Widget createDefLabel (DefDetail def)
    {
        return Widgets.newLabel(def.name, _rsrc.styles().Type());
    }

    protected Widget createSuperLabel (Def sup)
    {
        return Widgets.newLabel(sup.name);
    }

    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
