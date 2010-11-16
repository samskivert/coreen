//
// $Id$

package coreen.project;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Bindings;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Value;

import coreen.client.Link;
import coreen.client.Page;
import coreen.icons.IconResources;
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
    public static FlowPanel makeTypeHeader (DefDetail deet, DefMap defmap, UsePopup.Linker linker)
    {
        Widget deflabel = Widgets.newLabel(deet.name, _rsrc.styles().Type());
        new UsePopup.Popper(deet.id, deflabel, linker, defmap, true);
        return makeTypeHeader(deet, deflabel, defmap, linker);
    }

    public static FlowPanel makeTypeHeader (DefDetail deet, Widget deflabel, DefMap defmap,
                                            UsePopup.Linker linker)
    {
        FlowPanel header = Widgets.newFlowPanel(_rsrc.styles().typeLabelHeader());
        header.add(DefUtil.iconForDef(deet));
        for (DefId encl : deet.path) {
            ProjectPage.Detail detail = (encl.kind == Kind.MODULE) ?
                ProjectPage.Detail.MDS : ProjectPage.Detail.TYP;
            Widget plabel = Link.create(
                encl.name, Page.PROJECT, deet.unit.projectId, detail, encl.id);
            if (encl.kind != Kind.MODULE) {
                new UsePopup.Popper(encl.id, plabel, linker, defmap, true);
            }
            header.add(plabel);
            header.add(Widgets.newLabel(" ")); // TODO: customizable path separator?
        }
        header.add(deflabel);
        return header;
    }

    public TypeLabel (DefDetail deet, DefMap defmap, UsePopup.Linker linker)
    {
        this(deet, null, defmap, linker);
    }

    public TypeLabel (final DefDetail deet, Def[] supers, final DefMap defmap,
                      UsePopup.Linker linker)
    {
        addStyleName(_rsrc.styles().typeLabel());

        add(_header = makeTypeHeader(deet, createDefLabel(deet), defmap, linker));

        _supersIdx = _header.getWidgetCount();
        if (supers != null) {
            addSupers(supers, defmap, linker);
        }

        Hyperlink src = UsePopup.SOURCE.makeLink(deet);
        src.setHTML("<img border=0 title=\"View source\" src=\"" +
                    new Image(_icons.view_code()).getUrl() + "\"/>");
        _header.add(Widgets.newInlineLabel(" "));
        _header.add(src);

        Label supHier = Widgets.newLabel(" ▲ ");
        supHier.setTitle("Show supertypes...");
        new PopupGroup().bindClick(supHier, new PopupGroup.Thunk() {
            public Widget create (PopupGroup.Positioner repos) {
                return new SuperTypesPanel(deet.id, defmap, repos);
            }
        });
        _header.add(supHier);

        Label subs = Widgets.newLabel(" ▼ ");
        subs.setTitle("Show subtypes...");
        new PopupGroup().showBelow().bindClick(subs, new PopupGroup.Thunk() {
            public Widget create (PopupGroup.Positioner repos) {
                return new SubTypesPanel(deet.id, defmap, repos);
            }
        });
        _header.add(subs);

        Label uses = Widgets.newLabel(" ▶ ");
        uses.setTitle("Show uses...");
        new PopupGroup().showBelow().bindClick(uses, new PopupGroup.Thunk() {
            public Widget create (PopupGroup.Positioner repos) {
                return new DefUsesPanel(deet, defmap, repos);
            }
        });
        _header.add(uses);

        if (deet.doc != null) {
            add(new DocLabel(deet.doc));
        }
    }

    public void addSupers (Def[] supers, DefMap defmap, UsePopup.Linker linker)
    {
        for (int ii = 0, ll = supers.length; ii < ll; ii++) {
            _header.insert(Widgets.newLabel((ii == 0) ? " ← " : ", "), _supersIdx++);
            Widget suplab = createSuperLabel(supers[ii]);
            new UsePopup.Popper(supers[ii].id, suplab, linker, defmap, false);
            _header.insert(suplab, _supersIdx++);
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

    protected FlowPanel _header;
    protected int _supersIdx;
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
    protected static final IconResources _icons = GWT.create(IconResources.class);
}
