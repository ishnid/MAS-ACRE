package is.lill.acre.protocol;

/**
 * Exception to indicate an attempt to use a type of {@code State}
 * that is undefined
 * @author daithi
 * @version $Id: UnknownStateTypeException.java 439 2010-03-24 13:31:39Z daithi $
 *
 */
public class UnknownStateTypeException extends Exception {

	private static final long serialVersionUID = 5489470347139261734L;

	public UnknownStateTypeException( String message ) {
        super( message );
    }
}
