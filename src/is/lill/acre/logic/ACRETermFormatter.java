package is.lill.acre.logic;

/**
 * Class to facilitate the translation of a {@link Term} into a String.
 * @author daithi
 * @version $Id$
 */

public class ACRETermFormatter implements ITermFormatter {
   /**
    * Generate a String representation of this term
    * 
    * @return A String representation that can be converted into a Term again
    *         using {@link fromString(String)}
    */

   @Override
   public String format( Term t ) {

      StringBuilder b = new StringBuilder();
      if ( t.isVariable() ) {
         b.append( '?' );
         if ( t.isMutable() ) {
            b.append( '?' );
         }
         b.append( t.getFunctor() );
      }
      else if ( t.isConstant() ) {
         if ( !t.getFunctor().matches( "[\\w+-]*" ) ) {
            b.append( "\"" );
            b.append( t.getFunctor().replaceAll( "\"", "\\\"" ) );
            b.append( "\"" );
         }
         else {
            b.append( t.getFunctor() );
         }
      }
      else {
         b.append( t.getFunctor() );
         b.append( '(' );
         b.append( this.format( t.getArguments().get( 0 ) ) );
         for ( int i = 1; i < t.getArguments().size(); i++ ) {
            b.append( ", " );
            b.append( this.format( t.getArguments().get( i ) ) );
         }
         b.append( ')' );
      }
      return b.toString();
   }
}
