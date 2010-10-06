//
// $Id$

package coreen.project;

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
    public static void bind (final Def def, final Label trigger, final FlowPanel target)
    {
        new ClickCallback<TypeDetail>(trigger) {
            protected boolean callService () {
                if (_deets != null) {
                    target.remove(_deets);
                    _deets = null;
                    return false;
                }
                _projsvc.getType(def.id, this);
                return true;
            }
            protected boolean gotResult (TypeDetail detail) {
                target.add(_deets = new TypeDetailPanel(detail));
                return true;
            }
            protected TypeDetailPanel _deets;
        };
    }

    protected TypeDetailPanel (final TypeDetail detail)
    {
        initWidget(_binder.createAndBindUi(this));

        FluentTable deets = new FluentTable(2, 0);
        if (detail.doc != null) {
            deets.add().setHTML(detail.doc, _styles.doc()).setColSpan(2);
        }
        deets.add().setText(detail.sig, _rsrc.styles().code()).setColSpan(2);
        addDefs(deets, _msgs.tdpTypes(), detail.types);
        addDefs(deets, _msgs.tdpTerms(), detail.terms);
        addDefs(deets, _msgs.tdpFuncs(), detail.funcs);
        _contents.setWidget(deets);
    }

    protected void addDefs (FluentTable deets, String kind, Def[] defs)
    {
        if (defs.length == 0) {
            return;
        }

        final FlowPanel panel = Widgets.newFlowPanel(_styles.defs());
        for (final Def def : defs) {
            if (panel.getWidgetCount() > 0) {
                InlineLabel gap = new InlineLabel(" ");
                gap.addStyleName(_styles.Gap());
                panel.add(gap);
            }
            InlineLabel label = new InlineLabel(def.name);
            // new UsePopup.Popper(def.id, label);
            new ClickCallback<DefContent>(label) {
                protected boolean callService () {
                    if (_content != null) {
                        panel.remove(_content);
                        _content = null;
                        return false;
                    }
                    _projsvc.getContent(def.id, this);
                    return true;
                }
                protected boolean gotResult (DefContent content) {
                    panel.add(_content = new SourcePanel(
                                  content.text, content.defs, content.uses, 0L));
                    return true;
                }
                protected SourcePanel _content;
            };
            panel.add(label);
        }

        deets.add().setText(kind, _styles.kind()).alignTop().
            right().setWidget(panel, _rsrc.styles().code());
    }

    protected interface Styles extends CssResource
    {
        String doc ();
        String kind ();
        String defs ();
        String Gap ();
    }

    protected @UiField SimplePanel _contents;
    protected @UiField Styles _styles;

    protected interface Binder extends UiBinder<Widget, TypeDetailPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
    protected static final ProjectMessages _msgs = GWT.create(ProjectMessages.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
