package is.lill.acre.group;

import is.lill.acre.conversation.Conversation;

import java.util.HashSet;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ConversationGroup {

   private static Logger logger = Logger.getLogger( ConversationGroup.class.getName() );

   static {
      logger.setLevel( Level.OFF );
   }

   private String groupIdentifier;
   private Set<Conversation> conversations = new HashSet<Conversation>();

   ConversationGroup( String identifier ) {
      this.groupIdentifier = identifier;
   }

   public void addConversation( Conversation c ) {
      logger.info( "Conversation " + c.getConversationIdentifier() + " added to group" );
      this.conversations.add( c );
      if ( groupIdentifier == null ) {
         groupIdentifier =
               c.getProtocol().getDescriptor() + "_" + c.getConversationIdentifier();
         logger.info( "Conversation group id set to " + groupIdentifier );
      }
   }

   public void removeConversation( Conversation c ) {
      this.conversations.remove( c );
   }

   public Set<Conversation> getConversations() {
      return this.conversations;
   }
   
   public String getName() {
      return this.groupIdentifier;
   }
   
   public boolean isEmpty() {
       return this.conversations.isEmpty();
   }
}
