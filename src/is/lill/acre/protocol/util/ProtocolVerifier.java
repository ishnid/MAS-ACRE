package is.lill.acre.protocol.util;

import is.lill.acre.logic.ITermParser;
import is.lill.acre.logic.MalformedTermException;
import is.lill.acre.logic.Term;
import is.lill.acre.logic.Utilities;
import is.lill.acre.protocol.Protocol;
import is.lill.acre.protocol.State;

import java.util.HashSet;
import java.util.Set;

public class ProtocolVerifier {

   public static final int STATE_NAME = 0;
   public static final int AGENT_NAME = 2;
   private static final int TERM = 3;
   public static final int NAMESPACE = 4;
   public static final int NAME = 5;
   public static final int VERSION = 6;

   public static final int FROM_STATE = STATE_NAME;
   public static final int TO_STATE = STATE_NAME;
   public static final int PERFORMATIVE = STATE_NAME;
   public static final int SENDER = AGENT_NAME;
   public static final int RECEIVER = AGENT_NAME;
   public static final int CONTENT = TERM;

   private static ITermParser parser = Utilities.getTermParser( "acre" );

   // don't allow instantiation
   private ProtocolVerifier() {};

   /**
    * Verify whether a field contains a valid value
    * 
    * @param type
    *           The field the data to be checked is related to
    * @param value
    *           The data to be checked
    * @return {@code true} if the data is valid, {@code false} otherwise.
    */
   public static boolean verifyField( int type, String value ) {
      switch ( type ) {
         case STATE_NAME:
            return value.matches( "\\w+" );
         case TERM:
            try {
               parser.parse( value );
            }
            catch ( MalformedTermException e ) {
               return false;
            }
            break;
         case AGENT_NAME:
            try {
               Term name = parser.parse( value );
               return name.isConstant() || name.isVariable();
            }
            catch ( MalformedTermException e ) {
               return false;
            }
         case NAMESPACE:
            return value.matches( "[a-zA-Z\\d]([a-zA-Z\\d-]*[a-zA-Z\\d])?(\\.[a-zA-Z\\d]([a-zA-Z\\d-]*[a-zA-Z\\d])?)*" );
         case NAME:
            return value.matches( "[a-zA-Z\\d]([a-zA-Z\\d-]*[a-zA-Z\\d])?" );
         case VERSION:
            return value.matches( "\\d+\\.\\d+" );
      }
      return true;
   }

   public static Set<ProtocolWarning> check( Protocol p ) {
      Set<ProtocolWarning> toReturn = new HashSet<ProtocolWarning>();

      int initials = 0;

      if ( p.getStates().isEmpty() ) {
         toReturn.add( new ProtocolWarning( "Protocol has no states" ) );
      }
      if ( p.getTransitions().isEmpty() ) {
         toReturn.add( new ProtocolWarning( "Protocol has no transitions" ) );
      }

      for ( State s : p.getStates() ) {
         if ( s.getInTransitions().isEmpty() ) {
            initials++;
            if ( s.getOutTransitions().isEmpty() ) {
               toReturn.add( new ProtocolWarning( "State " + s.getName() + " has no transitions" ) );
            }
         }
      }
      if ( initials > 1 ) {
         toReturn.add( new ProtocolWarning( "Multiple initial states" ) );
      }

      return toReturn;
   }
}
