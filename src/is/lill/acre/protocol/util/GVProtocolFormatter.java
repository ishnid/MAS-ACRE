package is.lill.acre.protocol.util;

import is.lill.acre.logic.ITermFormatter;
import is.lill.acre.logic.Utilities;
import is.lill.acre.protocol.Protocol;
import is.lill.acre.protocol.State;
import is.lill.acre.protocol.Transition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;

public class GVProtocolFormatter {

   private static final Logger logger = Logger.getLogger( GVProtocolFormatter.class.getName() );
   static {
      logger.setLevel( Level.WARNING );
   }

   // if no width and height are specified, use default values (1 inch square -
   // dot uses inches to measure distances)
   public static String formatProtocol( Protocol p ) {
      return formatProtocol( p, 1.0f, 1.0f );
   }

   public static String formatProtocol( Protocol p, float width, float height ) {
      logger.info( "Generating image for protocol " + p.getDescriptor().getName() + " with width:" + width + " height:" + height );

      ITermFormatter formatter = Utilities.getTermFormatter( "acre" );

      // map states to labels
      Map<State, String> stateLabelMap = new HashMap<State, String>();

      // set of initial states (that will require fake nodes to link from)
      Set<State> initialStates = new HashSet<State>();

      // count the labels
      int counter = 0;

      // this will hold the .gv file contents
      StringBuilder output = new StringBuilder();

      output.append( "digraph finite_state_machine {\n" );
      if ( width > 0 && height > 0 ) {
         output.append( "   size=\"" + width + "," + height + "\"\n" );
      }

      // create a node for each state
      for ( State s : p.getStates() ) {
         stateLabelMap.put( s, "LR_" + counter++ );
         if ( s.isInitialState() ) {
            initialStates.add( s );
         }
         output.append( formatState( s, stateLabelMap ) );
      }

      // create the fake nodes to connect to initial states
      for ( @SuppressWarnings ( "unused")
      State s : initialStates ) {
         output.append( "   node [shape = none, label = \"\" ]; LR_" + counter++ + ";\n" );
      }

      // generate all transitions
      for ( Transition t : p.getTransitions() ) {
         output.append( "   " ).append( stateLabelMap.get( t.getStartState() ) ).append( " -> " ).append( stateLabelMap.get( t.getEndState() ) ).append( " [ label=\"performative: " ).append( t.getPerformative() ).append( "\\n" );

         if ( t.getSender() != null ) {
            output.append( "sender: " ).append( formatter.format( t.getSender() ) ).append( "\\n" );
         }
         if ( t.getReceiver() != null ) {
            output.append( "receiver: " ).append( formatter.format( t.getReceiver() ) ).append( "\\n" );
         }
         if ( t.getContent() != null && t.getContent().getFunctor().length() > 0 ) {
            output.append( "content: " ).append( formatter.format( t.getContent() ) ).append( "\\n" );
         }
         output.append( "\" ];\n" );
      }

      // now the transitions for the initial states (i.e. from the fake states
      // to the real initial states)
      for ( State s : initialStates ) {
         output.append( "   LR_" ).append( counter-- ).append( " -> " ).append( stateLabelMap.get( s ) ).append( " [ label = \"\" ];\n" );
      }

      output.append( "}\n" );

      return output.toString();
   }

   private static String formatState( State s, Map<State, String> stateLabelMap ) {
      String shape = s.isTerminalState() ? "doublecircle" : "circle";
      return "   node [shape=" + shape + ", label=\"" + s.getName() + "\", color=\"black\"]; " + stateLabelMap.get( s ) + ";\n";
   }
}
