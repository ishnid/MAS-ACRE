package is.lill.acre.logic;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to hold utilities related to handling {@link Term} objects.
 * @author daithi
 * @version $Id$
 */

public class Utilities {

   // parsers and formatters for logic parsing/formatting
   private static Map<String, ITermFormatter> formatters = new HashMap<String, ITermFormatter>();
   private static Map<String, ITermParser> parsers = new HashMap<String, ITermParser>();

   static {
      formatters.put( "acre", new ACRETermFormatter() );
      parsers.put( "acre", new ACRETermParser() );
   }

   public static void addTermFormatter( String language, ITermFormatter formatter ) {
      formatters.put( language.toLowerCase(), formatter );
   }

   public static ITermParser getTermParser( String language ) {
      return parsers.get( language.toLowerCase() );
   }

   public static ITermFormatter getTermFormatter( String language ) {
      return formatters.get( language.toLowerCase() );
   }

   public static void addTermParser( String language, ITermParser parser ) {
      parsers.put( language.toLowerCase(), parser );
   }

}
