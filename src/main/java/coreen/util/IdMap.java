//
// $Id$

package coreen.util;

import java.util.HashMap;
import java.util.Map;

import com.threerings.gwt.util.Value;

/**
 * A convenient map from an id to boolean {@link Value}s which automatically creates entries as
 * needed.
 */
public class IdMap
{
    /**
     * Returns, creating if needed, the value for the specified id.
     */
    public Value<Boolean> get (long id) {
        Value<Boolean> value = _map.get(id);
        if (value == null) {
            _map.put(id, value = Value.create(false));
        }
        return value;
    }

    protected Map<Long, Value<Boolean>> _map = new HashMap<Long, Value<Boolean>>();
}
