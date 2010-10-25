//
// $Id$

package coreen.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Function;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Bindings;
import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Value;
import com.threerings.gwt.util.Values;

import coreen.client.AbstractPage;
import coreen.client.Args;
import coreen.client.ClientMessages;
import coreen.client.Link;
import coreen.client.Page;
import coreen.model.Project;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.util.ClickCallback;
import coreen.util.PanelCallback;

/**
 * Displays an interface for editing a project.
 */
public class EditProjectPage extends AbstractPage
{
    public EditProjectPage ()
    {
        initWidget(_binder.createAndBindUi(this));
    }

    @Override // from AbstractPage
    public Page getId ()
    {
        return Page.EDIT;
    }

    @Override // from AbstractPage
    public void setArgs (final Args args)
    {
        final long projectId = args.get(0, 0L);

        // load up the metadata for this project
        _contents.setWidget(Widgets.newLabel(_cmsgs.loading()));
        _projsvc.getProject(projectId, new PanelCallback<Project>(_contents) {
            public void onSuccess (Project p) {
                _name.setText(p.name);
                _name.setTargetHistoryToken(Args.createToken(Page.PROJECT, projectId));
                _contents.setWidget(createContents(p));
            }
        });
    }

    protected Widget createContents (final Project p)
    {
        FlowPanel contents = Widgets.newFlowPanel();

        FluentTable uptbl = new FluentTable();
        uptbl.add().setText("Edit project properties:").setColSpan(2);
        final Project np = new Project();
        np.id = p.id;
        List<Value<Boolean>> dlist = new ArrayList<Value<Boolean>>();
        for (final Property prop : Property.values()) {
            final Value<String> current = Value.create(prop.get(p));
            final TextBox box = Widgets.newTextBox(prop.get(p), prop.maxLen, prop.vizLen);
            Bindings.bindText(current, box);
            dlist.add(current.map(new Function<String, Boolean>() {
                public Boolean apply (String text) {
                    return !prop.get(p).equals(text);
                }
            }));
            current.addListenerAndTrigger(new Value.Listener<String>() {
                public void valueChanged (String text) {
                    prop.update(np, text.trim());
                }
            });
            uptbl.add().setText(prop.label + ":").right().setWidget(box);
            if (prop.tip != null) {
                uptbl.add().right().setText(prop.tip, _styles.tip());
            }
        }

        final Button update = new Button("Update");
        new ClickCallback<Void>(update) {
            protected boolean callService () {
                _projsvc.updateProject(np, this);
                return true;
            }
            protected boolean gotResult (Void result) {
                Popups.infoNear(_msgs.projectUpdated(), getPopupNear());
                return true;
            }
        };
        Bindings.bindEnabled(Values.or(dlist), update);
        uptbl.add().right().setWidget(update);
        contents.add(Widgets.newSimplePanel(_styles.section(), uptbl));

        Button delete = new Button("Delete");
        new ClickCallback<Void>(delete) {
            protected boolean callService () {
                _projsvc.deleteProject(p.id, this);
                return true;
            }
            protected boolean gotResult (Void result) {
                Popups.infoNear(_msgs.projectDeleted(), getPopupNear());
                Link.go(Page.LIBRARY);
                return false;
            }
        }.setConfirmText(_msgs.deleteConfirm());
        FluentTable deltbl = new FluentTable();
        deltbl.add().setText("Delete project:").right().setWidget(delete);
        contents.add(Widgets.newSimplePanel(_styles.section(), deltbl));

        return contents;
    }

    protected static enum Property {
        NAME("Name", 128, 20, null) {
            public String get (Project p) { return p.name; }
            public void update (Project p, String text) { p.name = text; }
        },
        ROOT("Root", 256, 40, null) {
            public String get (Project p) { return p.rootPath; }
            public void update (Project p, String text) { p.rootPath = text; }
        },
        VERSION("Version", 32, 10, null) {
            public String get (Project p) { return p.version; }
            public void update (Project p, String text) { p.version = text; }
        },
        SOURCE_DIRS("Source dirs", 256, 40, _msgs.sourceDirsTip()) {
            public String get (Project p) { return p.srcDirs; }
            public void update (Project p, String text) { p.srcDirs = text; }
        };

        public final String label;
        public final int maxLen;
        public final int vizLen;
        public final String tip;

        public abstract String get (Project p);
        public abstract void update (Project p, String text);

        Property (String label, int maxLen, int vizLen, String tip) {
            this.label = label;
            this.maxLen = maxLen;
            this.vizLen = vizLen;
            this.tip = tip;
        }
    }

    protected interface Styles extends CssResource
    {
        String section ();
        String tip ();
    }
    protected @UiField Styles _styles;

    protected @UiField Hyperlink _name;
    protected @UiField SimplePanel _contents;

    protected interface Binder extends UiBinder<Widget, EditProjectPage> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ConfigMessages _msgs = GWT.create(ConfigMessages.class);
    protected static final ClientMessages _cmsgs = GWT.create(ClientMessages.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
}
