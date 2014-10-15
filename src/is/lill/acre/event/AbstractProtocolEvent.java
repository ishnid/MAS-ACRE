package is.lill.acre.event;

import is.lill.acre.protocol.Protocol;

public abstract class AbstractProtocolEvent extends ACREEvent implements IProtocolEvent {
   private Protocol protocol;

   protected AbstractProtocolEvent( Protocol p ) {
      this.protocol = p;
   }

   public Protocol getProtocol() {
      return this.protocol;
   }
}
