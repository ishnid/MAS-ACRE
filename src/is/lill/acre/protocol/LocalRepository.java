package is.lill.acre.protocol;

import is.lill.acre.xml.XMLProtocolSerialiser;
import is.lill.acre.xml.XMLRepositoryWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LocalRepository extends AbstractRepository implements IEditableRepository {

    protected File basedir;
    protected File protocolDir;

    private static final Logger logger = Logger.getLogger( LocalRepository.class.getName() );
    static {
        logger.setLevel( Level.WARNING );
    }

    protected LocalRepository( File basedir ) {
        this.basedir = basedir;
        if ( !basedir.exists() ) {
            basedir.mkdir();
        }
        this.protocolDir = new File( basedir, "repository" );
        if ( !protocolDir.exists() ) {
            protocolDir.mkdir();
        }
    }

    public static LocalRepository openRepository( File basedir ) throws RepositoryException {
        logger.info( "Opening repository at " + basedir );
        if ( new File( basedir, "repository.xml" ).exists() ) {
            return new LocalRepository( basedir );
        }
        else {
            logger.severe( "No ACRE repository found in " + basedir.toString() );
            throw new RepositoryException( "No ACRE repository found in " + basedir.toString() );
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
