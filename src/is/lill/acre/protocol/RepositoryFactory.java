package is.lill.acre.protocol;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RepositoryFactory {

   private static final Logger logger = Logger.getLogger( RepositoryFactory.class.toString() );
   static {
      logger.setLevel( Level.INFO );
   }

   public static IRepository createRepository( String description ) throws RepositoryException {
      try {
         FileSystem fs = null;
         String base = null;
         if ( description.endsWith( ".zip" ) ) {
            fs = FileSystems.newFileSystem( URI.create( "jar:file://" + description ), new HashMap<String, String>() );
            base = "/";
         }
         else {
            fs = FileSystems.getDefault();
            base = description;
         }
         initRepository( fs, base );
         return new FileSystemRepository( fs, base );
      }
      catch ( IOException e ) {
         throw new RepositoryException( "Failed to create repository: " + e );
      }
   }

   public static IRepository openRepository( String description ) throws RepositoryException {
      try {
         URL u = new URL( description );
         return new HTTPRepository( u );
      }
      catch ( MalformedURLException e ) {
         File base = new File( description );

         // it's a zip repository
         if ( description.endsWith( ".zip" ) ) {
            logger.info( "Trying to open zip repository: " + description );
            try {
               FileSystemRepository toReturn = new FileSystemRepository( FileSystems.newFileSystem( URI.create( "jar:file://" + description ), new HashMap<String, String>() ), "/" );
               toReturn.refresh();
               return toReturn;
            }
            catch ( IOException e1 ) {
               throw new RepositoryException( "Failed to open repository: " + description );
            }
         }
         // new local repository
         /*
          * else if ( !base.exists() ) { if ( !description.startsWith( "/" ) ) {
          * description = "/" + description; }
          * 
          * URL u = RepositoryFactory.class.getResource( description ); if ( u
          * == null ) { throw new RepositoryException(
          * "Repository could not be found: " + description ); } else { try {
          * return new ZipFileRepository( u.toURI() ); } catch (
          * URISyntaxException e1 ) {
          * 
          * e1.printStackTrace(); return null; } } }
          */

         // pre-existing local filesystem repository
         else if ( base.isDirectory() ) {
            logger.info( "Trying to open local directory repository: " + description );
            FileSystem fs = FileSystems.getDefault();
            FileSystemRepository toReturn = new FileSystemRepository( fs, base.toString() );
            toReturn.refresh();
            return toReturn;
         }

         else {
            throw new RepositoryException( "Repository type could not be identified: " + description );
         }
      }
   }

   private static void initRepository( FileSystem fs, String base ) throws RepositoryException {
      // create 'repository' directory
      Path repoPath = fs.getPath( base, "repository" );
      Path xmlPath = fs.getPath( base, "repository.xml" );
      try {
         Files.createDirectory( repoPath );
         InputStream xmlStream = RepositoryFactory.class.getResourceAsStream( "/is/lill/acre/xml/repository.xml" );
         Files.copy( xmlStream, xmlPath );
         
      }
      catch ( IOException e ) {
         throw new RepositoryException( "Failed to create repository: " + e );
      }
   }

}
