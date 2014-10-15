package is.lill.acre.event;

import is.lill.acre.conversation.Conversation;
import is.lill.acre.message.IACREMessage;

public class NotUnderstoodMessageEvent extends AbstractConversationMessageEvent {
   public NotUnderstoodMessageEvent( IACREMessage m, Conversation c ) {
      super( m, c );
   }
}
