package is.lill.acre.group;

import is.lill.acre.message.IACREAgentIdentifier;

import java.util.HashSet;
import java.util.Set;

public class AgentGroup {
   private Set<IACREAgentIdentifier> agents =
         new HashSet<IACREAgentIdentifier>();
   private String name;

   public AgentGroup( String groupName ) {
      this.name = groupName;
   }

   public void addAgent( IACREAgentIdentifier agent ) {
      this.agents.add( agent );
   }

   public void removeAgent( IACREAgentIdentifier agent ) {
      this.agents.remove( agent );
   }

   public String getName() {
      return this.name;
   }

   public Set<IACREAgentIdentifier> getAgents() {
      return this.agents;
   }
}
