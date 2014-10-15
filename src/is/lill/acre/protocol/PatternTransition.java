package is.lill.acre.protocol;

import is.lill.acre.logic.Term;

import java.util.regex.Pattern;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PatternTransition {

    private static Logger logger = Logger.getLogger( PatternTransition.class.getName() );
    static {
        logger.setLevel( Level.OFF );
    }

    private Pattern startState;
    private String endStateName;

    private String performative;
    private Term sender;
    private Term receiver;
    private Term content;

    @SuppressWarnings ( "unused")
    private PatternTransition() {}

    /**
     * Constructor
     * 
     * @param startState
     *            State from which this transition can be invoked
     * @param endState
     *            Resultant state from invoking this transition.
     */
    public PatternTransition( Pattern startStatePattern, String endStateName ) {
        logger.info( "Recording PatternTransition from " + startStatePattern.toString() + " to " + endStateName );

        // add the start state and add this to the state
        this.startState = startStatePattern;

        // add the end state and add this to the state
        this.endStateName = endStateName;
    }

    /**
     * Get the state that enables this transition to be invoked.
     * 
     * @return An appropriate {@link State} object.
     */
    public Pattern getStartStatePattern() {
        return this.startState;
    }

    /**
     * Get the state that results from invoking this transition.
     * 
     * @return An appropriate {@link State} object.
     */
    public String getEndStateName() {
        return this.endStateName;
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
        return "TRANSITION\n\tPerformative:\t[" + this.getPerformative() + "]\n" + "\tSTART:\t\t[" + this.getStartStatePattern().toString() + "]\n" + "\tEND:\t\t[" + this.getEndStateName() + "]\n" + "\tSENDER:\t\t[" + this.getSender() + "]\n" + "\tRECIPIENT:\t[" + this.getReceiver() + "]\n" + "\tBY:\t\t[" + this.getContent() + "]\n";
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
}