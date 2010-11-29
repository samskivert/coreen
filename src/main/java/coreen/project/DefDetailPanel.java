//
// $Id$

package coreen.project;

import com.google.gwt.user.client.ui.FlowPanel;

import coreen.client.Args;
import coreen.model.DefContent;
import coreen.model.Project;
import coreen.util.DefMap;
import coreen.util.PanelCallback;

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
        final SourcePanel source = new SourcePanel(_defmap, UsePopup.TYPE);
        _contents.clear();
        _contents.add(source);

        _projsvc.getContent(args.get(2, 0L), new PanelCallback<DefContent>(_contents) {
            public void onSuccess (DefContent content) {
                _contents.insert(new TypeLabel(content, _defmap, UsePopup.TYPE), 0);
                source.init(content);
            }
        });
    }

    protected FlowPanel _contents;
    protected DefMap _defmap = new DefMap();
}
