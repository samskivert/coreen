//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.Widgets;

import coreen.client.ClientMessages;
import coreen.model.Def;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.util.DefMap;
import coreen.util.PanelCallback;

/**
 * Displays the supertypes of a def.
 */
public class SuperTypesPanel extends SimplePanel
{
    public SuperTypesPanel (Def def, UsePopup.Linker linker, DefMap defmap)
    {
        setWidget(Widgets.newLabel(_cmsgs.loading()));
        _def = def;
        _linker = linker;
        _defmap = defmap;
    }

    @Override // from Widget
    public void setVisible (boolean visible)
    {
        if (visible && !_loaded) {
            _loaded = true;
            _projsvc.getSuperTypes(_def.id, new PanelCallback<Def[][]>(this) {
                public void onSuccess (Def[][] defs) {
                    init(defs);
                }
            });
        }
        super.setVisible(visible);
    }

    protected void init (Def[][] defs)
    {
        if (defs.length == 0) {
            setWidget(Widgets.newLabel("No supertypes..."));
            return;
        }

        FluentTable table = new FluentTable(_rsrc.styles().typeGraph());
        for (Def[] row : defs) {
            FluentTable.Cell cell = table.add().setWidget(makeLabel(row[0], true));
            if (row.length > 1) {
                cell = cell.right().setText(" ⇙ ");
            }
            for (int ii = 1; ii < row.length; ii++) {
                cell = cell.right().setWidget(makeLabel(row[ii], true));
            }
        }
        table.add().setWidget(makeLabel(_def, false));
        setWidget(table);
    }

    protected Widget makeLabel (Def def, boolean popper)
    {
        if (def == null) {
            return Widgets.newInlineLabel("");
        }
        Widget label = Widgets.newInlineLabel(def.name);
        if (popper) {
            new UsePopup.Popper(def.id, label, _linker, _defmap, true);
        }
        return Widgets.newFlowPanel(DefUtil.iconForDef(def), label);
    }

    protected Def _def;
    protected UsePopup.Linker _linker;
    protected DefMap _defmap;
    protected boolean _loaded;

    protected static final ClientMessages _cmsgs = GWT.create(ClientMessages.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
}
