//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Label;

import coreen.model.Def;
import coreen.util.DefMap;

/**
 * Displays a def signature. Handles registering itself with a def map when visible.
 */
public class SigLabel extends Label
{
    public SigLabel (Def def, String sig, DefMap defmap)
    {
        super(sig);
        _def = def;
        _defmap = defmap;
        _defmap.map(_def.id, this); // we start out visible
        addStyleName(_rsrc.styles().code());
    }

    @Override // from Widget
    public void setVisible (boolean visible)
    {
        super.setVisible(visible);
        if (visible) {
            _defmap.map(_def.id, this);
        } else {
            _defmap.unmap(_def.id, this);
        }
    }

    protected Def _def;
    protected DefMap _defmap;

    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
