package is.lill.acre.protocol;

import is.lill.acre.xml.XMLProtocolSerialiser;
import is.lill.acre.xml.XMLRepositoryReader;
import is.lill.acre.xml.XMLRepositoryWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.logging.Logger;

public class AbstractFileSystemRepository extends AbstractRepository implements IEditableRepository {

   private static Logger logger = Logger.getLogger( AbstractFileSystemRepository.class.getName() );

   protected FileSystem fs;

   @Override
   public IProtocolSource getSourceFor( ProtocolDescriptor desc ) {
      return this.sources.get( desc );
   }

   @Override
   public void refresh() throws RepositoryException {
      logger.info( "Refreshing sources" );

      this.sources.clear();

      Path repoPath = fs.getPath( "repository.xml" );

      try {
         InputStream in = Files.newInputStream( repoPath, StandardOpenOption.READ );
         Set<ProtocolDescriptor> protocols = XMLRepositoryReader.readRepository( in );

         for ( ProtocolDescriptor desc : protocols ) {
            this.sources.put( desc, new FileSystemProtocolSource( fs, desc ) );

         }
         in.close();
      }
      catch ( IOException e ) {
         throw new RepositoryException( "Failed to read from repository: " + e );
      }
   }

   @Override
   public String getBase() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void saveRepositoryXML() throws RepositoryException {
      OutputStream out;
      try {
         Path path = this.fs.getPath( ".", "repository.xml" );
         out = Files.newOutputStream( path );
         XMLRepositoryWriter.writeRepository( this, out );
         out.close();
      }
      catch ( IOException e ) {
         throw new RepositoryException( e.getMessage() );
      }
   }

   @Override
   public FileSystem getFileSystem() {
      return this.fs;
   }

   public void saveProtocol( Protocol p ) throws RepositoryException {
      try {
         Path path = this.fs.getPath( "repository", p.getDescriptor().toString() + ".acr" );
         OutputStream out = Files.newOutputStream( path );
         XMLProtocolSerialiser.writeProtocol( p, out );
         out.close();
         this.saveRepositoryXML();
      }
      catch ( Exception e ) {
         throw new RepositoryException( e.getMessage() );
      }
   }

   @Override
   public void deleteProtocol( Protocol p ) throws RepositoryException {
      super.deleteProtocol( p );

      Path protocolPath = this.fs.getPath( "repository", p.getDescriptor().toString() + ".acr" );

      try {
         if ( Files.deleteIfExists( protocolPath ) ) {
            saveRepositoryXML();
         }
         else {
            throw new RepositoryException( "Protocol " + p.getDescriptor().toString() + " does not exist in repository" );
         }
      }
      catch ( IOException e ) {
         throw new RepositoryException( "Failed to delete protocol: " + p.getDescriptor().toString() + ": " + e );
      }
   }
   
   @Override
   public void addProtocol( Protocol p ) throws RepositoryException {
      super.addProtocol( p );
      this.saveProtocol( p );
   }
}
