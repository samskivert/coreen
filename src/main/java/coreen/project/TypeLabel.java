//
// $Id$

package coreen.project;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Bindings;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Value;

import coreen.client.Link;
import coreen.client.Page;
import coreen.model.Def;
import coreen.model.DefDetail;
import coreen.model.DefId;
import coreen.model.Kind;
import coreen.ui.PopupGroup;
import coreen.util.DefMap;

/**
 * Displays the name of a type including its enclosing types, which are linked.
 */
public class TypeLabel extends FlowPanel
{
    public TypeLabel (DefDetail deet, DefMap defmap, UsePopup.Linker linker)
    {
        this(deet, null, defmap, linker);
    }

    public TypeLabel (final DefDetail deet, Def[] supers, final DefMap defmap,
                      UsePopup.Linker linker)
    {
        addStyleName(_rsrc.styles().typeLabel());

        // header
        FlowPanel header = Widgets.newFlowPanel(_rsrc.styles().typeLabelHeader());
        add(header);
        header.add(DefUtil.iconForDef(deet));
        for (DefId encl : deet.path) {
            Widget plabel = Widgets.newLabel(encl.name);
            if (encl.kind != Kind.MODULE) {
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

        Label supHier = Widgets.newLabel(" ↑ ");
        supHier.setTitle("Show supertypes...");
        new PopupGroup().bindClick(supHier, new PopupGroup.Thunk() {
            public Widget create (PopupGroup.Positioner repos) {
                return new SuperTypesPanel(deet, defmap, repos);
            }
        });
        header.add(supHier);

        Label subs = Widgets.newLabel(" ↓ ");
        subs.setTitle("Show subtypes...");
        new PopupGroup().showBelow().bindClick(subs, new PopupGroup.Thunk() {
            public Widget create (PopupGroup.Positioner repos) {
                return new SubTypesPanel(deet, defmap, repos);
            }
        });
        header.add(subs);
        // END TODO

        if (deet.doc != null) {
            add(new DocLabel(deet.doc));
        }
    }

    protected Widget createDefLabel (DefDetail def)
    {
        List<Object> args = new ArrayList<Object>();
        args.add(def.unit.projectId);
        args.add(ProjectPage.Detail.TYP);
        for (DefId tid : def.path) {
            if (tid.kind != Kind.MODULE) {
                args.add(tid.id);
            }
        }
        args.add(def.id);
        return Link.create(def.name, Page.PROJECT, args.toArray());
        // return Widgets.newLabel(def.name, _rsrc.styles().Type());
    }

    protected Widget createSuperLabel (Def sup)
    {
        return Widgets.newLabel(sup.name);
    }

    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
