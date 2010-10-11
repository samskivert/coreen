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
import coreen.model.TypedId;
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
        return Widgets.newFlowPanel(_rsrc.styles().defLabel(), iconForDef(def.type), widget);
    }

    /**
     * Returns the appropriate icon for the supplied def.
     */
    public static Image iconForDef (Def.Type type)
    {
        Image icon = new Image(rsrcForDef(type));
        icon.addStyleName(_rsrc.styles().typeIcon());
        return icon;
    }

    /**
     * Returns the appropriate icon for the supplied def.
     */
    public static ImageResource rsrcForDef (Def.Type type)
    {
        switch (type) {
        default:
        case MODULE: // TODO: module icon
            return _icons.class_obj();
        case TYPE: // TODO: support specialization on class/ifc/enum/etc.
            return _icons.class_obj();
        case FUNC: // TODO: support public/protected/private, etc.
            return _icons.methpub_obj();
        case TERM: // TODO: support public/protected/private, etc.
            return _icons.field_public_obj();
        }
    }

    public TypeLabel (TypedId[] parents, Def def, UsePopup.Linker linker, DefMap defmap,
                      String docs, boolean showModules)
    {
        addStyleName(_rsrc.styles().typeLabel());
        // haxx0r!
        if (showModules && docs != null) {
            add(Widgets.newHTML(docs)); // not inline
        }
        if (!showModules) {
            StringBuilder buf = new StringBuilder();
            for (TypedId encl : parents) {
                if (encl.type == Def.Type.MODULE) {
                    if (buf.length() > 0) {
                        buf.append(".");
                    }
                    buf.append(encl.name);
                }
            }
            add(Widgets.newLabel(buf.toString(), _rsrc.styles().Module()));
        }
        add(iconForDef(def.type));
        for (TypedId encl : parents) {
            if (showModules || encl.type != Def.Type.MODULE) {
                Widget plabel = Widgets.newInlineLabel(encl.name);
                if (encl.type != Def.Type.MODULE) {
                    new UsePopup.Popper(encl.id, plabel, linker, defmap);
                }
                add(plabel);
                add(Widgets.newInlineLabel(".")); // TODO: customizable path separator?
            }
        }
        Widget dlabel = createDefLabel(def);
        dlabel.addStyleName("inline");
        add(dlabel);
        if (!showModules && docs != null) {
            add(Widgets.newHTML(docs, "inline"));
        }
    }

    protected Widget createDefLabel (Def def)
    {
        return Widgets.newLabel(def.name, _rsrc.styles().Type());
    }

    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
    protected static final IconResources _icons = GWT.create(IconResources.class);
}
