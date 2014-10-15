package is.lill.acre.event;

import is.lill.acre.conversation.Conversation;

public class ConversationEndedEvent extends AbstractConversationEvent {
   public ConversationEndedEvent( Conversation c ) {
      super( c );
   }
}
