//
// $Id$

package coreen.library;

import com.google.gwt.core.client.GWT;

import com.threerings.gwt.ui.FluentTable;

import coreen.model.Project;
import coreen.rpc.LibraryService;
import coreen.rpc.LibraryServiceAsync;
import coreen.ui.DataPanel;

/**
 * Displays all of the projects known to the system.
 */
public class ProjectsPanel extends DataPanel<Project[]>
{
    public ProjectsPanel () {
        super("projects");
        _libsvc.getProjects(createCallback());
    }

    @Override // from DataPanel
    protected void init (Project[] data) {
        FluentTable table = new FluentTable(5, 0);
        for (Project p : data) {
            table.add().setText(""+ p.id).
                right().setText(p.name).
                right().setText(p.rootPath);
        }
        add(table);
    }

    protected static final LibraryServiceAsync _libsvc = GWT.create(LibraryService.class);
}
