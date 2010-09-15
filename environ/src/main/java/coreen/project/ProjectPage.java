//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.DateUtil;

import coreen.client.AbstractPage;
import coreen.client.Args;
import coreen.client.ClientMessages;
import coreen.client.Page;
import coreen.model.Project;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.util.PanelCallback;

/**
 * Displays a single project.
 */
public class ProjectPage extends AbstractPage
{
    public ProjectPage ()
    {
        initWidget(_binder.createAndBindUi(this));
        _name.setText(_cmsgs.loading());
    }

    @Override // from AbstractPage
    public Page getId ()
    {
        return Page.PROJECT;
    }

    @Override // from AbstractPage
    public void setArgs (Args args)
    {
        _projsvc.getProject(args.get(0, 0), new PanelCallback<Project>(_contents) {
            public void onSuccess (Project p) {
                if (p == null) {
                    _name.setText(_msgs.pUnknown());
                    _contents.setWidget(Widgets.newLabel(_msgs.pNoSuchProject()));
                } else {
                    _name.setText(p.name);
                    _imported.setText(DateUtil.formatDateTime(p.imported));
                    _lastUpdated.setText(DateUtil.formatDateTime(p.lastUpdated));
                }
            }
        });
    }

    protected @UiField Label _name, _imported, _lastUpdated;
    protected @UiField TextBox _search;
    protected @UiField Button _update, _go;
    protected @UiField SimplePanel _contents;

    protected interface Binder extends UiBinder<Widget, ProjectPage> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
    protected static final ProjectMessages _msgs = GWT.create(ProjectMessages.class);
    protected static final ClientMessages _cmsgs = GWT.create(ClientMessages.class);
}
