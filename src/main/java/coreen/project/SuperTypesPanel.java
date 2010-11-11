//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;

import coreen.client.ClientMessages;
import coreen.model.Def;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.ui.PopupGroup;
import coreen.ui.UIUtil;
import coreen.util.DefMap;
import coreen.util.PanelCallback;

/**
 * Displays the supertypes of a def.
 */
public class SuperTypesPanel extends FlowPanel
{
    public SuperTypesPanel (Def def, DefMap defmap, PopupGroup.Positioner repos)
    {
        add(Widgets.newLabel(_cmsgs.loading()));
        _def = def;
        _defmap = defmap;
        _repos = repos;

        _projsvc.getSuperTypes(_def.id, new PanelCallback<Def[][]>(this) {
            public void onSuccess (Def[][] defs) {
                init(defs);
            }
        });
    }

    protected void init (Def[][] defs)
    {
        clear();
        if (defs.length == 0) {
            add(Widgets.newLabel("No supertypes..."));
            return;
        }

        for (Def[] row : defs) {
            FlowPanel rc = new FlowPanel();
            rc.add(makeLabel(row[0], row[0].id != _def.id));
            if (row.length > 1) {
                rc.add(Widgets.newInlineLabel(" ← "));
            }
            for (int ii = 1; ii < row.length; ii++) {
                rc.add(makeLabel(row[ii], true));
            }
            add(rc);
        }
        _repos.sizeDidChange();
    }

    protected Widget makeLabel (Def def, boolean popper)
    {
        if (def == null) {
            return Widgets.newInlineLabel("");
        }
        Widget label = Widgets.newInlineLabel(def.name);
        if (popper) {
            new UsePopup.Popper(def.id, label, UsePopup.TYPE, _defmap, true);
        }
        return DefUtil.adornDef(def, label);
    }

    protected Def _def;
    protected DefMap _defmap;
    protected PopupGroup.Positioner _repos;

    protected static final ClientMessages _cmsgs = GWT.create(ClientMessages.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
}
