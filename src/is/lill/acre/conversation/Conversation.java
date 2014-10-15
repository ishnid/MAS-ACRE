package is.lill.acre.conversation;

import is.lill.acre.event.ConversationAdvancedEvent;
import is.lill.acre.event.ConversationEndedEvent;
import is.lill.acre.event.ConversationStartedEvent;
import is.lill.acre.logic.Bindings;
import is.lill.acre.message.ACREMessage;
import is.lill.acre.message.IACREAgentIdentifier;
import is.lill.acre.message.IACREMessage;
import is.lill.acre.protocol.ActiveTransition;
import is.lill.acre.protocol.Protocol;
import is.lill.acre.protocol.State;
import is.lill.acre.protocol.Transition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Conversation extends Observable {

    private static final Logger logger = Logger.getLogger( Conversation.class.getName() );

    static {
        logger.setLevel( Level.WARNING );
    }

    // the underlying protocol
    private Protocol protocol;

    // the participants in this conversation
    private Set<IACREAgentIdentifier> participants = new HashSet<IACREAgentIdentifier>();

    // the manager managing this conversation - needed for consultation on
    // conversation ids
    private ConversationManager manager;

    // the bindings associated with this conversation
    private Bindings bindings = new Bindings();

    // current conversation state
    private State currentState;

    // the transitions that are currently available from the current state
    private Set<ActiveTransition> activeTransitions = new HashSet<ActiveTransition>();

    // the unique ID to identify this conversation
    private String conversationIdentifier;

    // has the conversation been archived
    private boolean archived = false;

    private ConversationStatus status;

    // previous messages that have been part of this conversation
    private List<IACREMessage> history = new ArrayList<IACREMessage>();

    // don't enforce timeouts by default
    private long timeout = 0;

    /**
     * Constructor
     * 
     * @param p
     *            The underlying {@link Protocol}
     * @param manager
     *            The {@link ConversationManager} tasked with managing this
     *            conversation
     */
    public Conversation( Protocol p, ConversationManager manager ) {
        this( p );
        this.manager = manager;
        this.addObserver( manager );

    }

    public Conversation( Protocol p ) {
        logger.info( "Creating new conversation for protocol: " + p.getDescriptor().getName() );
        this.protocol = p;
        this.currentState = p.getInitialState();
        generateActiveTransitions();
        this.status = ConversationStatus.READY;
    }

    public void setArchived( boolean archived ) {
        this.archived = archived;
    }

    public boolean isArchived() {
        return this.archived;
    }

    /**
     * Get the number of messages that have been sent as part of this
     * conversation
     * 
     * @return The number of messages
     */
    public int getLength() {
        return this.history.size();
    }

    public void setTimeout( long timeout ) {
        logger.info( "Setting conversation timeout for " + this.getConversationIdentifier() + " to " + timeout );
        this.timeout = timeout;
    }

    public boolean timeout( long timeout ) {
        logger.info( "Checking timeout: " + this.timeout + " v. " + timeout );
        return this.timeout > 0 && this.timeout < timeout;
    }

    // Method to generate the transitions from the current state. Should be
    // called whenever there is
    // a change in state - creates transitions based on the transitions in the
    // underlying Protocol
    // and the current conversation bindings
    private void generateActiveTransitions() {
        // clear old ones
        activeTransitions.clear();

        // add new ones
        for ( Transition pt : this.currentState.getOutTransitions() ) {
            activeTransitions.add( new ActiveTransition( this, pt, this.bindings ) );
        }
    }

    /**
     * Determine whether the given message is capable of advancing this
     * conversation.
     * 
     * @param message
     *            A message against which to match the conversation's active
     *            transitions
     * @return {@code true} if the conversation can be advanced by the given
     *         message, {@code false} otherwise
     */
    public boolean advancedBy( IACREMessage message ) {

        logger.info( "Checking if Conversation in state " + this.getState().getName() + " is advanced by message" );
        logger.info( "-> Conversation ID: " + message.getConversationIdentifier() + " v. " + this.getConversationIdentifier() );
        logger.info( "-> Protocol ID: " + message.getProtocol() + " v. " + this.getProtocol().getDescriptor() );

        // message either has no conversation identifier
        // or it has one that matches this conversation
        if ( ( message.getConversationIdentifier() == null || this.getConversationIdentifier() == null || message.getConversationIdentifier().equals( this.conversationIdentifier ) ) && ( message.getProtocol() == null || message.getProtocol().equals( this.protocol.getDescriptor() ) ) ) {

            logger.info( "-> Matched Protocol/Conversation IDs: checking for available transitions" );

            // try active transitions to see if it triggers any
            for ( ActiveTransition ct : activeTransitions ) {
                if ( ct.isTriggeredBy( message ) ) {
                    logger.info( "-> Success (state: " + getState().getName() + "), (transition from " + ct.getStartState().getName() + " to " + ct.getEndState().getName() + ")" );
                    // it does
                    return true;
                }
            }
            if ( message.getConversationIdentifier() != null ) {
                logger.warning( "-> Failed to match any transitions but did match protocol/conversation ids: " + message.getPerformative() + "(" + message.getContent() + ")" );
            }
        }
        else {
            logger.info( "-> Conversation IDs don't match" );
            return false;
        }

        // either the message has a conversation identifier for another
        // conversation, a protocol identifier for another protocol
        // or it doesn't match any active transition
        logger.info( "-> No transitions match" );
        return false;
    }

    public void setStatus( ConversationStatus status ) {
        this.status = status;
    }

    public ConversationStatus getStatus() {
        return this.status;
    }

    public void setConversationIdentifier( String id ) {
        this.conversationIdentifier = id;
    }

    /**
     * Advance this conversation by matching one of its transitions against the
     * given message
     * 
     * @param message
     *            A message to trigger a transition TODO: this assumes only one
     *            transition matches
     */
    public boolean advance( IACREMessage message ) {
        for ( ActiveTransition ct : activeTransitions ) {
            if ( ct.isTriggeredBy( message ) ) {

                // this was an initial state so the conversation is only
                // starting
                // so we'll need a conversation ID
                if ( this.currentState.isInitialState() ) {

                    // the message had a conversation id already, so use that
                    if ( message.getConversationIdentifier() != null ) {
                        this.setConversationId( message.getConversationIdentifier() );
                        logger.info( "Setting conversation ID from message: " + this.getConversationIdentifier() );
                    }

                    // no conversation id in the message
                    else {
                        this.setConversationId( this.manager.getNextConversationId() );
                        logger.info( "Auto-generating conversation ID: " + this.getConversationIdentifier() );
                    }

                    // add the participants in the conversation
                    this.participants.add( message.getSender() );
                    this.participants.add( message.getReceiver() );

                    // conversation is now active
                    this.setStatus( ConversationStatus.ACTIVE );

                    // raise an event that this conversation has begun
                    setChanged();
                    notifyObservers( new ConversationStartedEvent( this ) );
                }

                // get the bindings from triggering this transition
                this.bindings.update( ct.getBindings( message ) );

                logger.info( "Bindings generated: " + ct.getBindings( message ) );

                // change the current state
                this.currentState = ct.getEndState();

                // load the active transitions (with current bindings applied)
                this.generateActiveTransitions();

                // raise event that the conversation has advanced
                setChanged();
                notifyObservers( new ConversationAdvancedEvent( message, this, this.getCurrentState() ) );

                // conversation is now finished, so change the status
                // and raise the appropriate event
                if ( this.currentState.isTerminalState() ) {
                    this.setStatus( ConversationStatus.FINISHED );
                    setChanged();
                    notifyObservers( new ConversationEndedEvent( this ) );
                }
                else {
                    this.setStatus( ConversationStatus.ACTIVE );
                }

                logger.info( "Advanced conversation " + this.getConversationIdentifier() + " to state " + this.getState().getName() );

                this.history.add( message );
                ( (ACREMessage) message ).setTriggered( ct.getParent() );

                if ( message.getReplyBy() != null ) {
                    this.setTimeout( message.getReplyBy() );
                }
                else {
                    this.setTimeout( 0 );

                }

                return true;
            }
        }
        return false;
    }

    /**
     * Give this conversation a new conversation ID
     * 
     * @param id
     *            The conversation ID
     */
    private void setConversationId( String id ) {
        this.conversationIdentifier = id;
    }

    /**
     * Get the unique Conversation ID associated with this conversation
     * 
     * @return The conversation id
     */
    public String getConversationIdentifier() {
        return this.conversationIdentifier;
    }

    /**
     * Get the participants in this conversation
     * 
     * @return A set of the agent ids associated with the conversation
     *         participants.
     */
    public Set<IACREAgentIdentifier> getParticipants() {
        return this.participants;
    }

    public void addParticipant( IACREAgentIdentifier id ) {
        this.participants.add( id );
    }

    /**
     * Get the current state of this conversation
     * 
     * @return The current state of the conversation
     */
    public State getCurrentState() {
        return this.currentState;
    }

    /**
     * Get the underlying protocol behind this conversation
     * 
     * @return
     */
    public Protocol getProtocol() {
        return this.protocol;
    }

    public State getState() {
        return this.currentState;
    }

    public IACREAgentIdentifier getInitiator() {
        if ( !this.history.isEmpty() ) {
            return this.history.get( 0 ).getSender();
        }
        return null;
    }

    @Override
    public Conversation clone() {
        Conversation toReturn = new Conversation( protocol );
        toReturn.status = status;
        toReturn.currentState = currentState;
        toReturn.history = new ArrayList<IACREMessage>( history );
        toReturn.participants = participants;
        toReturn.bindings = bindings.clone();
        toReturn.conversationIdentifier = conversationIdentifier;
        toReturn.timeout = timeout;
        return toReturn;
    }

    public List<IACREMessage> getHistory() {
        return this.history;
    }
}
