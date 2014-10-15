package is.lill.acre.group.monitor;

import is.lill.acre.conversation.Conversation;
import is.lill.acre.conversation.ConversationStatus;

/**
 * Group Monitor to test if all conversations in a group are in the same state.
 * 
 * @author daithi
 * @version $Id$
 */

public class AllInState extends AbstractGroupMonitor {

    static {
        PARAMS = 1;
    }

    @Override
    /**
     * Check if an event should be raised by this monitor
     * @return {@code true} if all conversations in the group are in the given state, {@code false} otherwise.
     */
    public boolean raiseEvent() {
        boolean toReturn = false;
        for ( Conversation c : this.group.getConversations() ) {
            if ( c.getStatus() == ConversationStatus.ACTIVE ) {
                if ( !c.getState().getName().equals( this.params.get( 0 ) ) ) {
                    return false;
                }
                // found at least one active conversation
                toReturn = true;
            }
        }
        return toReturn;
    }
}
