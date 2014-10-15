package is.lill.acre.conversation;

import is.lill.acre.exception.NoSuchGroupException;
import is.lill.acre.group.AgentGroup;
import is.lill.acre.message.IACREAgentIdentifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to keep track of addresses of agents with which the owner agent is in
 * contact.
 * 
 * @author daithi
 * @version $Id$
 */

public class AddressBook {

    private static Logger logger = Logger.getLogger( AddressBook.class.getName() );
    static {
        logger.setLevel( Level.WARNING );
    }

    private Map<String, IACREAgentIdentifier> addresses = new HashMap<String, IACREAgentIdentifier>();

    private Map<String, AgentGroup> groups = new HashMap<String, AgentGroup>();

    /**
     * Add a contact to the address book
     * 
     * @param identifier
     *            The identifier of the contact to be added
     */
    public void addContact( IACREAgentIdentifier identifier ) {
        if ( this.addresses.containsKey( identifier.getName() ) ) {
            this.addresses.get( identifier.getName() ).mergeAddresses( identifier );
        }
        else {
            this.addresses.put( identifier.getName(), identifier );
        }
        logger.info( "Added Contact: " + identifier.getName() );
    }

    public void removeContact( String name ) {
        this.addresses.remove( name );
        logger.info( "Removed Contact: " + name );
    }

    public void removeContact( IACREAgentIdentifier identifier ) {
        this.addresses.remove( identifier.getName() );
        logger.info( "Removed Contact: " + identifier.getName() );
    }

    public void disband( String groupName ) {
        this.groups.remove( groupName );
    }

    /**
     * Get a list of addresses for the given agent name
     * 
     * @param agentName
     *            The name of the agent to get address for
     * @return A list of addresses (in order of priority) relating to the
     *         specified agent
     */
    public List<String> getAddresses( String agentName ) {
        if ( this.addresses.containsKey( agentName ) ) {
            return this.addresses.get( agentName ).getAddresses();
        }
        return null;
    }

    public IACREAgentIdentifier getContact( String agentName ) {
        return this.addresses.get( agentName );
    }

    /**
     * Check if the specified agent has an entry in this address book
     * 
     * @param agentName
     *            The name of the agent
     * @return {@code true} if the agent name is found, {@code false} otherwise.
     */
    public boolean hasContact( String agentName ) {
        return this.addresses.containsKey( agentName );
    }

    /**
     * Create and return a new group
     * 
     * @param name
     *            The name of the group being created
     * @return The {@code AgentGroup} that has just been created
     */
    public AgentGroup createGroup( String name ) {
        AgentGroup newGroup = new AgentGroup( name );
        this.groups.put( name, newGroup );
        return newGroup;
    }

    /**
     * Get an existing agent group
     * 
     * @param groupName
     *            The name of the group
     * @return The {@code AgentGroup} with the specified name
     * @throws NoSuchGroupException
     *             if the specified group does not already exist.
     */
    public AgentGroup getGroup( String groupName ) throws NoSuchGroupException {
        if ( this.groups.containsKey( groupName ) )
            return this.groups.get( groupName );
        throw new NoSuchGroupException( groupName );
    }

    public Collection<AgentGroup> getGroups() {
        return this.groups.values();
    }
}
