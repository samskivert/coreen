//
// $Id$

package coreen.project;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Bindings;
import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.InlineLabel;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Value;

import coreen.icons.IconResources;
import coreen.model.Def;
import coreen.model.DefContent;
import coreen.model.TypeDetail;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.util.IdMap;
import coreen.util.PanelCallback;

/**
 * Displays details for a particular type.
 */
public class TypeDetailPanel extends Composite
{
    public TypeDetailPanel (Def def, Map<Long, Widget> defmap, IdMap expanded)
    {
        initWidget(_binder.createAndBindUi(this));
        _def = def;
        _defmap = defmap;
        _expanded = expanded;
    }

    @Override // from Widget
    public void setVisible (boolean visible)
    {
        if (visible && !_loaded) {
            _loaded = true;
            _contents.setWidget(Widgets.newLabel("Loading..."));
            _projsvc.getType(_def.id, new PanelCallback<TypeDetail>(_contents) {
                public void onSuccess (TypeDetail deets) {
                    init(deets);
                }
            });
        }
        super.setVisible(visible);
    }

    public void showMember (long memberId)
    {
        _expanded.get(memberId).update(true);
    }

    protected void init (final TypeDetail detail)
    {
        _detail = detail;

        FlowPanel contents = Widgets.newFlowPanel();
        String header = "";
        if (detail.def.type == Def.Type.TYPE) {
            header = "<b>" + detail.def.name + "</b> ";
        }
        if (detail.doc != null) {
            header += detail.doc;
        }
        if (header.length() > 0) {
            contents.add(Widgets.newHTML(header, _styles.doc()));
        }

        FlowPanel members = Widgets.newFlowPanel(_styles.members());
        FlowPanel deets = Widgets.newFlowPanel();
        addDefs(members, _msgs.tdpTypes(), detail.types, deets);
        addDefs(members, _msgs.tdpFuncs(), detail.funcs, deets);
        if (detail.def.type == Def.Type.TYPE) { // non-types terms are only displayed in-source
            addDefs(members, _msgs.tdpTerms(), detail.terms, deets);
        }
        if (members.getWidgetCount() > 0) {
            members.add(Widgets.newLabel(" ", _styles.Spacer()));
            contents.add(members);
        }

        final Label sig = Widgets.newLabel(detail.sig, _rsrc.styles().code());
        final SourcePanel source = new SourcePanel(_defmap) {
            public void setVisible (boolean visible) {
                super.setVisible(visible);
                if (visible && !_loaded) {
                    _loaded = true;
                    _projsvc.getContent(detail.def.id, new PanelCallback<DefContent>(_contents) {
                        public void onSuccess (DefContent content) {
                            init(content.text, content.defs, content.uses, 0L, UsePopup.BY_TYPES);
                        }
                    });
                }
            }
            protected boolean _loaded;
        };
        source.setVisible(false);

        ToggleButton toggle = new ToggleButton(new Image(_icons.codeClosed()),
                                               new Image(_icons.codeOpen()), new ClickHandler() {
            public void onClick (ClickEvent event) {
                sig.setVisible(!sig.isVisible());
                source.setVisible(!source.isVisible());
            }
        });
        toggle.addStyleName(_styles.toggle());
        contents.add(toggle);
        contents.add(sig);
        contents.add(source);
        contents.add(deets);

        _contents.setWidget(contents);
    }

    protected void addDefs (FlowPanel panel, String kind, Def[] defs, FlowPanel members)
    {
        for (final Def def : defs) {
            Image icon = iconForDef(def);
            icon.addStyleName(_styles.Icon());
            InlineLabel label = new InlineLabel(def.name);
            label.addClickHandler(new ClickHandler() {
                public void onClick (ClickEvent event) {
                    Value<Boolean> expanded = _expanded.get(def.id);
                    expanded.update(!expanded.get());
                }
            });
            label.addMouseOverHandler(new MouseOverHandler() {
                public void onMouseOver (MouseOverEvent event) {
                    // if this def is already onscreen, just highlight it
                    Widget dw = _defmap.get(def.id);
                    if (dw != null) { // TODO: && is visible
                        dw.addStyleName(_rsrc.styles().highlight());
                    }
                }
            });
            label.addMouseOutHandler(new MouseOutHandler() {
                public void onMouseOut (MouseOutEvent event) {
                    // if we've highlighted our onscreen def, unhighlight it
                    Widget dw = _defmap.get(def.id);
                    if (dw != null) {
                        dw.removeStyleName(_rsrc.styles().highlight());
                    }
                }
            });
            // new UsePopup.Popper(def.id, label, _defmap, UsePopup.BY_TYPES);
            panel.add(Widgets.newFlowPanel(_styles.Member(), icon, label));

            TypeDetailPanel deets = new TypeDetailPanel(def, _defmap, _expanded);
            deets.addStyleName(_rsrc.styles().indent());
            Bindings.bindVisible(_expanded.get(def.id), deets);
            members.add(deets);
        }
    }

    protected Image iconForDef (Def def)
    {
        switch (def.type) {
        default:
        case MODULE: // TODO: module icon
            return new Image(_icons.class_obj());
        case TYPE: // TODO: support specialization on class/ifc/enum/etc.
            return new Image(_icons.class_obj());
        case FUNC: // TODO: support public/protected/private, etc.
            return new Image(_icons.methpub_obj());
        case TERM: // TODO: support public/protected/private, etc.
            return new Image(_icons.field_public_obj());
        }
    }

    protected interface Styles extends CssResource
    {
        String doc ();
        String members ();
        String /*members*/ Icon ();
        String /*members*/ Member ();
        String /*members*/ Spacer ();
        String toggle ();
    }

    protected boolean _loaded;
    protected Def _def;
    protected TypeDetail _detail;
    protected Map<Long, Widget> _defmap;
    protected IdMap _expanded;

    protected @UiField SimplePanel _contents;
    protected @UiField Styles _styles;

    protected interface Binder extends UiBinder<Widget, TypeDetailPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
    protected static final ProjectMessages _msgs = GWT.create(ProjectMessages.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
    protected static final IconResources _icons = GWT.create(IconResources.class);
}
