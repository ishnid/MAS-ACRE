package is.lill.acre.protocol;

import java.util.Collection;
import java.util.Set;

public interface IRepository {
   public Collection<IProtocolSource> getSources() throws RepositoryException;

   public boolean contains( ProtocolDescriptor desc );

   public IProtocolSource getSourceFor( ProtocolDescriptor desc );

   public void refresh() throws RepositoryException;

   public String getBase();

   public Set<String> getNamespaces();

   public Set<ProtocolDescriptor> getDescriptorsByNamespace( String namespace );
}
