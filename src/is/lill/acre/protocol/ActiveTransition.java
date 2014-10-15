package is.lill.acre.protocol;

import is.lill.acre.conversation.Conversation;
import is.lill.acre.logic.Bindings;
import is.lill.acre.logic.ITermParser;
import is.lill.acre.logic.MalformedTermException;
import is.lill.acre.logic.Term;
import is.lill.acre.logic.Utilities;
import is.lill.acre.message.IACREMessage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class representing a transition between two {@link State} objects. Each Transition
 * describes a message that must be passed in order to trigger a state change.
 * 
 * @author daithi
 * @version $Id: Transition.java 515 2010-06-14 14:51:16Z daithi $
 */
public class ActiveTransition {

   private static Logger logger = Logger.getLogger( ActiveTransition.class.getName() );

   private Transition parentTransition;
   // these are the only fields that can be affected by bindings
   private Term sender;
   private Term receiver;
   private Term content;

   static {
      logger.setLevel( Level.WARNING );
   }

   /**
    * Private constructor to allow Transitions to be created that are not
    * automatically linked with the start and end states This is designed to
    * create Transitions that already have bindings applied to them, but would
    * otherwise duplicate transitions between states.
    */
   protected ActiveTransition() {}

   protected void setSender( Term sender ) {
      this.sender = sender;
   }
   protected void setReceiver( Term receiver ) {
      this.receiver = receiver;
   }
   protected void setContent( Term content ) {
      this.content = content;
   }
   protected void setConversation( Conversation conversation ) {
   }
   protected void setParentTransition( Transition transition ) {
      this.parentTransition = transition;
   }
   
   /**
    * Create a new Transition with the supplied bindings applied. Has the effect
    * of cloning this object and applying any relevant variable bindings.
    * 
    * @param bindings
    *           Bindings to apply to this transition.
    * @return A clone of this Transition with the given bindings applied.
    */
   public ActiveTransition( Conversation conversation, Transition transition, Bindings bindings ) {
      
      logger.info( "Bindings for new transition: " + bindings );
      
      this.parentTransition = transition;
      this.receiver = transition.getReceiver().applyBindings( bindings );
      
      this.sender = transition.getSender().applyBindings( bindings );
      logger.info( "Sender: " + transition.getSender().toDebuggingString() + " => " + this.sender.toDebuggingString() );
      this.content = transition.getContent().applyBindings( bindings );
   }

   public boolean isTriggeredBy( IACREMessage message ) {
      /*
       * Sender and Recipient can only be single terms so using the
       * ITerm.matches() method is far too much overkill for this. Either the
       * sender/recipient is a variable or else it's a constant string that is
       * the same as the string provided in the message
       * 
       * In other words, there's no point in parsing the sender/recipient
       * contained in the message as an Term until we're actually matching it to
       * bind variables.
       */

      // false if sender has been set and doesn't match: the functor is the
      // constant value as a string
      // (formatting only becomes necessary for predicates and functions)
      if ( this.sender.isConstant() && ! this.sender.getFunctor().equals( message.getSender().getName() ) ) {
         logger.info( "Sender mismatch: " + this.sender.getFunctor() + " v. " + message.getSender().getName() );
         return false;
      }
      else {
         logger.info( "Sender matched: " +  this.sender.getFunctor() + " matched " + message.getSender().getName() );
      }

      // false if recipient has been set and doesn't match
      if ( this.receiver.isConstant() && !this.receiver.getFunctor().equals( message.getReceiver().getName() ) ) {
         logger.info( "Recipient mismatch" );
         return false;
      }

      // performatives don't match: this can't be a variable so it's a
      // straightforward String comparison
      if ( !this.parentTransition.getPerformative().equalsIgnoreCase( message.getPerformative() ) ) {
         logger.info( "Performative mismatch: " + this.parentTransition.getPerformative() + " != " + message.getPerformative() );
         return false;
      }

      // message content doesn't match description
      ITermParser contentParser = Utilities.getTermParser( message.getLanguage() );
      if ( contentParser != null ) {
         try {
            Term messageContent = contentParser.parse( message.getContent() );

            if ( !this.content.matches( messageContent ) ) {
               logger.info( "Content mismatch: " + this.content.toDebuggingString() + " != " + message.getContent() );
               return false;
            }
            else {
               logger.info( "Content match: " + this.content.toDebuggingString() + " matched " + message.getContent() );
            }
         }
         catch ( MalformedTermException e ) {
            logger.warning( "Content could not be parsed: " + e );
            return false;
         }
      }
      else {
         logger.severe( "Logic parser for " + message.getLanguage() + " could not be loaded" );
      }

      logger.info( "Message matched" );
      return true;
   }

   // TODO: this doesn't take into account the possibility of a variable being
   // used in multiple
   // fields and matching different values in each
   public Bindings getBindings( IACREMessage message ) {
      Bindings toReturn = new Bindings();

      try {
         if ( this.sender.isVariable() ) {
            toReturn.addBinding( this.sender, Utilities.getTermParser( "acre" ).parse( message.getSender().getName() ) );
         }
         if ( this.receiver.isVariable() ) {
            toReturn.addBinding( this.receiver, Utilities.getTermParser( "acre" ).parse( message.getReceiver().getName() ) );
         }
         this.content.getBindings( Utilities.getTermParser( message.getLanguage() ).parse( message.getContent() ) );
      }
      catch ( MalformedTermException e ) {
         logger.severe( "Bindings could not be created: " + e );
      }
      return toReturn;
   }

   public State getEndState() {
      return this.parentTransition.getEndState();
   }
   
   public State getStartState() {
       return this.parentTransition.getStartState();
   }
   
   public Transition getParent() {
       return this.parentTransition;
   }
}
