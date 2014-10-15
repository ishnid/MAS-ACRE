package is.lill.acre.protocol;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileProtocolSource implements IProtocolSource {

   private ProtocolDescriptor desc;
   private File file;
   
   public FileProtocolSource( File file, ProtocolDescriptor desc ) {
      this.file = file;
      this.desc = desc;
   }
   
   @Override
   public InputStream getInputStream() throws IOException {
      return new FileInputStream( this.file );
   }
   
   public String getDescription() {
      return this.file.toString();
   }
   
   public ProtocolDescriptor getDescriptor() {
      return this.desc;
   }
}
