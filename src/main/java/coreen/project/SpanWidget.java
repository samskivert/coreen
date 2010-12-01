//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.InlineLabel;

import coreen.model.Span;

/**
 * Displays either a def or use in a source panel.
 */
public class SpanWidget extends InlineLabel
{
    /** A span widget, not styled. */
    public static class Plain extends SpanWidget {
        public Plain (String text, Span span) {
            super(text, span);
        }
    }

    /** A span widget, styled for defs. */
    public static class Def extends SpanWidget {
        public Def (String text, Span span) {
            super(text, span);
            addStyleName(DefUtil.getDefStyle(span.getKind()));
        }
    }

    /** A span widget, styled for uses. */
    public static class Use extends SpanWidget {
        public Use (String text, Span span) {
            super(text, span);
            addStyleName(DefUtil.getUseStyle(span.getKind()));
        }
    }

    /**
     * Toggles the highlightedness of this span.
     */
    public void setHighlighted (boolean highlighted)
    {
        if (highlighted) {
            addStyleName(DefUtil.getHighStyle(_span.getKind()));
        } else {
            removeStyleName(DefUtil.getHighStyle(_span.getKind()));
        }
    }

    protected SpanWidget (String text, Span span)
    {
        super(text);
        _span = span;
    }

    protected Span _span;
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
