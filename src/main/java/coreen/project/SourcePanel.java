//
// $Id$

package coreen.project;

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
import coreen.util.PanelCallback;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;

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
        StringBuilder text = new StringBuilder();
        for (String line : detail.text) {
            text.append(line).append("\n");
        }
        return Widgets.newLabel(text.toString(), _styles.code());
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
