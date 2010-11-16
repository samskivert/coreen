//
// $Id$

package coreen.model;

/**
 * Defines various configuration data.
 */
public class ConfigData
{
    /** The maximum allowed length of a configuration value. */
    public static final int MAX_CONFIG_VALUE_LENGTH = 1024;

    /** The hostname to which the server binds its HTTP socket. */
    public static final String HTTP_HOSTNAME = "http_hostname";

    /** The port to which the server binds its HTTP socket. */
    public static final String HTTP_PORT = "http_port";

    /** The default value for {@link #HTTP_HOSTNAME}. */
    public static final String DEFAULT_HTTP_HOSTNAME = "localhost";

    /** The default value for {@link #HTTP_PORT}. */
    public static final int DEFAULT_HTTP_PORT = 8192;

    private ConfigData () {} // no constructy
}
