package is.lill.acre.protocol;

public class RepositoryException extends Exception {

   private static final long serialVersionUID = -6684170069652378826L;

   public RepositoryException( String msg ) {
      super( msg );
   }

   public RepositoryException( Exception e ) {
      super( e );
   }
}
