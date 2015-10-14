package is.lill.acre.protocol.util;

import is.lill.acre.protocol.FileSystemRepository;
import is.lill.acre.protocol.Protocol;
import is.lill.acre.xml.XMLProtocolSerialiser;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProtocolStore extends FileSystemRepository {

    private static final Logger logger = Logger.getLogger( ProtocolStore.class.getName() );
    static {
        logger.setLevel( Level.WARNING );
    }

    public ProtocolStore( FileSystem fs, String basepath ) {
        super( fs, basepath );
    }

    public void storeProtocol( Protocol p ) throws IOException {
        Path protocolPath = generateProtocolPath( p );
        if ( Files.exists( protocolPath ) ) {
            logger.info( "Protocol already in store: " + p.getDescriptor().getName() );
        }
        else {
            OutputStream out = Files.newOutputStream( protocolPath );
            XMLProtocolSerialiser.writeProtocol( p, out );
            out.close();
        }
    }

    public void removeProtocol( Protocol p ) throws IOException {
        Files.delete( generateProtocolPath( p ) );
    }
    
    private Path generateProtocolPath( Protocol p ) {
       return fs.getPath( basepath, "repository", p.getDescriptor().getUniqueID() + ".acr" );
    }
}
