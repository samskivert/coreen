//
// $Id$

package coreen.util;

import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Bindings;
import com.threerings.gwt.util.Value;

/**
 * Creates and adds a widget to a panel the first time a boolean value becomes true. Binds the
 * visibility of the widget to the boolean value.
 */
public abstract class Shower implements Value.Listener<Boolean>
{
    // from interface Value.Listener<Boolean>
    public void valueChanged (Boolean showing)
    {
        if (showing && _widget == null) {
            _widget = createWidget();
            Bindings.bindVisible(_value, _widget);
            addWidget();
            _value.removeListener(this); // no longer needed
        }
    }

    protected Shower (Value<Boolean> value, Panel target)
    {
        _target = target;
        _value = value;
        _value.addListenerAndTrigger(this);
    }

    protected abstract Widget createWidget ();

    protected void addWidget ()
    {
        // if possible, we add the widget at the top
        if (_target instanceof InsertPanel) {
            ((InsertPanel)_target).insert(_widget, 0);
        } else {
            _target.add(_widget);
        }
    }

    protected Value<Boolean> _value;
    protected Panel _target;
    protected Widget _widget;
}
