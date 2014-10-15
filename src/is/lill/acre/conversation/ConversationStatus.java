package is.lill.acre.conversation;

/**
 * The status of a conversation: can be ACTIVE, FAILED, FINISHEd, STALE, READY or CANCELLED
 * @author daithi
 * @version $Id$
 */
public enum ConversationStatus {
    ACTIVE, FAILED, FINISHED, STALE, READY, CANCELLING, CANCELLED;
}