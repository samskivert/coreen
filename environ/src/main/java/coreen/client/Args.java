//
// $Id$

package coreen.client;

/**
 * Handles encoding and decoding of arguments to pages.
 */
public class Args
{
    /** The page represented by these parsed args. */
    public final Page page;

    /**
     * Creates a history token for the specified page and args.
     */
    public static String createToken (Page page, Object... args)
    {
        StringBuilder buf = new StringBuilder();
        buf.append(page);
        for (Object arg : args) {
            buf.append(SEPARATOR);
            buf.append(arg);
        }
        return buf.toString();
    }

    /**
     * Parses the supplied history token into an args instance.
     */
    public Args (String token)
    {
        String[] args = token.split(SEPARATOR);
        if (args.length > 0) {
            this.page = parsePage(args[0]);
            _args = new String[args.length-1];
            System.arraycopy(args, 1, _args, 0, _args.length);
        } else {
            this.page = Page.LIBRARY;
            _args = new String[0];
        }
    }

    /**
     * Returns the argument at the specified index or the default value if an argument was not
     * specified at that index.
     */
    public String get (int index, String defval)
    {
        return (index < _args.length) ? _args[index] : defval;
    }

    /**
     * Returns the argument at the specified index or the default value if no argument or a
     * non-integer argument was provided at that index.
     */
    public int get (int index, int defval)
    {
        try {
            return (index < _args.length) ? Integer.parseInt(_args[index]) : defval;
        } catch (Exception e) {
            return defval;
        }
    }

    protected static Page parsePage (String pagestr)
    {
        try {
            return Page.valueOf(pagestr);
        } catch (Exception e) {
            return Page.LIBRARY;
        }
    }

    protected String[] _args;

    protected static final String SEPARATOR = "~";
}
