//
// $Id$

package coreen.project;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.InlineLabel;
import com.threerings.gwt.ui.Widgets;

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
        ShowCallback<DefContent, Widget> shower = _showers.get(memberId);
        if (shower != null) {
            shower.show();
        } else {
            GWT.log("No shower for member " + memberId);
        }
    }

    protected TypeDetailPanel (final TypeDetail detail, Map<Long, Widget> defmap)
    {
        initWidget(_binder.createAndBindUi(this));
        _defmap = defmap;

        FluentTable deets = new FluentTable(2, 0, _styles.Deets());
        if (detail.doc != null) {
            deets.add().setHTML(detail.doc, _styles.doc()).setColSpan(2);
        }
        FlowPanel code = Widgets.newFlowPanel(_styles.defContent());
        code.setVisible(false);
        deets.add().setText(detail.sig, _rsrc.styles().code()).setColSpan(2);
        addDefs(deets, _msgs.tdpTypes(), detail.types, code);
        addDefs(deets, _msgs.tdpTerms(), detail.terms, code);
        addDefs(deets, _msgs.tdpFuncs(), detail.funcs, code);
        deets.add().setWidget(code).setColSpan(2);
        _contents.setWidget(deets);
    }

    protected void addDefs (FluentTable deets, String kind, Def[] defs, FlowPanel code)
    {
        if (defs.length == 0) {
            return;
        }

        FlowPanel panel = Widgets.newFlowPanel(_styles.defs());
        for (final Def def : defs) {
            if (panel.getWidgetCount() > 0) {
                InlineLabel gap = new InlineLabel(" ");
                gap.addStyleName(_styles.Gap());
                panel.add(gap);
            }
            InlineLabel label = new InlineLabel(def.name);
            new UsePopup.Popper(def.id, label, _defmap, UsePopup.BY_TYPES);
            _showers.put(def.id, new ShowCallback<DefContent, Widget>(label, code) {
                protected boolean callService () {
                    _projsvc.getContent(def.id, this);
                    return true;
                }
                protected Widget createDisplay (DefContent content) {
                    FlowPanel bits = Widgets.newFlowPanel();
                    if (content.doc != null) {
                        bits.add(Widgets.newHTML(content.doc));
                    }
                    bits.add(new SourcePanel(content.text, content.defs, content.uses, 0L,
                                             _defmap, UsePopup.BY_TYPES));
                    return bits;
                }
                protected void displayShown () {
                    _target.setVisible(true);
                    super.displayShown();
                }
                protected void displayHidden () {
                    _target.setVisible(_target.getWidgetCount() > 0);
                }
            });
            panel.add(label);
        }

        deets.add().setText(kind, _styles.kind()).alignTop().
            right().setWidget(panel);
    }

    protected interface Styles extends CssResource
    {
        String /*content*/ Deets ();
        String doc ();
        String kind ();
        String defs ();
        String /*defs*/ Gap ();
        String defContent();
    }

    protected Map<Long, Widget> _defmap;
    protected Map<Long, ShowCallback<DefContent, Widget>> _showers =
        new HashMap<Long, ShowCallback<DefContent, Widget>>();

    protected @UiField SimplePanel _contents;
    protected @UiField Styles _styles;

    protected interface Binder extends UiBinder<Widget, TypeDetailPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
    protected static final ProjectMessages _msgs = GWT.create(ProjectMessages.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
