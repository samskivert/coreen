//
// $Id$

package coreen.library;

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
import coreen.model.Def;
import coreen.model.TypedId;
import coreen.project.ProjectPage;
import coreen.project.TypeLabel;
import coreen.project.UsePopup;
import coreen.rpc.LibraryService;
import coreen.rpc.LibraryServiceAsync;
import coreen.util.DefMap;
import coreen.util.PanelCallback;

/**
 * Issues a search and displays its results.
 */
public class SearchResultsPanel extends Composite
{
    public SearchResultsPanel (final String query)
    {
        initWidget(_binder.createAndBindUi(this));
        _contents.setWidget(Widgets.newLabel("Searching '" + query + "'..."));

        _libsvc.search(query, new PanelCallback<LibraryService.SearchResult[]>(_contents) {
            public void onSuccess (LibraryService.SearchResult[] results) {
                // partition the results by type (TODO: rewrite with Guava Multimap)
                Map<Def.Type, List<LibraryService.SearchResult>> bytype =
                    new HashMap<Def.Type, List<LibraryService.SearchResult>>();
                for (LibraryService.SearchResult result : results) {
                    List<LibraryService.SearchResult> rlist = bytype.get(result.def.type);
                    if (rlist == null) {
                        bytype.put(result.def.type,
                                   rlist = new ArrayList<LibraryService.SearchResult>());
                    }
                    rlist.add(result);
                }

                FluentTable contents = new FluentTable(5, 0);
                for (Def.Type type : Def.Type.values()) {
                    if (bytype.containsKey(type)) {
                        for (LibraryService.SearchResult result : bytype.get(type)) {
                            addResult(contents, result);
                        }
                    }
                }
                if (results.length == 0) {
                    contents.add().setText("No results for '" + query + "'...");
                }
                _contents.setWidget(contents);
            }
        });
    }

    protected void addResult (FluentTable table, final LibraryService.SearchResult result)
    {
        table.add().setText(result.project, _styles.resultCell()).alignTop().
            right().setWidget(new TypeLabel(result.path, result.def, UsePopup.SOURCE,
                                            _defmap, result.doc, true) {
                protected Widget createDefLabel (Def def) {
                    List<Object> args = new ArrayList<Object>();
                    args.add(result.projectId);
                    args.add(ProjectPage.Detail.TYP);
                    for (TypedId tid : result.path) {
                        if (tid.type != Def.Type.MODULE) {
                            args.add(tid.id);
                        }
                    }
                    args.add(result.def.id);
                    return Link.create(def.name, Page.PROJECT, args.toArray());
                }
            }, _styles.resultCell());
    }

    protected interface Styles extends CssResource
    {
        String resultCell ();
    }
    protected @UiField Styles _styles;
    protected @UiField SimplePanel _contents;

    protected DefMap _defmap = new DefMap();

    protected interface Binder extends UiBinder<Widget, SearchResultsPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final LibraryServiceAsync _libsvc = GWT.create(LibraryService.class);
}
