package is.lill.acre.logic;

import java.util.HashMap;
import java.util.Map;

/**
 * Track variable bindings within a conversation 
 * @author daithi
 * @version $Id$
 */
public class Bindings {

   private Map<String, Term> bindings = new HashMap<String, Term>();
   
   /**
    * Find if a binding is present for a given variable
    * @param term A term to be checked
    * @return {@code true} if {@code term} is a variable and a binding exists for it.
    */
   public boolean binds( Term term ) {
      return term.isVariable() && bindings.containsKey( term.getFunctor() );
   }

   /**
    * Get the bindings for a term
    * 
    * @param t
    *           The term to check the bindings for
    * @return The bindings for the supplied term, or {@code null} if {@code t}
    *         is not a variable or if no appropriate binding exists
    */
   public Term getBindingFor( Term t ) {
      return t.isVariable() ? this.bindings.get( t.getFunctor() ) : null;
   }

   /**
    * Add a binding for a term
    * @param k The variable to create the value for
    * @param v The value to be bound to the variable {@code k}
    */
   public void addBinding( Term k, Term v ) {
      if ( k.isVariable() && ( k.isMutable() || !bindings.containsKey( k.getFunctor() ) ) ) {
         bindings.put( k.getFunctor(), v );
      }
   }

   /**
    * Get a string representation of this set of bindings
    * @return A human-readable string describing the bindings stored in this object
    */
   public String toString() {
      StringBuilder b = new StringBuilder();
      for ( String s : this.bindings.keySet() ) {
         b.append( "[" + s + "]: " + "[" + this.bindings.get( s ).toDebuggingString() + "]\n" );
      }
      return b.toString();
   }

   /**
    * Update this set of bindings
    * @param b Another set of bindings to be added to this object. Overrides conflicting bindings.
    */
   public void update( Bindings b ) {
      this.bindings.putAll( b.bindings );
   }
   
   @Override
   public Bindings clone() {
       Bindings toReturn = new Bindings();
       toReturn.bindings = new HashMap<String,Term>( bindings );
       return toReturn;
   }
}
