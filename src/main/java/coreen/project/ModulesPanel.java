//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Bindings;
import com.threerings.gwt.ui.Widgets;

import coreen.client.Link;
import coreen.client.Page;
import coreen.model.Def;
import coreen.util.PanelCallback;

/**
 * Displays all modules for a project, and their direct type members.
 */
public class ModulesPanel extends SummaryPanel
{
    public ModulesPanel ()
    {
        initWidget(_binder.createAndBindUi(this));
    }

    @Override // from AbstractProjectPanel
    public ProjectPage.Detail getId ()
    {
        return ProjectPage.Detail.MDS;
    }

    @Override // from SummaryPanel
    protected void updateContents (long projectId)
    {
        _projsvc.getModsAndMembers(projectId, new PanelCallback<Def[][]>(_contents) {
            public void onSuccess (Def[][] modsMems) {
                initContents(modsMems);
            }
        });
    }

    protected void initContents (Def[][] modsMems)
    {
        _contents.clear();

        for (Def[] modMems : modsMems) {
            final Def mod = modMems[0];
            _contents.add(Widgets.newLabel(mod.name, _styles.module()));
            for (int ii = 1; ii < modMems.length; ii++) {
                final Def def = modMems[ii];
                Label label = DefUtil.addDef(_contents, def, UsePopup.BY_MODS, _defmap);
                label.addClickHandler(new ClickHandler() {
                    public void onClick (ClickEvent event) {
                        if (_types.get(def.id).get()) {
                            _types.get(def.id).update(false);
                        } else {
                            Link.go(Page.PROJECT, _projectId, ProjectPage.Detail.MDS, def.id);
                        }
                    }
                });
            }
            DefUtil.addClear(_contents);

            for (int ii = 1; ii < modMems.length; ii++) {
                final Def def = modMems[ii];
                // create and add the detail panel (hidden) and bind its visibility to a value
                TypeDetailPanel deets = new TypeDetailPanel(
                    def.id, _defmap, _members, UsePopup.BY_MODS);
                Bindings.bindVisible(_types.get(def.id), deets);
                _contents.add(deets);
            }
        }
    }

    protected interface Styles extends CssResource
    {
        String type ();
        String module ();
    }
    protected @UiField Styles _styles;
    protected @UiField FlowPanel _contents;

    protected interface Binder extends UiBinder<Widget, ModulesPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
}
