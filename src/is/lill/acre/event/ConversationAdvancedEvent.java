package is.lill.acre.event;

import is.lill.acre.conversation.Conversation;
import is.lill.acre.message.IACREMessage;
import is.lill.acre.protocol.State;

public class ConversationAdvancedEvent extends AbstractConversationMessageEvent {

    private State s;
    
   public ConversationAdvancedEvent( IACREMessage m, Conversation c, State s ) {
      super( m, c );
      this.s = s;
   }
   
   public State getState() {
       return this.s;
   }
}
