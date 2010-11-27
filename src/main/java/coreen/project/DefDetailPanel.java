//
// $Id$

package coreen.project;

import com.google.gwt.user.client.ui.FlowPanel;

import coreen.client.Args;
import coreen.model.DefContent;
import coreen.model.Kind;
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
        _contents.clear();

        long defId = args.get(3, 0L);
        switch (args.get(2, Kind.class, Kind.TERM)) {
        case MODULE:
            // TODO
            break;

        case TYPE:
            _contents.add(TypeSummaryPanel.create(defId));
            break;

        default: // FUNC, TERM
            final DefMap defmap = new DefMap();
            final SourcePanel source = new SourcePanel(defmap);
            _contents.add(source);
            _projsvc.getContent(defId, new PanelCallback<DefContent>(_contents) {
                public void onSuccess (DefContent content) {
                    _contents.insert(new TypeLabel(content, defmap, UsePopup.TYPE), 0);
                    source.init(content, UsePopup.TYPE, false);
                }
            });
            break;
        }
    }

    protected FlowPanel _contents;
}
