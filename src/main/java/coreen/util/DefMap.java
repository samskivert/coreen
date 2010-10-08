//
// $Id$

package coreen.util;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.Widget;

/**
 * Maintains a mapping from def id to the {@link Widget} currently displaying that def.
 */
public class DefMap
{
    /** Creates a standalone def map. */
    public DefMap ()
    {
    }

    /** Creates a def map that will search the specified parent map when a mapping is not contained
     * in this def. */
    public DefMap (DefMap parent)
    {
        _parent = parent;
    }

    /**
     * Establishes a mapping from the specified def id to the specified widget. This mapping will
     * overwrite any existing mapping.
     */
    public void map (Long defId, Widget w)
    {
        _map.put(defId, w);
    }

    /**
     * Removes the mapping from the specified def id to the specified widget. If another mapping
     * has already replaced this mapping, this is a noop.
     */
    public void unmap (Long defId, Widget w)
    {
        Widget have = _map.get(defId);
        if (w == have) {
            _map.remove(defId);
        }
    }

    /**
     * Returns the widget that is displaying the specified def, or null.
     */
    public Widget get (Long defId)
    {
        Widget w = _map.get(defId);
        if (w != null) {
            return w;
        }
        return (_parent != null) ? _parent.get(defId) : null;
    }

    /**
     * Adds the mappings in this defmap to the specified target map.
     */
    public void addTo (DefMap other)
    {
        for (Map.Entry<Long, Widget> entry : _map.entrySet()) {
            other.map(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Removes the mappings in this defmap from the specified target map.
     */
    public void removeFrom (DefMap other)
    {
        for (Map.Entry<Long, Widget> entry : _map.entrySet()) {
            other.unmap(entry.getKey(), entry.getValue());
        }
    }

    protected DefMap _parent;
    protected Map<Long, Widget> _map = new HashMap<Long, Widget>();
}
