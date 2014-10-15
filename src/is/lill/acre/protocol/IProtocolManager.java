package is.lill.acre.protocol;

import java.util.Collection;

public interface IProtocolManager {

   /**
    * Given a protocol descriptor, get the associated protocol
    * 
    * @param pd
    *           Descriptor of the desired protocol
    * @return The protocol described by the descriptor, or {@code null} if the
    *         protocol manager has no way of finding it.
    * @throws RepositoryException 
    */
   public abstract Protocol getProtocolByDescriptor( ProtocolDescriptor pd ) throws RepositoryException;
   public abstract Collection<Protocol> getProtocols() throws RepositoryException;
   public abstract String getBase();

}