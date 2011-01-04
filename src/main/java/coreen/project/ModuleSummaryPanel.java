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
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Bindings;
import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Value;

import coreen.client.Args;
import coreen.client.Link;
import coreen.client.Page;
import coreen.model.Def;
import coreen.model.Project;
import coreen.ui.PopupGroup;
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
        // reset our modules cache if we switch projects
        if (_proj == null || _proj.id != proj.id) {
            _defmap = new DefMap();
            _allMods = null;
        }
        _proj = proj;
        _moduleId = args.get(2, 0l);

        if (_allMods == null) {
            _projsvc.getModules(proj.id, new PanelCallback<Def[]>(_contents) {
                public void onSuccess (Def[] modules) {
                    _contents.clear();
                    gotModules(modules);
                }
            });
        } else {
            _contents.clear();
            displayModule(_moduleId, _contents, false);
        }
    }

    protected void gotModules (Def[] modules)
    {
        if (modules.length == 0) {
            _contents.add(Widgets.newLabel("No modules in this project?"));
        } else {
            // arrange our modules into a tree
            _allMods = ModuleNode.createTree(MOD_SEP, modules);
            displayModule(_moduleId, _contents, false);
        }
    }

    protected void displayModule (long moduleId, final FlowPanel contents, final boolean nested)
    {
        // if we have copious modules, just display the top-level modules; TODO: improve heuristic
        if (moduleId == 0 && _allMods.countMods() > 20) {
            for (final Def tip : collectTips(_allMods, new ArrayList<Def>())) {
                Widget link = Link.createInline(
                    tip.name, Page.PROJECT, _proj.id, ProjectPage.Detail.MDS, tip.id);
                Value<Boolean> showing = Value.create(false);
                // TODO: remember toggled status
                Widget toggle = TogglePanel.makeToggleButton(showing);
                toggle.addStyleName("inline");
                Widget title = Widgets.newFlowPanel(_styles.title(), toggle, link);
                title.addStyleName(_styles.toggleTitle());
                contents.add(title);

                FlowPanel members = new FlowPanel() {
                    public void setVisible (boolean visible) {
                        if (visible && getWidgetCount() == 0) {
                            displayModule(tip.id, this, true);
                        }
                        super.setVisible(visible);
                    }
                };
                Bindings.bindVisible(showing, members);
                contents.add(members);
            }
            return;
        }

        // scan down the tree to the first node with children; e.g. if we have ("", "com", "foo",
        // "bar", ("baz", "bif", "boo")), we want tree to point to "bar" rather than ""
        ModuleNode tree = findBranches(_allMods);
        // TODO: this will interact badly with rootName below if we scan down to a phantom module
        // that has multiple submodules; we should probably revamp ModuleTree to keep track of the
        // partial fully qualified name that is valid at each node...

        // locate our target module in this tree and use that as the root
        final ModuleNode mods = (moduleId > 0) ? tree.findNode(moduleId) : tree;

        // collect the ids of all modules at or below our target module and request their members
        List<Long> modIds = collectIds(mods, new ArrayList<Long>());
        contents.add(Widgets.newLabel("Loading..."));
        _projsvc.getModsMembers(modIds, new PanelCallback<Def[]>(contents) {
            public void onSuccess (Def[] modules) {
                contents.clear();
                gotModsMembers(mods, modules, contents, nested);
            }
        });
    }

    protected void gotModsMembers (ModuleNode mods, Def[] members,
                                   FlowPanel contents, boolean nested)
    {
        // split the members up by owner
        Map<Long, List<Def>> byOwner = new HashMap<Long, List<Def>>();
        for (Def member : members) {
            if (!member.isPublic()) {
                continue; // skip non-public members; TODO: filter elsewhere?
            }
            List<Def> olist = byOwner.get(member.outerId);
            if (olist == null) {
                byOwner.put(member.outerId, olist = new ArrayList<Def>());
            }
            olist.add(member);
        }

        // compute some layout metrics
        int availWidth = contents.getOffsetWidth() - 16; // body margin
        int cols = availWidth / 200, gap = 5;
        int colwidth = ((availWidth - gap*(cols-1)) / cols);

        // if our root module has members, display that
        String rootName = mods.name;
        if (mods.mod != null) {
            List<Def> tldefs = byOwner.get(mods.mod.id);
            if (tldefs != null) {
                ModulePanel mp = new ModulePanel(tldefs.size(), cols, colwidth);
                // if we're a nested display, our title is already visible
                if (!nested) {
                    addTitle(contents, Widgets.newLabel(mods.mod.name, _styles.rootTitle()));
                }
                mp.addModContents("", 0, tldefs);
                contents.add(mp);
                rootName = mods.mod.name;
            }
        }

        // now add the immediate children of the root, including their children (if any)
        for (ModuleNode child : mods.children) {
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

            List<Def> mdefs = collectMods(child, new ArrayList<Def>());
            int memberCount = 0;
            for (Def mod : mdefs) {
                List<Def> modmems = byOwner.get(mod.id);
                if (modmems != null) {
                    memberCount += (modmems.size() + 1);
                }
            }

            ModulePanel mp = new ModulePanel(memberCount, cols, colwidth);
            String childName;
            if (child.mod != null) {
                childName = child.mod.name;
                addTitle(contents, Link.createInline(childName, Page.PROJECT, _proj.id,
                                                     ProjectPage.Detail.MDS, child.mod.id));
            } else {
                childName = prefix + MOD_SEP + child.name;
                addTitle(contents, Widgets.newLabel(childName));
            }
            for (Def mod : mdefs) {
                String header = (mod == child.mod) ? "" : unprefix(childName, mod.name, MOD_SEP);
                mp.addModContents(header, mod.id, byOwner.get(mod.id));
            }
            DefUtil.addClear(mp);
            contents.add(mp);
        }
    }

    protected void addTitle (FlowPanel contents, Widget title)
    {
        // we do this double wrapping to avoid the annoying feature whereby the entire width of the
        // page is clickable even though the hyperlink 
        contents.add(Widgets.newFlowPanel(_styles.title(), title));
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

    protected List<Def> collectTips (ModuleNode node, List<Def> tips)
    {
        if (node.mod != null) {
            tips.add(node.mod);
        } else {
            for (ModuleNode child : node.children) {
                collectTips(child, tips);
            }
        }
        return tips;
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
                Widget link = Link.createInline(member.name, Page.PROJECT, _proj.id,
                                                ProjectPage.Detail.forKind(member.kind), member.id);
                FocusPanel wrapper = new FocusPanel(link);
                wrapper.addStyleName("inline");
                new UsePopup.Popper(member.id, wrapper, UsePopup.TYPE, _defmap, false).
                    setGroup(_group);
                _panel.add(Widgets.newFlowPanel(_styles.modItem(),
                                                DefUtil.iconForDef(member), wrapper));
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
        String toggleTitle ();
        String modPanel ();
        String modColumn ();
        String modColumnX ();
        String modItem ();
    }
    protected @UiField Styles _styles;
    protected @UiField FlowPanel _contents;

    protected Project _proj;
    protected ModuleNode _allMods;
    protected DefMap _defmap;
    protected PopupGroup _group = new PopupGroup();

    protected long _moduleId;

    protected static final Comparator<Def> BY_NAME = new Comparator<Def>() {
        public int compare (Def one, Def two) {
            return one.name.compareTo(two.name);
        }
    };

    protected interface Binder extends UiBinder<Widget, ModuleSummaryPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);

    protected static final char MOD_SEP = '.'; // TODO
}
