package is.lill.acre.logic;

/**
 * Interface for formatting ACRE logic {@link Term} objects to strings.
 * @author daithi
 *
 */
public interface ITermFormatter {
    /**
     * Format an ACRE logic term as a string
     * 
     * @param term An ACRE logic term to be formatted
     * @return A string representation of {@code term}
     */
   public String format( Term term );
}
