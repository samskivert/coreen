//
// $Id$

package coreen.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coreen.project.SpanWidget;

/**
 * Maintains a mapping from def id to the {@link SpanWidget} currently displaying that def, as well
 * as a mapping from def id to the list of {@link SpanWidget}s displaying its uses.
 */
public class DefMap
{
    /**
     * Establishes a mapping from the specified def id to the specified widget. This mapping will
     * overwrite any existing mapping.
     */
    public void map (Long defId, SpanWidget w)
    {
        _map.put(defId, w);
    }

    /**
     * Notes a widget that contains a use of the specified def. This mapping will be appended to
     * any existing use mappings.
     */
    public void mapUse (Long defId, SpanWidget w)
    {
        getOrCreateUses(defId).add(w);
    }

    /**
     * Notes that the supplied list of widgets represent uses of the specified def. The mappings
     * will be appended to any existing use mappings.
     */
    public void mapUses (Long defId, List<SpanWidget> ws)
    {
        getOrCreateUses(defId).addAll(ws);
    }

    /**
     * Removes the mapping from the specified def id to the specified widget. If another mapping
     * has already replaced this mapping, this is a noop.
     */
    public void unmap (Long defId, SpanWidget w)
    {
        SpanWidget have = _map.get(defId);
        if (w == have) {
            _map.remove(defId);
        }
    }

    /**
     * Removes the supplied use mapping for the specified def.
     */
    public void unmapUse (Long defId, SpanWidget w)
    {
        List<SpanWidget> uses = _useMap.get(defId);
        if (uses != null) {
            uses.remove(w);
        }
    }

    /**
     * Removes the supplied use mappings for the specified def.
     */
    public void unmapUses (Long defId, List<SpanWidget> ws)
    {
        List<SpanWidget> ouses = _useMap.get(defId);
        if (ouses != null) {
            // optimize removal if all the mappings are being removed; note: we're assuming these
            // mappings are the same without checking them; given the way we use defmap, this will
            // be the case
            if (ouses.size() == ws.size()) {
                _useMap.remove(defId);
            } else {
                for (SpanWidget w : ws) { // TODO: ugh, O(n^2)
                    ouses.remove(w);
                }
            }
        }
    }

    /**
     * Returns the widget that is displaying the specified def, or null.
     */
    public SpanWidget get (Long defId)
    {
        return _map.get(defId);
    }

    /**
     * Returns the widgets that represent uses of the specified def. The returned list may be empty
     * but will not be null.
     */
    public List<SpanWidget> getUses (Long defId)
    {
        return getUses(defId, new ArrayList<SpanWidget>());
    }

    /**
     * Copies the widgets that represent uses of the specified def into the supplied list.
     * @return the supplied list.
     */
    public List<SpanWidget> getUses (Long defId, List<SpanWidget> into)
    {
        List<SpanWidget> uses = _useMap.get(defId);
        if (uses != null) {
            into.addAll(uses);
        }
        return into;
    }

    /**
     * Adds the mappings in this defmap to the specified target map.
     */
    public void addTo (DefMap other)
    {
        for (Map.Entry<Long, SpanWidget> entry : _map.entrySet()) {
            other.map(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Long, List<SpanWidget>> entry : _useMap.entrySet()) {
            other.mapUses(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Removes the mappings in this defmap from the specified target map.
     */
    public void removeFrom (DefMap other)
    {
        for (Map.Entry<Long, SpanWidget> entry : _map.entrySet()) {
            other.unmap(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Long, List<SpanWidget>> entry : _useMap.entrySet()) {
            other.unmapUses(entry.getKey(), entry.getValue());
        }
    }

    protected List<SpanWidget> getOrCreateUses (Long defId)
    {
        List<SpanWidget> uses = _useMap.get(defId);
        if (uses == null) {
            _useMap.put(defId, uses = new ArrayList<SpanWidget>());
        }
        return uses;
    }

    protected Map<Long, SpanWidget> _map = new HashMap<Long, SpanWidget>();
    protected Map<Long, List<SpanWidget>> _useMap = new HashMap<Long, List<SpanWidget>>();
}
