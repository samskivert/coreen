//
// $Id$

package coreen.project;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Bindings;
import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.Widgets;

import coreen.client.Link;
import coreen.client.Page;
import coreen.model.Def;
import coreen.util.PanelCallback;

/**
 * Displays the types declared in an entire project.
 */
public class TypesPanel extends SummaryPanel
{
    public TypesPanel ()
    {
        initWidget(_binder.createAndBindUi(this));
    }

    @Override // from AbstractProjectPanel
    public ProjectPage.Detail getId ()
    {
        return ProjectPage.Detail.TPS;
    }

    @Override // from SummaryPanel
    protected void updateContents (long projectId)
    {
        _projsvc.getTypes(projectId, new PanelCallback<Def[]>(_contents) {
            public void onSuccess (Def[] defs) {
                _contents.setWidget(createContents(defs));
            }
        });
    }

    protected Widget createContents (Def[] defs)
    {
        // map the defs by id for some later fiddling
        final Map<Long, Def> byid = new HashMap<Long, Def>();
        for (Def def : defs) {
            byid.put(def.id, def);
        }

        FluentTable table = new FluentTable(5, 0, _styles.byname());
        FlowPanel types = null, details = null;
        char c = 0;
        for (final Def def : defs) {
            if (def.name.length() == 0) {
                continue; // skip blank types; TODO: better anonymous inner class handling
            }
            if (def.name.charAt(0) != c) {
                types = Widgets.newFlowPanel();
                details = Widgets.newFlowPanel();
                c = def.name.charAt(0);
                table.add().setText(String.valueOf(c), _styles.Letter()).alignTop().
                    right().setWidget(Widgets.newFlowPanel(types, details));
            }

            Label label = DefUtil.addDef(types, def, UsePopup.BY_TYPES, _defmap);
            label.addClickHandler(new ClickHandler() {
                public void onClick (ClickEvent event) {
                    if (_showing.get(def.id).get()) {
                        _showing.get(def.id).update(false);
                    } else {
                        long outerId = def.id, innerId = 0L;
                        Def d = def;
                        while (d != null) {
                            d = byid.get(d.parentId);
                            if (d != null) {
                                innerId = outerId;
                                outerId = d.id;
                            }
                        }
                        Link.go(Page.PROJECT, _projectId, ProjectPage.Detail.TPS, outerId, innerId);
                    }
                }
            });

            // create and add the detail panel (hidden) and bind its visibility to a value
            TypeDetailPanel deets = new TypeDetailPanel(
                def.id, _defmap, _showing, UsePopup.BY_TYPES);
            Bindings.bindVisible(_showing.get(def.id), deets);
            details.add(deets);
        }
        return table;
    }

    protected interface Styles extends CssResource
    {
        String byname ();
        String Letter ();
    }
    protected @UiField Styles _styles;
    protected @UiField SimplePanel _contents;

    protected interface Binder extends UiBinder<Widget, TypesPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
}
