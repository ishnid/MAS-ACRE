package is.lill.acre.message;

import is.lill.acre.protocol.ProtocolDescriptor;
import is.lill.acre.protocol.Transition;

/**
 * An interface to implements a standard FIPA ACL Message. It should be used by
 * specific implementations of the standard (i.e. string encoding, xml
 * encoding).
 * 
 * @author daithi
 * @version $Id$
 */
public interface IACREMessage {

    public final static int UNMATCHED_STATUS = 0;
    public final static int AMBIGUOUS_STATUS = 1;
    public final static int MATCHED_STATUS = 2;
    public final static int UNPROCESSED_STATUS = 3;

    public String getPerformative();

    public IACREAgentIdentifier getSender();

    public IACREAgentIdentifier getReceiver();

    public String getContent();

    public ProtocolDescriptor getProtocol();

    public String getConversationIdentifier();

    public String getLanguage();

    public String getInReplyTo();

    public String getReplyWith();

    public Long getReplyBy();

    public Transition getTriggered();

    public int getStatus();

    @Override
    public boolean equals( Object o );
}
