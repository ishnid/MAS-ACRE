package is.lill.acre.protocol;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipFileRepository extends AbstractRepository {

    private URL base;

    private static final Logger logger = Logger.getLogger( ZipFileRepository.class.getName() );
    static {
        logger.setLevel( Level.WARNING );
    }

    public ZipFileRepository( URL base ) {
        this.base = base;
        logger.info( "Zip File Repository loaded: " + base.toString() );
    }

    @Override
    public IProtocolSource getSourceFor( ProtocolDescriptor desc ) {
        return this.sources.get( desc );
    }

    @Override
    public void refresh() throws RepositoryException {
        this.setReadSources( true );

        ZipInputStream zis;
        try {
            zis = new ZipInputStream( base.openConnection().getInputStream() );

            ZipEntry e;
            while ( ( e = zis.getNextEntry() ) != null ) {
                if ( e.getName().endsWith( "acr" ) ) {

                    byte[] contents = new byte[ (int) e.getSize() ];
                    zis.read( contents );

                    String descriptorString = e.getName().replaceAll( "\\.acr$", "" ).replaceAll( "^.*/", "" );

                    ProtocolDescriptor pd = ProtocolDescriptor.parseString( descriptorString );

                    this.sources.put( pd, new MemoryProtocolSource( contents, pd ) );
                }
            }
        }
        catch ( IOException e1 ) {
            throw new RepositoryException( "Failed to open Zip stream: " + base.toString() );
        }
    }

    @Override
    public String getBase() {
        return this.base.toString();
    }
}
