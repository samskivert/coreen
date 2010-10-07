//
// $Id$

package coreen.project;

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
import coreen.util.ClickCallback;
import coreen.util.PanelCallback;

/**
 * Displays details for a particular type.
 */
public class TypeDetailPanel extends Composite
{
    public static void bind (final Def def, final Label trigger, final FlowPanel target,
                             final Map<Long, Widget> defmap)
    {
        new ClickCallback<TypeDetail>(trigger) {
            protected boolean callService () {
                if (_deets != null) {
                    target.remove(_deets);
                    ((Widget)_trigger).removeStyleName(_rsrc.styles().openDef());
                    _deets = null;
                    return false;
                }
                _projsvc.getType(def.id, this);
                return true;
            }
            protected boolean gotResult (TypeDetail detail) {
                ((Widget)_trigger).addStyleName(_rsrc.styles().openDef());
                target.add(_deets = new TypeDetailPanel(detail, defmap));
                return true;
            }
            protected TypeDetailPanel _deets;
        };
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

    protected void addDefs (FluentTable deets, String kind, Def[] defs, final FlowPanel code)
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
            new UsePopup.Popper(def.id, label, _defmap);
            new ClickCallback<DefContent>(label) {
                protected boolean callService () {
                    if (_content != null) {
                        code.remove(_content);
                        code.setVisible(code.getWidgetCount() > 0);
                        ((Widget)_trigger).removeStyleName(_rsrc.styles().openDef());
                        _content = null;
                        return false;
                    }
                    _projsvc.getContent(def.id, this);
                    return true;
                }
                protected boolean gotResult (DefContent content) {
                    ((Widget)_trigger).addStyleName(_rsrc.styles().openDef());
                    code.add(_content = createContentPanel(content));
                    code.setVisible(true);
                    return true;
                }
                protected Widget _content;
            };
            panel.add(label);
        }

        deets.add().setText(kind, _styles.kind()).alignTop().
            right().setWidget(panel);
    }

    protected Widget createContentPanel (DefContent content)
    {
        FlowPanel bits = Widgets.newFlowPanel();
        if (content.doc != null) {
            bits.add(Widgets.newHTML(content.doc));
        }
        bits.add(new SourcePanel(content.text, content.defs, content.uses, 0L, _defmap));
        return bits;
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
    protected @UiField SimplePanel _contents;
    protected @UiField Styles _styles;

    protected interface Binder extends UiBinder<Widget, TypeDetailPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
    protected static final ProjectMessages _msgs = GWT.create(ProjectMessages.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
