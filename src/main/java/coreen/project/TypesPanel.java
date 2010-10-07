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
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.Widgets;

import coreen.client.Link;
import coreen.client.Page;
import coreen.model.Def;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.util.PanelCallback;

/**
 * Displays the types declared in an entire project.
 */
public class TypesPanel extends Composite
{
    public TypesPanel (final long projectId)
    {
        initWidget(_binder.createAndBindUi(this));

        _projsvc.getTypes(projectId, new PanelCallback<Def[]>(_contents) {
            public void onSuccess (Def[] defs) {
                _contents.setWidget(createContents(projectId, defs));
            }
        });
    }

    protected Widget createContents (long projectId, Def[] defs)
    {
        FluentTable table = new FluentTable(5, 0, _styles.byname());
        FlowPanel types = null;
        char c = 0;
        for (Def def : defs) {
            if (def.name.length() == 0) {
                continue; // skip blank types; TODO: what are these?
            }
            if (def.name.charAt(0) != c) {
                types = Widgets.newFlowPanel();
                c = def.name.charAt(0);
                table.add().setText(String.valueOf(c), _styles.Letter()).alignTop().
                    right().setWidget(types);
            }
            if (types.getWidgetCount() > 0) {
                InlineLabel gap = new InlineLabel(" ");
                gap.addStyleName(_styles.Gap());
                types.add(gap);
            }
            InlineLabel label = new InlineLabel(def.name);
            TypeDetailPanel.bind(def, label, types, _defmap);
            types.add(label);
        }
        return table;
    }

    protected interface Styles extends CssResource
    {
        String byname ();
        String Letter ();
        String Gap ();
    }

    protected Map<Long, Widget> _defmap = new HashMap<Long, Widget>();

    protected @UiField SimplePanel _contents;
    protected @UiField Styles _styles;

    protected interface Binder extends UiBinder<Widget, TypesPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
