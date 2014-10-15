package is.lill.acre.event;

import is.lill.acre.message.IACREMessage;

public class AmbiguousMessageEvent extends AbstractMessageEvent {

   public AmbiguousMessageEvent( IACREMessage m ) {
      super( m );
   }
}
