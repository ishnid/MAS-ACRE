package is.lill.acre.event;

import is.lill.acre.protocol.Protocol;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ProtocolAddedEvent extends AbstractProtocolEvent {

    private static final Logger logger = Logger.getLogger( ProtocolAddedEvent.class.getName() );
    static {
        logger.setLevel( Level.OFF );
    }

    public ProtocolAddedEvent( Protocol p ) {
        super( p );
        logger.info( "Protocol added: " + p.getDescriptor().getName() );
    }
}
