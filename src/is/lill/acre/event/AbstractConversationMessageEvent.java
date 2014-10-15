package is.lill.acre.event;

import is.lill.acre.conversation.Conversation;
import is.lill.acre.message.IACREMessage;

public abstract class AbstractConversationMessageEvent extends AbstractConversationEvent implements IMessageEvent {

   private IACREMessage message;

   protected AbstractConversationMessageEvent( IACREMessage m, Conversation c ) {
      super( c );
      this.message = m;
   }

   public IACREMessage getMessage() {
      return this.message;
   }
}
