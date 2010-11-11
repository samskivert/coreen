//
// $Id$

package coreen.project;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;

import com.threerings.gwt.ui.Widgets;

import coreen.model.Def;
import coreen.ui.PopupGroup;
import coreen.util.DefMap;

/**
 * Displays the supertypes of a def.
 */
public class SuperTypesPanel extends TypeHierPanel
{
    public SuperTypesPanel (Def def, DefMap defmap, PopupGroup.Positioner repos)
    {
        super(def, defmap, repos);
    }

    protected void fetchTypes (AsyncCallback<Def[][]> callback)
    {
        _projsvc.getSuperTypes(_def.id, callback);
    }

    protected void init (Def[][] defs)
    {
        if (defs.length == 1 && defs[0].length == 1) {
            add(Widgets.newInlineLabel("No supertypes of "));
            add(makeLabel(defs[0][0]));
            return;
        }

        add(Widgets.newLabel("Supertypes:"));
        for (Def[] row : defs) {
            FlowPanel rc = new FlowPanel();
            rc.add(makeLabel(row[0]));
            if (row.length > 1) {
                rc.add(Widgets.newInlineLabel(" ← "));
            }
            for (int ii = 1; ii < row.length; ii++) {
                rc.add(makeLabel(row[ii]));
                rc.add(Widgets.newInlineLabel(" "));
            }
            add(rc);
        }
    }
}
