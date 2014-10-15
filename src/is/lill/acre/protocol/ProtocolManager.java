package is.lill.acre.protocol;

import is.lill.acre.event.ProtocolAddedEvent;
import is.lill.acre.exception.ProtocolParseException;
import is.lill.acre.xml.XMLProtocolSerialiser;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to manage protocols for an agent platform. This is designed to be shared
 * among all agents.
 * 
 * @author daithi
 * @version $Id$
 */
public class ProtocolManager extends Observable implements IProtocolManager {

    private static Logger logger = Logger.getLogger( ProtocolManager.class.getName() );

    static {
        logger.setLevel( Level.WARNING );
    }

    private Set<IProtocolSource> sources;
    private Map<ProtocolDescriptor, Protocol> loadedProtocols;
    //private ProtocolStore protocolStore;
    private Set<String> loadedRepositoryNames = new HashSet<String>();

    public ProtocolManager() {
        this.sources = new HashSet<IProtocolSource>();
        this.loadedProtocols = new HashMap<ProtocolDescriptor, Protocol>();

        String acreHome = System.getProperty( "ACRE_HOME" );
        if ( acreHome == null ) {
            acreHome = System.getProperty( "user.home" ) + File.separator + ".acre";
        }

        //logger.info( "Loading protocol store at: " + acreHome );

        //this.protocolStore = new ProtocolStore( new File( acreHome ) );

        // try {
        // for ( IProtocolSource source : this.protocolStore.getSources() ) {
        // disable protocol store
        // this.addSource( source );
        // }
        // }
        // catch ( RepositoryException e ) {
        // logger.severe( "Failed to read backed-up protocols from " +
        // this.protocolStore.getBasedir().getAbsolutePath() );
        // }
    }

    /**
     * Add a single source to this manager, from which a protocol should be read
     * 
     * @param source
     */
    public void addSource( IProtocolSource source ) {
        this.sources.add( source );
        try {
            readSource( source );
            logger.info( "Added Source: " + source.getDescription() );
        }
        catch ( IOException e ) {
            logger.severe( "Exception when reading from Protocol Source: " + e );
        }
    }

    public String getBase() {
        return "[Anonymous]";
    }

    /**
     * Get the number of protocols loaded
     * 
     * @return
     */
    public int size() {
        return this.loadedProtocols.size();
    }

    private void readSource( IProtocolSource source ) throws IOException {
        logger.info( "Reading protocol from source: " + source.getDescription() );
        try {
            Protocol p = XMLProtocolSerialiser.readProtocol( source.getInputStream(), this );
            this.addProtocol( p );
        }
        catch ( ProtocolParseException e ) {
            logger.severe( "Invalid XML in resource: " + source.getDescription() );
        }
        catch ( IOException e ) {
            // TODO: raise an event
            e.printStackTrace();
        }
    }

    /**
     * Add a {@link Protocol} to the manager
     * 
     * @param protocol
     */
    public void addProtocol( Protocol protocol ) {
        logger.info( "Adding protocol definition: " + protocol.getDescriptor().getName() );
        if ( !this.loadedProtocols.containsKey( protocol.getDescriptor() ) ) {
            this.loadedProtocols.put( protocol.getDescriptor(), protocol );

            logger.info( "Informing observers of new protocol: " + protocol.getDescriptor().getName() );
            setChanged();
            notifyObservers( new ProtocolAddedEvent( protocol ) );
            /*
             * try { this.protocolStore.storeProtocol( protocol ); } catch (
             * IOException e ) { logger.severe( "Failed to save protocol " +
             * protocol.getDescriptor().getName() + " to the protocol store" );
             * }
             */
        }
        else {
            logger.warning( "Attempt to load duplicate protocol: " + protocol.getDescriptor().getUniqueID() );
        }
    }

    /**
     * Get all the protocols loaded
     * 
     * @return
     */
    public Collection<Protocol> getProtocols() {
        return new HashSet<Protocol>( this.loadedProtocols.values() );
    }

    /**
     * Unload a protocol from the manager
     * 
     * @param protocol
     */
    public void removeProtocol( Protocol protocol ) {
        this.loadedProtocols.remove( protocol );
        //try {
        //    this.protocolStore.removeProtocol( protocol );
        //}
        //catch ( IOException e ) {
        //    logger.warning( "Failed to remove protocol " + protocol.getDescriptor().getName() + " from the protocol store" );
        //}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * is.lill.acre.protocol.IProtocolManager#getProtocolByDescriptor(is.lill
     * .acre.protocol.ProtocolDescriptor)
     */
    public Protocol getProtocolByDescriptor( ProtocolDescriptor pd ) {
        if ( this.loadedProtocols.containsKey( pd ) ) {
            return this.loadedProtocols.get( pd );
        }

        // not yet loaded, but I have a descriptor that I can use to read it
        else {
            for ( IProtocolSource ps : this.sources ) {
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

    public Set<Protocol> getProtocolsMatchingDescriptor( ProtocolDescriptor pd ) {
        Set<Protocol> toReturn = new HashSet<Protocol>();

        for ( Protocol p : this.loadedProtocols.values() ) {
            if ( p.getDescriptor().matches( pd ) ) {
                toReturn.add( p );
            }
        }
        return toReturn;
    }

    public synchronized void addRepository( IRepository repository ) throws RepositoryException {

        if ( this.loadedRepositoryNames.contains( repository.getBase() ) ) {
            logger.info( repository.getBase() + " already loaded: skipping" );
        }
        else {
            this.loadedRepositoryNames.add( repository.getBase() );
            logger.info( "Adding repository: " + repository.getBase() );

            for ( IProtocolSource source : repository.getSources() ) {
                this.sources.add( source );
            }
            for ( IProtocolSource source : repository.getSources() ) {
                try {
                    readSource( source );
                    logger.info( "Added Source: " + source.getDescription() );
                }
                catch ( IOException e ) {
                    logger.severe( "Exception when reading from Protocol Source: " + e );
                }
            }

        }
    }
}