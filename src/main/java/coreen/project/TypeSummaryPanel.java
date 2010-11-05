//
// $Id$

package coreen.project;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Bindings;
import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Functions;
import com.threerings.gwt.util.Value;
import com.threerings.gwt.util.WindowUtil;

import coreen.icons.IconResources;
import coreen.model.Def;
import coreen.model.DefContent;
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

    /** Used when we're totally standalone. */
    public TypeSummaryPanel (long defId, boolean headerless)
    {
        this(defId, new DefMap(), IdMap.create(false), UsePopup.TYPE, headerless);
    }

    /** Used when we're part of a type hierarchy. */
    public TypeSummaryPanel (long defId, DefMap defmap, IdMap<Boolean> expanded,
                             UsePopup.Linker linker)
    {
        this(defId, defmap, expanded, linker, false);
        addStyleName(_styles.topgap());
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
    protected TypeSummaryPanel (long defId, DefMap defmap, IdMap<Boolean> expanded,
                                UsePopup.Linker linker, boolean headerless)
    {
        initWidget(_binder.createAndBindUi(this));
        this.defId = defId;
        _defmap = defmap;
        _expanded = expanded;
        _linker = linker;
        _headerless = headerless;
    }

    protected void ensureLoaded ()
    {
        if (!_loaded) {
            _loaded = true;
            _contents.setWidget(Widgets.newLabel("Loading..."));
            _projsvc.getSummary(defId, new PanelCallback<TypeSummary>(_contents) {
                public void onSuccess (TypeSummary sum) {
                    init(sum);
                    // make sure we fit in the view
                    DeferredCommand.addCommand(new Command() {
                        public void execute () {
                            WindowFX.scrollToPos(
                                WindowUtil.getScrollIntoView(TypeSummaryPanel.this));
                        }
                    });
                }
            });
        }
    }

    protected void init (final TypeSummary sum)
    {
        FlowPanel contents = Widgets.newFlowPanel();

        // these will control the visibility of members defined by this supertype
        final Map<Long, Value<Boolean>> superViz = new HashMap<Long, Value<Boolean>>();
        final Map<Long, Value<Boolean>> outerHov = new HashMap<Long, Value<Boolean>>();
        for (Def sup : sum.supers) {
            boolean showMembers = !sup.name.equals("Object"); // TODO
            superViz.put(sup.id, Value.create(showMembers));
            outerHov.put(sup.id, Value.create(false));
        }
        outerHov.put(sum.id, Value.create(false));

        FlowPanel header = Widgets.newFlowPanel(_styles.header());
        if (!_headerless) {
            if (sum.kind == Kind.TYPE) {
                header.add(new TypeLabel(sum, _linker, _defmap) {
                    protected Widget createDefLabel (DefDetail def) {
                        Label label = Widgets.newLabel(def.name, _rsrc.styles().Type());
                        Bindings.bindHovered(outerHov.get(def.id), label);
                        return label;
                    }
                    protected Widget createSuperLabel (Def sup) {
                        Label label = Widgets.newLabel(sup.name);
                        // toggle visibility when this label is clicked
                        Value<Boolean> viz = superViz.get(sup.id);
                        label.addClickHandler(Bindings.makeToggler(viz));
                        label.addStyleName(_rsrc.styles().actionable());
                        Bindings.bindStateStyle(viz, null, _styles.superUp(), label);
                        // also note hoveredness when hovered
                        final Value<Boolean> hov = outerHov.get(sup.id);
                        Bindings.bindHovered(hov, label);
                        return label;
                    }
                });
            } else if (sum.doc != null) {
                header.add(new DocLabel(sum.doc));
            }
        }
        SigLabel sig = new SigLabel(sum, sum.sig, _defmap);
        sig.addStyleName(_styles.sigPanel());
        header.add(sig);
        contents.add(header);

        FlowPanel members = Widgets.newFlowPanel(_styles.members());
        int added = addMembers(members, true, sum.members, superViz, outerHov);
        if (added < sum.members.length) {
            FlowPanel nonpubs = new FlowPanel() {
                public void setVisible (boolean visible) {
                    if (visible && getWidgetCount() == 0) {
                        addMembers(this, false, sum.members, superViz, outerHov);
                    }
                    super.setVisible(visible);
                }
            };
            Bindings.bindVisible(_npshowing, nonpubs);
            members.add(TogglePanel.makeTogglePanel(_styles.nonPublic(), _npshowing,
                                                    Widgets.newLabel("Non-public members")));
            members.add(nonpubs);
        }
        contents.add(members);

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

        _contents.setWidget(contents);
    }

    protected int addMembers (FlowPanel panel, boolean access, DefInfo[] members,
                              Map<Long, Value<Boolean>> outerViz,
                              Map<Long, Value<Boolean>> outerHov)
    {
        int added = 0;
        for (DefInfo member : members) {
            if (member.isPublic() == access) {
                Widget mpanel = createMemberWidget(member);
                panel.add(mpanel);
                Value<Boolean> isViz = outerViz.get(member.outerId);
                if (isViz != null) {
                    Bindings.bindVisible(isViz, mpanel);
                }
                Value<Boolean> isHov = outerHov.get(member.outerId);
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
                final SourcePanel sig = new SourcePanel(
                    member, member.sig, member.sigUses, _defmap, _linker);
                // final SigLabel sig = new SigLabel(member, member.sig, _defmap);
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
                        protected void didInit (FlowPanel contents) {
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
    protected @UiField SimplePanel _contents;

    protected boolean _loaded, _headerless;
    protected DefMap _defmap;
    protected IdMap<Boolean> _expanded;
    protected UsePopup.Linker _linker;
    protected Value<Boolean> _npshowing = Value.create(false);

    protected PopupGroup _popups = new PopupGroup(300);

    protected interface Binder extends UiBinder<Widget, TypeSummaryPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
    protected static final ProjectMessages _msgs = GWT.create(ProjectMessages.class);
}
