//
// $Id$

package coreen.project;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
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
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.ui.PopupGroup;
import coreen.ui.UIUtil;
import coreen.ui.WindowFX;
import coreen.util.DefMap;
import coreen.util.IdMap;
import coreen.util.PanelCallback;

/**
 * Displays a summary for a type.
 */
public class TypeSummaryPanel extends Composite
{
    public final long defId;

    /** Creates a totally standalone panel that fetches all of its own data. */
    public static TypeSummaryPanel create (long defId)
    {
        return new TypeSummaryPanel(defId, new DefMap(), IdMap.create(false), UsePopup.TYPE);
    }

    /** Creates a panel to be used as part of a type hierarchy. */
    public static TypeSummaryPanel create (long defId, DefMap defmap, IdMap<Boolean> expanded,
                                           UsePopup.Linker linker)
    {
        TypeSummaryPanel panel = new TypeSummaryPanel(defId, defmap, expanded, linker);
        panel.addStyleName(panel._styles.topgap());
        return panel;
    }

    /** Creates a panel that acts like a type label and allows deferred expansion of members. */
    public static TypeSummaryPanel create (DefDetail deets, DefMap defmap, UsePopup.Linker linker)
    {
        TypeSummaryPanel panel = new TypeSummaryPanel(
            deets.id, defmap, IdMap.create(false), linker);
        panel.initHeader(deets, new Def[0]);
        panel._loaded = true;
        return panel;
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

    public void showMember (long memberId)
    {
        _expanded.get(memberId).update(true);
    }

    /** Used when we're part of a type hierarchy. */
    protected TypeSummaryPanel (
        long defId, DefMap defmap, IdMap<Boolean> expanded, UsePopup.Linker linker)
    {
        initWidget(_binder.createAndBindUi(this));
        this.defId = defId;
        _defmap = defmap;
        _expanded = expanded;
        _linker = linker;
    }

    protected void ensureLoaded ()
    {
        if (!_loaded) {
            _loaded = true;
            _contents.add(Widgets.newLabel("Loading..."));
            _projsvc.getSummary(defId, new PanelCallback<TypeSummary>(_contents) {
                public void onSuccess (TypeSummary sum) {
                    _contents.clear();
                    initHeader(sum, sum.supers);
                    initBody(sum);
                    // make sure we fit in the view
                    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                        public void execute () {
                            WindowFX.scrollToPos(
                                WindowUtil.getScrollIntoView(TypeSummaryPanel.this));
                        }
                    });
                }
            });
        }
    }

    protected void initHeader (final DefDetail deets, Def[] supers)
    {
        configSupers(supers);

        FlowPanel header = Widgets.newFlowPanel(_styles.header());
        final TypeLabel tlabel;
        if (deets.kind == Kind.TYPE) {
            _outerHov.put(deets.id, Value.create(false));
            header.add(tlabel = new TypeLabel(deets, supers, _defmap, _linker) {
                protected Widget createDefLabel (DefDetail def) {
                    Widget label = super.createDefLabel(def);
                    FocusPanel focus = new FocusPanel(label);
                    Bindings.bindHovered(_outerHov.get(def.id), focus);
                    return focus;
                }
                protected Widget createSuperLabel (Def sup) {
                    Label label = Widgets.newLabel(sup.name);
                    // toggle visibility when this label is clicked
                    Value<Boolean> viz = _superViz.get(sup.id);
                    UIUtil.makeActionable(label, Bindings.makeToggler(viz));
                    Bindings.bindStateStyle(viz, null, _styles.superUp(), label);
                    // also note hoveredness when hovered
                    final Value<Boolean> hov = _outerHov.get(sup.id);
                    Bindings.bindHovered(hov, label);
                    return label;
                }
            });
        } else {
            tlabel = null;
            if (deets.doc != null) {
                header.add(new DocLabel(deets.doc));
            }
        }

        final FlowPanel body = Widgets.newFlowPanel(_rsrc.styles().belowTypeLabel());
        SourcePanel sig = new SourcePanel(deets, _defmap, _linker);
        sig.addStyleName(_styles.sigPanel());

        if (deets instanceof TypeSummary) {
            body.add(sig);

        } else {
            final Value<Boolean> expanded = Value.create(false);
            body.add(TogglePanel.makeToggleButton(expanded));
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
                            Bindings.bindVisible(expanded, initBody(sum));
                            configSupers(sum.supers);
                            if (tlabel != null) {
                                tlabel.addSupers(sum.supers, _defmap, _linker);
                            }
                        }
                    });
                }
                });
        }

        _contents.add(header);
        _contents.add(body);
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
        FlowPanel members = Widgets.newFlowPanel(_styles.members());
        int added = addMembers(members, true, sum.members);
        if (added < sum.members.length) {
            FlowPanel nonpubs = new FlowPanel() {
                public void setVisible (boolean visible) {
                    if (visible && getWidgetCount() == 0) {
                        addMembers(this, false, sum.members);
                    }
                    super.setVisible(visible);
                }
            };
            Bindings.bindVisible(_npshowing, nonpubs);
            members.add(TogglePanel.makeTogglePanel(_styles.nonPublic(), _npshowing,
                                                    Widgets.newLabel("Non-public members")));
            members.add(nonpubs);
        }
        _contents.add(members);

        // add a listener to all non-public members that shows the non-public members section
        // whenever any of them are marked as showing
        Value.Listener<Boolean> syncer = new Value.Listener<Boolean>() {
            public void valueChanged (Boolean value) {
                if (value) {
                    _npshowing.update(true);
                }
            }
        };
        for (DefInfo member : sum.members) {
            if (!member.isPublic()) {
                _expanded.get(member.id).addListenerAndTrigger(syncer);
            }
        }

        return members;
    }

    protected int addMembers (FlowPanel panel, boolean access, DefInfo[] members)
    {
        int added = 0;
        for (DefInfo member : members) {
            if (member.isPublic() == access) {
                Widget mpanel = createMemberWidget(member);
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

    protected Widget createMemberWidget (final DefInfo member)
    {
        FlowPanel panel = new FlowPanel();
        if (member.doc != null) {
            panel.add(new DocLabel(member.doc));
        }
        panel.add(new TogglePanel(_expanded.get(member.id)) {
            protected Widget createCollapsed () {
                final SourcePanel sig = new SourcePanel(member, _defmap, _linker);
                Widget panel = Widgets.newFlowPanel(
                    _styles.sigPanel(), DefUtil.iconForDef(member), sig);
                // if we lack doc label to put a dashed line above our sig, we add one manually
                if (member.doc == null) {
                    // add this to the toggle panel so that it is used by both the siglabel and the
                    // source panel regardless of which is showing
                    addStyleName(_styles.sigPanelBare());
                }
                return panel;
            }
            protected Widget createExpanded () {
                if (member.kind == Kind.TYPE) {
                    return new TypeSummaryPanel(member.id, _defmap, _expanded, _linker);
                } else {
                    return new SourcePanel(member.id, _defmap, _linker, true) {
                        protected void didInit () {
                            GWT.log("Scrolling to expanded member " + member.id);
                            WindowFX.scrollToPos(WindowUtil.getScrollIntoView(this));
                        }
                    };
                }
            }
        });
        return panel;
    }

    protected interface Styles extends CssResource
    {
        String topgap ();
        String header ();
        String members ();
        String nonPublic ();
        String sigPanel ();
        String sigPanelBare ();
        String superUp ();
        String outerHovered ();
    }
    protected @UiField Styles _styles;
    protected @UiField FlowPanel _contents;

    protected boolean _loaded, _headerless;
    protected DefMap _defmap;
    protected IdMap<Boolean> _expanded;
    protected UsePopup.Linker _linker;
    protected Value<Boolean> _npshowing = Value.create(false);

    // these control the visibility of members defined by this supertype
    protected Map<Long, Value<Boolean>> _superViz = new HashMap<Long, Value<Boolean>>();
    protected Map<Long, Value<Boolean>> _outerHov = new HashMap<Long, Value<Boolean>>();

    protected interface Binder extends UiBinder<Widget, TypeSummaryPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
    protected static final ProjectMessages _msgs = GWT.create(ProjectMessages.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
