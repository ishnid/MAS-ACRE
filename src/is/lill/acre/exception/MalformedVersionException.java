package is.lill.acre.exception;

@SuppressWarnings ( "serial")
public class MalformedVersionException extends RuntimeException {
    public MalformedVersionException( String msg ) {
        super(msg);
    }
}
