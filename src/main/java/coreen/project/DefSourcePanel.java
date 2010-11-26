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
public class DefSourcePanel extends AbstractProjectPanel
{
    public DefSourcePanel ()
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
        long defId = args.get(2, 0L);
        final SourcePanel source = new SourcePanel(_defmap);
        _contents.add(source);
        _projsvc.getContent(defId, new PanelCallback<DefContent>(_contents) {
            public void onSuccess (DefContent content) {
                _contents.insert(new TypeLabel(content, _defmap, UsePopup.TYPE), 0);
                source.init(content, UsePopup.TYPE, false);
            }
        });
    }

    protected FlowPanel _contents;
            protected DefMap _defmap = new DefMap();
}
