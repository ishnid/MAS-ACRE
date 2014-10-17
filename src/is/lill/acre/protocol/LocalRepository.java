package is.lill.acre.protocol;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocalRepository extends AbstractFileSystemRepository {

   private static final Logger logger = Logger.getLogger( LocalRepository.class.getName() );
   static {
      logger.setLevel( Level.WARNING );
   }

   protected LocalRepository( FileSystem fs ) {
      this.fs = fs;
   }

   // currently assumes repository exists and is real!
   public static AbstractFileSystemRepository openRepository( File basedir ) throws RepositoryException {
      logger.info( "Opening repository at " + basedir );
      System.out.println( basedir.toURI() );
      return new LocalRepository( FileSystems.getFileSystem( URI.create( "/Users/daithi/test/repotest/trading/" ) ) );

      /*
       * logger.severe( "No ACRE repository found in " + basedir.toString() );
       * throw new RepositoryException( "No ACRE repository found in " +
       * basedir.toString() );
       */

   }

   // create a new empty repository
   public static AbstractFileSystemRepository newRepository( File basedir ) throws RepositoryException {
      FileSystem fs = FileSystems.getFileSystem( basedir.toURI() );
      try {
         Files.createDirectory( fs.getPath( "repository" ) );
      }
      catch ( IOException e ) {
         throw new RepositoryException( "Failed to create new repository: " + e );
      }
      return new LocalRepository( fs );
   }

   public static void main( String[] args ) throws Exception {
      IRepository r = openRepository( new File( "/Users/daithi/test/repotest/trading" ) );
   }
}