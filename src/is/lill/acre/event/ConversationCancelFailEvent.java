package is.lill.acre.event;

import is.lill.acre.conversation.Conversation;
import is.lill.acre.message.IACREMessage;

public class ConversationCancelFailEvent extends AbstractConversationMessageEvent {

   public ConversationCancelFailEvent( IACREMessage m, Conversation c ) {
      super( m, c );
   }
}
