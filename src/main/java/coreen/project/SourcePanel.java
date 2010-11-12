//
// $Id$

package coreen.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;

import coreen.client.Args;
import coreen.model.CompUnitDetail;
import coreen.model.DefContent;
import coreen.model.DefInfo;
import coreen.model.Project;
import coreen.model.Span;
import coreen.ui.WindowFX;
import coreen.util.DefMap;
import coreen.util.PanelCallback;

/**
 * Displays a single compilation unit.
 */
public class SourcePanel extends AbstractProjectPanel
{
    /** A source panel that displays an entire compilation unit. */
    public static class Full extends SourcePanel {
        public Full () {
            super(new DefMap());
        }
    }

    public SourcePanel (DefMap defmap)
    {
        initWidget(_binder.createAndBindUi(this));
        _contents.setWidget(Widgets.newLabel("Loading..."));
        _defmap = defmap;
        _local = new DefMap(_defmap);
    }

    public SourcePanel (long defId, DefMap defmap, UsePopup.Linker linker, boolean addDefIcon)
    {
        this(defmap);
        loadDef(defId, linker, addDefIcon);
    }

    public SourcePanel (DefInfo def, DefMap defmap, UsePopup.Linker linker)
    {
        this(defmap);
        init(def.sig, def.sigDefs, def.sigUses, -1L, linker, false);
        // TODO: add a def icon
    }

    public SourcePanel (String text, Span use, DefMap defmap, UsePopup.Linker linker)
    {
        this(defmap);
        init(text, new Span[0], new Span[] { use }, -1L, linker, true);
    }

    /**
     * Loads the source for the specified def into this panel.
     */
    public void loadDef (long defId, final UsePopup.Linker linker, final boolean addDefIcon)
    {
        _projsvc.getContent(defId, new PanelCallback<DefContent>(_contents) {
            public void onSuccess (DefContent content) {
                Widget deficon = addDefIcon ? DefUtil.iconForDef(content) : null;
                init(content.text, content.defs, content.uses, -1L, linker, false);
                if (deficon != null) {
                    ((FlowPanel)_contents.getWidget()).insert(deficon, 0);
                }
            }
        });
    }

    @Override // from AbstractProjectPanel
    public ProjectPage.Detail getId ()
    {
        return ProjectPage.Detail.SRC;
    }

    @Override // from AbstractProjectPanel
    public void setArgs (Project proj, Args args)
    {
        final long scrollToDefId = args.get(3, 0L);
        _projsvc.getCompUnit(args.get(2, 0L), new PanelCallback<CompUnitDetail>(_contents) {
            public void onSuccess (CompUnitDetail detail) {
                init(detail.text, detail.defs, detail.uses, scrollToDefId, UsePopup.SOURCE, false);
            }
        });
    }

    @Override // from Widget
    public void setVisible (boolean visible)
    {
        super.setVisible(visible);
        if (visible) {
            _local.addTo(_defmap);
        } else {
            _local.removeFrom(_defmap);
        }
    }

    @Override // from Widget
    protected void onUnload ()
    {
        super.onUnload();
        _local.removeFrom(_defmap);
    }

    protected void init (String text, Span[] defs, Span[] uses, long scrollToDefId,
                         final UsePopup.Linker linker, boolean disablePrefixTrim)
    {
        // TODO: make sure this doesn't freak out when source uses CRLF
        JsArrayString lines = splitString(text, "\n");
        String first = expandTabs(lines.get(0));
        int prefix = disablePrefixTrim ? 0 : first.indexOf(first.trim());
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
        for (final Span def : defs) {
            elems.add(new Elementer(def.getStart(), def.getStart()+def.getLength()) {
                public Widget createElement (String text) {
                    Widget w = Widgets.newInlineLabel(text, DefUtil.getDefStyle(def.getKind()));
                    if (def.getId() > 0) { // TODO: nix when we add ids to SigDefs
                        w.setTitle(""+def.getId());
                        _local.map(def.getId(), w);
                    }
                    return w;
                }
            });
        }
        for (final Span use : uses) {
            elems.add(new Elementer(use.getStart(), use.getStart()+use.getLength()) {
                public Widget createElement (String text) {
                    Widget span = Widgets.newInlineLabel(text, DefUtil.getUseStyle(use.getKind()));
                    new UsePopup.Popper(use.getId(), span, linker, _local, true);
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
                if (elem.startPos >= text.length()) {
                    GWT.log("Invalid element? " + elem + " " + text.length() + " " + elem.startPos);
                    continue;
                }
                String seg = expandTabs(text.substring(offset, elem.startPos));
                // special handling for the first line since we can't rely on it following a
                // newline to tell us that it needs to be trimmed
                if (offset == 0 && prefix > 0) {
                    seg = seg.substring(prefix);
                }
                code.add(Widgets.newInlineLabel(trimPrefix(seg, prefix)));
            }
            if (elem.endPos > text.length()) {
                GWT.log("Invalid element " + elem.startPos + ":" + elem.endPos + " exceeds " +
                        text.length());
            } else {
                code.add(elem.createElement(text.substring(elem.startPos, elem.endPos)));
            }
            offset = elem.endPos;
        }
        if (offset < text.length()) {
            code.add(Widgets.newInlineLabel(
                         trimPrefix(expandTabs(text.substring(offset)), prefix)));
        }

        final Widget scrollTo = _local.get(scrollToDefId);
        if (scrollTo != null) {
            GWT.log("Scrolling to " + scrollToDefId);
            DeferredCommand.addCommand(new Command() {
                public void execute () {
                    WindowFX.scrollTo(scrollTo);
                }
            });
        }

        _contents.setWidget(code);
        _local.addTo(_defmap);
        didInit(code);
    }

    protected void didInit (FlowPanel contents)
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

    protected DefMap _defmap, _local;

    protected @UiField SimplePanel _contents;

    protected interface Binder extends UiBinder<Widget, SourcePanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
