//
// $Id$

package coreen.project;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Bindings;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Value;

import coreen.client.ClientMessages;
import coreen.client.Link;
import coreen.client.Page;
import coreen.model.Def;
import coreen.ui.UIUtil;
import coreen.util.ModuleNode;
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
        return ProjectPage.Detail.CMD;
    }

    @Override // from SummaryPanel
    protected void updateContents (long projectId)
    {
        _linker = UsePopup.byModsInProject(_projectId);
        _modules.clear();
        _moddefs.clear();
        _types.clear();
        _modules.add(Widgets.newLabel(_cmsgs.loading()));
        _projsvc.getModules(projectId, new PanelCallback<Def[]>(_modules) {
            public void onSuccess (Def[] modules) {
                _modules.clear();
                initContents(modules);
            }
        });
    }

    protected void initContents (Def[] modules)
    {
        if (modules.length == 0) {
            _modules.add(Widgets.newLabel("No modules in this project?"));
            return;
        }

        // build the module hierarchy
        char modSep = '.'; // TODO
        ModuleNode root = ModuleNode.createTree(modSep, modules);

        // now flatten it into a bunch of links
        if (root.mod != null) {
            addModuleLabels(root, "", modSep, DEF_THRESH); // one nameless top-level module
        } else {
            for (ModuleNode top : root.children) {
                addModuleLabels(top, "", modSep, DEF_THRESH);
            }
        }
    }

    protected void addModuleLabels (ModuleNode root, String prefix, char modSep, int defthres)
    {
        List<Deferral> deferred = new ArrayList<Deferral>();
        addModuleLabels(root, prefix, prefix, modSep, 0, deferred, defthres);
        _modules.add(Widgets.newHTML("<br/>"));
        for (Deferral def : deferred) {
            addModuleLabels(def.node, def.prefix, modSep, DEF_THRESH_2);
        }
    }

    protected void addModuleLabels (ModuleNode node, String prefix, String fullPrefix, char modSep,
                                    int nest, List<Deferral> deferred, int defthres)
    {
        // if we have a deferral list and more than two children, defer ourselves until later
        if (nest > 0 && node.countMods() > defthres) {
            deferred.add(new Deferral(fullPrefix, node));
            return;
        }

        boolean atRoot = node.name.equals("");
        if (node.mod != null) {
            final Def mod = node.mod;
            _modules.add(Widgets.newInlineLabel(" " + prefix));
            Widget label = Widgets.newActionLabel(node.name, new ClickHandler() {
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
            prefix = "";
            nest += 1;

        } else if (node.children.size() > 1) {
            _modules.add(Widgets.newInlineLabel(" "));
            _modules.add(Widgets.newLabel(prefix + node.name));
            prefix = "";
            nest += 1;

        } else if (!atRoot) {
            prefix = prefix + node.name + modSep;
        }

        fullPrefix = fullPrefix + node.name + modSep;
        if (node.children.size() > 0) {
            if (prefix.length() == 0 && !atRoot) {
                _modules.add(Widgets.newInlineLabel(" {"));
            }
            for (ModuleNode child : node.children) {
                addModuleLabels(child, prefix, fullPrefix, modSep, nest, deferred, defthres);
            }
            if (prefix.length() == 0 && !atRoot) {
                _modules.add(Widgets.newInlineLabel(" }"));
            }
        }
    }

    protected Widget createModulePanel (final Def mod)
    {
        final FlowPanel defs = Widgets.newFlowPanel();
        defs.add(Widgets.newLabel("Loading..."));
        _projsvc.getMembers(mod.id, new PanelCallback<Def[]>(defs) {
            public void onSuccess (final Def[] members) {
                defs.clear();
                Widget title = Widgets.newLabel(mod.name, _styles.modtitle());
                title.setTitle(""+mod.id);
                defs.add(title);

                int added = addMembers(defs, mod, true, members);
                if (added < members.length) {
                    NonPublicPanel nonpubs = new NonPublicPanel() {
                        protected void populate () {
                            addMembers(this, mod, false, members);
                        }
                    };
                    defs.add(nonpubs.makeToggle("Non-public members"));
                    defs.add(nonpubs);
                }
            }
        });
        return defs;
    }

    protected int addMembers (FlowPanel panel, Def mod, boolean access, Def[] members)
    {
        int added = 0;
        for (Def member : members) {
            if (member.isPublic() == access) {
                addMember(panel, mod, member);
                added += 1;
            }
        }
        return added;
    }

    protected void addMember (FlowPanel panel, final Def mod, final Def member)
    {
        Label label = DefUtil.addDef(panel, member, _defmap, _linker);
        Bindings.bindStateStyle(_showing.get(member.id), _rsrc.styles().selected(), null, label);
        UIUtil.makeActionable(label, new ClickHandler() {
            public void onClick (ClickEvent event) {
                if (_showing.get(member.id).get()) {
                    Link.go(Page.PROJECT, _projectId,
                            ProjectPage.Detail.MDS, mod.id, -member.id);
                } else {
                    Link.go(Page.PROJECT, _projectId,
                            ProjectPage.Detail.MDS, mod.id, member.id);
                }
            }
        });
        new Shower(_showing.get(member.id), _types) {
            protected Widget createWidget () {
                return TypeSummaryPanel.create(member.id, _defmap, _linker, _showing);
            }
        };
    }

    protected static class Deferral
    {
        public final String prefix;
        public final ModuleNode node;
        public Deferral (String prefix, ModuleNode node) {
            this.prefix = prefix;
            this.node = node;
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

    protected UsePopup.Linker _linker;

    protected interface Binder extends UiBinder<Widget, ModulesPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ClientMessages _cmsgs = GWT.create(ClientMessages.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);

    protected static final int DEF_THRESH = 3;
    protected static final int DEF_THRESH_2 = 6;
}
