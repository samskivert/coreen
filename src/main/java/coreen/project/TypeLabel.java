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
                      String docs)
    {
        addStyleName(_rsrc.styles().typeLabel());
        add(iconForDef(def.type));
        for (TypedId encl : parents) {
            if (encl.type != Def.Type.MODULE) {
                Widget plabel = Widgets.newInlineLabel(encl.name);
                new UsePopup.Popper(encl.id, plabel, linker, defmap);
                add(plabel);
                add(Widgets.newInlineLabel(".")); // TODO: customizable path separator?
            }
        }
        add(Widgets.newInlineLabel(def.name, _rsrc.styles().Type()));
        if (docs != null) {
            add(Widgets.newHTML(docs, "inline"));
        }
    }

    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
    protected static final IconResources _icons = GWT.create(IconResources.class);
}
