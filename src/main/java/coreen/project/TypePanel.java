//
// $Id$

package coreen.project;

import com.google.gwt.user.client.ui.SimplePanel;

import coreen.client.Args;
import coreen.model.Project;

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
    public void setArgs (Project proj, Args args)
    {
        long defId = args.get(2, 0L);
        if (_panel == null || _panel.defId != defId) {
            ((SimplePanel)getWidget()).setWidget(_panel = new TypeSummaryPanel(defId, false));
        }
        for (int idx = 3; args.get(idx, 0L) != 0L; idx++) {
            _panel.showMember(args.get(idx, 0L));
        }
    }

    protected TypeSummaryPanel _panel;
}
