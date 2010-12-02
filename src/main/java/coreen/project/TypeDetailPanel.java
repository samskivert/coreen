//
// $Id$

package coreen.project;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Bindings;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Value;

import coreen.model.Def;
import coreen.model.Kind;
import coreen.model.TypeDetail;
import coreen.ui.UIUtil;
import coreen.util.DefMap;
import coreen.util.IdMap;
import coreen.util.PanelCallback;
import coreen.util.Shower;

/**
 * Displays details for a particular type.
 */
public class TypeDetailPanel extends TypeAndMembersPanel<TypeDetail>
{
    /** Used when we're totally standalone. */
    public TypeDetailPanel (long defId)
    {
        this(defId, new DefMap(), UsePopup.TYPE);
    }

    /** Used when we're mostly standalone. */
    public TypeDetailPanel (long defId, DefMap defmap, UsePopup.Linker linker)
    {
        this(defId, defmap, linker, IdMap.create(false));
    }

    /** Used when we're part of a type hierarchy. */
    public TypeDetailPanel (long defId, DefMap defmap, UsePopup.Linker linker,
                            IdMap<Boolean> expanded)
    {
        super(defId, defmap, linker, expanded);
    }

    protected void loadData ()
    {
        _projsvc.getType(defId, new PanelCallback<TypeDetail>(_contents) {
            public void onSuccess (TypeDetail deets) {
                _contents.clear();
                init(deets, new Def[0], deets); // TODO: add deets.supers
                // make sure we fit in the view
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    public void execute () {
                        recenterPanel();
                    }
                });
            }
        });
    }

    protected void initBody (final FlowPanel body, SourcePanel sig, TypeDetail detail)
    {
        _detail = detail;

        body.add(sig);

        // if this is a type, display nested fields, funcs, etc.
        FlowPanel deets = null;
        if (detail.kind == Kind.TYPE) {
            FlowPanel members = Widgets.newFlowPanel(_styles.detailMembers());
            deets = Widgets.newFlowPanel();
            addDefs(members, _msgs.tdpTypes(), detail.members, deets);
            // addDefs(members, _msgs.tdpFuncs(), detail.funcs, deets);
            // addDefs(members, _msgs.tdpTerms(), detail.terms, deets);
            if (members.getWidgetCount() > 0) {
                DefUtil.addClear(members);
                body.add(members);
            }
        }

        if (deets != null) {
            body.add(deets);
        }

        // _contents.setWidget(contents);
    }

    protected void addDefs (FlowPanel panel, String flavor, Def[] defs, FlowPanel members)
    {
        for (final Def def : defs) {
            Label label = DefUtil.addDef(panel, def, _defmap, _linker);
            Value<Boolean> viz = _expanded.get(def.id);
            Bindings.bindStateStyle(viz, _rsrc.styles().selected(), null, label);
            UIUtil.makeActionable(label, Bindings.makeToggler(viz));
            new Shower(viz, members) {
                protected Widget createWidget () {
                    Widget deets;
                    if (def.kind == Kind.TYPE) {
                        deets = new TypeDetailPanel(def.id, _defmap, _linker, _expanded);
                    } else {
                        deets = new DefSourcePanel(def.id);
                    }
                    deets.addStyleName(_rsrc.styles().indent());
                    return deets;
                }
            };
        }
    }

    protected TypeDetail _detail;

    /** We keep a global toggle to track whether to open defs with source or summary first. When
     * you expand a def into source, you switch to source first mode, when you contract, you return
     * to summary first. */
    protected static boolean _sourceFirst = false;
}
