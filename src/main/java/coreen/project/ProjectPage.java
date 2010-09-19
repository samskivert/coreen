//
// $Id$

package coreen.project;

import com.google.common.base.Function;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Bindings;
import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.DateUtil;
import com.threerings.gwt.util.Value;

import coreen.client.AbstractPage;
import coreen.client.Args;
import coreen.client.ClientMessages;
import coreen.client.Page;
import coreen.model.Project;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.util.ClickCallback;
import coreen.util.PanelCallback;

/**
 * Displays a single project.
 */
public class ProjectPage extends AbstractPage
{
    public ProjectPage ()
    {
        initWidget(_binder.createAndBindUi(this));

        // some UI elements are only visible/enabled when we have a project
        Value<Boolean> projp = _proj.map(new Function<Project,Boolean>() {
            public Boolean apply (Project proj) { return (proj != null); }
        });
        Bindings.bindEnabled(projp, _search, _go, _update);
        Bindings.bindVisible(projp, _header);

        new ClickCallback<Void>(_update) {
            protected boolean callService () {
                _projsvc.updateProject(_proj.get().id, this);
                return true;
            }
            protected boolean gotResult (Void result) {
                Popups.infoNear(_msgs.pUpdateStarted(), _update);
                return true;
            }
        };

        new ClickCallback<Void>(_go, _search) {
            protected boolean callService () {
                return false; // TODO
            }
            protected boolean gotResult (Void result) {
                return false; // TODO
            }
        };
    }

    @Override // from AbstractPage
    public Page getId ()
    {
        return Page.PROJECT;
    }

    @Override // from AbstractPage
    public void setArgs (Args args)
    {
        _proj.update(null);
        _contents.setWidget(Widgets.newLabel(_cmsgs.loading()));
        _projsvc.getProject(args.get(0, 0), new PanelCallback<Project>(_contents) {
            public void onSuccess (Project p) {
                _proj.update(p);
                _name.setText(p.name);
                _version.setText(p.version);
                _imported.setText(DateUtil.formatDateTime(p.imported));
                _lastUpdated.setText(DateUtil.formatDateTime(p.lastUpdated));
                _contents.setWidget(new CompUnitsPanel(p, _styles)); // TODO: tabs
            }
        });
    }

    protected interface Styles extends CssResource {
        String bydir ();
        String Path ();
        String Gap ();
    }

    protected @UiField HTMLPanel _header;
    protected @UiField Label _name, _version, _imported, _lastUpdated;
    protected @UiField TextBox _search;
    protected @UiField Button _update, _go;
    protected @UiField SimplePanel _contents;
    protected @UiField Styles _styles;

    protected Value<Project> _proj = Value.create(null);

    protected interface Binder extends UiBinder<Widget, ProjectPage> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
    protected static final ProjectMessages _msgs = GWT.create(ProjectMessages.class);
    protected static final ClientMessages _cmsgs = GWT.create(ClientMessages.class);
}
