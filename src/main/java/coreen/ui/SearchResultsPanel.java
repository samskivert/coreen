//
// $Id$

package coreen.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Value;

import coreen.client.Link;
import coreen.client.Page;
import coreen.model.Def;
import coreen.model.DefDetail;
import coreen.model.DefId;
import coreen.model.Type;
import coreen.project.ProjectPage;
import coreen.project.ProjectResources;
import coreen.project.SourcePanel;
import coreen.project.TogglePanel;
import coreen.project.TypeLabel;
import coreen.project.TypeSummaryPanel;
import coreen.project.UsePopup;
import coreen.util.DefMap;
import coreen.util.PanelCallback;

/**
 * Issues a search and displays its results.
 */
public class SearchResultsPanel<R extends DefDetail> extends Composite
{
    public SearchResultsPanel ()
    {
        initWidget(_binder.createAndBindUi(this));
    }

    public void setQuery (String query)
    {
        _query = query;
        _contents.setWidget(Widgets.newLabel("Searching '" + query + "'..."));
    }

    public PanelCallback<R[]> createCallback () {
        return new PanelCallback<R[]>(_contents) {
            public void onSuccess (R[] results) {
                init(results);
            }
        };
    }

    protected void init (R[] results)
    {
        // partition the results by type (TODO: rewrite with Guava Multimap)
        Map<Type, List<R>> bytype = new HashMap<Type, List<R>>();
        for (R result : results) {
            List<R> rlist = bytype.get(result.type);
            if (rlist == null) {
                bytype.put(result.type, rlist = new ArrayList<R>());
            }
            rlist.add(result);
        }

        FluentTable table = new FluentTable(5, 0);
        for (Type type : Type.values()) {
            if (bytype.containsKey(type)) {
                for (R result : bytype.get(type)) {
                    addResult(table, result);
                }
            }
        }
        if (results.length == 0) {
            table.add().setText("No results for '" + _query + "'...");
        }

        _contents.setWidget(table);
    }

    protected void addResult (FluentTable table, R result)
    {
        table.add().setWidget(createResultView(result), _styles.resultCell());
        // table.add().setText(result.project, _styles.resultCell()).alignTop().
        //     right().setWidget(createResultView(result), _styles.resultCell());
    }

    protected Widget createResultView (final R result)
    {
        TypeLabel label = new TypeLabel(result.path, result, UsePopup.TYPE, _defmap, result.doc) {
            protected Widget createDefLabel (Def def) {
                List<Object> args = new ArrayList<Object>();
                args.add(result.unit.projectId);
                args.add(ProjectPage.Detail.TYP);
                for (DefId tid : result.path) {
                    if (tid.type != Type.MODULE) {
                        args.add(tid.id);
                    }
                }
                args.add(result.id);
                return Link.create(def.name, Page.PROJECT, args.toArray());
            }
        };
        return Widgets.newFlowPanel(label, new TogglePanel(Value.create(false)) {
            protected Widget createCollapsed () {
                return Widgets.newLabel(result.sig, _rsrc.styles().code());
            }
            protected Widget createExpanded () {
                switch (result.type) {
                case MODULE:
                case TYPE:
                    return new TypeSummaryPanel(result.id, true);
                default:
                    return new SourcePanel(result.id, _defmap, UsePopup.TYPE, false);
                }
            }
        });
    }

    protected interface Styles extends CssResource
    {
        String resultCell ();
    }
    protected @UiField Styles _styles;
    protected @UiField SimplePanel _contents;

    protected String _query;
    protected DefMap _defmap = new DefMap();

    protected interface Binder extends UiBinder<Widget, SearchResultsPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    // TODO: make UI resources, factor non-project bits thereinto
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
