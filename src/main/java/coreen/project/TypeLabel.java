//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;

import coreen.icons.IconResources;
import coreen.model.Def;
import coreen.model.DefId;
import coreen.model.Type;
import coreen.util.DefMap;

/**
 * Displays the name of a type including its enclosing types, which are linked.
 */
public class TypeLabel extends FlowPanel
{
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
        switch (def.type) {
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
                return makeIcon(_icons.class_obj(), null, null); // TODO
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
        icon.add(Widgets.newImage(base, _rsrc.styles().typeIconBase()));
        if (upright != null) {
            icon.add(Widgets.newImage(upright, _rsrc.styles().typeIconUR()));
        }
        if (lowleft != null) {
            icon.add(Widgets.newImage(lowleft, _rsrc.styles().typeIconLL()));
        }
        return icon;
    }

    public TypeLabel (DefId[] parents, Def def, UsePopup.Linker linker, DefMap defmap, String docs)
    {
        addStyleName(_rsrc.styles().typeLabel());
        add(iconForDef(def));
        for (DefId encl : parents) {
            Widget plabel = Widgets.newInlineLabel(encl.name);
            if (encl.type != Type.MODULE) {
                new UsePopup.Popper(encl.id, plabel, linker, defmap, true);
            }
            add(plabel);
            add(Widgets.newInlineLabel(".")); // TODO: customizable path separator?
        }
        Widget dlabel = createDefLabel(def);
        dlabel.addStyleName("inline");
        add(dlabel);
        if (docs != null) {
            add(new DocLabel(docs));
        }
    }

    protected Widget createDefLabel (Def def)
    {
        return Widgets.newLabel(def.name, _rsrc.styles().Type());
    }

    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
    protected static final IconResources _icons = GWT.create(IconResources.class);
}
