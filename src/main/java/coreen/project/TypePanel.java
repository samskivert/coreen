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

    /** Returns the detail id of the current project panel. */
    public ProjectPage.Detail getId ()
    {
        return ProjectPage.Detail.TYP;
    }

    /**
     * Called either immediately after this panel has been added to the UI hierarchy, or when the
     * panel has received new arguments, due to the user clicking on an internal link.
     */
    public void setArgs (Project proj, Args args)
    {
        long defId = args.get(2, 0L);
        if (_panel == null || _panel.defId != defId) {
            ((SimplePanel)getWidget()).setWidget(_panel = new TypeDetailPanel(defId));
        }
        for (int idx = 3; args.get(idx, 0L) != 0L; idx++) {
            _panel.showMember(args.get(idx, 0L));
        }
    }

    protected TypeDetailPanel _panel;
}
