//
// $Id$

package coreen.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;

import coreen.client.Args;
import coreen.model.Def;
import coreen.model.Project;
import coreen.util.DefMap;
import coreen.util.PanelCallback;

/**
 * Displays a summary of the modules and types contained within a given module.
 */
public class ModuleSummaryPanel extends AbstractProjectPanel
{
    public ModuleSummaryPanel ()
    {
        initWidget(_binder.createAndBindUi(this));
    }

    @Override // from AbstractProjectPanel
    public ProjectPage.Detail getId ()
    {
        return ProjectPage.Detail.MDS;
    }

    @Override // from AbstractProjectPanel
    public void setArgs (Project proj, Args args)
    {
        _rootModuleId = args.get(2, 0l);
        _projsvc.getModules(proj.id, new PanelCallback<Def[]>(_modules) {
            public void onSuccess (Def[] modules) {
                gotModules(modules);
            }
        });
    }

    protected void gotModules (Def[] modules)
    {
        if (modules.length == 0) {
            _modules.add(Widgets.newLabel("No modules in this project?"));
            return;
        }

        // locate our root module and obtain the ids of all modules that are its lexical children
        // (TODO: have java-reader structure modules properly?)
        Def shortest = modules[0];
        for (Def mod : modules) {
            if (mod.id == _rootModuleId) {
                _rootModule = mod;
            }
            if (mod.name.length() < shortest.name.length()) {
                shortest = mod;
            }
        }
        if (_rootModule == null) {
            _rootModule = shortest;
        }

        // collect the ids of the modules that are lexically prefixed by the root module
        _mods = new HashMap<Long, Def>();
        for (Def mod : modules) {
            if (mod.name.startsWith(_rootModule.name)) {
                _mods.put(mod.id, mod);
            }
        }

        _projsvc.getModsMembers(_mods.keySet(), new PanelCallback<Def[]>(_modules) {
            public void onSuccess (Def[] modules) {
                _modules.clear();
                gotModsMembers(modules);
            }
        });
    }

    protected void gotModsMembers (Def[] members)
    {
        // split the members up by owner
        Map<Long, List<Def>> byOwner = new HashMap<Long, List<Def>>();
        for (Def member : members) {
            List<Def> olist = byOwner.get(member.outerId);
            if (olist == null) {
                byOwner.put(member.outerId, new ArrayList<Def>());
            }
            olist.add(member);
        }

        _modules.add(Widgets.newLabel(_rootModule.name));
        _mods.remove(_rootModule.id);
        List<Def> mods = new ArrayList<Def>(_mods.values());
        Collections.sort(mods, new Comparator<Def>() {
            public int compare (Def one, Def two) {
                return one.name.compareTo(two.name);
            }
        });

        for (Def mod : mods) {
            _modules.add(Widgets.newLabel(" " + mod.name));
            for (Def member : byOwner.get(mod.id)) {
                _modules.add(Widgets.newLabel("  " + member.name));
            }
        }
    }

    protected interface Styles extends CssResource
    {
    }
    protected @UiField Styles _styles;
    protected @UiField FlowPanel _modules;

    protected long _rootModuleId;
    protected DefMap _defmap = new DefMap();

    protected Def _rootModule;
    protected Map<Long,Def> _mods;

    protected interface Binder extends UiBinder<Widget, ModuleSummaryPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
}
