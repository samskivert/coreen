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

import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.InlineLabel;
import com.threerings.gwt.ui.Widgets;

import coreen.icons.IconResources;
import coreen.model.Def;
import coreen.model.DefContent;
import coreen.model.TypeDetail;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.util.PanelCallback;

/**
 * Displays details for a particular type.
 */
public class TypeDetailPanel extends Composite
{
    public static class Shower extends ShowCallback<TypeDetail, TypeDetailPanel>
    {
        public Shower (Def def, Label trigger, FlowPanel target, Map<Long, Widget> defmap) {
            super(trigger, target);
            _def = def;
            _defmap = defmap;
        }

        public void show (long memberId) {
            _pendingMemberId = memberId;
            show();
        }

        protected boolean callService () {
            _projsvc.getType(_def.id, this);
            return true;
        }

        protected TypeDetailPanel createDisplay (TypeDetail detail) {
            return new TypeDetailPanel(detail, _defmap);
        }

        protected void displayShown () {
            super.displayShown();
            _display.showMember(_pendingMemberId);
            _pendingMemberId = 0L;
        }

        protected Def _def;
        protected Map<Long, Widget> _defmap;
        protected long _pendingMemberId;
    }

    public void showMember (long memberId)
    {
        Shower shower = _showers.get(memberId);
        if (shower != null) {
            shower.show(0L);
        } else {
            GWT.log("No shower for member " + memberId);
        }
    }

    protected TypeDetailPanel (final TypeDetail detail, Map<Long, Widget> defmap)
    {
        initWidget(_binder.createAndBindUi(this));
        _detail = detail;
        _defmap = defmap;

        FlowPanel deets = Widgets.newFlowPanel();
        if (detail.doc != null) {
            deets.add(Widgets.newHTML(detail.doc, _styles.doc()));
        }

        FlowPanel members = Widgets.newFlowPanel(_styles.members());
        addDefs(members, _msgs.tdpTypes(), detail.types, deets);
        addDefs(members, _msgs.tdpFuncs(), detail.funcs, deets);
        if (detail.def.type == Def.Type.TYPE) { // non-types terms are only displayed in-source
            addDefs(members, _msgs.tdpTerms(), detail.terms, deets);
        }
        if (members.getWidgetCount() > 0) {
            members.add(Widgets.newLabel(" ", _styles.Spacer()));
            deets.add(members);
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
        deets.add(toggle);
        deets.add(sig);
        deets.add(source);

        _contents.setWidget(deets);
    }

    protected void addDefs (FlowPanel panel, String kind, Def[] defs, FlowPanel members)
    {
        for (final Def def : defs) {
            Image icon = iconForDef(def);
            icon.addStyleName(_styles.Icon());
            InlineLabel label = new InlineLabel(def.name);
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
            _showers.put(def.id, new Shower(def, label, members, _defmap));
            panel.add(Widgets.newFlowPanel(_styles.Member(), icon, label));
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

    protected TypeDetail _detail;
    protected Map<Long, Widget> _defmap;
    // protected Map<Long, ShowCallback<DefContent, Widget>> _showers =
    //     new HashMap<Long, ShowCallback<DefContent, Widget>>();
    protected Map<Long, Shower> _showers = new HashMap<Long, Shower>();

    protected @UiField SimplePanel _contents;
    protected @UiField Styles _styles;

    protected interface Binder extends UiBinder<Widget, TypeDetailPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
    protected static final ProjectMessages _msgs = GWT.create(ProjectMessages.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
    protected static final IconResources _icons = GWT.create(IconResources.class);
}
