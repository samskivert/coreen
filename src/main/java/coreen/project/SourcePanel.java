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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.StringUtil;

import coreen.model.CompUnitDetail;
import coreen.model.Def;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.util.PanelCallback;

/**
 * Displays a single compilation unit.
 */
public class SourcePanel extends Composite
{
    public SourcePanel (long unitId)
    {
        initWidget(_binder.createAndBindUi(this));

        _projsvc.getCompUnit(unitId, new PanelCallback<CompUnitDetail>(_contents) {
            public void onSuccess (CompUnitDetail detail) {
                _contents.setWidget(createContents(detail));
            }
        });
    }

    protected static class Edit implements Comparable<Edit> {
        public final int offset;
        public final String text;
        public Edit (int offset, String text) {
            this.offset = offset;
            this.text = text;
        }
        public int compareTo (Edit other) {
            return offset - other.offset;
        }
        public String toString () {
            return text + ":" + offset;
        }
    }

    protected Widget createContents (CompUnitDetail detail)
    {
        // turn the defs into edits and sort the edits (TODO: clean up this hackitude)
        List<Edit> edits = new ArrayList<Edit>();
        for (Def def : detail.defs) {
            edits.add(new Edit(def.loc.start, "<b>"));
            edits.add(new Edit(def.loc.start + def.loc.length, "</b>"));
        }
        Collections.sort(edits);

        StringBuilder text = new StringBuilder();
        int pos = 0;
        for (String line : detail.text) {
            int npos = pos + line.length() + 1; // TODO: crlf will probably screw us here
            int added = 0;
            while (edits.size() > 0) {
                Edit e = edits.get(0);
                if (e.offset < npos) {
                    line = line.substring(0, e.offset-pos+added) + e.text +
                        line.substring(e.offset-pos+added);
                    added += e.text.length();
                    edits.remove(0);
                    GWT.log("Added edit at " + e.offset);
                } else {
                    break;
                }
            }
            text.append(line).append("\n");
            pos = npos;
        }
        return Widgets.newHTML(text.toString(), _styles.code());
    }

    protected interface Styles extends CssResource
    {
        String code ();
    }

    protected @UiField SimplePanel _contents;
    protected @UiField Styles _styles;

    protected interface Binder extends UiBinder<Widget, SourcePanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
}
