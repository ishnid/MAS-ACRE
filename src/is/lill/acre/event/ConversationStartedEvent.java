package is.lill.acre.event;

import is.lill.acre.conversation.Conversation;

public class ConversationStartedEvent extends AbstractConversationEvent {
   public ConversationStartedEvent( Conversation c ) {
      super( c );
   }
}
