package is.lill.acre.protocol;

import is.lill.acre.exception.ProtocolParseException;
import is.lill.acre.xml.XMLProtocolSerialiser;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractRepository implements IRepository, IProtocolManager {

    private static Logger logger = Logger.getLogger( AbstractRepository.class.getName() );
    static {
        logger.setLevel( Level.OFF );
    }

    // has this repository already been read?
    private boolean readProtocols = false;
    private boolean readSources = false;

    private Map<ProtocolDescriptor, Protocol> loadedProtocols = new HashMap<ProtocolDescriptor, Protocol>();

    protected Map<ProtocolDescriptor, IProtocolSource> sources = new HashMap<ProtocolDescriptor, IProtocolSource>();;

    protected Map<String, Set<ProtocolDescriptor>> namespaces = new HashMap<String, Set<ProtocolDescriptor>>();

    public Collection<Protocol> getProtocols() throws RepositoryException {
        if ( !this.readProtocols() ) {
            this.setReadProtocols( true );
            logger.info( "Loading protocols" );

            for ( IProtocolSource ps : this.getSources() ) {
                try {
                    logger.info( "Reading protocol: " + ps.getDescription() );
                    this.addProtocol( XMLProtocolSerialiser.readProtocol( ps.getInputStream(), this ) );
                }
                catch ( Exception e ) {
                    logger.info( "RepositoryException " + e );
                    throw new RepositoryException( e );
                }
            }
        }
        return this.loadedProtocols.values();
    }

    public Collection<IProtocolSource> getSources() throws RepositoryException {
        if ( !this.readSources() ) {
            logger.info( "Loading sources" );
            this.refresh();
            this.setReadSources( true );
        }
        return this.sources.values();
    }

    public boolean readProtocols() {
        return this.readProtocols;
    }

    public void setReadProtocols( boolean read ) {
        this.readProtocols = read;
    }

    public boolean readSources() {
        return this.readSources;
    }

    public void setReadSources( boolean read ) {
        this.readSources = read;
    }

    public Protocol getProtocolByDescriptor( ProtocolDescriptor pd ) throws RepositoryException {
        logger.info( "Trying to find protocol for " + pd.toString() );
        if ( this.loadedProtocols.containsKey( pd ) ) {
            return this.loadedProtocols.get( pd );
        }

        // not yet loaded, but I have a descriptor that I can use to read it
        else {
            for ( IProtocolSource ps : this.getSources() ) {
                if ( ps.getDescriptor().equals( pd ) ) {
                    try {
                        readSource( ps );
                        return this.loadedProtocols.get( pd );
                    }
                    // TODO: better logging
                    catch ( IOException e ) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return null;
    }

    private void readSource( IProtocolSource source ) throws IOException {
        logger.info( "Reading protocol from source: " + source.getDescription() );
        try {
            Protocol p = XMLProtocolSerialiser.readProtocol( source.getInputStream(), this );
            this.addProtocol( p );
        }
        catch ( ProtocolParseException e ) {
            // TODO: raise events
            e.printStackTrace();
        }
        catch ( IOException e ) {
            // TODO: raise an event
            e.printStackTrace();
        }
        catch ( RepositoryException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void deleteProtocol( Protocol p ) throws RepositoryException {
        if ( this.loadedProtocols.containsKey( p.getDescriptor() ) ) {
            this.loadedProtocols.remove( p.getDescriptor() );
            this.namespaces.get( p.getDescriptor().getNamespace() ).remove( p.getDescriptor() );
            if ( this.namespaces.get( p.getDescriptor().getNamespace() ).isEmpty() ) {
                this.namespaces.remove( p.getDescriptor().getNamespace() );
            }
        }
        else {
            throw new RepositoryException( "Protocol " + p.getDescriptor().toString() + " does not exist in repository" );
        }
    }

    protected void addProtocol( Protocol p ) throws RepositoryException {
        if ( this.loadedProtocols.containsKey( p.getDescriptor() ) ) {
            throw new RepositoryException( "Protocol " + p.getDescriptor().toString() + " is already in this repository" );
        }
        else {
            this.loadedProtocols.put( p.getDescriptor(), p );
            if ( !this.namespaces.containsKey( p.getDescriptor().getNamespace() ) ) {
                this.namespaces.put( p.getDescriptor().getNamespace(), new HashSet<ProtocolDescriptor>() );
            }
            this.namespaces.get( p.getDescriptor().getNamespace() ).add( p.getDescriptor() );
        }
    }

    public Set<ProtocolDescriptor> getDescriptorsByNamespace( String namespace ) {
        return this.namespaces.get( namespace );
    }

    public Set<String> getNamespaces() {
        return this.namespaces.keySet();
    }
    
    @Override
    public boolean contains( ProtocolDescriptor desc ) {
        return this.sources.containsKey( desc );
    }
}
