package is.lill.acre.logic;

/**
 * Parse a string to generate an ACRE logic term
 * 
 * @author daithi
 * @version $Id$
 */
public interface ITermParser {
    /**
     * Parse a String to produce a term of ACRE logic.
     * 
     * @param s The string to be parsed
     * @return An ACRE logic term represtented by the String
     * @throws MalformedTermException if the string cannot be parsed as an ACRE term
     */
    public Term parse( String s ) throws MalformedTermException;
}
