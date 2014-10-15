package is.lill.acre.logic;
/**
 * Class to facilitate the translation of a String into a {@link Term}.
 * @author daithi
 * @version $Id$
 */

public class ACRETermParser implements ITermParser {

   /**
    * Parse a String and create a Term from it
    * 
    * @param s
    *           A String to be parsed as a term
    * @return A Term representation of the String provided
    * @throws MalformedTermException
    *            if the provided String is formatted incorrectly to be parsed as
    *            a term.
    */

   @Override
   public Term parse( String s ) throws MalformedTermException {

      Term toReturn = new Term();

      // remove trailing and leading whitespace
      s = s.trim();

      // anonymous variable
      if ( s.equals( "?" ) ) {
         toReturn.setType( Term.VARIABLE );
         toReturn.setMutable( false );
         toReturn.setFunctor( "" );
      }
      // immutable variable
      else if ( s.matches( "\\?[\\w-]+" ) ) {
         toReturn.setType( Term.VARIABLE );
         toReturn.setMutable( false );
         toReturn.setFunctor( s.substring( 1 ) );
      }
      // mutable variable
      else if ( s.matches( "\\?\\?[\\w-]*" ) ) {
         toReturn.setType( Term.VARIABLE );
         toReturn.setMutable( true );
         toReturn.setFunctor( s.substring( 2 ) );
      }
      // constant, including empty content
      else if ( s.matches( "[\\w-]*" ) ) {
         toReturn.setType( Term.CONSTANT );
         toReturn.setMutable( false );
         toReturn.setFunctor( s.replaceAll( "\\(?!\\)", "" ) );
      }
      else if ( s.matches( "\"(.+)\"" ) ) {
         toReturn.setType( Term.CONSTANT );
         toReturn.setMutable( false );
         toReturn.setFunctor( s.substring( 1, s.length() - 1 ).replaceAll( "\\\\(?!\\\\)", "" ) );
      }
      // experimental hackish list support (treated as constants, regardless of what's in them)
      else if ( s.matches(  "\\[.+\\]" ) ) {
          toReturn.setType( Term.CONSTANT );
          toReturn.setMutable(  false  );
          toReturn.setFunctor( s );
      }
      // function/predicate - more complex!
      else if ( s.matches( "[\\w-]+\\((.*)\\)" ) ) {
         toReturn.setType( Term.FUNCTION );
         toReturn.setFunctor( s.substring( 0, s.indexOf( '(' ) ) );

         boolean escaped = false;
         boolean quoted = false;
         int marker = s.indexOf( '(' ) + 1;
         int parenCount = 0;
         int bracketCount = 0;
         for ( int i = marker; i < s.length(); i++ ) {

            char c = s.charAt( i );
            
            if ( !escaped ) {
                // found a double quote character (unescaped) so either open or close quotes
               if ( c == '"' ) {
                  quoted = !quoted;
               }
               // found a backslash, so the next character is escaped
               else if ( c == '\\' ) {
                  escaped = true;
               }
               
               // not in quotes ...
               if ( !quoted ) {
                   // ... so a comma separates arguments (as long as I have no unclosed parentheses)
                  if ( c == ',' && parenCount == 0 && bracketCount == 0 ) {
                      
                     // parse everything from the marker to here
                     toReturn.addArgument( parse( s.substring( marker, i ) ) );
                     
                     // reset the marker to the next position
                     marker = i + 1;
                  }
                  // opening parenthesis
                  else if ( c == '(' ) {
                     parenCount++;
                  }
                  // closing parenthesis
                  else if ( c == ')' ) {
                     parenCount--;
                  }
                  else if ( c == '[' ) {
                      bracketCount++;
                  }
                  else if ( c == ']' ) {
                      bracketCount--;
                  }
               }
            }
            else {
               escaped = false;
            }
         }
         toReturn.addArgument( parse( s.substring( marker, s.length() - 1 ) ) );

         // it looked like a function, but there are no arguments
         if ( toReturn.getArguments() == null || toReturn.getArguments().isEmpty() ) {
            toReturn.setType( Term.CONSTANT );
         }
      }
      else {
          // Strings aren't always passed with quotes arounnd them.
          toReturn.setType( Term.CONSTANT );
          toReturn.setMutable(  false  );
          toReturn.setFunctor( s );
         //throw new MalformedTermException( "Malformed term: [" + s + "]" );
      }
      return toReturn;
   }
}
