//
// $Id$

package coreen.project;

import java.util.Arrays;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
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
public class CompUnitsPanel extends FlowPanel
{
    public CompUnitsPanel (final Project p, final ProjectPage.Styles styles)
    {
        _projsvc.getCompUnits(p.id, new PanelCallback<CompUnit[]>(this) {
            public void onSuccess (CompUnit[] units) {
                Arrays.sort(units);
                FluentTable table = new FluentTable(2, 0, styles.bydir());
                FlowPanel files = null;
                String dir = "";
                for (CompUnit unit : units) {
                    int nidx = unit.path.lastIndexOf("/");
                    String uname = unit.path.substring(nidx+1);
                    String udir = (nidx < 0) ? "" : unit.path.substring(0, nidx);
                    if (!udir.equals(dir)) {
                        if (files != null) {
                            table.add().setText(dir, styles.Path()).alignTop().
                                right().setWidget(files);
                        }
                        dir = udir;
                        files = new FlowPanel();
                    }
                    if (files.getWidgetCount() > 0) {
                        InlineLabel gap = new InlineLabel(" ");
                        gap.addStyleName(styles.Gap());
                        files.add(gap);
                    }
                    files.add(Link.createInline(uname, Page.PROJECT, p.id, unit.id));
                }
                if (files != null) {
                    table.add().setText(dir).alignTop().right().setWidget(files);
                }
                add(table);
            }
        });
    }

    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
}
