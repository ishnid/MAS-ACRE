package is.lill.acre.exception;

@SuppressWarnings ( "serial")
public class InvalidProtocolException extends ProtocolParseException {
   public InvalidProtocolException( String message ) {
      super( message );
   }
}
