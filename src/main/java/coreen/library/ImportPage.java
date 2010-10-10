//
// $Id$

package coreen.library;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.DateUtil;

import coreen.client.AbstractPage;
import coreen.client.Args;
import coreen.client.Link;
import coreen.client.Page;
import coreen.model.PendingProject;
import coreen.rpc.LibraryService;
import coreen.rpc.LibraryServiceAsync;
import coreen.util.ClickCallback;
import coreen.util.PanelCallback;

/**
 * Displays a UI for importing projects.
 */
public class ImportPage extends AbstractPage
{
    public ImportPage ()
    {
        initWidget(_binder.createAndBindUi(this));

        new ClickCallback<PendingProject>(_go, _source) {
            @Override protected boolean callService () {
                String source = _source.getText().trim();
                if (source.length() == 0) {
                    return false;
                }
                _libsvc.importProject(source, this);
                return true;
            }
            @Override protected boolean gotResult (PendingProject pp) {
                if (_penders == null) {
                    _contents.setWidget(_penders = Widgets.newFlowPanel(toWidget(pp)));
                } else {
                    _penders.add(toWidget(pp));
                }
                scheduleRefresh(); // this project is no doubt incomplete
                return true;
            }
        };
    }

    @Override // from AbstractPage
    public Page getId ()
    {
        return Page.IMPORT;
    }

    @Override // from AbstractPage
    public void setArgs (Args args)
    {
        refreshPendingProjects();
    }

    protected void refreshPendingProjects ()
    {
        _libsvc.getPendingProjects(new PanelCallback<PendingProject[]>(_contents) {
            public void onSuccess (PendingProject[] pps) {
                if (pps.length == 0) {
                    _contents.setWidget(Widgets.newLabel(_msgs.ipNoPending()));
                    return;
                }

                _penders = new FlowPanel();
                boolean incomplete = false;
                for (PendingProject pp : pps) {
                    _penders.add(toWidget(pp));
                    incomplete |= !pp.isComplete();
                }
                _contents.setWidget(_penders);

                if (incomplete) {
                    scheduleRefresh();
                }
            }
        });
    }

    protected void scheduleRefresh ()
    {
        new Timer() {
            public void run () {
                refreshPendingProjects();
            }
        }.schedule(2000);
    }

    protected Widget toWidget (PendingProject pp)
    {
        Widget status = Widgets.newLabel(pp.status);
        return new FluentTable().add().setText("Source:").right().setText(pp.source).
            add().setText("Status:").right().setWidget(
                (pp.isComplete() && !pp.isFailed()) ?
                Widgets.newRow(status, Link.create("View project", Page.PROJECT, pp.projectId)) :
                status).
            add().setText("Started:").right().setText(DateUtil.formatTime(new Date(pp.started))).
            add().setText("Last updated:").right().setText(
                DateUtil.formatTime(new Date(pp.lastUpdated))).
            table();
    }

    protected @UiField TextBox _source;
    protected @UiField Button _go;
    protected @UiField SimplePanel _contents;

    protected FlowPanel _penders;

    protected interface Binder extends UiBinder<Widget, ImportPage> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final LibraryServiceAsync _libsvc = GWT.create(LibraryService.class);
    protected static final LibraryMessages _msgs = GWT.create(LibraryMessages.class);
}
