//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;

import com.threerings.gwt.ui.Widgets;

import coreen.client.ClientMessages;
import coreen.model.Def;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.ui.PopupGroup;
import coreen.util.DefMap;
import coreen.util.PanelCallback;

/**
 * A panel that shows all of the uses of a particular def.
 */
public class DefUsesPanel extends FlowPanel
{
    public DefUsesPanel (Def def, final PopupGroup.Positioner repos)
    {
        add(Widgets.newLabel(_cmsgs.loading()));
        _def = def;
        // _defmap = defmap;

        _projsvc.findUses(def.id, new PanelCallback<ProjectService.UsesResult[]>(this) {
            public void onSuccess (ProjectService.UsesResult[] results) {
                clear();
                init(results);
                repos.sizeDidChange();
            }
        });
    }

    protected void init (ProjectService.UsesResult[] results)
    {
        DefMap defmap = new DefMap();
        for (ProjectService.UsesResult result : results) {
            add(new TypeLabel(result, defmap, UsePopup.TYPE));
            for (int ii = 0; ii < result.uses.length; ii++) {
                // TODO: add line number
                add(new SourcePanel(result.lines[ii], result.uses[ii], defmap, UsePopup.TYPE));
            }
        }
    }

    protected Def _def;
    // protected DefMap _defmap;
    protected PopupGroup _pgroup = new PopupGroup();

    protected static final ClientMessages _cmsgs = GWT.create(ClientMessages.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
}
