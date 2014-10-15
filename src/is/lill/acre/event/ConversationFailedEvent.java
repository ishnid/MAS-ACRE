package is.lill.acre.event;

import is.lill.acre.conversation.Conversation;
import is.lill.acre.message.IACREMessage;

public class ConversationFailedEvent extends AbstractConversationMessageEvent {

   public ConversationFailedEvent( IACREMessage m, Conversation c ) {
      super( m, c );
   }
}
