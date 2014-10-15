package is.lill.acre.conversation;

import is.lill.acre.event.AmbiguousMessageEvent;
import is.lill.acre.event.ConversationAdvancedEvent;
import is.lill.acre.event.ConversationCancelConfirmEvent;
import is.lill.acre.event.ConversationCancelFailEvent;
import is.lill.acre.event.ConversationCancelRequestEvent;
import is.lill.acre.event.ConversationEndedEvent;
import is.lill.acre.event.ConversationFailedEvent;
import is.lill.acre.event.ConversationStartedEvent;
import is.lill.acre.event.ConversationTimeoutEvent;
import is.lill.acre.event.IConversationEvent;
import is.lill.acre.event.ProtocolAddedEvent;
import is.lill.acre.event.UnmatchedMessageEvent;
import is.lill.acre.group.GroupReasoner;
import is.lill.acre.message.ACREMessage;
import is.lill.acre.message.IACREAgentIdentifier;
import is.lill.acre.message.IACREMessage;
import is.lill.acre.message.Performative;
import is.lill.acre.protocol.Protocol;
import is.lill.acre.protocol.ProtocolDescriptor;
import is.lill.acre.protocol.ProtocolManager;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for managing the conversations of one agent
 * 
 * @author daithi
 * @version $Id$
 * 
 */
public class ConversationManager extends Observable implements Observer {

    private static final Logger logger = Logger.getLogger( ConversationManager.class.getName() );

    static {
        logger.setLevel( Level.WARNING );
    }

    // not yet fully (?) implemented
    private AddressBook ab = new AddressBook();

    // group reasoner
    private GroupReasoner gr;

    // track the protocolmanager associated with this
    private ProtocolManager pm;

    // each conversation manager is owned by an agent
    // this is required to identify which party to a message
    // is the owner and which is the other participant in the
    // conversation
    private IACREAgentIdentifier ownerId;

    // we need to be able to look up protocol by their protocol ids when
    // initiating conversations
    private Map<ProtocolDescriptor, Protocol> protocols = new HashMap<ProtocolDescriptor, Protocol>();

    // need to record current conversations so we can look them up by
    // conversation ID (when this is provided)
    // and the agent id of the other participant (if conversation id is not
    // provided)
    private Map<String, Set<Conversation>> activeByParticipantId = new HashMap<String, Set<Conversation>>();

    // no particular lookups required for completed conversations - historical
    // reference only
    // private Set<Conversation> finished = new HashSet<Conversation>();

    // store all conversations regardless of status - key is Conversation
    // Identifier
    private Map<String, Conversation> allConversations = new HashMap<String, Conversation>();

    private Map<ConversationStatus, Map<String, Conversation>> byStatus = new HashMap<ConversationStatus, Map<String, Conversation>>();
    // private Map<String, Conversation> cancellingByConversationId = new
    // HashMap<String, Conversation>();
    // private Map<String, Conversation> activeByConversationId = new
    // HashMap<String, Conversation>();

    // counter for generating conversation ids by this agent
    private int counter = 0;

    private Map<Conversation, Integer> timeouts = new HashMap<Conversation, Integer>();

    /**
     * 
     */
    public ConversationManager() {
        for ( ConversationStatus cs : ConversationStatus.values() ) {
            this.byStatus.put( cs, new HashMap<String, Conversation>() );
        }
    }

    public void setOwner( IACREAgentIdentifier ownerId ) {
        this.ownerId = ownerId;
    }

    public void doTimeouts() {
        Date now = new Date();
        for ( Conversation c : this.allConversations.values() ) {
            if ( c.getStatus() == ConversationStatus.ACTIVE ) {
                if ( c.timeout( now.getTime() ) ) {
                    changeStatus( c, ConversationStatus.FAILED );
                    setChanged();
                    notifyObservers( new ConversationTimeoutEvent( c ) );
                }
            }
        }
    }

    public Map<String, Conversation> getAllConversations() {
        return this.allConversations;
    }

    public Conversation newConversation( ProtocolDescriptor pd, IACREAgentIdentifier participant ) {
        Conversation toAdd = new Conversation( pm.getProtocolByDescriptor( pd ), this );
        toAdd.setConversationIdentifier( this.getNextConversationId() );
        toAdd.addParticipant( participant );
        byStatus.get( toAdd.getStatus() ).put( toAdd.getConversationIdentifier(), toAdd );
        allConversations.put( toAdd.getConversationIdentifier(), toAdd );
        if ( !activeByParticipantId.containsKey( participant ) ) {
            activeByParticipantId.put( participant.getName(), new HashSet<Conversation>() );
        }
        activeByParticipantId.get( participant.getName() ).add( toAdd );
        return toAdd;
    }

