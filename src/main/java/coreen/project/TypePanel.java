//
// $Id$

package coreen.project;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.SimplePanel;

import com.threerings.gwt.util.Value;

import coreen.client.Args;
import coreen.model.DefDetail;
import coreen.model.Project;
import coreen.ui.UIUtil;

/**
 * Displays a single type.
 */
public class TypePanel extends AbstractProjectPanel
{
    public TypePanel ()
    {
        initWidget(new SimplePanel());
    }

    @Override // from AbstractProjectPanel
    public ProjectPage.Detail getId ()
    {
        return ProjectPage.Detail.TYP;
    }

    @Override // from AbstractProjectPanel
    public void setArgs (final Project proj, Args args)
    {
        long defId = args.get(2, 0L);
        if (_panel == null || _panel.defId != defId) {
            ((SimplePanel)getWidget()).setWidget(_panel = TypeSummaryPanel.create(defId));
            _panel.detail.addListener(new Value.Listener<DefDetail>() {
                public void valueChanged (DefDetail deets) {
                    UIUtil.setWindowTitle(proj.name, deets.name);
                    // the def detail is posted before the UI is created, so we have to delay the
                    // expansion of the docs until the next event loop pass
                    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                        public void execute () {
                            DocLabel doc = _panel.getTypeLabel().doc;
                            if (doc != null) {
                                doc.expanded.update(true);
                            }
                        }
                    });
                }
            });
        }
        for (int idx = 3; args.get(idx, 0L) != 0L; idx++) {
            _panel.showMember(args.get(idx, 0L));
        }
    }

    protected TypeSummaryPanel _panel;
}
