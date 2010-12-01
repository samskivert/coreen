//
// $Id$

package coreen.project;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.StringUtil;
import com.threerings.gwt.util.Value;

import coreen.client.Link;
import coreen.client.Page;
import coreen.icons.IconResources;
import coreen.model.Def;
import coreen.model.DefDetail;
import coreen.model.DefId;
import coreen.model.Kind;
import coreen.ui.UIUtil;
import coreen.util.DefMap;

/**
 * Displays a list of defs in a flowed left-to-right panel.
 */
public class DefUtil
{
    /**
     * Creates a summary view for the supplied def details.
     */
    public static Widget createDefSummary (final DefDetail deets, final DefMap defmap,
                                           final UsePopup.Linker linker)
    {
        if ((deets.kind == Kind.MODULE || deets.kind == Kind.TYPE) &&
            // this is a hack to handle Java anonymous classes; we should either have a CLOSURE
            // type or somehow identify explicitly that a type is anonymous; we want to treat such
            // types differently in the user interface
            !StringUtil.isBlank(deets.name)) {
            return TypeSummaryPanel.create(deets, defmap, linker);

        } else {
            TogglePanel contents = new TogglePanel(Value.create(false)) {
                protected Widget createCollapsed () {
                    return new SourcePanel(deets, defmap, linker);
                }
                protected Widget createExpanded () {
                    return new SourcePanel(deets.id, defmap, linker);
                }
            };
            contents.addStyleName(_rsrc.styles().belowTypeLabel());
            return Widgets.newFlowPanel(new TypeLabel(deets, defmap, linker), contents);
        }
    }

    /**
     * Adds a label for a def to a flow panel, along with all the appropriate accouterments.
     */
    public static Label addDef (FlowPanel panel, final Def def, final DefMap defmap,
                                UsePopup.Linker linker)
    {
        SpanWidget label = new SpanWidget.Plain(def.name, def);
        // new UsePopup.Popper(def.id, label, linker, defmap, false);
        UseHighlighter.bind(def.id, label, defmap);
        panel.add(adornDef(def, label));
        panel.add(new InlineLabel(" "));
        return label;
    }

    /**
     * Adds a clearing div to the supplied panel, which presumably has had numerous {@link #addDef}
     * calls on it. Only needed if additional elements are to be added beyond the defs.
     */
    public static void addClear (FlowPanel panel)
    {
        panel.add(UIUtil.newClear());
    }

    /**
     * Returns the appropriate style for a def of the specified kind.
     */
    public static String getDefStyle (Kind kind)
    {
        switch (kind) {
        case MODULE: return _rsrc.styles().defModule();
        case TYPE: return _rsrc.styles().defType();
        case FUNC: return _rsrc.styles().defFunc();
        case TERM: return _rsrc.styles().defTerm();
        default: return _rsrc.styles().defUnknown();
        }
    }

    /**
     * Returns the appropriate style for a use of the specified kind.
     */
    public static String getUseStyle (Kind kind)
    {
        switch (kind) {
        case MODULE: return _rsrc.styles().useModule();
        case TYPE: return _rsrc.styles().useType();
        case FUNC: return _rsrc.styles().useFunc();
        case TERM: return _rsrc.styles().useTerm();
        default: return _rsrc.styles().useUnknown();
        }
    }

    /**
     * Returns the appropriate highlight style for a def or use of the specified kind.
     */
    public static String getHighStyle (Kind kind)
    {
        switch (kind) {
        case MODULE: return _rsrc.styles().highModule();
        case TYPE: return _rsrc.styles().highType();
        case FUNC: return _rsrc.styles().highFunc();
        case TERM: return _rsrc.styles().highTerm();
        default: return _rsrc.styles().highUnknown();
        }
    }

    /**
     * Adorns the supplied def label with the appropriate icon.
     */
    public static Widget adornDef (Def def, Widget widget)
    {
        return Widgets.newFlowPanel(_rsrc.styles().defLabel(), iconForDef(def), widget);
    }

    /**
     * Returns the appropriate icon for the supplied def.
     */
    public static Widget iconForDef (Def def)
    {
        switch (def.kind) {
        default:
        case MODULE: // TODO: module icon
            return makeIcon(_icons.class_obj(), null, null);
        case TYPE: // TODO: support specialization on class/ifc/enum/etc.
            switch (def.flavor) {
            default:
            case CLASS:
                return makeIcon(_icons.class_obj(), null, null);
            case INTERFACE:
                return makeIcon(_icons.int_obj(), null, null);
            case ABSTRACT_CLASS:
                return makeIcon(_icons.class_obj(), _icons.abstract_co(), null);
            case ENUM:
                return makeIcon(_icons.enum_obj(), null, null);
            case ANNOTATION:
                return makeIcon(_icons.int_obj(), null, null); // TODO
            case OBJECT:
                return makeIcon(_icons.class_obj(), null, null); // TODO
            case ABSTRACT_OBJECT:
                return makeIcon(_icons.class_obj(), null, null); // TODO
            }
        case FUNC: // TODO: support public/protected/private, etc.
            switch (def.flavor) {
            default:
            case METHOD:
                return makeIcon(_icons.methpub_obj(), null, null);
            case ABSTRACT_METHOD:
                return makeIcon(_icons.methpub_obj(), _icons.abstract_co(), null);
            case STATIC_METHOD:
                return makeIcon(_icons.methpub_obj(), _icons.static_co(), null);
            case CONSTRUCTOR:
                return makeIcon(_icons.methpub_obj(), _icons.constr_ovr(), null);
            }
        case TERM: // TODO: support public/protected/private, etc.
            switch (def.flavor) {
            default:
            case FIELD:
                return makeIcon(_icons.field_public_obj(), null, null);
            case STATIC_FIELD:
                return makeIcon(_icons.field_public_obj(), _icons.static_co(), null);
            case PARAM:
                return makeIcon(_icons.field_public_obj(), null, null); // TODO
            case LOCAL:
                return makeIcon(_icons.field_public_obj(), null, null); // TODO
            }
        }
    }

    protected static Widget makeIcon (ImageResource base,
                                      ImageResource upright, ImageResource lowleft)
    {
        FlowPanel icon = Widgets.newFlowPanel(_rsrc.styles().typeIcon());
        icon.add(Widgets.newShim(16, 16));
        icon.add(Widgets.newImage(base, _rsrc.styles().typeIconBase()));
        if (upright != null) {
            icon.add(Widgets.newImage(upright, _rsrc.styles().typeIconUR()));
        }
        if (lowleft != null) {
            icon.add(Widgets.newImage(lowleft, _rsrc.styles().typeIconLL()));
        }
        return icon;
    }

    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
    protected static final IconResources _icons = GWT.create(IconResources.class);
}
