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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.Widgets;

import coreen.client.Args;
import coreen.client.Link;
import coreen.client.Page;
import coreen.model.Def;
import coreen.model.Project;
import coreen.util.DefMap;
import coreen.util.ModuleNode;
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
        _proj = proj;
        _moduleId = args.get(2, 0l);
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

        // arrange our modules into a tree
        ModuleNode tree = ModuleNode.createTree(MOD_SEP, modules);

        // scan down the tree to the first node with children; e.g. if we have ("", "com", "foo",
        // "bar", ("baz", "bif", "boo")), we want tree to point to "bar" rather than ""
        tree = findBranches(tree);
        // TODO: this will interact badly with rootName below if we scan down to a phantom module
        // that has multiple submodules; we should probably revamp ModuleTree to keep track of the
        // partial fully qualified name that is valid at each node...

        // locate our target module in this tree and use that as the root
        _mods = (_moduleId > 0) ? tree.findNode(_moduleId) : tree;

        // collect the ids of all modules at or below our target module and request their members
        List<Long> modIds = collectIds(_mods, new ArrayList<Long>());
        _projsvc.getModsMembers(modIds, new PanelCallback<Def[]>(_modules) {
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
                byOwner.put(member.outerId, olist = new ArrayList<Def>());
            }
            olist.add(member);
        }

        // compute some layout metrics
        int availWidth = _modules.getOffsetWidth() - 16; // body margin
        int cols = availWidth / 200, gap = 5;
        int colwidth = ((availWidth - gap*(cols-1)) / cols);

        // if our root module has members, display that
        String rootName = _mods.name;
        if (_mods.mod != null) {
            List<Def> tldefs = byOwner.get(_mods.mod.id);
            if (tldefs != null) {
                ModulePanel mp = new ModulePanel(tldefs.size(), cols, colwidth);
                addTitle(Widgets.newLabel(_mods.mod.name, _styles.rootTitle()));
                mp.addModContents("", 0, tldefs);
                _modules.add(mp);
                rootName = _mods.mod.name;
            }
        }

        // now add the immediate children of the root, including their children (if any)
        for (ModuleNode child : _mods.children) {
            // if we're at a phantom module (e.g. com.google.common.util which only contains the
            // single package com.google.common.util.concurrent), we need to skip down to the
            // actual module, and we'll use its name as our title; but we also need to keep track
            // of our prefix because we might end up skipping to a phantom module that has multiple
            // children (e.g. com.threerings.parlor.game which has children client, data, and
            // server), in the latter case we use the prefix to create our title
            String prefix = rootName;
            while (child.children.size() == 1 && child.mod == null) {
                prefix = prefix + MOD_SEP + child.name;
                child = child.children.get(0);
            }

            List<Def> mods = collectMods(child, new ArrayList<Def>());
            int memberCount = 0;
            for (Def mod : mods) {
                List<Def> modmems = byOwner.get(mod.id);
                if (modmems != null) {
                    memberCount += (modmems.size() + 1);
                }
            }

            ModulePanel mp = new ModulePanel(memberCount, cols, colwidth);
            String childName;
            if (child.mod != null) {
                childName = child.mod.name;
                addTitle(Link.createInline(childName, Page.PROJECT, _proj.id,
                                           ProjectPage.Detail.MDS, child.mod.id));
            } else {
                childName = prefix + MOD_SEP + child.name;
                addTitle(Widgets.newLabel(childName));
            }
            for (Def mod : mods) {
                String header = (mod == child.mod) ? "" : unprefix(childName, mod.name, MOD_SEP);
                mp.addModContents(header, mod.id, byOwner.get(mod.id));
            }
            DefUtil.addClear(mp);
            _modules.add(mp);
        }

        // // members.length + byOwner.size(), availWidth

        // // TODO: subtract one if rootModuleId has members
        // ModulePanel mp = new ModulePanel();
        // _modules.add(mp);

        // if (_rootModuleId > 0) {
        //     _mods.remove(_rootModuleId);
        //     mp.addModContents("", 0, byOwner.get(_rootModuleId));
        // }

        // List<Def> mods = new ArrayList<Def>(_mods.values());
        // Collections.sort(mods, BY_NAME);
        // for (Def mod : mods) {
        //     String title = (_rootModuleName.equals(mod.name)) ? "" :
        //         ((_rootModuleName.length() > 0 && mod.name.startsWith(_rootModuleName)) ?
        //          ("." + mod.name.substring(_rootModuleName.length())) : mod.name);
        //     mp.addModContents(title, mod.id, byOwner.get(mod.id));
        // }
    }

    protected void addTitle (Widget title)
    {
        // we do this double wrapping to avoid the annoying feature whereby the entire width of the
        // page is clickable even though the hyperlink 
        _modules.add(Widgets.newFlowPanel(_styles.title(), title));
    }

    protected String unprefix (String root, String path, char charSep)
    {
        if (root.length() > 0 && path.startsWith(root)) {
            String suff = path.substring(root.length());
            return "..." + ((suff.length() == 0 || suff.charAt(0) != charSep) ?
                            suff : suff.substring(1));
        } else {
            return path;
        }
    }

    protected ModuleNode findBranches (ModuleNode tree)
    {
        return (tree.children.size() == 1 && tree.mod == null) ?
            findBranches(tree.children.get(0)) : tree;
    }

    protected List<Long> collectIds (ModuleNode node, List<Long> modIds)
    {
        if (node.mod != null) {
            modIds.add(node.mod.id);
        }
        for (ModuleNode child : node.children) {
            collectIds(child, modIds);
        }
        return modIds;
    }

    protected List<Def> collectMods (ModuleNode node, List<Def> defs)
    {
        if (node.mod != null) {
            defs.add(node.mod);
        }
        for (ModuleNode child : node.children) {
            collectMods(child, defs);
        }
        return defs;
    }

    protected class ModulePanel extends FlowPanel {
        public ModulePanel (int totalRows, int cols, int colwidth) {
            addStyleName(_styles.modPanel());
            _colwidth = colwidth + "px";
            _rowsPerColumn = totalRows/cols + (totalRows % cols == 0 ? 0 : 1);
        }

        public void addModContents (String title, long modId, List<Def> members) {
            if (members == null) {
                return;
            }
            Collections.sort(members, BY_NAME);

            if (title.length() > 0) {
                // if we're at the last row in this column and need to place a title, skip to the
                // next column instead
                if (_row == 0 || _row == _rowsPerColumn-1) {
                    _row = 0;
                    addPanel();
                }
                _panel.add(Link.createInline(title, Page.PROJECT, _proj.id,
                                             ProjectPage.Detail.CMD, modId));
                _row += 1;
            }

            for (Def member : members) {
                if (_row == 0) {
                    addPanel();
                }
                _panel.add(Widgets.newFlowPanel(_styles.modItem(),
                                                DefUtil.iconForDef(member),
                                                Widgets.newInlineLabel(member.name)));
                if (++_row >= _rowsPerColumn) {
                    _row = 0;
                }
            }
        }

        protected void addPanel () {
            boolean hadPanel = (_panel != null);
            add(_panel = Widgets.newFlowPanel(_styles.modColumn()));
            _panel.setWidth(_colwidth);
            if (hadPanel) {
                _panel.addStyleName(_styles.modColumnX());
            }
        }

        protected FlowPanel _panel;
        protected int _row;

        protected final int _rowsPerColumn;
        protected final String _colwidth;

        protected static final int HEADER_HEIGHT = 80, TITLE_HEIGHT = 25, ROW_HEIGHT = 22;
    }

    protected interface Styles extends CssResource
    {
        String title ();
        String rootTitle ();
        String modPanel ();
        String modColumn ();
        String modColumnX ();
        String modItem ();
    }
    protected @UiField Styles _styles;
    protected @UiField FlowPanel _modules;

    protected Project _proj;
    protected long _moduleId;
    protected ModuleNode _mods;
    protected DefMap _defmap = new DefMap();

    protected static final Comparator<Def> BY_NAME = new Comparator<Def>() {
        public int compare (Def one, Def two) {
            return one.name.compareTo(two.name);
        }
    };

    protected interface Binder extends UiBinder<Widget, ModuleSummaryPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);

    protected static final char MOD_SEP = '.'; // TODO
}