    @Override
    public void update( Observable arg0, Object arg1 ) {

        if ( arg0 instanceof ProtocolManager ) {
            if ( arg1 instanceof ProtocolAddedEvent ) {
                Protocol p = ( (ProtocolAddedEvent) arg1 ).getProtocol();
                this.newProtocol( p );
            }
            else {
                logger.info( "Unrecognised protocol event detected" );
            }
        }

        // event received from a conversation
        else if ( arg0 instanceof Conversation ) {

            // it's the beginning of a conversation
            if ( arg1 instanceof ConversationStartedEvent ) {
                Conversation c = ( (IConversationEvent) arg1 ).getConversation();
                changeStatus( c, ConversationStatus.ACTIVE );
                allConversations.put( c.getConversationIdentifier(), c );

                IACREAgentIdentifier p = getOtherParticipant( c );

                if ( !activeByParticipantId.containsKey( p.getName() ) ) {
                    activeByParticipantId.put( p.getName(), new HashSet<Conversation>() );
                }
                activeByParticipantId.get( p.getName() ).add( c );

                // pass on the notification
                setChanged();
                notifyObservers( arg1 );
            }
            else if ( arg1 instanceof ConversationAdvancedEvent ) {
                // pass on the notification
                setChanged();
                notifyObservers( arg1 );
            }
            else if ( arg1 instanceof ConversationEndedEvent ) {
                Conversation c = ( (ConversationEndedEvent) arg1 ).getConversation();

                // remove from the current conversations
                // activeByParticipantId.get( getOtherParticipant( c ).getName()
                // ).remove( c );

                this.changeStatus( c, ConversationStatus.FINISHED );

                // pass on the notification
                setChanged();
                notifyObservers( arg1 );
            }
            else {
                logger.info( "Unrecognised conversation event detected" );
            }
        }
        else {
            logger.info( "Unrecognised event detected" );
        }
    }

    public void setTimeout( Conversation c, int timeout ) {
        timeouts.put( c, timeout );
    }

    public boolean hasTimeout( Conversation c ) {
        return timeouts.containsKey( c );
    }

    public int getTimeout( Conversation c ) {
        return timeouts.remove( c );
    }

    public void removeTimeout( Conversation c ) {
        timeouts.remove( c );
    }

    public void removeFinished() {
        Iterator<String> agentIterator = activeByParticipantId.keySet().iterator();
        while ( agentIterator.hasNext() ) {
            String aid = agentIterator.next();
            Iterator<Conversation> conversationIterator = activeByParticipantId.get( aid ).iterator();
            while ( conversationIterator.hasNext() ) {
                Conversation c = conversationIterator.next();
                if ( c.getStatus() == ConversationStatus.FINISHED ) {
                    conversationIterator.remove();
                }
            }
            if ( activeByParticipantId.get( aid ).isEmpty() ) {
                agentIterator.remove();
            }
        }
    }

    public void forget( String cid ) {
        Conversation toForget = allConversations.remove( cid );
        if ( toForget != null ) {
            byStatus.get( toForget.getStatus() ).remove( cid );
            this.activeByParticipantId.remove( this.getOtherParticipant( toForget ).getName() );

            if ( this.gr != null ) {
                this.gr.forget( toForget );
            }
        }
    }

    /**
     * From a conversation, find the participant that is not the agent that owns
     * this conversation manager.
     * 
     * @param c
     *            A conversation
     * @return The Agent id of the other participating agent in the conversation
     */
    public IACREAgentIdentifier getOtherParticipant( Conversation c ) {
        // find the participants
        for ( IACREAgentIdentifier aid : c.getParticipants() ) {
            if ( !aid.equals( this.ownerId ) ) {
                return aid;
            }
        }
        return null;
    }

    /**
     * Generate the next unique conversation id - concatenate the agent ID of
     * the owner of this manager to an incrementing counter
     * 
     * @return A uniquely generated conversation id
     */
    public String getNextConversationId() {
        return this.ownerId.getName() + "_" + counter++;
    }

    public void setProtocolManager( ProtocolManager pm ) {
        this.pm = pm;

        // add all known protocols
        for ( Protocol p : pm.getProtocols() ) {
            logger.info( "Adding new protocol: " + p.getDescriptor().getName() );
            this.newProtocol( p );
        }
        // monitor for future events
        this.pm.addObserver( this );
    }

