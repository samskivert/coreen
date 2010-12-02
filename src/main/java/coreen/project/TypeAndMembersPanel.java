//
// $Id$

package coreen.project;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Bindings;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Value;
import com.threerings.gwt.util.WindowUtil;

import coreen.model.Def;
import coreen.model.DefDetail;
import coreen.model.Kind;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.ui.UIUtil;
import coreen.ui.WindowFX;
import coreen.util.DefMap;
import coreen.util.IdMap;

/**
 * An abstract base class for {@link TypeSummaryPanel} and {@link TypeDetailPanel}.
 */
public abstract class TypeAndMembersPanel<C extends DefDetail> extends Composite
{
    /** Contains the id of the configured def. */
    public final long defId;

    /** Contains the currently displayed def detail. */
    public final Value<DefDetail> detail = Value.<DefDetail>create(null);

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

    /** Used when we're part of a type hierarchy. */
    protected TypeAndMembersPanel (long defId, DefMap defmap, UsePopup.Linker linker,
                                   IdMap<Boolean> expanded)
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
            loadData();
        }
    }

    protected abstract void loadData ();

    protected void init (final DefDetail deets, Def[] supers, C bodyData)
    {
        detail.update(deets);
        configSupers(supers);

        FlowPanel header = Widgets.newFlowPanel(_styles.header());
        if (deets.kind == Kind.TYPE) {
            _outerHov.put(deets.id, Value.create(false));
            header.add(_tlabel = new TypeLabel(deets, supers, _defmap, _linker) {
                protected Widget createDefLabel (DefDetail def) {
                    Widget label = super.createDefLabel(def);
                    FocusPanel focus = new FocusPanel(label);
                    Bindings.bindHovered(_outerHov.get(def.id), focus);
                    return focus;
                }
                protected SpanWidget createSuperLabel (Def sup) {
                    SpanWidget label = super.createSuperLabel(sup);
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
            if (deets.doc != null) {
                header.add(new DocLabel(deets.doc));
            }
        }

        FlowPanel body = Widgets.newFlowPanel(_rsrc.styles().belowTypeLabel());
        SourcePanel sig = new SourcePanel(deets, _defmap, _linker);
        sig.addStyleName(_styles.sigPanel());
        _contents.add(header);
        _contents.add(body);
        initBody(body, sig, bodyData);
    }

    protected void configSupers (Def[] supers)
    {
        for (Def sup : supers) {
            boolean showMembers = !sup.name.equals("Object"); // TODO
            _superViz.put(sup.id, Value.create(showMembers));
            _outerHov.put(sup.id, Value.create(false));
        }
    }

    protected void recenterPanel ()
    {
        WindowFX.scrollToPos(WindowUtil.getScrollIntoView(this));
    }

    protected abstract void initBody (FlowPanel body, SourcePanel sig, C bodyData);

    protected interface Styles extends CssResource
    {
        String topgap ();
        String header ();
        String summaryMembers ();
        String detailMembers ();
        String nonPublic ();
        String sigPanel ();
        String sigPanelBare ();
        String superUp ();
        String outerHovered ();
        String filterLabel ();
        String filterBox ();
    }
    protected @UiField Styles _styles;
    protected @UiField FlowPanel _contents;

    protected boolean _loaded;
    protected TypeLabel _tlabel;
    protected DefMap _defmap;
    protected UsePopup.Linker _linker;
    protected IdMap<Boolean> _expanded;

    // these control the visibility of members defined by this supertype
    protected Map<Long, Value<Boolean>> _superViz = new HashMap<Long, Value<Boolean>>();
    protected Map<Long, Value<Boolean>> _outerHov = new HashMap<Long, Value<Boolean>>();

    protected interface Binder extends UiBinder<Widget, TypeAndMembersPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
    protected static final ProjectMessages _msgs = GWT.create(ProjectMessages.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
