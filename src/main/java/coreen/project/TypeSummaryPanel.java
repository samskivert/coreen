//
// $Id$

package coreen.project;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Bindings;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Value;
import com.threerings.gwt.util.WindowUtil;

import coreen.model.Def;
import coreen.model.DefDetail;
import coreen.model.DefInfo;
import coreen.model.Kind;
import coreen.model.TypeSummary;
import coreen.ui.UIUtil;
import coreen.ui.WindowFX;
import coreen.util.DefMap;
import coreen.util.IdMap;
import coreen.util.PanelCallback;

/**
 * Displays a summary for a type.
 */
public class TypeSummaryPanel extends TypeAndMembersPanel<TypeSummary>
{
    /** Creates a totally standalone panel that fetches all of its own data. */
    public static TypeSummaryPanel create (long defId)
    {
        return create(defId, new DefMap(), UsePopup.TYPE);
    }

    /** Creates a totally standalone panel that fetches all of its own data. */
    public static TypeSummaryPanel create (long defId, DefMap defmap, UsePopup.Linker linker)
    {
        return new TypeSummaryPanel(defId, defmap, linker, IdMap.create(false));
    }

    /** Creates a panel to be used as part of a type hierarchy. */
    public static TypeSummaryPanel create (long defId, DefMap defmap, UsePopup.Linker linker,
                                           IdMap<Boolean> expanded)
    {
        TypeSummaryPanel panel = new TypeSummaryPanel(defId, defmap, linker, expanded);
        panel.addStyleName(panel._styles.topgap());
        return panel;
    }

    /** Creates a panel that acts like a type label and allows deferred expansion of members. */
    public static TypeSummaryPanel create (DefDetail dd, DefMap defmap, UsePopup.Linker linker)
    {
        TypeSummaryPanel panel = new TypeSummaryPanel(dd.id, defmap, linker, IdMap.create(false));
        panel.init(dd, new Def[0], null);
        panel._loaded = true;
        return panel;
    }

    /** Notes that the specified member def should be shown once it is loaded. */
    public void showMember (long memberId)
    {
        _expanded.get(memberId).update(true);
    }

    @Override // from Widget
    public void onLoad ()
    {
        super.onLoad();
        if (isVisible()) {
            ensureLoaded();
        }
    }

    @Override // from Widget
    public void setVisible (boolean visible)
    {
        boolean wasVisible = isVisible();
        if (visible) {
            ensureLoaded();
        }
        super.setVisible(visible);
    }

    protected TypeSummaryPanel (long defId, DefMap defmap, UsePopup.Linker linker,
                                IdMap<Boolean> expanded)
    {
        super(defId, defmap, linker, expanded);
    }