    private void newProtocol( Protocol protocol ) {
        protocols.put( protocol.getDescriptor(), protocol );
    }

    public Map<String, Set<Conversation>> getActiveByParticipant() {
        return this.activeByParticipantId;
    }

    public Conversation getConversationByID( String identifier ) {
        return this.allConversations.get( identifier );
    }

    public boolean initiates( ProtocolDescriptor pd, IACREMessage m ) {
        return this.protocols.containsKey( pd ) && this.protocols.get( pd ).isInitiatedBy( m );
    }

    public IACREMessage cancel( String cid ) {
        Conversation c = this.getConversationByID( cid );
        if ( c != null && c.getStatus() == ConversationStatus.ACTIVE ) {

            ACREMessage m = new ACREMessage();
            m.setPerformative( Performative.CANCEL );
            m.setReceiver( this.getOtherParticipant( c ) );
            m.setSender( this.ownerId );
            m.setReplyWith( "cancel" );
            m.setLanguage( "AFAPL" );
            m.setConversationIdentifier( cid );
            m.setContent( "" );
            m.setProtocol( c.getProtocol().getDescriptor() );

            this.changeStatus( c, ConversationStatus.CANCELLING );

            return m;
        }
        return null;
    }

    public IACREMessage confirmCancel( String cid ) {
        Conversation c = this.getConversationByID( cid );
        if ( c != null && c.getStatus() == ConversationStatus.CANCELLING ) {

            ACREMessage m = new ACREMessage();
            m.setPerformative( Performative.INFORM );
            m.setReceiver( this.getOtherParticipant( c ) );
            m.setSender( this.ownerId );
            m.setInReplyTo( "cancel" );
            m.setLanguage( "AFAPL" );
            m.setConversationIdentifier( cid );
            m.setContent( "" );
            m.setProtocol( c.getProtocol().getDescriptor() );

            this.changeStatus( c, ConversationStatus.CANCELLED );
            return m;
        }
        return null;
    }

    public IACREMessage failcancel( String cid ) {
        Conversation c = this.getConversationByID( cid );
        if ( c != null && c.getStatus() == ConversationStatus.CANCELLING ) {
            ACREMessage m = new ACREMessage();
            m.setPerformative( Performative.FAILURE );
            m.setReceiver( this.getOtherParticipant( c ) );
            m.setSender( this.ownerId );
            m.setInReplyTo( "cancel" );
            m.setLanguage( "AFAPL" );
            m.setConversationIdentifier( cid );
            m.setContent( "" );
            m.setProtocol( c.getProtocol().getDescriptor() );

            this.changeStatus( c, ConversationStatus.ACTIVE );
            return m;
        }
        return null;
    }

