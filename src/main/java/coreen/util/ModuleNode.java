//
// $Id$

package coreen.util;

import java.util.ArrayList;
import java.util.List;

import coreen.model.Def;

/**
 * Used to construct a module hierarchy from a bunch of flat modules.
 */
public class ModuleNode implements Comparable<ModuleNode>
{
    /** The name of this module node. */
    public String name;

    /** The module associated with this tree node, if any. */
    public Def mod;

    /** The children of this module node. */
    public List<ModuleNode> children = new ArrayList<ModuleNode>();

    /**
     * Creates a tree of module nodes given the supplied collection of modules.
     *
     * @param modSep the path character used in module names (e.g. {@code '.'}).
     */
    public static ModuleNode createTree (char modSep, Def[] modules)
    {
        ModuleNode root = new ModuleNode("");
        for (Def module : modules) {
            root.addModule(modSep, module);
        }
        return root;
    }

    /**
     * Returns the number of modules contained in this node and its children.
     */
    public int countMods ()
    {
        if (_count == -1) {
            _count = (mod == null) ? 0 : 1;
            for (ModuleNode child : children) {
                _count += child.countMods();
            }
        }
        return _count;
    }

    /**
     * Adds a module to this node, locating the appropriate parent among this node's children,
     * creating nodes as needed along the way.
     *
     * @param modSep the path character used in module names.
     * @param mod the module to be added.
     */
    public void addModule (char modSep, Def mod)
    {
        addModule(modSep, mod.name, mod);
    }

    /**
     * Searches the tree for the module node that contains the supplied module id.
     *
     * @return the node for the specified module, or null.
     */
    public ModuleNode findNode (long modId)
    {
        // depth first search!
        if (mod != null && modId == mod.id) {
            return this;
        }
        for (ModuleNode child : children) {
            ModuleNode node = child.findNode(modId);
            if (node != null) {
                return node;
            }
        }
        return null;
    }

    // from interface Comparable<ModuleNode>
    public int compareTo (ModuleNode other)
    {
        return name.compareTo(other.name);
    }

    @Override // from Object
    public String toString ()
    {
        return name + "/" + mod + "/" + children.size();
    }

    protected ModuleNode (String name)
    {
        this.name = name;
    }

    protected void addModule (char modSep, String name, Def mod)
    {
        // if we're at the end of the line, fill in our module def
        if (name.equals("")) {
            assert this.mod == null;
            this.mod = mod;
            return;
        }

        // otherwise look for (and add if necesary) the appropriate child
        int sepIdx = name.indexOf(modSep);
        String prefix, suffix;
        if (sepIdx == -1) {
            prefix = name;
            suffix = "";
        } else {
            prefix = name.substring(0, sepIdx);
            suffix = name.substring(sepIdx+1);
        }
        ModuleNode next = null;
        for (ModuleNode child : children) {
            if (child.name.equals(prefix)) {
                next = child;
                break;
            }
        }
        if (next == null) {
            children.add(next = new ModuleNode(prefix));
        }
        next.addModule(modSep, suffix, mod);
    }

    protected int _count = -1;
}
