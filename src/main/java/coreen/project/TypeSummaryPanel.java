//
// $Id$

package coreen.project;

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
import coreen.model.DefInfo;
import coreen.model.Type;
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

        FlowPanel header = Widgets.newFlowPanel(_styles.header());
        if (!_headerless) {
            if (sum.type == Type.TYPE) {
                header.add(new TypeLabel(sum.path, sum, _linker, _defmap, sum.doc));
            } else if (sum.doc != null) {
                header.add(new DocLabel(sum.doc));
            }
        }
        SigLabel sig = new SigLabel(sum, sum.sig, _defmap);
        sig.addStyleName(_styles.sigPanel());
        header.add(sig);
        contents.add(header);

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

    protected int addMembers (FlowPanel panel, boolean access, DefInfo[] members)
    {
        int added = 0;
        for (DefInfo member : members) {
            if (member.isPublic() == access) {
                addMember(panel, member);
                added++;
            }
        }
        return added;
    }

    protected void addMember (FlowPanel panel, final DefInfo member)
    {
        if (member.doc != null) {
            panel.add(new DocLabel(member.doc));
        }
        panel.add(new TogglePanel(_expanded.get(member.id)) {
            protected Widget createCollapsed () {
                final SigLabel sig = new SigLabel(member, member.sig, _defmap);
                // sig.addStyleName(_rsrc.styles().actionable());
                // _popups.bindPopup(sig, new PopupGroup.Thunk() {
                //     public Widget create () {
                //         return new DocLabel(member.doc, true);
                //     }
                // });
                // new UsePopup.Popper(member.id, sig, _linker, _defmap, false).setHighlight(false);
                Widget panel = Widgets.newFlowPanel(
                    _styles.sigPanel(), DefUtil.iconForDef(member), sig);
                if (member.doc == null) {
                    // since we have no doc label to put a dashed line above our signature, we need
                    // to add one manually
                    panel.addStyleName(_styles.sigPanelBare());
                }
                return panel;
            }
            protected Widget createExpanded () {
                if (member.type == Type.TYPE) {
                    return new TypeSummaryPanel(member.id, _defmap, _expanded, _linker);
                } else {
                    return createSourceView(member);
                }
            }
        });
    }

    protected Widget createSourceView (final DefInfo member)
    {
        // return Widgets.newFlowPanel(
        //     new DocLabel(member.doc), );
        return new SourcePanel(member.id, _defmap, _linker, true) {
            protected void didInit (FlowPanel contents) {
                WindowFX.scrollToPos(WindowUtil.getScrollIntoView(this));
            }
        };
    }

    protected interface Styles extends CssResource
    {
        String topgap ();
        String header ();
        String members ();
        String nonPublic ();
        String sigPanel ();
        String sigPanelBare ();
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
