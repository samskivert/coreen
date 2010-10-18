//
// $Id$

package coreen.project;

import coreen.client.Args;
import coreen.model.Project;
import coreen.util.DefMap;
import coreen.util.IdMap;

/**
 * Contains functionality shared by top-level "project summary" panels.
 */
public abstract class SummaryPanel extends AbstractProjectPanel
{
    @Override // from AbstractProjectPanel
    public void setArgs (Project proj, Args args)
    {
        if (_projectId != proj.id) {
            updateContents(_projectId = proj.id);
            // reset our showing map when we switch projects
            _showing = IdMap.create(false);
        }
        // toggle def showingness as dictated by the args
        for (int idx = 2; args.get(idx, 0L) != 0L; idx++) {
            long id = args.get(idx, 0L);
            if (id > 0) {
                _showing.get(id).update(true);
            } else {
                _showing.get(-id).update(false);
            }
        }
    }

    protected abstract void updateContents (long projectId);

    protected long _projectId;
    protected DefMap _defmap = new DefMap();
    protected IdMap<Boolean> _showing = IdMap.create(false);
}
