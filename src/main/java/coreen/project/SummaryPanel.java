//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;

import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;

/**
 * Contains functionality shared by top-level "project summary" panels.
 */
public abstract class SummaryPanel extends Composite
{
    public void display (long projectId, long typeId)
    {
        if (_projectId != projectId) {
            updateContents(_projectId = projectId);
            // reset our type and id maps when we switch projects
            _types = IdMap.create(false);
            _members = IdMap.create(false);
        }
        _types.get(typeId).update(true);
    }

    public void showMember (long memberId)
    {
        _members.get(memberId).update(true);
    }

    protected abstract void updateContents (long projectId);

    protected long _projectId;
    protected DefMap _defmap = new DefMap();
    protected IdMap<Boolean> _types = IdMap.create(false);
    protected IdMap<Boolean> _members = IdMap.create(false);

    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
}
