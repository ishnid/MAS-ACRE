package is.lill.acre.message;

/**
 * A class to allow access to standard FIPA performatives
 * @author daithi
 * @version $Id$
 *
 */
public class Performative {
   // hide constructor
   private Performative() {}

   public static final String ACCEPT_PROPOSAL = "accept-proposal";
   public static final String AGREE = "agree";
   public static final String CANCEL = "cancel";
   public static final String CALL_FOR_PROPOSAL = "cfp";
   public static final String CONFIRM = "confirm";
   public static final String DISCONFIRM = "disconfirm";
   public static final String FAILURE = "failure";
   public static final String INFORM = "inform";
   public static final String INFORM_IF = "inform-if";
   public static final String INFORM_REF = "inform-ref";
   public static final String NOT_UNDERSTOOD = "not-understood";
   public static final String PROPAGATE = "propagate";
   public static final String PROPOSE = "propose";
   public static final String PROXY = "proxy";
   public static final String QUERY_IF = "query-if";
   public static final String QUERY_REF = "query-ref";
   public static final String REFUSE = "refuse";
   public static final String REJECT_PROPOSAL = "reject-proposal";
   public static final String REQUEST = "request";
   public static final String REQUEST_WHEN = "request-when";
   public static final String REQUEST_WHENEVER = "request-whenever";
   public static final String SUBSCRIBE = "subscribe";
}
