//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.Widgets;

import coreen.client.ClientMessages;
import coreen.model.Def;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.ui.UIResources;
import coreen.ui.UIUtil;
import coreen.util.DefMap;
import coreen.util.PanelCallback;

/**
 * Displays the supertypes of a def.
 */
public class SuperTypesPopup extends PopupPanel
{
    /**
     * Adds a click handler to the supplied target which pops up a super types panel for the
     * specified def.
     */
    public static void bind (final HasClickHandlers target, final Def def, final DefMap defmap)
    {
        target.addClickHandler(new ClickHandler() {
            public void onClick (ClickEvent event) {
                if (_panel == null) {
                    _panel = new SuperTypesPopup(def, defmap) {
                        protected void recenter () {
                            UIUtil.showAbove(this, (Widget)target);
                        }
                    };
                }
                _panel.recenter();
            }
            protected SuperTypesPopup _panel;
        });
    }

    public SuperTypesPopup (Def def, DefMap defmap)
    {
        super(true);
        addStyleName(_ursrc.styles().popup());
        setWidget(Widgets.newLabel(_cmsgs.loading()));
        _def = def;
        _defmap = defmap;

        _projsvc.getSuperTypes(_def.id, new PanelCallback<Def[][]>(this) {
            public void onSuccess (Def[][] defs) {
                init(defs);
            }
        });
    }

    protected void init (Def[][] defs)
    {
        if (defs.length == 0) {
            setWidget(Widgets.newLabel("No supertypes..."));
            return;
        }

        FluentTable table = new FluentTable();
        for (Def[] row : defs) {
            FluentTable.Cell cell = table.add().setWidget(makeLabel(row[0], true));
            if (row.length > 1) {
                cell = cell.right().setText(" ⇙ ");
            }
            for (int ii = 1; ii < row.length; ii++) {
                cell = cell.right().setWidget(makeLabel(row[ii], true));
            }
        }
        table.add().setWidget(makeLabel(_def, false));
        setWidget(table);
        recenter();
    }

    protected Widget makeLabel (Def def, boolean popper)
    {
        if (def == null) {
            return Widgets.newInlineLabel("");
        }
        Widget label = Widgets.newInlineLabel(def.name);
        if (popper) {
            new UsePopup.Popper(def.id, label, UsePopup.TYPE, _defmap, true);
        }
        return Widgets.newFlowPanel(DefUtil.iconForDef(def), label);
    }

    protected void recenter ()
    {
        // overridden in bind()
    }

    protected Def _def;
    protected DefMap _defmap;

    protected static final ClientMessages _cmsgs = GWT.create(ClientMessages.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
    protected static final UIResources _ursrc = GWT.create(UIResources.class);
}
