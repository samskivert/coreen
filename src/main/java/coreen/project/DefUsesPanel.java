//
// $Id$

package coreen.project;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Value;

import coreen.client.ClientMessages;
import coreen.model.DefId;
import coreen.model.Use;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.ui.PopupGroup;
import coreen.ui.UIUtil;
import coreen.util.DefMap;
import coreen.util.PanelCallback;

/**
 * A panel that shows all of the uses of a particular def.
 */
public class DefUsesPanel extends FlowPanel
{
    public DefUsesPanel (DefId def, DefMap defmap,
                         final PopupPanel popup, final PopupGroup.Positioner repos)
    {
        add(Widgets.newLabel(_cmsgs.loading()));
        _def = def;
        _defmap = defmap;

        _projsvc.findUses(def.id, new PanelCallback<ProjectService.UsesResult[]>(this) {
            public void onSuccess (ProjectService.UsesResult[] results) {
                clear();
                init(popup, results);
                repos.sizeDidChange();
            }
        });
    }

    protected void init (PopupPanel popup, ProjectService.UsesResult[] results)
    {
        String title = (results.length == 0) ? "No uses of " : "Uses of ";
        add(Widgets.newFlowPanel(UIUtil.makeFloatLeft(UIUtil.makeDragger(popup)),
                                 Widgets.newLabel(title + _def.name)));
        // DefUtil.addDef(this, _def, _defmap, UsePopup.TYPE);

        for (final ProjectService.UsesResult result : results) {
            FlowPanel header = TypeLabel.makeTypeHeader(result, _defmap, UsePopup.TYPE);
            header.addStyleName(_rsrc.styles().borderTop());
            add(header);
            add(new TogglePanel(Value.create(false)) {
                protected Widget createCollapsed () {
                    FlowPanel bits = new FlowPanel();
                    List<Use> uses = new ArrayList<Use>();
                    for (int ii = 0; ii < result.uses.length; ii++) {
                        uses.add(result.uses[ii]);
                        // combine multiple on the same line into one SourcePanel
                        int lineNo = result.lineNos[ii];
                        if (ii == result.uses.length-1 || lineNo != result.lineNos[ii+1]) {
                            // TODO: add line number
                            bits.add(new SourcePanel(result.lines[ii], uses,
                                                     _defmap, UsePopup.TYPE));
                            uses.clear();
                        }
                    }
                    return bits;
                }
                protected Widget createExpanded () {
                    return new SourcePanel(result.id, _defmap, UsePopup.TYPE);
                }
            });
        }
    }

    protected DefId _def;
    protected DefMap _defmap;
    protected PopupGroup _pgroup = new PopupGroup();

    protected static final ClientMessages _cmsgs = GWT.create(ClientMessages.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
}
