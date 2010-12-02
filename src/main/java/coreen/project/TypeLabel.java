//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;

import coreen.client.Link;
import coreen.client.Page;
import coreen.icons.IconResources;
import coreen.model.Def;
import coreen.model.DefDetail;
import coreen.model.DefId;
import coreen.ui.PopupGroup;
import coreen.util.DefMap;

/**
 * Displays the name of a type including its enclosing types, which are linked.
 */
public class TypeLabel extends FlowPanel
{
    public static FlowPanel makeTypeHeader (DefDetail deet, DefMap defmap, UsePopup.Linker linker)
    {
        SpanWidget label = new SpanWidget.Plain(deet.name, deet);
        label.addStyleName(_rsrc.styles().Type());
        new UsePopup.Popper(deet.id, label, linker, defmap, true);
        return makeTypeHeader(deet, label, defmap, linker);
    }

    public static FlowPanel makeTypeHeader (DefDetail deet, Widget deflabel, DefMap defmap,
                                            UsePopup.Linker linker)
    {
        FlowPanel header = Widgets.newFlowPanel(_rsrc.styles().typeLabelHeader());
        header.add(DefUtil.iconForDef(deet));
        for (DefId encl : deet.path) {
            header.add(Link.create(encl.name, Page.PROJECT, deet.unit.projectId,
                                   ProjectPage.Detail.forKind(encl.kind), encl.id));
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
            SpanWidget suplab = createSuperLabel(supers[ii]);
            new UsePopup.Popper(supers[ii].id, suplab, linker, defmap, false);
            _header.insert(suplab, _supersIdx++);
        }
    }

    public void addToHeader (Widget widget)
    {
        _header.add(widget);
    }

    protected Widget createDefLabel (DefDetail def)
    {
        return Link.create(def.name, Page.PROJECT, def.unit.projectId,
                           ProjectPage.Detail.forKind(def.kind), def.id);
    }

    protected SpanWidget createSuperLabel (Def sup)
    {
        return new SpanWidget.Plain(sup.name, sup);
    }

    protected FlowPanel _header;
    protected int _supersIdx;
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
    protected static final IconResources _icons = GWT.create(IconResources.class);
}
