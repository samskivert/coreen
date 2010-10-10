//
// $Id$

package coreen.project;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;

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
            _contents.add(Widgets.newLabel(modMems[0].name, _styles.module()));
            FlowPanel types = new FlowPanel();
            for (int ii = 1; ii < modMems.length; ii++) {
                Def def = modMems[ii];
                Widget label = Widgets.newInlineLabel(def.name, _styles.type());

            // label.addClickHandler(new ClickHandler() {
            //     public void onClick (ClickEvent event) {
            //         if (_types.get(def.id).get()) {
            //             _types.get(def.id).update(false);
            //         } else {
            //             long outerId = def.id, innerId = 0L;
            //             Def d = def;
            //             while (d != null) {
            //                 d = byid.get(d.parentId);
            //                 if (d != null) {
            //                     innerId = outerId;
            //                     outerId = d.id;
            //                 }
            //             }
            //             Link.go(Page.PROJECT, _projectId, ProjectPage.Detail.TPS, outerId, innerId);
            //         }
            //     }
            // });
                new UsePopup.Popper(def.id, label, UsePopup.BY_TYPES, _defmap);
                types.add(label);
            }
            _contents.add(types);
        }

        // FlowPanel types = null, details = null;
        // char c = 0;
        // for (final Def def : defs) {
        //     if (def.name.length() == 0) {
        //         continue; // skip blank types; TODO: better anonymous inner class handling
        //     }
        //     if (def.name.charAt(0) != c) {
        //         types = Widgets.newFlowPanel();
        //         details = Widgets.newFlowPanel();
        //         c = def.name.charAt(0);
        //         table.add().setText(String.valueOf(c), _styles.Letter()).alignTop().
        //             right().setWidget(Widgets.newFlowPanel(types, details));
        //     }
        //     if (types.getWidgetCount() > 0) {
        //         InlineLabel gap = new InlineLabel(" ");
        //         gap.addStyleName(_styles.Gap());
        //         types.add(gap);
        //     }


        //     // create and add the detail panel (hidden) and bind its visibility to a value
        //     TypeDetailPanel deets = new TypeDetailPanel(def.id, _defmap, _members);
        //     Bindings.bindVisible(_types.get(def.id), deets);
        //     details.add(deets);
        // }
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
