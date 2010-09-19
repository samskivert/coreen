//
// $Id$

package coreen.project;

import java.util.Arrays;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.FluentTable;

import coreen.client.Link;
import coreen.client.Page;
import coreen.model.CompUnit;
import coreen.model.Project;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.util.PanelCallback;

/**
 * Displays all of the compilation units in a particular project.
 */
public class CompUnitsPanel extends Composite
{
    public CompUnitsPanel (final Project p)
    {
        initWidget(_binder.createAndBindUi(this));

        _projsvc.getCompUnits(p.id, new PanelCallback<CompUnit[]>(_contents) {
            public void onSuccess (CompUnit[] units) {
                _contents.setWidget(createContents(p, units));
            }
        });
    }

    protected Widget createContents (Project p, CompUnit[] units)
    {
        Arrays.sort(units);
        FluentTable table = new FluentTable(5, 0, _styles.bydir());
        FlowPanel files = null;
        String dir = "";
        for (CompUnit unit : units) {
            int nidx = unit.path.lastIndexOf("/");
            String uname = unit.path.substring(nidx+1);
            String udir = (nidx < 0) ? "" : unit.path.substring(0, nidx);
            if (!udir.equals(dir)) {
                if (files != null) {
                    table.add().setText(dir, _styles.Path()).alignTop().
                        right().setWidget(files);
                }
                dir = udir;
                files = new FlowPanel();
            }
            if (files.getWidgetCount() > 0) {
                InlineLabel gap = new InlineLabel(" ");
                gap.addStyleName(_styles.Gap());
                files.add(gap);
            }
            files.add(Link.createInline(uname, Page.PROJECT, p.id,
                                        ProjectPage.Detail.SRC, unit.id));
        }
        if (files != null) {
            table.add().setText(dir).alignTop().right().setWidget(files);
        }
        return table;
    }

    protected interface Styles extends CssResource
    {
        String bydir ();
        String Path ();
        String Gap ();
    }

    protected @UiField SimplePanel _contents;
    protected @UiField Styles _styles;

    protected interface Binder extends UiBinder<Widget, CompUnitsPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
}
