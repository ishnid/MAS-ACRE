package is.lill.acre.event;

import is.lill.acre.conversation.Conversation;

public class ConversationTimeoutEvent extends AbstractConversationEvent {
    public ConversationTimeoutEvent( Conversation c ) {
        super( c );
    }
}
