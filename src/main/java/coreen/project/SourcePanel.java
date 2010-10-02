//
// $Id$

package coreen.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.WindowUtil;

import coreen.model.CompUnitDetail;
import coreen.model.Def;
import coreen.model.Use;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.util.Edit;
import coreen.util.PanelCallback;

/**
 * Displays a single compilation unit.
 */
public class SourcePanel extends Composite
{
    public interface Styles extends CssResource
    {
        String code ();
        String def ();
        String use ();

        String usePopup ();
    }

    public SourcePanel (long unitId, final long scrollToDefId)
    {
        initWidget(_binder.createAndBindUi(this));

        _projsvc.getCompUnit(unitId, new PanelCallback<CompUnitDetail>(_contents) {
            public void onSuccess (CompUnitDetail detail) {
                _contents.setWidget(createContents(detail, scrollToDefId));
            }
        });
    }

    protected Widget createContents (CompUnitDetail detail, long scrollToDefId)
    {
        List<Elementer> elems = new ArrayList<Elementer>();
        Elementer scrollTo = null;
        for (Def def : detail.defs) {
            elems.add(new Elementer(def.loc.start, def.loc.start+def.loc.length) {
                public Widget createElement (String text) {
                    return Widgets.newInlineLabel(text, _styles.def());
                }
            });
            if (scrollToDefId == def.id) {
                scrollTo = elems.get(elems.size()-1);
            }
        }
        for (final Use use : detail.uses) {
            elems.add(new Elementer(use.loc.start, use.loc.start+use.loc.length) {
                public Widget createElement (String text) {
                    Widget span = Widgets.newInlineLabel(text, _styles.use());
                    new UsePopup.Popper(_styles, use.referentId, span);
                    return span;
                }
            });
        }
        Collections.sort(elems);

        int offset = 0;
        FlowPanel code = Widgets.newFlowPanel(_styles.code());
        for (Elementer elem : elems) {
            if (elem.startPos < 0) continue; // filter undisplayable elems
            if (elem.startPos > offset) {
                code.add(Widgets.newInlineLabel(detail.text.substring(offset, elem.startPos)));
            }
            final Widget span = elem.createElement(
                detail.text.substring(elem.startPos, elem.endPos));
            if (elem == scrollTo) {
                DeferredCommand.addCommand(new Command() {
                    public void execute () {
                        WindowUtil.scrollTo(span);
                    }
                });
            }
            code.add(span);
            offset = elem.endPos;
        }
        if (offset < detail.text.length()) {
            code.add(Widgets.newInlineLabel(detail.text.substring(offset), _styles.code()));
        }
        return code;
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

    protected @UiField SimplePanel _contents;
    protected @UiField Styles _styles;

    protected interface Binder extends UiBinder<Widget, SourcePanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
}
