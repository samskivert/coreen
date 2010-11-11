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
import coreen.util.DefMap;
import coreen.util.PanelCallback;

/**
 * Displays the subtypes of a def.
 */
public class SubTypesPanel extends FlowPanel
{
    public SubTypesPanel (Def def, DefMap defmap, PopupGroup.Positioner repos)
    {
        add(Widgets.newLabel(_cmsgs.loading()));
        _def = def;
        _defmap = defmap;
        _repos = repos;

        _projsvc.getSubTypes(_def.id, new PanelCallback<Def[][]>(this) {
            public void onSuccess (Def[][] defs) {
                init(defs);
            }
        });
    }

    protected void init (Def[][] defs)
    {
        clear();
        if (defs.length == 0) {
            add(Widgets.newLabel("No subtypes..."));
            return;
        }

        if (defs.length > 1) {
            add(Widgets.newInlineLabel("Subtypes of "));
        } else {
            add(Widgets.newInlineLabel("No subtypes of "));
        }
        add(makeLabel(defs[0][0])); // this is the type we asked about
        for (int ii = 1; ii < defs.length; ii++) {
            FlowPanel rc = Widgets.newFlowPanel(_rsrc.styles().subtypesRow());
            for (Def stype : defs[ii]) {
                rc.add(makeLabel(stype));
                rc.add(Widgets.newInlineLabel(" "));
            }
            add(rc);
        }
        _repos.sizeDidChange();
    }

    protected Widget makeLabel (Def def)
    {
        if (def == null) {
            return Widgets.newInlineLabel("");
        }
        Widget label = Widgets.newInlineLabel(def.name);
        if (def.id != _def.id) {
            new UsePopup.Popper(def.id, label, UsePopup.TYPE, _defmap, true).setGroup(_pgroup);
        }
        return DefUtil.adornDef(def, label);
    }

    protected Def _def;
    protected DefMap _defmap;
    protected PopupGroup _pgroup = new PopupGroup();
    protected PopupGroup.Positioner _repos;

    protected static final ClientMessages _cmsgs = GWT.create(ClientMessages.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
}
