package is.lill.acre.protocol;

import is.lill.acre.protocol.UnknownStateTypeException;

import java.util.HashMap;
import java.util.Map;

public class StateType {

   private static final int INITIAL_INT = 1;
   private static final int MIDDLE_INT = 2;
   private static final int TERMINAL_INT = 3;
   
   /**
    * Object representing an initial state.
    */
   public static final StateType INITIAL = new StateType( INITIAL_INT );
   
   /**
    * Object representing a terminal state.
    */
   public static final StateType TERMINAL = new StateType( TERMINAL_INT );
   

   /**
    * Object representing a non-initial, non-terminal state.
    */
   public static final StateType MIDDLE = new StateType( MIDDLE_INT );
   
   
   private static final Map<String, StateType> typeMap = new HashMap<String, StateType>();

   static {
      typeMap.put( "INITIAL", INITIAL );
      typeMap.put( "MIDDLE", MIDDLE );
      typeMap.put( "TERMINAL", TERMINAL );

      // default Values
      typeMap.put( "", MIDDLE );
      typeMap.put( null, MIDDLE );
   }
   private int type;

   private StateType( int type ) {
      this.type = type;
   }

   @Override
   /**
    * Get a string representation of this state type.
    * @return A string representation
    */
   public String toString() {
      switch ( this.type ) {
         case INITIAL_INT:
            return "INITIAL";
         case MIDDLE_INT:
            return "MIDDLE";
         // this can be the default case (3) because
         // the range of state types is restricted by
         // having a private constructor
         default:
            return "TERMINAL";
      }
   }

   /**
    * Test if this represents the same state type as another.
    * @param t The state type to compare with.
    * @return {@code true} if the two objects represent the same state type, {@code false} otherwise.
    */
   public boolean equals( StateType t ) {
      return this.type == t.type;
   }

   /**
    * Given the name of a state type, get a {@code StateType} object to represent it.
    * @param name The name of the state type.
    * @return A {@code StateType} object representing the state type named {@code name}.
    * @throws UnknownStateTypeException if {@code name} is not a valid state type.
    */
   public static StateType getStateTypeByName( String name ) throws UnknownStateTypeException {
      if ( name == null || typeMap.containsKey( name.toUpperCase() ) ) {
         return typeMap.get( name == null ? null : name.toUpperCase() );
      }
      throw new UnknownStateTypeException( "State Type [" + name + "] not known" );
   }
}