    /**
     * Process a message that has been sent/received This implements the Match,
     * Fail, New and Update stages of the formal model
     * 
     * @param m
     *            A message that has been sent/received
     * @return The conversation the message matches, or {@code null} if the
     *         message was either unmatched or ambiguous
     */
    public Conversation processMessage( ACREMessage m ) {
        Set<Conversation> candidates = new HashSet<Conversation>();

        // Per the formal semantics, the Start and Inialise states come first.
        // But this method is the Match/Fail/New/Update stages.
        // some shortcuts taken and are noted

        // firstly, if it has a conversation identifier that we're already aware
        // of, checking that first will save some time
        // we already know that no other active conversations will have the same
        // conversation identifier, so although this deviates from the formal
        // model, it has the same effect.
        if ( m.getConversationIdentifier() != null ) {

            logger.info( "Message has conversation identifier: " + m.getConversationIdentifier() );

            Conversation candidate = this.getConversationByID( m.getConversationIdentifier() );

            if ( candidate != null ) {

                logger.info( "Conversation ID in message matched an active conversation" );

                /**
                 * Handle cancellations - don't return a candidate for these,
                 * but raise the appropriate events
                 */
                if ( m.getPerformative().equals( Performative.CANCEL ) ) {
                    this.changeStatus( candidate, ConversationStatus.CANCELLING );
                    setChanged();
                    notifyObservers( new ConversationCancelRequestEvent( m, candidate ) );
                    return null;
                }
                else if ( m.getInReplyTo() != null && m.getInReplyTo().equals( "cancel" ) ) {
                    if ( m.getPerformative().equals( Performative.INFORM ) ) {
                        logger.info( "Received confirmation of cancellation" );
                        this.changeStatus( candidate, ConversationStatus.CANCELLED );
                        setChanged();
                        notifyObservers( new ConversationCancelConfirmEvent( m, candidate ) );
                    }
                    else if ( m.getPerformative().equals( Performative.FAILURE ) ) {
                        logger.info( "Received failure of cancellation" );
                        this.changeStatus( candidate, ConversationStatus.ACTIVE );
                        setChanged();
                        notifyObservers( new ConversationCancelFailEvent( m, candidate ) );
                    }
                    return null;
                }
                else if ( candidate.getStatus() == ConversationStatus.READY && candidate.advancedBy( m ) ) {
                    logger.info( "Ready conversation " + candidate.getConversationIdentifier() + " is a candidate" );
                    candidates.add( candidate );
                }
                else if ( candidate.getStatus() == ConversationStatus.ACTIVE && candidate.advancedBy( m ) ) {
                    logger.info( "Active conversation " + candidate.getConversationIdentifier() + " is a candidate" );
                    candidates.add( candidate );
                }
                else {
                    // this is the "Fail" stage, which is not called in the same
                    // order
                    // as in the
                    // formal model
                    candidate.setStatus( ConversationStatus.FAILED );

                    logger.info( "Unmatched outgoing message: " + m.toString() );

                    // pass on the event
                    setChanged();
                    notifyObservers( new ConversationFailedEvent( m, candidate ) );
                }
            }
        }

        // no conversation identifier, so we need to check all active
        // conversations
        if ( m.getConversationIdentifier() == null ) {
            // This is the "Match" stage of the formal model
            for ( Conversation c : this.byStatus.get( ConversationStatus.ACTIVE ).values() ) {
                if ( c.advancedBy( m ) ) {
                    logger.info( "Conversation " + c.getConversationIdentifier() + " is a candidate" );
                    candidates.add( c );
                }
            }
        }

        // Next is the "New" stage - any protocols that could be initiated by
        // this?
        // create a new (unadvanced) conversation
        // don't do this for messages with conversation ids of active
        // conversations
        if ( m.getConversationIdentifier() == null || ( !this.byStatus.get( ConversationStatus.ACTIVE ).containsKey( m.getConversationIdentifier() ) && !this.byStatus.get( ConversationStatus.READY ).containsKey( m.getConversationIdentifier() ) ) ) {
            for ( Protocol p : this.protocols.values() ) {
                if ( p.isInitiatedBy( m ) ) {
                    logger.info( "New protocol " + p.getDescriptor() + " is a candidate" );
                    candidates.add( new Conversation( p, this ) );
                }
            }
        }

        // The "Update" state is next: check how many candidates there were and
        // act accordingly
        // only one candidate, so that is advanced
        if ( candidates.size() == 1 ) {
            for ( Conversation c : candidates ) {

                if ( this.hasTimeout( c ) ) {
                    logger.info( "Setting message timeout" );
                    m.setReplyBy( new Date().getTime() + this.getTimeout( c ) );
                }

                // I can even do this for new conversations, as I'm observing
                // this
                // conversation
                // When I get the message to say it's been started, update()
                // will be
                // called
                // automatically, so the conversation moves from unstarted to
                // active
                c.advance( m );
                m.setStatus( IACREMessage.MATCHED_STATUS );

                return c;
            }
        }
        // nothing matched: unmatched message
        else if ( candidates.isEmpty() ) {
            setChanged();
            notifyObservers( new UnmatchedMessageEvent( m ) );
            m.setStatus( IACREMessage.UNMATCHED_STATUS );
            logger.info( "Unmatched message: " + m.toString() );
        }

        // multiple matches: ambiguous message
        else {
            setChanged();
            notifyObservers( new AmbiguousMessageEvent( m ) );
            m.setStatus( IACREMessage.AMBIGUOUS_STATUS );
            logger.info( "Ambiguous message: " + m.toString() );
            logger.info( "Options were:" );
            for ( Conversation c : candidates ) {
                logger.info( "-> " + c.getProtocol().getDescriptor() );
            }
        }
        return null;
    }

    private void changeStatus( Conversation conversation, ConversationStatus newStatus ) {
        ConversationStatus oldStatus = conversation.getStatus();
        byStatus.get( oldStatus ).remove( conversation.getConversationIdentifier() );
        byStatus.get( newStatus ).put( conversation.getConversationIdentifier(), conversation );
        conversation.setStatus( newStatus );
    }

    public AddressBook getAddressBook() {
        return this.ab;
    }

    public void setGroupReasoner( GroupReasoner gr ) {
        this.gr = gr;
    }

    public GroupReasoner getGroupReasoner() {
        return gr;
    }
}
