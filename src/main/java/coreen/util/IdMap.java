//
// $Id$

package coreen.util;

import java.util.HashMap;
import java.util.Map;

import com.threerings.gwt.util.Value;

/**
 * A convenient map from an id to {@link Value}s which automatically creates entries as needed.
 */
public class IdMap<T>
{
    /**
     * Creates an id map with the supplied default value.
     */
    public static <T> IdMap<T> create (T defval)
    {
        return new IdMap<T>(defval);
    }

    /**
     * Returns, creating if needed, the value for the specified id.
     */
    public Value<T> get (long id) {
        Value<T> value = _map.get(id);
        if (value == null) {
            _map.put(id, value = Value.create(_defval));
        }
        return value;
    }

    protected IdMap (T defval)
    {
        _defval = defval;
    }

    protected T _defval;
    protected Map<Long, Value<T>> _map = new HashMap<Long, Value<T>>();
}
