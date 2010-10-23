//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Value;

/**
 * Displays def documentation. Provides an abbreviated mode where contents following the first
 * period are hidden, and a mechanism by which to expand the hidden contents.
 */
public class DocLabel extends FlowPanel
{
    public DocLabel (String docHTML)
    {
        this(docHTML, false);
    }

    public DocLabel (String docHTML, boolean alwaysFull)
    {
        addStyleName(_rsrc.styles().doc());

        // if we have no docs, display a message to that effect
        final String fullDoc = (docHTML == null) ? _msgs.pNoDocs() : docHTML;

        if (alwaysFull) {
            add(Widgets.newHTML(fullDoc));
            return;
        }

        // TODO: smarter "sentence" detection?
        String[] bits = fullDoc.split("\\.(\\s|<br/>|<br>)+");
        if (bits.length == 1) {
            add(Widgets.newHTML(fullDoc));
        } else {
            final String shortDoc = bits[0] + ".";
            add(new TogglePanel(Value.create(false)) {
                protected Widget createCollapsed () {
                    return Widgets.newHTML(shortDoc);
                }
                protected Widget createExpanded () {
                    return Widgets.newHTML(fullDoc);
                }
            });
        }
    }

    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
    protected static final ProjectMessages _msgs = GWT.create(ProjectMessages.class);
}
