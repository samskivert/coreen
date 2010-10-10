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
            // reset our type and id maps when we switch projects
            _types = IdMap.create(false);
            _members = IdMap.create(false);
        }
        // activate the type and members specified in the args
        _types.get(args.get(2, 0L)).update(true);
        for (int idx = 3; args.get(idx, 0L) != 0L; idx++) {
            _members.get(args.get(idx, 0L)).update(true);
        }
    }

    protected abstract void updateContents (long projectId);

    protected long _projectId;
    protected DefMap _defmap = new DefMap();
    protected IdMap<Boolean> _types = IdMap.create(false);
    protected IdMap<Boolean> _members = IdMap.create(false);
}
