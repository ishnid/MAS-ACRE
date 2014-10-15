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
 * Representation of a transition that belongs to a protocol
 * 
 * @author daithi
 * @version $Id$
 * 
 */
public class Transition {

    // init logger
    private static Logger logger = Logger.getLogger( Transition.class.getName() );
    static {
        logger.setLevel( Level.OFF );
    }

    private State startState;
    private State endState;

    private String performative;
    private Term sender;
    private Term receiver;
    private Term content;

    private boolean imported = false;

    // constants for transition field values
    public static final String FROM_STATE = "from-state";
    public static final String TO_STATE = "to-state";
    public static final String PERFORMATIVE = "performative";
    public static final String SENDER = "sender";
    public static final String RECEIVER = "receiver";
    public static final String CONTENT = "content";

    @SuppressWarnings ( "unused")
    private Transition() {}

    public boolean isImported() {
        return this.imported;
    }

    public void setImported( boolean imported ) {
        this.imported = imported;
    }

    /**
     * Constructor
     * 
     * @param startState
     *            State from which this transition can be invoked
     * @param endState
     *            Resultant state from invoking this transition.
     */
    public Transition( State startState, State endState ) {
        logger.info( "Creating Transition from " + startState.getName() + " to " + endState.getName() );

        // add the start state and add this to the state
        this.startState = startState;
        startState.addOutTransition( this );

        // add the end state and add this to the state
        this.endState = endState;
        endState.addInTransition( this );
    }

    /**
     * Get the state that enables this transition to be invoked.
     * 
     * @return An appropriate {@link State} object.
     */
    public State getStartState() {
        return this.startState;
    }

    /**
     * Get the state that results from invoking this transition.
     * 
     * @return An appropriate {@link State} object.
     */
    public State getEndState() {
        return this.endState;
    }

    /**
     * Get the performative required in a message matching this transition.
     * 
     * @return The performative as a String.
     */
    public String getPerformative() {
        return this.performative;
    }

    /**
     * Set the performative required in a message matching this transition.
     * 
     * @param performative
     *            The performative as a String.
     */
    public void setPerformative( String performative ) {
        this.performative = performative;
    }

    /**
     * Create a String representation of this Transition.
     * 
     * @return String
     */
    @Override
    public String toString() {
        return "TRANSITION\n\tPerformative:\t[" + this.getPerformative() + "]\n" + "\tSTART:\t\t[" + this.getStartState().getName() + "]\n" + "\tEND:\t\t[" + this.getEndState().getName() + "]\n" + "\tSENDER:\t\t[" + this.getSender() + "]\n" + "\tRECIPIENT:\t[" + this.getReceiver() + "]\n" + "\tBY:\t\t[" + this.getContent() + "]\n";
    }

    /**
     * Set the name of the agent required to send the message matching this
     * transition.
     * 
     * @param sender
     *            The agent name that must match the sender in the message. May
     *            be parseable as a variable.
     */
    public void setSender( Term sender ) {
        this.sender = sender;
    }

    /**
     * Find details of the agent required to send the message matching this
     * Transition.
     * 
     * @return
     */
    public Term getSender() {
        return sender;
    }

    public void setReceiver( Term receiver ) {
        this.receiver = receiver;
    }

    public Term getReceiver() {
        return receiver;
    }

    /**
     * Specify the message content that would trigger this transition
     * 
     * @param by
     *            Message content (including variables)
     */
    public void setContent( Term content ) {
        this.content = content;
    }

    /**
     * Get the message content required to trigger this transition (including
     * variables).
     * 
     * @return Message content as an ITerm
     */
    public Term getContent() {
        return content;
    }

    /**
     * Create a new Transition with the supplied bindings applied. Has the
     * effect of cloning this object and applying any relevant variable
     * bindings.
     * 
     * @param bindings
     *            Bindings to apply to this transition.
     * @return An {@code ActiveTransition} based on this Transition with the
     *         given bindings applied.
     */
    public ActiveTransition activate( Conversation c, Bindings b ) {

        ActiveTransition toReturn = new ActiveTransition();
        toReturn.setParentTransition( this );
        toReturn.setConversation( c );
        toReturn.setReceiver( this.getReceiver().applyBindings( b ) );
        toReturn.setSender( this.getSender().applyBindings( b ) );
        toReturn.setContent( this.getContent().applyBindings( b ) );
        return toReturn;
    }

    public boolean isTriggeredBy( IACREMessage message ) {
        /*
         * Sender and Recipient can only be single terms so using the
         * ITerm.matches() method is far too much overkill for this. Either the
         * sender/recipient is a variable or else it's a constant string that is
         * the same as the string provided in the message
         * 
         * In other words, there's no point in parsing the sender/recipient
         * contained in the message as an Term until we're actually matching it
         * to bind variables.
         */

        // false if sender has been set and doesn't match: the functor is the
        // constant value as a string
        // (formatting only becomes necessary for predicates and functions)
        if ( this.sender.isConstant() && !this.sender.getFunctor().equals( message.getSender().getName() ) ) {
            logger.info( "Sender mismatch" );
            return false;
        }

        // false if recipient has been set and doesn't match
        if ( this.receiver.isConstant() && !this.receiver.getFunctor().equals( message.getReceiver().getName() ) ) {
            logger.info( "Recipient mismatch" );
            return false;
        }

        // performatives don't match: this can't be a variable so it's a
        // straightforward String comparison
        if ( !this.getPerformative().equalsIgnoreCase( message.getPerformative() ) ) {
            logger.info( "Performative mismatch: " + this.getPerformative() + " != " + message.getPerformative() );
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

    public void setStartState( State s ) {
        this.startState.removeOutTransition( this );
        this.startState = s;
        this.startState.addOutTransition( this );
    }

    public void setEndState( State s ) {
        this.endState.removeInTransition( this );
        this.endState = s;
        this.endState.addInTransition( this );
    }
}