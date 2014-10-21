package is.lill.acre.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class PathProtocolSource implements IProtocolSource {

   private Path path;
   private ProtocolDescriptor desc;

   public PathProtocolSource( Path path, ProtocolDescriptor desc ) {
      this.path = path;
      this.desc = desc;
   }

   @Override
   public InputStream getInputStream() throws IOException {
      return Files.newInputStream( path );
   }

   @Override
   public String getDescription() {
      return this.path.toString();
   }

   @Override
   public ProtocolDescriptor getDescriptor() {
      return this.desc;
   }

}
