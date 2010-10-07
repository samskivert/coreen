//
// $Id$

package coreen.icons;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * Contains various icons used by the UI.
 */
public interface IconResources extends ClientBundle
{
    // basic ui stuff
    ImageResource codeOpen ();
    ImageResource codeClosed ();

    // member annotation icons
    ImageResource abstract_co ();
    ImageResource class_default_obj ();
    ImageResource class_obj ();
    ImageResource constr_ovr ();
    ImageResource enum_obj ();
    ImageResource field_default_obj ();
    ImageResource field_private_obj ();
    ImageResource field_protected_obj ();
    ImageResource field_public_obj ();
    ImageResource final_co ();
    ImageResource innerclass_private_obj ();
    ImageResource innerclass_protected_obj ();
    ImageResource int_obj ();
    ImageResource methdef_obj ();
    ImageResource methpri_obj ();
    ImageResource methpro_obj ();
    ImageResource methpub_obj ();
    ImageResource static_co ();
    ImageResource synch_co ();
}
