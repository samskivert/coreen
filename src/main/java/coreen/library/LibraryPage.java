//
// $Id$

package coreen.library;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.EnterClickAdapter;
import com.threerings.gwt.ui.FluentTable;

import coreen.client.AbstractPage;
import coreen.client.Args;
import coreen.client.Link;
import coreen.client.Page;
import coreen.rpc.LibraryService;
import coreen.rpc.LibraryServiceAsync;
import coreen.ui.SearchResultsPanel;
import coreen.ui.UIUtil;

/**
 * Displays all of the projects known to the system.
 */
public class LibraryPage extends AbstractPage
{
    public LibraryPage ()
    {
        initWidget(_binder.createAndBindUi(this));
        ClickHandler onSearch = new ClickHandler() {
            public void onClick (ClickEvent event) {
                String query = _search.getText().trim();
                if (query.equals("")) {
                    Link.go(Page.LIBRARY);
                } else {
                    Link.go(Page.LIBRARY, SEARCH, query);
                }
            }
        };
        _search.addKeyPressHandler(new EnterClickAdapter(onSearch));
    }

    @Override // from AbstractPage
    public Page getId ()
    {
        return Page.LIBRARY;
    }

    @Override // from AbstractPage
    public void setArgs (Args args)
    {
        String action = args.get(0, "");
        if (action.equals(SEARCH)) {
            final String query = args.get(1, "").trim();
            _search.setText(query);
            UIUtil.setWindowTitle(query);
            _contents.setWidget(new SearchResultsPanel<LibraryService.SearchResult>() {
                /* ctor */ {
                    setQuery(query);
                    _libsvc.search(query, createCallback());
                }
                @Override protected void addResult (
                    FluentTable table, LibraryService.SearchResult result) {
                    table.add().setText(result.project, _styles.resultCell()).alignTop().
                        right().setWidget(createResultView(result), _styles.resultCell());
                }
            });
        } else {
            _contents.setWidget(_projects);
        }
    }

    protected @UiField TextBox _search;
    protected @UiField SimplePanel _contents;

    protected ProjectsPanel _projects = new ProjectsPanel();

    protected interface Binder extends UiBinder<Widget, LibraryPage> {}
    protected static final Binder _binder = GWT.create(Binder.class);

    protected static final String SEARCH = "search";

    protected static final LibraryServiceAsync _libsvc = GWT.create(LibraryService.class);
}
