//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Bindings;
import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Value;

import coreen.client.Link;
import coreen.client.Page;
import coreen.model.Def;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.util.DefMap;
import coreen.util.IdMap;
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
                }
            });
        }
        _types.get(typeId).update(true);
        _members.get(memberId).update(true);
    }

    protected Widget createContents (Def[] defs)
    {
        FluentTable table = new FluentTable(5, 0, _styles.byname());
        FlowPanel types = null, details = null;
        char c = 0;
        for (final Def def : defs) {
            if (def.name.length() == 0) {
                continue; // skip blank types; TODO: what are these?
            }
            if (def.name.charAt(0) != c) {
                types = Widgets.newFlowPanel();
                details = Widgets.newFlowPanel();
                c = def.name.charAt(0);
                table.add().setText(String.valueOf(c), _styles.Letter()).alignTop().
                    right().setWidget(Widgets.newFlowPanel(types, details));
            }
            if (types.getWidgetCount() > 0) {
                InlineLabel gap = new InlineLabel(" ");
                gap.addStyleName(_styles.Gap());
                types.add(gap);
            }

            // add a label for this type
            types.add(Link.createInline(def.name, Page.PROJECT, _projectId,
                                        ProjectPage.Detail.TPS, def.id));
            // InlineLabel label = new InlineLabel(def.name);
            // label.addClickHandler(new ClickHandler() {
            //     public void onClick (ClickEvent event) {
            //         Value<Boolean> showing = _types.get(def.id);
            //         showing.update(!showing.get());
            //     }
            // });
            // types.add(label);

            // create and add the detail panel (hidden) and bind its visibility to a value
            TypeDetailPanel deets = new TypeDetailPanel(def.id, _defmap, _members);
            Bindings.bindVisible(_types.get(def.id), deets);
            details.add(deets);
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
    protected DefMap _defmap = new DefMap();
    protected IdMap<Boolean> _types = IdMap.create(false);
    protected IdMap<Boolean> _members = IdMap.create(false);

    protected @UiField SimplePanel _contents;
    protected @UiField Styles _styles;

    protected interface Binder extends UiBinder<Widget, TypesPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
