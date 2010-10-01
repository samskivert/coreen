//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.PopupCallback; // TODO: make a custom version that handles errors

import coreen.client.Link;
import coreen.client.Page;
import coreen.model.DefDetail;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;

/**
 * Displays information about a use when the mouse is hovered over it.
 */
public class UsePopup extends PopupPanel
{
    public static class Popper implements MouseOverHandler, MouseOutHandler
    {
        public UsePopup popup; // read and written by UsePopup

        public Popper (SourcePanel.Styles styles, long referentId, Widget target) {
            _styles = styles;
            _referentId = referentId;
            _target = target;

            if (!(target instanceof HasMouseOverHandlers)) {
                GWT.log("Can't listen for mouse over on " + target);
            } else {
                ((HasMouseOverHandlers)target).addMouseOverHandler(this);
                ((HasMouseOutHandlers)target).addMouseOutHandler(this);
            }
        }

        public void onMouseOver (MouseOverEvent event) {
            if (popup == null) {
                _timer.schedule(500);
            }
        }

        public void onMouseOut (MouseOutEvent event) {
            _timer.cancel();
        }

        protected Timer _timer = new Timer() {
            @Override public void run () {
                _projsvc.getDef(_referentId, new PopupCallback<DefDetail>(_target) {
                    public void onSuccess (DefDetail deet) {
                        Popups.showNear(new UsePopup(Popper.this, _styles, deet), _target);
                    }
                });
            }
        };

        protected SourcePanel.Styles _styles;
        protected long _referentId;
        protected Widget _target;
    }

    protected UsePopup (Popper popper, SourcePanel.Styles styles, DefDetail deet)
    {
        super(true);
        setStyleName(styles.usePopup());

        _popper = popper;
        _popper.popup = this;

        if (deet.projectId > 0) {
            add(Link.create(deet.signature, Page.PROJECT, deet.projectId,
                            ProjectPage.Detail.SRC, deet.unitId, deet.defId));
        } else {
            add(Widgets.newLabel(deet.signature));
        }
    }

    @Override // from PopupPanel
    protected void onUnload ()
    {
        super.onUnload();
        _popper.popup = null;
    }

    protected Popper _popper;

    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
}
