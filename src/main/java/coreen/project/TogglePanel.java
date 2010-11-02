//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Bindings;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Value;

import coreen.icons.IconResources;

/**
 * Toggles between two widgets, one which is the collapsed representation and one that is the
 * expanded representation. Users must implement {@link #createCollapsed} and {@link
 * #createExpanded} which will be called to create the appropriate visualization the first time
 * said visualization is needed.
 */
public abstract class TogglePanel extends FlowPanel // FlexTable
{
    public static ToggleButton makeToggleButton (Value<Boolean> model)
    {
        ToggleButton toggle = new ToggleButton(
            new Image(_icons.codeClosed()), new Image(_icons.codeOpen()));
        toggle.addStyleName(_rsrc.styles().toggle());
        Bindings.bindDown(model, toggle);
        return toggle;
    }

    public TogglePanel (Value<Boolean> model)
    {
        // setCellSpacing(0);
        // setCellPadding(0);

        add(makeToggleButton(model));
        // setWidget(0, 0, makeToggleButton(model));
        // getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);

        // we keep a reference to the model in case clients want to make their collapsed widget a
        // clickable label that expands the toggle panel
        _model = model;
        _model.addListenerAndTrigger(new Value.Listener<Boolean>() {
            public void valueChanged (Boolean value) {
                remove(_clear);
                if (value) {
                    if (_expanded == null) {
                        _expanded = createExpanded();
                    }
                    if (_collapsed != null) {
                        remove(_collapsed);
                    }
                    // setWidget(0, 1, _expanded);
                    add(_expanded);
                } else {
                    if (_collapsed == null) {
                        _collapsed = createCollapsed();
                    }
                    if (_expanded != null) {
                        remove(_expanded);
                    }
                    // setWidget(0, 1, _collapsed);
                    add(_collapsed);
                }
                add(_clear);
            }
            protected Widget _collapsed, _expanded;
        });
    }

    protected abstract Widget createCollapsed ();
    protected abstract Widget createExpanded ();

    protected Value<Boolean> _model;
    protected Widget _clear = Widgets.newLabel("", _rsrc.styles().defClear());

    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
    protected static final IconResources _icons = GWT.create(IconResources.class);
}
