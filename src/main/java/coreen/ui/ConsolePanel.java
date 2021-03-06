//
// $Id$

package coreen.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Value;

import coreen.client.ClientMessages;
import coreen.rpc.ConsoleService;
import coreen.rpc.ConsoleServiceAsync;
import coreen.util.Errors;
import coreen.util.PanelCallback;

/**
 * Dispalys the contents of a console.
 */
public class ConsolePanel extends ScrollPanel
{
    /** Whether or not this console is "open" (may receive new data). */
    public final Value<Boolean> isOpen;

    /**
     * Creates a panel to display the specified console.
     *
     * @param defaultOpenValue the value to report for {@link #isOpen} until the actual open-status
     * of the console is known.
     */
    public ConsolePanel (String id, boolean defaultOpenValue)
    {
        addStyleName(_rsrc.styles().console());
        _id = id;
        isOpen = Value.create(defaultOpenValue);
        setWidget(Widgets.newLabel(_cmsgs.loading()));
        _consvc.fetchConsole(id, 0, new PanelCallback<ConsoleService.ConsoleResult>(this) {
            public void onSuccess (ConsoleService.ConsoleResult result) {
                setWidget(_data);
                update(result.lines, result.isOpen);
            }
        });
    }

    public void reset ()
    {
        _offset = 0;
        _data.clear();
        refresh();
    }

    public void refresh ()
    {
        _consvc.fetchConsole(_id, _offset, new AsyncCallback<ConsoleService.ConsoleResult>() {
            public void onSuccess (ConsoleService.ConsoleResult result) {
                update(result.lines, result.isOpen);
            }
            public void onFailure (Throwable cause) {
                _data.add(Widgets.newLabel(Errors.xlate(cause), "errorLabel"));
            }
        });
    }

    protected void update (String[] lines, boolean isOpen)
    {
        this.isOpen.update(isOpen);
        _offset += lines.length;
        for (String line : lines) {
            _data.add(Widgets.newLabel(line));
        }
        scrollToBottom();

        if (isOpen) {
            new Timer() {
                public void run () {
                    refresh();
                }
            }.schedule(1000);
        }
    }

    protected String _id;
    protected int _offset;
    protected FlowPanel _data = new FlowPanel();

    protected static final ConsoleServiceAsync _consvc = GWT.create(ConsoleService.class);
    protected static final ClientMessages _cmsgs = GWT.create(ClientMessages.class);

    protected static final UIResources _rsrc = GWT.create(UIResources.class);
    static {
        _rsrc.styles().ensureInjected();
    }
}
