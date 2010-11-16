//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
