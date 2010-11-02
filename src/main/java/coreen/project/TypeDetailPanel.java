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
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Functions;
import com.threerings.gwt.util.Value;
import com.threerings.gwt.util.WindowUtil;

import coreen.icons.IconResources;
import coreen.model.Def;
import coreen.model.DefContent;
import coreen.model.Type;
import coreen.model.TypeDetail;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.ui.WindowFX;
import coreen.util.DefMap;
import coreen.util.IdMap;
import coreen.util.PanelCallback;

/**
 * Displays details for a particular type.
 */
public class TypeDetailPanel extends Composite
{
    public final long defId;

    /** Used when we're totally standalone. */
    public TypeDetailPanel (long defId)
    {
        this(defId, new DefMap(), IdMap.create(false), UsePopup.TYPE);
    }

    /** Used when we're part of a type hierarchy. */
    public TypeDetailPanel (long defId, DefMap defmap, IdMap<Boolean> expanded,
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
            _projsvc.getType(defId, new PanelCallback<TypeDetail>(_contents) {
                public void onSuccess (TypeDetail deets) {
                    init(deets);
                    // make sure we fit in the view
                    DeferredCommand.addCommand(new Command() {
                        public void execute () {
                            recenterPanel();
                        }
                    });
                }
            });
        }
    }

    protected void init (final TypeDetail detail)
    {
        _detail = detail;

        FlowPanel contents = Widgets.newFlowPanel();
        if (detail.type == Type.TYPE) {
            contents.add(new TypeLabel(detail, _linker, _defmap));
        } else if (detail.doc != null) {
            contents.add(Widgets.newHTML(detail.doc));
        }

        // if this is a type, display nested fields, funcs, etc.
        FlowPanel deets = null;
        if (detail.type == Type.TYPE) {
            FlowPanel members = Widgets.newFlowPanel(_styles.members());
            deets = Widgets.newFlowPanel();
            addDefs(members, _msgs.tdpTypes(), detail.members, deets);
            // addDefs(members, _msgs.tdpFuncs(), detail.funcs, deets);
            // addDefs(members, _msgs.tdpTerms(), detail.terms, deets);
            if (members.getWidgetCount() > 0) {
                DefUtil.addClear(members);
                contents.add(members);
            }
        }

        // show source first if we last expanded a source *and* this is not type
        final Value<Boolean> showSource = Value.create(
            _sourceFirst && (detail.type != Type.TYPE));
        ToggleButton toggle = new ToggleButton(new Image(_icons.codeClosed()),
                                               new Image(_icons.codeOpen()), new ClickHandler() {
            public void onClick (ClickEvent event) {
                showSource.update(!showSource.get());
                _sourceFirst = showSource.get();
            }
        });
        toggle.setDown(showSource.get());
        toggle.addStyleName(_styles.toggle());
        contents.add(toggle);

        Label sig = new Label(detail.sig) {
            /* ctor */ {
                addStyleName(_rsrc.styles().code());
            }

            @Override public void setVisible (boolean visible) {
                super.setVisible(visible);
                if (visible) {
                    _defmap.map(detail.id, this);
                } else {
                    _defmap.unmap(detail.id, this);
                }
            }
        };
        Bindings.bindVisible(showSource.map(Functions.NOT), sig);
        contents.add(sig);

        SourcePanel source = new SourcePanel(_defmap) {
            @Override public void setVisible (boolean visible) {
                super.setVisible(visible);
                if (visible && !_loaded) {
                    _loaded = true;
                    loadDef(detail.id, _linker, false);
                }
            }
            @Override protected void didInit (FlowPanel contents) {
                recenterPanel();
            }
            protected boolean _loaded;
        };
        Bindings.bindVisible(showSource, source);
        contents.add(source);

        if (deets != null) {
            contents.add(deets);
        }

        _contents.setWidget(contents);
    }

    protected void addDefs (FlowPanel panel, String kind, Def[] defs, FlowPanel members)
    {
        for (final Def def : defs) {
            // create a type detail panel for this def, which will most likely be hidden
            final TypeDetailPanel deets = new TypeDetailPanel(def.id, _defmap, _expanded, _linker);
            deets.addStyleName(_rsrc.styles().indent());
            Bindings.bindVisible(_expanded.get(def.id), deets);
            members.add(deets);

            // add the def label and its various hangers-on
            DefUtil.addDef(panel, def, _linker, _defmap).addClickHandler(new ClickHandler() {
                public void onClick (ClickEvent event) {
                    if (!deets.isVisible()) {
                        deets.setVisible(true);
                    } else {
                        deets.recenterPanel();
                    }
                }
            });
        }
    }

    protected void recenterPanel ()
    {
        WindowFX.scrollToPos(WindowUtil.getScrollIntoView(this));
    }

    protected interface Styles extends CssResource
    {
        String members ();
        String toggle ();
    }
    protected @UiField Styles _styles;
    protected @UiField SimplePanel _contents;

    protected boolean _loaded;
    protected TypeDetail _detail;
    protected DefMap _defmap;
    protected IdMap<Boolean> _expanded;
    protected UsePopup.Linker _linker;

    /** We keep a global toggle to track whether to open defs with source or summary first. When
     * you expand a def into source, you switch to source first mode, when you contract, you return
     * to summary first. */
    protected static boolean _sourceFirst = false;

    protected interface Binder extends UiBinder<Widget, TypeDetailPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
    protected static final ProjectMessages _msgs = GWT.create(ProjectMessages.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
    protected static final IconResources _icons = GWT.create(IconResources.class);
}
