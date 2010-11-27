//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;

import coreen.client.Args;
import coreen.model.DefDetail;
import coreen.model.Project;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.ui.SearchResultsPanel;
import coreen.ui.UIUtil;

/**
 * Handles searching within a project.
 */
public class SearchPanel extends AbstractProjectPanel
{
    public SearchPanel ()
    {
        initWidget(_panel = new SearchResultsPanel<DefDetail>() {
            protected String createNoResultsLabel (String query) {
                return "No definitions of '" + query + "' found in project " + _pname + ".";
            }
        });
    }

    @Override // from AbstractProjectPanel
    public ProjectPage.Detail getId ()
    {
        return ProjectPage.Detail.SEARCH;
    }

    @Override // from AbstractProjectPanel
    public void setArgs (Project proj, Args args)
    {
        _pname = proj.name;
        String query = args.get(2, "").trim();
        _panel.setQuery(query);
        UIUtil.setWindowTitle(proj.name, query);
        _projsvc.search(proj.id, query, _panel.createCallback());
    }

    protected String _pname;
    protected SearchResultsPanel<DefDetail> _panel;

    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
}
