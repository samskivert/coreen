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
    public DocLabel (final String docHTML)
    {
        addStyleName(_rsrc.styles().doc());

        final int didx = docHTML.indexOf(". "); // TODO: smarter "sentence" detection
        if (didx == -1 || docHTML.substring(didx+1).trim().length() == 0) {
            add(Widgets.newHTML(docHTML));
        } else {
            add(new TogglePanel(Value.create(false)) {
                protected Widget createCollapsed () {
                    return Widgets.newHTML(docHTML.substring(0, didx+1));
                }
                protected Widget createExpanded () {
                    return Widgets.newHTML(docHTML);
                }
            });
        }
    }

    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
