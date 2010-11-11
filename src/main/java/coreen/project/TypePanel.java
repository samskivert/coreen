//
// $Id$

package coreen.project;

import com.google.gwt.user.client.ui.SimplePanel;

import coreen.client.Args;
import coreen.ui.UIUtil;
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
    public void setArgs (final Project proj, Args args)
    {
        long defId = args.get(2, 0L);
        if (_panel == null || _panel.defId != defId) {
            ((SimplePanel)getWidget()).setWidget(_panel = TypeSummaryPanel.create(defId));
            // TODO: UIUtil.setWindowTitle(proj.name, sum.name);
        }
        for (int idx = 3; args.get(idx, 0L) != 0L; idx++) {
            _panel.showMember(args.get(idx, 0L));
        }
    }

    protected TypeSummaryPanel _panel;
}
