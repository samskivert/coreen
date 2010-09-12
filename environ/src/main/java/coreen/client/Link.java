//
// $Id$

package coreen.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Hyperlink;

/**
 * Handles creation of intra-app links.
 */
public class Link
{
    /**
     * Creates a link to the specified page with the specified label text.
     */
    public static Hyperlink create (String label, Page page, Object... args)
    {
        return create(label, null, page, args);
    }

    /**
     * Creates an inline link to the specified page with the specified label text.
     */
    public static Hyperlink createInline (String label, Page page, Object... args)
    {
        return create(label, "inline", page, args);
    }

    /**
     * Creates a link to the specified page with the specified label text and additional style.
     */
    public static Hyperlink create (String label, String styleName, Page page, Object... args)
    {
        Hyperlink link = new Hyperlink(label, Args.createToken(page, args));
        link.addStyleName("nowrap");
        if (styleName != null) {
            link.addStyleName(styleName);
        }
        return link;
    }

    /**
     * Creates a click handler that when triggered will traverse to the specified link.
     */
    public static ClickHandler createHandler (Page page, Object... args)
    {
        final String token = Args.createToken(page, args);
        return new ClickHandler() {
            public void onClick (ClickEvent event) {
                History.newItem(token);
            }
        };
    }

    /**
     * Immediately directs the browser to the specified page with the supplied arguments.
     */
    public static void go (Page page, Object... args)
    {
        History.newItem(Args.createToken(page, args));
    }
}
