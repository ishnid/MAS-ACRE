package is.lill.acre.event;

import is.lill.acre.conversation.Conversation;

public abstract class AbstractConversationEvent extends ACREEvent implements IConversationEvent {
   private Conversation conversation;

   protected AbstractConversationEvent( Conversation c ) {
      this.conversation = c;
   }
   
   public Conversation getConversation() {
      return this.conversation;
   }
}
