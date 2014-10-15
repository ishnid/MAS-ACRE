package is.lill.acre.event;

import is.lill.acre.event.ACREEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ACREEventObserver implements Observer {

   private static final Logger logger = Logger.getLogger( ACREEventObserver.class.getName() );
   
   private List<ACREEvent> events = new LinkedList<ACREEvent>();
   
   static {
      logger.setLevel( Level.WARNING );
   }
   
   @Override
   public void update( Observable observable, Object event ) {
      if ( event instanceof ACREEvent ) {
         logger.info( "ProtocolManager Event: " + event );
         this.events.add( (ACREEvent) event );
      }
      else {
         logger.warning( "Unrecognised event: " + event.toString() );
      }
   }
   
   public synchronized ACREEvent consumeEvent() {
      return this.events.isEmpty() ? null : this.events.remove( 0 );
   }
}
