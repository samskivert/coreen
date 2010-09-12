//
// $Id$

package coreen.library;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;

import coreen.client.Args;
import coreen.client.AbstractPage;
import coreen.client.Link;
import coreen.client.Page;

/**
 * Displays all of the projects known to the system.
 */
public class LibraryPage extends AbstractPage
{
    public LibraryPage ()
    {
        initWidget(_binder.createAndBindUi(this));
    }

    @Override // from AbstractPage
    public void setArgs (Args args)
    {
        String action = args.get(0, "");
        if (action.equals(SEARCH)) {
            _contents.setWidget(Widgets.newLabel("TODO: search/filter: " + args.get(1, "")));
        } else {
            _contents.setWidget(_projects);
        }
    }

    @UiHandler("_search")
    protected void onValueChange (ValueChangeEvent<String> event)
    {
        if (event.getValue().trim().equals("")) {
            Link.go(Page.LIBRARY);
        } else {
            Link.go(Page.LIBRARY, SEARCH, event.getValue());
        }
    }

    protected @UiField TextBox _search;
    protected @UiField SimplePanel _contents;

    protected ProjectsPanel _projects = new ProjectsPanel();

    protected interface Binder extends UiBinder<Widget, LibraryPage> {}
    protected static Binder _binder = GWT.create(Binder.class);

    protected static final String SEARCH = "search";
}
