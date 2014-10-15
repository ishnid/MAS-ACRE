package is.lill.acre.event;

import is.lill.acre.message.IACREMessage;

public abstract class AbstractMessageEvent extends ACREEvent implements IMessageEvent {

   private IACREMessage message;

   protected AbstractMessageEvent( IACREMessage m ) {
      this.message = m;
   }

   public IACREMessage getMessage() {
      return this.message;
   }
}
