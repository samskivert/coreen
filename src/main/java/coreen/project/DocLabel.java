//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;

import com.threerings.gwt.ui.Widgets;

/**
 * Displays def documentation. Provides an abbreviated mode where contents following the first
 * period are hidden, and a mechanism by which to expand the hidden contents.
 */
public class DocLabel extends FlowPanel
{
    public DocLabel (String docHTML)
    {
        addStyleName(_rsrc.styles().doc());
        _docs = docHTML;

        int didx = docHTML.indexOf("."); // TODO: smarter "sentence" detection
        if (didx == -1 || docHTML.substring(didx+1).trim().length() == 0) {
            add(Widgets.newHTML(_shortDocs = docHTML));
        } else {
            _shortDocs = docHTML.substring(0, didx+1);
            setExpanded(false);
        }
    }

    public void setExpanded (final boolean expanded)
    {
        clear();
        add(Widgets.newHTML(expanded ? _docs : _shortDocs, "inline"));
        add(Widgets.newInlineLabel(" "));
        String exlbl = expanded ? "[less]" : "[more]";
        add(Widgets.newActionLabel(exlbl, "inline", new ClickHandler() {
            public void onClick (ClickEvent event) {
                setExpanded(!expanded);
            }
        }));
    }

    protected String _shortDocs, _docs;

    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
