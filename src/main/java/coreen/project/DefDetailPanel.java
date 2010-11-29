//
// $Id$

package coreen.project;

import com.google.gwt.user.client.ui.FlowPanel;

import coreen.client.Args;
import coreen.model.Project;

/**
 * Displays a single type.
 */
public class DefDetailPanel extends AbstractProjectPanel
{
    public DefDetailPanel ()
    {
        initWidget(_contents = new FlowPanel());
    }

    @Override // from AbstractProjectPanel
    public ProjectPage.Detail getId ()
    {
        return ProjectPage.Detail.DEF;
    }

    @Override // from AbstractProjectPanel
    public void setArgs (final Project proj, Args args)
    {
        _contents.clear();
        _contents.add(new DefSourcePanel(args.get(2, 0L)));
    }

    protected FlowPanel _contents;
}
