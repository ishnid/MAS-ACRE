package is.lill.acre.protocol;


public interface IEditableRepository extends IRepository {
   public void saveRepositoryXML() throws RepositoryException;
   public void addProtocol( Protocol p ) throws RepositoryException;
   public void deleteProtocol( Protocol p ) throws RepositoryException;
}
