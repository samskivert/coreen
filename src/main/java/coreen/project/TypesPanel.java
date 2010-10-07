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
    public TypesPanel ()
    {
        initWidget(_binder.createAndBindUi(this));
    }

    public void display (long projectId, final long typeId, final long memberId)
    {
        if (_projectId != projectId) {
            _projsvc.getTypes(_projectId = projectId, new PanelCallback<Def[]>(_contents) {
                public void onSuccess (Def[] defs) {
                    _contents.setWidget(createContents(defs));
                    showMember(typeId, memberId);
                }
            });
        } else {
            showMember(typeId, memberId);
        }
    }

    protected void showMember (long typeId, long memberId)
    {
        TypeDetailPanel.Shower shower = _showers.get(typeId);
        if (shower != null) {
            shower.show(memberId);
        } else {
            GWT.log("Have no shower for " + typeId);
        }
    }

    protected Widget createContents (Def[] defs)
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
            _showers.put(def.id, new TypeDetailPanel.Shower(def, label, types, _defmap));
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

    protected long _projectId;
    protected Map<Long, Widget> _defmap = new HashMap<Long, Widget>();
    protected Map<Long, TypeDetailPanel.Shower> _showers =
        new HashMap<Long, TypeDetailPanel.Shower>();

    protected @UiField SimplePanel _contents;
    protected @UiField Styles _styles;

    protected interface Binder extends UiBinder<Widget, TypesPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
