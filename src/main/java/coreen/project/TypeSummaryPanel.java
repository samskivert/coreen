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
    public TypeSummaryPanel (long defId)
    {
        this(defId, new DefMap(), IdMap.create(false), UsePopup.TYPE);
    }

    /** Used when we're part of a type hierarchy. */
    public TypeSummaryPanel (long defId, DefMap defmap, IdMap<Boolean> expanded,
                             UsePopup.Linker linker)
    {
        initWidget(_binder.createAndBindUi(this));
        this.defId = defId;
        _defmap = defmap;
        _expanded = expanded;
        _linker = linker;
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
        if (sum.type == Type.TYPE) {
            contents.add(new TypeLabel(sum.path, sum, _linker, _defmap, sum.doc));
        } else if (sum.doc != null) {
            contents.add(new DocLabel(sum.doc));
        }
        contents.add(new SigLabel(sum, sum.sig, _defmap));

        for (DefInfo member : sum.types) {
            addMember(contents, member);
        }
        for (DefInfo member : sum.funcs) {
            addMember(contents, member);
        }
        for (DefInfo member : sum.terms) {
            addMember(contents, member);
        }

        _contents.setWidget(contents);
    }

    protected void addMember (FlowPanel panel, final DefInfo member)
    {
        ToggleButton toggle = new ToggleButton(new Image(_icons.codeClosed()),
                                               new Image(_icons.codeOpen()), new ClickHandler() {
            public void onClick (ClickEvent event) {
                Value<Boolean> expanded = _expanded.get(member.id);
                expanded.update(!expanded.get());
            }
        });
        toggle.setDown(_expanded.get(member.id).get());
        toggle.addStyleName(_styles.toggle());
        panel.add(toggle);

        FlowPanel bits = new FlowPanel();
        panel.add(new FluentTable(0, 0).add().setWidget(toggle).alignTop().
                  right().setWidget(bits).table());

        SigLabel sig = new SigLabel(member, member.sig, _defmap);
        sig.addStyleName("inline");
        new UsePopup.Popper(member.id, sig, _linker, _defmap, false);
        Widget asig = Widgets.newFlowPanel(TypeLabel.iconForDef(member.type), sig);
        Bindings.bindVisible(_expanded.get(member.id).map(Functions.NOT), asig);
        bits.add(asig);

        if (member.doc != null) {
            Widget doc = new DocLabel(member.doc);
            Bindings.bindVisible(_expanded.get(member.id), doc);
            bits.add(doc);
        }

        SourcePanel source = new SourcePanel(_defmap) {
            public void setVisible (boolean visible) {
                super.setVisible(visible);
                if (visible && !_loaded) {
                    _loaded = true;
                    _projsvc.getContent(member.id, new PanelCallback<DefContent>(_contents) {
                        public void onSuccess (DefContent content) {
                            init(content.text, content.defs, content.uses, 0L, _linker);
                        }
                    });
                }
            }
            protected void didInit () {
                WindowFX.scrollToPos(WindowUtil.getScrollIntoView(this));
            }
            protected boolean _loaded;
        };
        Bindings.bindVisible(_expanded.get(member.id), source);
        bits.add(source);
    }

    protected interface Styles extends CssResource
    {
        String toggle ();
    }
    protected @UiField Styles _styles;
    protected @UiField SimplePanel _contents;

    protected boolean _loaded;
    protected DefMap _defmap;
    protected IdMap<Boolean> _expanded;
    protected UsePopup.Linker _linker;

    protected interface Binder extends UiBinder<Widget, TypeSummaryPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
    protected static final ProjectMessages _msgs = GWT.create(ProjectMessages.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
    protected static final IconResources _icons = GWT.create(IconResources.class);
}
