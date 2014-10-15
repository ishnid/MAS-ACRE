package is.lill.acre.message;

import is.lill.acre.protocol.ProtocolDescriptor;
import is.lill.acre.protocol.Transition;

/**
 * Simple class to represent an ACL message Content is stored as a raw string -
 * it will be parsed by ACRE using an appropriate ITermParser. The appropriate
 * parser is identified using the {@code getLanguage()} method of this class
 * 
 * @author daithi
 * @version $Id$
 */
public class ACREMessage implements IACREMessage {

	private int status = UNPROCESSED_STATUS;
    private String content;
    private String cid;
    private ProtocolDescriptor pid;
    private String performative;
    private IACREAgentIdentifier sender;
    private IACREAgentIdentifier receiver;
    private String language;
    private String replyWith;
    private String inReplyTo;
    private Long replyBy;
    private Transition triggered;

    /**
     * Get the value in the reply-by field, expressed as a Unix timestamp
     * @return The timestamp associated with the field.
     */
    public Long getReplyBy() {
        return this.replyBy;
    }
    
    public void setTriggered( Transition t ) {
        this.triggered = t;
    }
    
    public Transition getTriggered() {
        return this.triggered;
    }

    public void setReplyBy( Long replyBy ) {
        this.replyBy = replyBy;
    }

    @Override
    public String getContent() {
        return this.content == null ? "" : this.content;
    }

    public void setContent( String content ) {
        this.content = content;
    }

    @Override
    public String getConversationIdentifier() {
        return this.cid;
    }

    public void setConversationIdentifier( String cid ) {
        this.cid = cid;
    }

    @Override
    public String getPerformative() {
        return this.performative.toLowerCase();
    }

    public void setPerformative( String p ) {
        this.performative = p.toLowerCase();
    }

    @Override
    public ProtocolDescriptor getProtocol() {
        return this.pid;
    }

    public void setProtocol( ProtocolDescriptor protocol ) {
        this.pid = protocol;
    }

    @Override
    public IACREAgentIdentifier getReceiver() {
        return this.receiver;
    }

    public void setReceiver( IACREAgentIdentifier receiver ) {
        this.receiver = receiver;
    }

    @Override
    public IACREAgentIdentifier getSender() {
        return this.sender;
    }

    public void setSender( IACREAgentIdentifier sender ) {
        this.sender = sender;
    }

    @Override
    public String getLanguage() {
        return this.language;
    }

    public void setLanguage( String language ) {
        this.language = language;
    }

    @Override
    public String toString() {
        StringBuffer toReturn = new StringBuffer();
        toReturn.append( "performative: " ).append( this.getPerformative() ).append( ", sender: " ).append( this.getSender().getName() ).append( ", receiver: " ).append( this.getReceiver().getName() ).append( ", conversation: " ).append( this.getConversationIdentifier() ).append( ", protocol: " ).append( this.getProtocol() ).append( ", content: " ).append( this.getContent() ).append( ", language: " ).append( this.getLanguage() );
        return toReturn.toString();
    }

    public void setReplyWith( String replyWith ) {
        this.replyWith = replyWith;
    }

    public String getReplyWith() {
        return replyWith;
    }

    public void setInReplyTo( String inReplyTo ) {
        this.inReplyTo = inReplyTo;
    }

    public String getInReplyTo() {
        return inReplyTo;
    }

    public void setStatus(int status) {
    	this.status = status;
    }
    
	@Override
	public int getStatus() {
		return this.status;
	}
}
