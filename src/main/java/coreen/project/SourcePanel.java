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

import coreen.model.CompUnitDetail;
import coreen.model.Def;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.util.Edit;
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

    protected Widget createContents (CompUnitDetail detail)
    {
        // turn the defs into edits and sort the edits (TODO: clean up this hackitude)
        List<Edit> edits = new ArrayList<Edit>();
        for (Def def : detail.defs) {
            edits.add(new Edit(def.loc.start, "<b>"));
            edits.add(new Edit(def.loc.start + def.loc.length, "</b>"));
        }
        Collections.sort(edits);

        return Widgets.newHTML(Edit.applyEdits(edits, detail.text), _styles.code());
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
