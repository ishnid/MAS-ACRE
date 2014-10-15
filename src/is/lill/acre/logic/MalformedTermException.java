package is.lill.acre.logic;

/**
 * Exception to flag that an attempt to parse a string as a {@code Term} has failed, as the string is mis-formatted.
 * @author daithi
 * @version $Id$
 */
public class MalformedTermException extends Exception {

	private static final long serialVersionUID = 1748622311652410710L;

	/**
	 * Constructor
	 * @param msg The message associated with the exception
	 */
	public MalformedTermException( String msg ) {
        super( msg );
    }
}
