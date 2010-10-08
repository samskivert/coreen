//
// $Id$

package coreen.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;

import coreen.model.CompUnitDetail;
import coreen.model.Def;
import coreen.model.DefDetail;
import coreen.model.Use;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.ui.WindowFX;
import coreen.util.Edit;
import coreen.util.Errors;
import coreen.util.PanelCallback;

/**
 * Displays a single compilation unit.
 */
public abstract class SourcePanel extends Composite
{
    /** A source panel that displays an entire compilation unit. */
    public static class Full extends SourcePanel {
        public Full (final long unitId, final long scrollToDefId) {
            super(new HashMap<Long, Widget>());
            _projsvc.getCompUnit(unitId, new PanelCallback<CompUnitDetail>(_contents) {
                public void onSuccess (CompUnitDetail detail) {
                    init(detail.text, detail.defs, detail.uses, scrollToDefId, UsePopup.SOURCE);
                }
            });
        }
    }

    public SourcePanel (Map<Long, Widget> defmap)
    {
        initWidget(_binder.createAndBindUi(this));
        _contents.setWidget(Widgets.newLabel("Loading..."));
        _defmap = defmap;
    }

    @Override // from Widget
    protected void onUnload ()
    {
        super.onUnload();
        // clear out the defs we were displaying
        for (Long defId : _added) {
            _defmap.remove(defId);
        }
    }

    protected void init (String text, Def[] defs, Use[] uses, long scrollToDefId,
                         final Function<DefDetail, Widget> linker)
    {
        // TODO: make sure this doesn't freak out when source uses CRLF
        JsArrayString lines = splitString(text, "\n");
        String first = expandTabs(lines.get(0));
        int prefix = first.indexOf(first.trim());
        if (prefix > 0) {
            // scan through another ten lines to ensure that the first line wasn't anomalous in
            // establishing our indentation prefix
            for (int ii = 0, ll = Math.min(lines.length(), 10); ii < ll; ii++) {
                String line = expandTabs(lines.get(ii)), tline = line.trim();
                if (tline.length() != 0 && // line is not blank
                    line.substring(0, Math.min(line.length(), prefix)).trim().length() > 0) {
                    prefix = line.indexOf(tline);
                }
            }
        }

        List<Elementer> elems = new ArrayList<Elementer>();
        for (final Def def : defs) {
            elems.add(new Elementer(def.start, def.start+def.name.length()) {
                public Widget createElement (String text) {
                    Widget w = Widgets.newInlineLabel(text, _rsrc.styles().def());
                    _added.add(def.id);
                    _defmap.put(def.id, w);
                    return w;
                }
            });
        }
        for (final Use use : uses) {
            elems.add(new Elementer(use.start, use.start+use.length) {
                public Widget createElement (String text) {
                    Widget span = Widgets.newInlineLabel(text, _rsrc.styles().use());
                    new UsePopup.Popper(use.referentId, span, _defmap, linker);
                    return span;
                }
            });
        }
        Collections.sort(elems);

        int offset = 0;
        FlowPanel code = Widgets.newFlowPanel(_rsrc.styles().code());
        for (Elementer elem : elems) {
            if (elem.startPos < 0) continue; // filter undisplayable elems
            if (elem.startPos > offset) {
                String seg = expandTabs(text.substring(offset, elem.startPos));
                // special handling for the first line since we can't rely on it following a
                // newline to tell us that it needs to be trimmed
                if (offset == 0 && prefix > 0) {
                    seg = seg.substring(prefix);
                }
                code.add(Widgets.newInlineLabel(trimPrefix(seg, prefix)));
            }
            code.add(elem.createElement(text.substring(elem.startPos, elem.endPos)));
            offset = elem.endPos;
        }
        if (offset < text.length()) {
            code.add(Widgets.newInlineLabel(
                         trimPrefix(expandTabs(text.substring(offset)), prefix)));
        }

        // final Widget scrollTo = _defmap.get(scrollToDefId);
        // if (scrollTo != null) {
        //     DeferredCommand.addCommand(new Command() {
        //         public void execute () {
        //             WindowFX.scrollTo(scrollTo);
        //         }
        //     });
        // }

        _contents.setWidget(code);
        didInit();
    }

    protected void didInit ()
    {
    }

    protected abstract class Elementer implements Comparable<Elementer> {
        public final int startPos;
        public final int endPos;

        public abstract Widget createElement (String text);

        public int compareTo (Elementer other) {
            return startPos - other.startPos;
        }

        protected Elementer (int startPos, int endPos) {
            this.startPos = startPos;
            this.endPos = endPos;
        }
    }

    // TODO: allow per-project settings
    protected static String expandTabs (String text)
    {
        return text.replace("\t", "        ");
    }

    protected native JsArrayString splitString (String text, String delim) /*-{
        return text.split(delim);
    }-*/;

    protected native String trimPrefix (String text, int prefix) /*-{
        if (prefix == 0) {
            return text;
        }
        var lines = text.split("\n");
        var ii, ll = lines.length;
        for (ii = 1; ii < ll; ii++) {
           var line = lines[ii];
           if (line.length > prefix) { // TODO: avoid chopping non-whitespace
             lines[ii] = line.substring(prefix);
           }
         }
         return lines.join("\n");
    }-*/;

    protected Map<Long, Widget> _defmap;
    protected Set<Long> _added = new HashSet<Long>();

    protected @UiField SimplePanel _contents;

    protected interface Binder extends UiBinder<Widget, SourcePanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
