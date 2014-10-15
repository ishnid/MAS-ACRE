package is.lill.acre.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ACREAgentIdentifier implements IACREAgentIdentifier {

    List<String> addresses = new ArrayList<String>();
    String name;

    public ACREAgentIdentifier( String name ) {
        this.name = name;
    }

    public ACREAgentIdentifier( String name, String address ) {
        this( name );
        this.addresses.add( address );
    }

    public ACREAgentIdentifier( String name, Collection<String> addresses ) {
        this( name );
        this.addresses.addAll( addresses );
    }

    @Override
    public List<String> getAddresses() {
        return this.addresses;
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Check if this agent id is equal to a given agent id. Based on the fact
     * that agent names should be globally unique, per FIPA standards
     * 
     * @return {@code true} if the agent names are the same, {@code false}
     *         otherwise
     */
    @Override
    public boolean equals( Object id ) {
        return id instanceof ACREAgentIdentifier && ( (ACREAgentIdentifier) id ).getName().equals( this.getName() );
    }

    public int hashCode() {
        return this.getName().hashCode();
    }

    public String toString() {
        String toReturn = "agentID(" + getName() + ",addresses(" + getAddresses().get( 0 );
        for ( int i = 1; i < getAddresses().size(); i++ ) {
            toReturn += "," + getAddresses().get( i );
        }
        toReturn += "))";
        return toReturn;
    }

    /**
     * Adds addresses from {@code aid} to this agent ID if they are not present
     * already. TODO: This should probably maintain the priority order:
     * presently adds new addresses to the end
     */
    @Override
    public void mergeAddresses( IACREAgentIdentifier aid ) {
        for ( String a : aid.getAddresses() ) {
            if ( !this.addresses.contains( a ) ) {
                this.addresses.add( a );
            }
        }
    }
}
