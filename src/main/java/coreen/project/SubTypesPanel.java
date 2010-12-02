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
 * Displays the subtypes of a def.
 */
public class SubTypesPanel extends TypeHierPanel
{
    public SubTypesPanel (long defId, DefMap defmap, PopupGroup.Positioner repos)
    {
        super(defId, defmap, repos);
    }

    protected void fetchTypes (AsyncCallback<Def[][]> callback)
    {
        _projsvc.getSubTypes(_defId, callback);
    }

    protected void init (Def[][] defs)
    {
        if (defs.length > 1) {
            add(Widgets.newInlineLabel("Subtypes of "));
        } else {
            add(Widgets.newInlineLabel("No subtypes of "));
        }
        add(makeLabel(defs[0][0])); // this is the type we asked about
        for (int ii = 1; ii < defs.length; ii++) {
            FlowPanel rc = Widgets.newFlowPanel(_rsrc.styles().borderTop());
            for (Def stype : defs[ii]) {
                rc.add(makeLabel(stype));
                rc.add(Widgets.newInlineLabel(" "));
            }
            add(rc);
        }
    }
}
