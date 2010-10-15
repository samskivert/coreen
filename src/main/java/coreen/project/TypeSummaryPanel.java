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
        if (!_headerless) {
            if (sum.type == Type.TYPE) {
                contents.add(new TypeLabel(sum.path, sum, _linker, _defmap, sum.doc));
            } else if (sum.doc != null) {
                contents.add(new DocLabel(sum.doc));
            }
        }
        contents.add(new SigLabel(sum, sum.sig, _defmap));

        int added = addMembers(contents, true, sum.types, sum.funcs, sum.terms);
        if (added < sum.types.length + sum.funcs.length + sum.terms.length) {
            FlowPanel nonpubs = new FlowPanel() {
                public void setVisible (boolean visible) {
                    if (visible && getWidgetCount() == 0) {
                        addMembers(this, false, sum.types, sum.funcs, sum.terms);
                    }
                    super.setVisible(visible);
                }
            };
            Value<Boolean> model = Value.create(false);
            Bindings.bindVisible(model, nonpubs);
            contents.add(new FluentTable(0, 0, _styles.nonPublic()).
                         add().setWidget(TogglePanel.makeToggleButton(model)).
                         right().setText("Non-public members").table());
            contents.add(nonpubs);
        }

        _contents.setWidget(contents);
    }

    protected int addMembers (FlowPanel panel, boolean access, DefInfo[]... lists)
    {
        int added = 0;
        for (DefInfo[] list : lists) {
            for (DefInfo member : list) {
                if (member.isPublic() == access) {
                    addMember(panel, member);
                    added++;
                }
            }
        }
        return added;
    }

    protected void addMember (FlowPanel panel, final DefInfo member)
    {
        panel.add(new TogglePanel(_expanded.get(member.id)) {
            protected Widget createCollapsed () {
                SigLabel sig = new SigLabel(member, member.sig, _defmap);
                sig.addStyleName("inline");
                new UsePopup.Popper(member.id, sig, _linker, _defmap, false);
                return Widgets.newFlowPanel(DefUtil.iconForDef(member), sig);
            }
            protected Widget createExpanded () {
                if (member.type == Type.TYPE) {
                    return new TypeSummaryPanel(member.id, _defmap, _expanded, _linker);
                } else if (member.doc == null) {
                    return createSourceView(member);
                } else {
                    return Widgets.newFlowPanel(new DocLabel(member.doc), createSourceView(member));
                }
            }
        });
    }

    protected Widget createSourceView (final DefInfo member)
    {
        return new SourcePanel(_defmap) {
            /* ctor */ {
                _projsvc.getContent(member.id, new PanelCallback<DefContent>(_contents) {
                    public void onSuccess (DefContent content) {
                        _deficon = DefUtil.iconForDef(content);
                        init(content.text, content.defs, content.uses, 0L, _linker);
                    }
                });
            }
            @Override protected void didInit (FlowPanel contents) {
                contents.insert(_deficon, 0);
                WindowFX.scrollToPos(WindowUtil.getScrollIntoView(this));
            }
            protected Widget _deficon;
        };
    }

    protected interface Styles extends CssResource
    {
        String topgap ();
        String nonPublic ();
    }
    protected @UiField Styles _styles;
    protected @UiField SimplePanel _contents;

    protected boolean _loaded, _headerless;
    protected DefMap _defmap;
    protected IdMap<Boolean> _expanded;
    protected UsePopup.Linker _linker;

    protected interface Binder extends UiBinder<Widget, TypeSummaryPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
    protected static final ProjectMessages _msgs = GWT.create(ProjectMessages.class);
}
