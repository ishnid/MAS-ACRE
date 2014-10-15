package is.lill.acre.event;

import is.lill.acre.conversation.Conversation;
import is.lill.acre.message.IACREMessage;

public class ConversationCancelConfirmEvent extends AbstractConversationMessageEvent {

   public ConversationCancelConfirmEvent( IACREMessage m, Conversation c ) {
      super( m, c );
   }
}
