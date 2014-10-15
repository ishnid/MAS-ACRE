package is.lill.acre.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class URLProtocolSource implements IProtocolSource {

   private URL base;
   private ProtocolDescriptor desc;

   private static final Logger logger = Logger.getLogger( URLProtocolSource.class.getName() );
   static {
       logger.setLevel( Level.WARNING );
   }
   
   public URLProtocolSource( URL base, ProtocolDescriptor desc ) {
      this.base = base;
      this.desc = desc;
   }

   @Override
   public InputStream getInputStream() throws IOException {
      return this.getAddress().openStream();
   }

   public URL getAddress() {

      try {
         return new URL( this.base + "/repository/" + this.desc.getNamespace() + "_" + this.desc.getName() + "_" + this.desc.getVersion() + ".acr" );
      }
      catch ( MalformedURLException e ) {
         logger.severe( e.getMessage() );
         return null;
      }
   }

   public void setBase( URL u ) {
      this.base = u;
   }
   
   public String getDescription() {
      return this.getAddress().toString();
   }
   
   public ProtocolDescriptor getDescriptor() {
      return this.desc;
   }
}
