//
// $Id$

package coreen.rpc;

/**
 * Communicates an error to a GWT client.
 */
public class ServiceException extends Exception
{
    /**
     * Throws an exception with the given message unless the supplied condition is true.
     */
    public static void unless (boolean condition, String message) throws ServiceException {
        if (!condition) {
            throw new ServiceException(message);
        }
    }

    /**
     * Creates a service exception with the supplied translation message.
     */
    public ServiceException (String message) {
        super(message);
    }

    /**
     * Default constructor for use when unserializing.
     */
    public ServiceException () {
    }
}
