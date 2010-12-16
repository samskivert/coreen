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

import coreen.client.Link;
import coreen.client.Page;
import coreen.model.DefDetail;
import coreen.model.Flavor;
import coreen.model.Kind;
import coreen.project.DefUtil;
import coreen.project.ProjectPage;
import coreen.project.ProjectResources;
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
        // if we have only one result, or the first result is a type and the rest are its public
        // constructors, simply redirect to the type panel for that result
        if (results.length > 0 && justTypeAndCtors(results)) {
            goToResult(results[0]);
            return;
        }

        // partition the results by kind (TODO: rewrite with Guava Multimap)
        Map<Kind, List<R>> bykind = new HashMap<Kind, List<R>>();
        for (R result : results) {
            List<R> rlist = bykind.get(result.kind);
            if (rlist == null) {
                bykind.put(result.kind, rlist = new ArrayList<R>());
            }
            rlist.add(result);
        }

        FluentTable table = new FluentTable(5, 0, _styles.resultTable());
        for (Kind kind : Kind.values()) {
            if (bykind.containsKey(kind)) {
                for (R result : bykind.get(kind)) {
                    addResult(table, result);
                }
            }
        }
        if (results.length == 0) {
            table.add().setText(createNoResultsLabel(_query));
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
        return DefUtil.createDefSummary(result, _defmap, UsePopup.TYPE, null);
    }

    protected String createNoResultsLabel (String query)
    {
        return "No definitions of '" + query + "' were found.";
    }

    protected void goToResult (R result)
    {
        Link.go(Page.PROJECT, result.unit.projectId,
                ProjectPage.Detail.forKind(result.kind), result.id);
    }

    protected boolean justTypeAndCtors (R[] results)
    {
        R type = results[0];
        for (int ii = 1; ii < results.length; ii++) {
            R result = results[ii];
            if (result.outerId != type.id || result.flavor != Flavor.CONSTRUCTOR) {
                return false;
            }
        }
        return true;
    }

    protected interface Styles extends CssResource
    {
        String resultTable ();
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
