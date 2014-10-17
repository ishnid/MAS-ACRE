package is.lill.acre.protocol;

import is.lill.acre.xml.XMLProtocolSerialiser;
import is.lill.acre.xml.XMLRepositoryWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ZipFileRepository extends AbstractRepository implements IEditableRepository {

   protected URI zipFile;
   protected FileSystem zipfs;

   private static final Logger logger = Logger.getLogger( LocalRepository.class.getName() );
   static {
      logger.setLevel( Level.WARNING );
   }

   protected ZipFileRepository( URI zipFile ) {
      this.zipFile = zipFile;

   }

   public FileSystem getFileSystem() {
      return null;
   }
   
   public static ZipFileRepository openRepository( File zipFile ) throws RepositoryException {

      logger.info( "Opening repository at " + zipFile );

      if ( zipFile.exists() ) {
         // return new ZipFileRepository( zipFile );
         return null; // for now
      }
      else {
         // want to read, but it doesn't exist
         if ( ( readWrite & IRepository.READ ) != 0 ) {
            logger.severe( "No ACRE repository found in " + zipFile.toString() );
            throw new RepositoryException( "No ACRE repository found in " + zipFile.toString() );
         }
         else if ( ( readWrite & IRepository.WRITE ) != 0 ) {
            Map<String, String> env = new HashMap<>();
            env.put( "create", "true" );
            FileSystem zipfs = FileSystems.newFileSystem( zipFile.toURI(), env );
            Path repoPath = zipfs.getPath( "/repository" );
            Files.createDirectory( repoPath );

            return null;
         }
      }
   }

   public static LocalRepository newRepository( File basedir ) {
      return new LocalRepository( basedir );
   }

   public String getBase() {
      return this.basedir.toString();
   }

   @Override
   public void refresh() throws RepositoryException {
      logger.info( "Refreshing sources" );

      this.sources.clear();

      FilenameFilter filter = new FilenameFilter() {
         @Override
         public boolean accept( File dir, String name ) {
            return name.endsWith( ".acr" );
         }
      };

      for ( File f : protocolDir.listFiles( filter ) ) {

         logger.info( "Reading " + f.getName() );

         String[] pieces = f.getName().split( "_" );
         ProtocolDescriptor desc = new ProtocolDescriptor();
         if ( pieces.length == 3 ) {
            desc.setNamespace( pieces[ 0 ] );
            desc.setName( pieces[ 1 ] );
            desc.setVersion( new ProtocolVersion( pieces[ 2 ].replaceAll( "\\.acr$", "" ) ) );
         }
         else {
            logger.severe( "Failed to identify protocol descriptor for " + f.getName() );
            desc.setName( f.getName() );
         }
         this.sources.put( desc, new FileProtocolSource( f, desc ) );
      }
   }

   @Override
   public IProtocolSource getSourceFor( ProtocolDescriptor desc ) {
      return this.sources.get( desc );
   }

   @Override
   public void addProtocol( Protocol p ) throws RepositoryException {
      super.addProtocol( p );
      this.saveProtocol( p );
   }

   public void saveProtocol( Protocol p ) throws RepositoryException {

      File repositoryPath = new File( basedir, "repository" );
      if ( !repositoryPath.exists() ) {
         repositoryPath.mkdir();
      }
      try {

         OutputStream out = new FileOutputStream( new File( repositoryPath, p.getDescriptor().toString() + ".acr" ) );
         XMLProtocolSerialiser.writeProtocol( p, out );
         this.saveRepositoryXML();
      }
      catch ( Exception e ) {
         throw new RepositoryException( e.getMessage() );
      }
   }

   @Override
   public void saveRepositoryXML() throws RepositoryException {
      OutputStream out;
      try {
         out = new FileOutputStream( new File( this.basedir, "repository.xml" ) );
         XMLRepositoryWriter.writeRepository( this, out );
      }
      catch ( IOException e ) {
         throw new RepositoryException( e.getMessage() );
      }
   }

   @Override
   public void deleteProtocol( Protocol p ) throws RepositoryException {
      super.deleteProtocol( p );
      File protocolPath = new File( protocolDir, p.getDescriptor().toString() + ".acr" );
      if ( protocolPath.exists() ) {
         protocolPath.delete();
         saveRepositoryXML();
      }
      else {
         throw new RepositoryException( "Protocol " + p.getDescriptor().toString() + " does not exist in repository" );
      }
   }
}
