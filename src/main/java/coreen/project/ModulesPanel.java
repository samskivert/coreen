//
// $Id$

package coreen.project;

import java.util.Map;
import java.util.HashMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Bindings;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Value;

import coreen.client.ClientMessages;
import coreen.client.Link;
import coreen.client.Page;
import coreen.model.Def;
import coreen.util.PanelCallback;
import coreen.util.Shower;

/**
 * Displays all modules for a project, and their direct type members.
 */
public class ModulesPanel extends SummaryPanel
{
    public ModulesPanel ()
    {
        initWidget(_binder.createAndBindUi(this));
    }

    @Override // from AbstractProjectPanel
    public ProjectPage.Detail getId ()
    {
        return ProjectPage.Detail.MDS;
    }

    @Override // from SummaryPanel
    protected void updateContents (long projectId)
    {
        _linker = UsePopup.byModsInProject(_projectId);
        _modules.add(Widgets.newLabel(_cmsgs.loading()));
        _projsvc.getModules(projectId, new PanelCallback<Def[]>(_modules) {
            public void onSuccess (Def[] modules) {
                initContents(modules);
            }
        });
    }

    protected void initContents (Def[] modules)
    {
        _modules.clear();
        if (modules.length == 0) {
            _modules.add(Widgets.newLabel("No modules in this project?"));
            return;
        }

        // find the longest prefix among all the packages
        char modSep = '.'; // TODO
        String prefix = modules[0].name;
        prefix = prefix.substring(0, prefix.lastIndexOf(modSep)+1);
        for (int ii = 1; ii < modules.length; ii++) {
            String name = modules[ii].name;
            while (!name.startsWith(prefix)) {
                prefix = prefix.substring(0, prefix.lastIndexOf(modSep)+1);
            }
        }

        if (prefix.length() > 0) {
            String pname = prefix.charAt(prefix.length()-1) == modSep ?
                prefix.substring(0, prefix.length()-1) : prefix;
            _modules.add(Widgets.newLabel(pname + ":", _styles.modprefix()));
        }
        for (final Def mod : modules) {
            String ltext = mod.name.substring(prefix.length());
            Widget label = Widgets.newActionLabel(ltext, _styles.modlink(), new ClickHandler() {
                public void onClick (ClickEvent event) {
                    Value<Boolean> showing = _showing.get(mod.id);
                    showing.update(!showing.get());
                }
            });
            Bindings.bindStateStyle(_showing.get(mod.id), _rsrc.styles().selected(), null, label);
            _modules.add(label);
            new Shower(_showing.get(mod.id), _moddefs) {
                protected Widget createWidget () {
                    return createModulePanel(mod);
                }
            };
        }
    }

    protected Widget createModulePanel (final Def mod)
    {
        final FlowPanel defs = Widgets.newFlowPanel();
        defs.add(Widgets.newLabel("Loading..."));
        _projsvc.getMembers(mod.id, new PanelCallback<Def[]>(defs) {
            public void onSuccess (Def[] members) {
                defs.clear();
                Widget title = Widgets.newLabel(mod.name, _styles.modtitle());
                title.setTitle(""+mod.id);
                defs.add(title);
                addMembers(defs, mod, members);
            }
        });
        return defs;
    }

    protected void addMembers (FlowPanel panel, final Def mod, Def[] members)
    {
        for (final Def def : members) {
            Label label = DefUtil.addDef(panel, def, _linker, _defmap);
            Bindings.bindStateStyle(_showing.get(def.id), _rsrc.styles().selected(), null, label);
            label.addStyleName(_rsrc.styles().actionable());
            label.addClickHandler(new ClickHandler() {
                public void onClick (ClickEvent event) {
                    if (_showing.get(def.id).get()) {
                        Link.go(Page.PROJECT, _projectId,
                                ProjectPage.Detail.MDS, mod.id, -def.id);
                    } else {
                        Link.go(Page.PROJECT, _projectId,
                                ProjectPage.Detail.MDS, mod.id, def.id);
                    }
                }
            });
            new Shower(_showing.get(def.id), _types) {
                protected Widget createWidget () {
                    return new TypeSummaryPanel(def.id, _defmap, _showing, _linker);
                }
            };
        }
    }

    protected interface Styles extends CssResource
    {
        String modprefix ();
        String modlink ();
        String modtitle ();
        String type ();
    }
    protected @UiField Styles _styles;
    protected @UiField FlowPanel _modules;
    protected @UiField FlowPanel _moddefs;
    protected @UiField FlowPanel _types;

    protected Map<Def, Widget> _modpans = new HashMap<Def, Widget>();
    protected UsePopup.Linker _linker;

    protected interface Binder extends UiBinder<Widget, ModulesPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ClientMessages _cmsgs = GWT.create(ClientMessages.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
