package is.lill.acre.protocol;

import is.lill.acre.message.IACREMessage;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to represent a communication protocol as a finite state machine.
 * 
 * @author daithi
 * @version $Id: Protocol.java 515 2010-06-14 14:51:16Z daithi $
 */
public class Protocol {

   private static final Logger logger = Logger.getLogger( Protocol.class.getName() );

   static {
      logger.setLevel( Level.OFF );
   }

   private Map<String, State> states;
   private Set<Transition> transitions;
   private ProtocolDescriptor descriptor;
   private State initialState;
   private ProtocolDescriptor imported;
   private String description;

   /**
    * Create protocol with specified name
    * 
    * @param name
    *           The protocol's name
    */
   public Protocol( ProtocolDescriptor descriptor ) {
      this();
      this.setDescriptor( descriptor );
   }

   public void setDescriptor( ProtocolDescriptor descriptor ) {
      this.descriptor = descriptor;
   }

   public ProtocolDescriptor getDescriptor() {
      return this.descriptor;
   }
   
   public String getDescription() {
     return this.description;
   }

   public void setDescription( String description ) {
     this.description = description;
   }
   
   /**
    * Default constructor to create an empty protocol
    */
   public Protocol() {
      this.states = new HashMap<String, State>();
      this.transitions = new HashSet<Transition>();
      this.descriptor = ProtocolDescriptor.parseString( "default_newprotocol_1.0" );
   }

   /**
    * Add a {@link State} to this Protocol
    * 
    * @param state
    *           The {@link State to add}
    */
   public void addState( State state ) {
      this.states.put( state.getName(), state );

      logger.info( "Adding state: " + state.getName() );
   }

   public void addTransition( Transition transition ) {
      this.transitions.add( transition );
   }

   public void removeTransition( Transition transition ) {
      this.transitions.remove( transition );
      transition.getStartState().removeOutTransition( transition );
      transition.getEndState().removeInTransition( transition );
   }

   /**
    * Remove a State from the transition. Will also remove all related
    * protocols.
    * 
    * @param state
    */
   public void removeState( State state ) {
      Set<Transition> toRemove = new HashSet<Transition>();
      toRemove.addAll( state.getInTransitions() );
      toRemove.addAll( state.getOutTransitions() );

      for ( Transition t : toRemove ) {
         this.removeTransition( t );
         logger.info( "Removing transition" );
      }
      this.states.remove( state.getName() );
   }

   public State getInitialState() {
      // no initial state previously set
      if ( this.initialState == null ) {

         // loop through all states looking for an initial state
         for ( State s : this.states.values() ) {
            if ( s.isInitialState() ) {

               // this is an initial state but one has already been found
               if ( initialState != null ) {
                  logger.warning( "Attempt to use multiple initial states in protocol " + this.getDescriptor().getName() );
               }
               this.initialState = s;
            }
         }
         // after looping, we still have no initial state
         if ( this.initialState == null ) {
            logger.severe( "Protocol has no initial state" );
         }
      }

      return this.initialState;
   }

   public String toString() {
      return this.getDescriptor().getName();
   }

   public String toDebuggingString() {
      StringBuilder toReturn = new StringBuilder();
      toReturn.append( "Protocol name: " + this.getDescriptor().getName() + "\n" );
      toReturn.append( "Namespace: " + this.getDescriptor().getNamespace() + "\n" );
      toReturn.append( "Version: " + this.getDescriptor().getVersion() + "\n" );
      toReturn.append( "States:\n" );
      for ( State s : this.states.values() ) {
         toReturn.append( s.toString() + "\n" );
      }
      toReturn.append( "Transitions:\n" );
      for ( Transition t : this.transitions ) {
         toReturn.append( t.toString() + "\n" );
      }

      return toReturn.toString();
   }

   public State getStateByName( String name ) {
      return this.states.get( name );
   }

   /**
    * Retrieve the set of transitions associated with this protocol
    * 
    * @return
    */
   public Set<Transition> getTransitions() {
      return this.transitions;
   }

   public Collection<State> getStates() {
      return this.states.values();
   }

   public boolean equals( Object o ) {
      if ( o instanceof Protocol ) {
         return this.getDescriptor().equals( ( (Protocol) o ).getDescriptor() );
      }
      return false;
   }

   public int hashCode() {
      return this.getDescriptor().hashCode();
   }



   public boolean isInitiatedBy( IACREMessage m ) {
      logger.info( "Checking if " + this.getDescriptor().getName() + " can be started" );
      logger.info( "Initial state is " + this.getInitialState() );
      if ( m.getProtocol() != null && ! m.getProtocol().equals( this.getDescriptor() ) ) {
          return false;
      }
      for ( Transition t : this.initialState.getOutTransitions() ) {
         if ( t.isTriggeredBy( m ) ) {
            return true;
         }
      }
      return false;
   }

   public ProtocolDescriptor getImport() {
      return this.imported;
   }

   public void importProtocol( Protocol p ) {
      this.imported = p.getDescriptor();

      Protocol toImport = p.clone();
      for ( State s : toImport.getStates() ) {
         s.setImported( true );
         this.addState( s );
      }
      for ( Transition t : toImport.getTransitions() ) {
         t.setImported( true );
         this.addTransition( t );
      }
   }

   public Protocol clone() {
      Protocol toReturn = new Protocol();
      toReturn.setDescriptor( getDescriptor().clone() );
      if ( getDescription() != null ) {
        toReturn.setDescription( getDescription() );
      }
      for ( State s : this.getStates() ) {
         if ( !s.isImported() ) {
            toReturn.addState( new State( s.getName() ) );
         }
      }
      for ( Transition t : this.getTransitions() ) {
         if ( !t.isImported() ) {
            Transition n = new Transition( toReturn.getStateByName( t.getStartState().getName() ), toReturn.getStateByName( t.getEndState().getName() ) );
            n.setContent( t.getContent() == null ? null : t.getContent().clone() );
            n.setSender( t.getSender().clone() );
            n.setReceiver( t.getReceiver().clone() );
            n.setPerformative( t.getPerformative() );
            toReturn.addTransition( n );
         }
      }

      return toReturn;

   }
}
