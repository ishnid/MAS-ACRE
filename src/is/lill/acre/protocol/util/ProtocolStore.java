package is.lill.acre.protocol.util;

import is.lill.acre.protocol.LocalRepository;
import is.lill.acre.protocol.Protocol;
import is.lill.acre.protocol.ProtocolDescriptor;
import is.lill.acre.xml.XMLProtocolSerialiser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ProtocolStore extends LocalRepository {

    private static final Logger logger = Logger.getLogger( ProtocolStore.class.getName() );
    static {
        logger.setLevel( Level.WARNING );
    }

    public ProtocolStore( File basedir ) {
        super( basedir );
        if ( !basedir.exists() ) {
            basedir.mkdir();
        }
    }

    public void storeProtocol( Protocol p ) throws IOException {
        File outfile = generateProtocolFilename( p );
        if ( outfile.exists() ) {
            logger.info( "Protocol already in store: " + p.getDescriptor().getName() );
        }
        else {
            OutputStream out = new FileOutputStream( outfile );
            XMLProtocolSerialiser.writeProtocol( p, out );
            out.close();
        }
    }

    public void removeProtocol( Protocol p ) throws IOException {
        generateProtocolFilename( p ).delete();
    }

    private File generateProtocolFilename( Protocol p ) {
        ProtocolDescriptor pd = p.getDescriptor();
        return new File( this.protocolDir, pd.getNamespace() + "_" + pd.getName() + "_" + pd.getVersion() + ".acr" );
    }

    public File getBasedir() {
        return this.basedir;
    }
}
