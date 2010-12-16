//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
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

        addAttachHandler(new AttachEvent.Handler() {
            public void onAttachOrDetach (AttachEvent event) {
                if (event.isAttached() && isInPopup(DocLabel.this)) {
                    addStyleName(_rsrc.styles().popDoc());
                } else {
                    removeStyleName(_rsrc.styles().popDoc());
                }
            }
        });

        // if we have no docs, display a message to that effect
        final String fullDoc = (docHTML == null) ? _msgs.pNoDocs() : docHTML;

        if (alwaysFull) {
            add(Widgets.newHTML(fullDoc));
            return;
        }

        // detect the end of the first "sentence"
        int didx = fullDoc.indexOf(".");
        while (didx > 0) {
            // if the period is at the end of the full doc, just show the full doc
            if (didx == fullDoc.length()-1) {
                didx = -1;
            } else {
                // if the character after the period is whitespace or an open tag, we've found the
                // end of the first sentence
                char afterDot = fullDoc.charAt(didx+1);
                if (isSpace(afterDot) || afterDot == '<') {
                    break;
                }
                // otherwise look for the next period
                didx = fullDoc.indexOf(".", didx+1);
            }
        }

        if (didx == -1) {
            add(Widgets.newHTML(fullDoc));
        } else {
            final String shortDoc = fullDoc.substring(0, didx+1);
            add(new TogglePanel(Value.create(false)) {
                protected Widget createCollapsed () {
                    return Widgets.newHTML(shortDoc, _rsrc.styles().shortDoc());
                }
                protected Widget createExpanded () {
                    return Widgets.newHTML(fullDoc);
                }
            });
        }
    }

    protected boolean isInPopup (Widget target)
    {
        if (target == null) {
            return false;
        } else if (target instanceof PopupPanel) {
            return true;
        } else {
            return isInPopup(target.getParent());
        }
    }

    // copied from Character to avoid deprecation warning; Character.isWhitespace is not
    // implemented for JavaScript, so we have no more appealing option
    protected static boolean isSpace (char c) {
        switch (c) {
        case ' ': return true;
        case '\n': return true;
        case '\t': return true;
        case '\f': return true;
        case '\r': return true;
        default: return false;
        }
    }

    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
    protected static final ProjectMessages _msgs = GWT.create(ProjectMessages.class);
}