    protected void loadData ()
    {
        _projsvc.getSummary(defId, new PanelCallback<TypeSummary>(_contents) {
            public void onSuccess (TypeSummary sum) {
                _contents.clear();
                init(sum, sum.supers, sum);
                // make sure we fit in the view
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    public void execute () {
                        recenterPanel();
                    }
                });
            }
        });
    }

    protected void initBody (final FlowPanel body, SourcePanel sig, TypeSummary bodyData)
    {
        if (bodyData != null) {
            body.add(sig);
            if (_tlabel != null) {
                addFilterControls();
            }
            initBody(bodyData);

        } else {
            final Value<Boolean> expanded = Value.create(false);
            body.add(TogglePanel.makeFloatingToggle(expanded));
            body.add(sig);
            body.add(UIUtil.newClear());
            expanded.addListener(new Value.Listener<Boolean>() {
                public void valueChanged (Boolean value) {
                    if (!value) {
                        return; // shouldn't happen, but who knows
                    }
                    expanded.removeListener(this); // avoid repeat clicky
                    final Widget loading = Widgets.newLabel("Loading...");
                    body.add(loading);
                    _projsvc.getSummary(defId, new PanelCallback<TypeSummary>(body) {
                        public void onSuccess (TypeSummary sum) {
                            body.remove(loading);
                            configSupers(sum.supers);
                            Bindings.bindVisible(expanded, initBody(sum));
                            if (_tlabel != null) {
                                _tlabel.addSupers(sum.supers, _defmap, _linker);
                                addFilterControls();
                            }
                        }
                    });
                }
            });
        }
    }

    protected void configSupers (Def[] supers)
    {
        for (Def sup : supers) {
            boolean showMembers = !sup.name.equals("Object"); // TODO
            _superViz.put(sup.id, Value.create(showMembers));
            _outerHov.put(sup.id, Value.create(false));
        }
    }

    protected FlowPanel initBody (final TypeSummary sum)
    {
        FlowPanel members = Widgets.newFlowPanel(_styles.summaryMembers());
        int added = addMembers(members, true, sum.members);
        if (added < sum.members.length) {
            NonPublicPanel nonpubs = new NonPublicPanel() {
                protected void populate () {
                    addMembers(this, false, sum.members);
                }
            };
            members.add(nonpubs.makeToggle("Non-public members"));
            members.add(nonpubs);
            for (DefInfo member : sum.members) {
                if (!member.isPublic()) {
                    _expanded.get(member.id).addListenerAndTrigger(nonpubs.syncer);
                }
            }
        }
        _contents.add(members);

        return members;
    }

    protected int addMembers (FlowPanel panel, boolean access, DefInfo[] members)
    {
        int added = 0;
        for (DefInfo member : members) {
            if (member.isPublic() == access) {
                MemberPanel mpanel = createMemberWidget(member);
                _mpanels.add(mpanel);
                panel.add(mpanel);
                Value<Boolean> isViz = _superViz.get(member.outerId);
                if (isViz != null) {
                    Bindings.bindVisible(isViz, mpanel);
                }
                Value<Boolean> isHov = _outerHov.get(member.outerId);
                if (isHov != null) {
                    Bindings.bindStateStyle(isHov, _styles.outerHovered(), null, mpanel);
                }
                added++;
            }
        }
        return added;
    }

    protected MemberPanel createMemberWidget (final DefInfo member)
    {
        MemberPanel panel = new MemberPanel(member);
        if (member.doc != null) {
            panel.add(new DocLabel(member.doc));
        }
        panel.add(new TogglePanel(_expanded.get(member.id)) {
            protected Widget createCollapsed () {
                final SourcePanel sig = new SourcePanel(member, _defmap, _linker);
                sig.addFirstLineIcon(DefUtil.iconForDef(member));
                sig.addStyleName(_styles.sigPanel());
                // if we lack doc label to put a dashed line above our sig, we add one manually
                if (member.doc == null) {
                    // add this to the toggle panel so that it is used by both the siglabel and the
                    // source panel regardless of which is showing
                    addStyleName(_styles.sigPanelBare());
                }
                return sig;
            }
            protected Widget createExpanded () {
                if (member.kind == Kind.TYPE) {
                    return create(member.id, _defmap, _linker, _expanded);
                } else {
                    return new SourcePanel(member.id, _defmap, _linker) {
                        protected void didInit () {
                            addFirstLineIcon(DefUtil.iconForDef(member));
                            GWT.log("Scrolling to expanded member " + member.id);
                            WindowFX.scrollToPos(WindowUtil.getScrollIntoView(this));
                        }
                    };
                }
            }
        });
        return panel;
    }

    protected void addFilterControls ()
    {
        _tlabel.addToHeader(Widgets.newLabel("Filter: ", _styles.filterLabel()));
        final TextBox filter = Widgets.newTextBox("", -1, 10);
        filter.setTitle(_msgs.filterTip());
        filter.addStyleName(_styles.filterBox());
        filter.addKeyUpHandler(new KeyUpHandler() {
            public void onKeyUp (KeyUpEvent event) {
                _ftimer.cancel();
                _ftimer.schedule(250);
            }
            protected Timer _ftimer = new Timer() {
                public void run () {
                    updateFilter(filter.getText().trim().toLowerCase());
                }
            };
        });
        _tlabel.addToHeader(filter);
    }

    protected void updateFilter (String filter)
    {
        for (MemberPanel mpanel : _mpanels) {
            // don't cause already hidden members to show up
            Value<Boolean> viz = _superViz.get(mpanel.member.outerId);
            boolean defaultViz = (viz == null || viz.get());
            mpanel.setVisible(defaultViz && (mpanel.filterText.indexOf(filter) != -1));
        }
    }

    protected static class MemberPanel extends FlowPanel
    {
        public final DefInfo member;
        public final String filterText;

        public MemberPanel (DefInfo member) {
            this.member = member;
            String ftext = "";
            if (member.sig != null) {
                ftext += member.sig.toLowerCase();
                ftext += "\n";
            }
            if (member.doc != null) {
                ftext += member.doc.toLowerCase(); // TODO: strip out HTML tags
            }
            this.filterText = ftext;
        }
    }

    protected List<MemberPanel> _mpanels = new ArrayList<MemberPanel>();
}
