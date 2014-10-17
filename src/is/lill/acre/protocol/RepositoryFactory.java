package is.lill.acre.protocol;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RepositoryFactory {

   private static final Logger logger = Logger.getLogger( RepositoryFactory.class.toString() );
   static {
      logger.setLevel( Level.WARNING );
   }

   public static IRepository openRepository( String description ) throws RepositoryException {
      try {
         URL u = new URL( description );
         return new HTTPRepository( u );
      }
      catch ( MalformedURLException e ) {
         File base = new File( description );
         if ( !base.exists() ) {
            if ( !description.startsWith( "/" ) ) {
               description = "/" + description;
            }

            URL u = RepositoryFactory.class.getResource( description );
            if ( u == null ) {
               throw new RepositoryException( "Repository could not be found: " + description );
            }
            else {
               try {
                  return new ZipFileRepository( u.toURI() );
               }
               catch ( URISyntaxException e1 ) {

                  e1.printStackTrace();
                  return null;
               }
            }
         }
         else if ( base.isDirectory() ) {
            return new LocalRepository( base );
         }
         else if ( description.endsWith( ".zip" ) ) {
            URI u = base.toURI();
            return new ZipFileRepository( u );
         }
         else {
            throw new RepositoryException( "Repository type could not be identified: " + description );
         }
      }
   }
}
