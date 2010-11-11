//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.StringUtil;

import coreen.client.ClientMessages;
import coreen.model.Def;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.ui.PopupGroup;
import coreen.util.DefMap;
import coreen.util.PanelCallback;

/**
 * Provides shared functionality for {@link SuperTypesPanel} and {@link SubTypesPanel}.
 */
public abstract class TypeHierPanel extends FlowPanel
{
    protected TypeHierPanel (Def def, DefMap defmap, final PopupGroup.Positioner repos)
    {
        add(Widgets.newLabel(_cmsgs.loading()));
        _def = def;
        _defmap = defmap;

        fetchTypes(new PanelCallback<Def[][]>(this) {
            public void onSuccess (Def[][] defs) {
                clear();
                init(defs);
                repos.sizeDidChange();
            }
        });
    }

    protected abstract void fetchTypes (AsyncCallback<Def[][]> callback);

    protected abstract void init (Def[][] types);

    protected Widget makeLabel (Def def)
    {
        if (def == null) {
            return Widgets.newInlineLabel("");
        }
        String name = StringUtil.isBlank(def.name) ? "<anon>" : def.name;
        Widget label = Widgets.newInlineLabel(def.name);
        if (def.id != _def.id) {
            new UsePopup.Popper(def.id, label, UsePopup.TYPE, _defmap, true).setGroup(_pgroup);
        }
        return DefUtil.adornDef(def, label);
    }

    protected Def _def;
    protected DefMap _defmap;
    protected PopupGroup _pgroup = new PopupGroup();

    protected static final ClientMessages _cmsgs = GWT.create(ClientMessages.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
}
