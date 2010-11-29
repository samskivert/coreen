//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Value;

import coreen.client.ClientMessages;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.ui.PopupGroup;
import coreen.model.DefId;
import coreen.util.DefMap;
import coreen.util.PanelCallback;

/**
 * A panel that shows all of the uses of a particular def.
 */
public class DefUsesPanel extends FlowPanel
{
    public DefUsesPanel (DefId def, DefMap defmap, final PopupGroup.Positioner repos)
    {
        add(Widgets.newLabel(_cmsgs.loading()));
        _def = def;
        _defmap = defmap;

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
        String title = (results.length == 0) ? "No uses of " : "Uses of ";
        add(Widgets.newLabel(title + _def.name));
        // DefUtil.addDef(this, _def, _defmap, UsePopup.TYPE);
        for (final ProjectService.UsesResult result : results) {
            FlowPanel header = TypeLabel.makeTypeHeader(result, _defmap, UsePopup.TYPE);
            header.addStyleName(_rsrc.styles().borderTop());
            add(header);
            add(new TogglePanel(Value.create(false)) {
                protected Widget createCollapsed () {
                    FlowPanel bits = new FlowPanel();
                    // bits.add(new SourcePanel(result, _defmap, UsePopup.TYPE));
                    for (int ii = 0; ii < result.uses.length; ii++) {
                        // TODO: add line number
                        bits.add(new SourcePanel(result.lines[ii], result.uses[ii],
                                                 _defmap, UsePopup.TYPE));
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
