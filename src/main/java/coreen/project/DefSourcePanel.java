//
// $Id$

package coreen.project;

import coreen.model.DefContent;
import coreen.util.DefMap;
import coreen.util.PanelCallback;

/**
 * A source panel with a type label at its top.
 */
public class DefSourcePanel extends SourcePanel
{
    public DefSourcePanel (long defId)
    {
        this(defId, new DefMap(), UsePopup.TYPE);
    }

    public DefSourcePanel (long defId, DefMap defmap, UsePopup.Linker linker)
    {
        super(defmap, linker);

        _projsvc.getContent(defId, new PanelCallback<DefContent>(_contents) {
            public void onSuccess (DefContent content) {
                init(content);
                _contents.insert(new TypeLabel(content, _defmap, _linker), 0);
            }
        });
    }
}
