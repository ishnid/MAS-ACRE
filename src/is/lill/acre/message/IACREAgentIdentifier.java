package is.lill.acre.message;

import java.util.List;
/**
 * Interface to be implemented by any classes representing a unique identifier for use in ACRE
 * @author daithi
 * @version $Id$
 */
public interface IACREAgentIdentifier {
    /**
     * Get the name of the agent
     * @return The agent name
     */
   public String getName();
   
  /**
   * Get the addresses by which this agent may be contacted
   * @return A {@code List} of addresses, in order of priority.
   */
   public List<String> getAddresses();
   
   /**
    * Combine the addresses from this agent ID with those from another.
    * @param aid The agent ID to be merged
    */
   public void mergeAddresses( IACREAgentIdentifier aid );
}
