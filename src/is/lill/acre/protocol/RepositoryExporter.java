package is.lill.acre.protocol;

import java.net.URI;
import java.net.URISyntaxException;

public class RepositoryExporter {
   public static void export( IProtocolManager pm ) {
      try {
         URI toExport = new URI( "jar:/Users/daithi/code/acre_workspace/MAS-ACRE/repositorytest.acr" );
         //pm.
      }
      catch ( URISyntaxException e ) {

      }

   }
}
