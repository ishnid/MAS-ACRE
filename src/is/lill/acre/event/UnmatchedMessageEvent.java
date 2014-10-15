package is.lill.acre.event;

import is.lill.acre.message.IACREMessage;

public class UnmatchedMessageEvent extends ACREEvent implements IMessageEvent {

   private IACREMessage message;
   
   public UnmatchedMessageEvent( IACREMessage m ) {
      this.message = m;
   }

   @Override
   public IACREMessage getMessage() {
      return this.message;
   }
}
