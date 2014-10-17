package is.lill.acre.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSystemProtocolSource implements IProtocolSource {

   private FileSystem fs;
   private ProtocolDescriptor desc;

   public FileSystemProtocolSource( FileSystem fs, ProtocolDescriptor desc ) {
      this.fs = fs;
      this.desc = desc;
   }

   @Override
   public InputStream getInputStream() throws IOException {
      Path p = fs.getPath( "repository", desc.toString() + ".acr" );
      return Files.newInputStream( p );
   }

   @Override
   public String getDescription() {
      return this.fs.toString();
   }

   @Override
   public ProtocolDescriptor getDescriptor() {
      return this.desc;
   }

}
