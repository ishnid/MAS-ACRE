package is.lill.acre.protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MemoryProtocolSource implements IProtocolSource {

    private ProtocolDescriptor desc;
    private byte[] contents;

    public MemoryProtocolSource( byte[] contents, ProtocolDescriptor desc ) {
        this.contents = contents;
        this.desc = desc;
    }

    @Override
   public InputStream getInputStream() throws IOException {
      return new ByteArrayInputStream( contents );
   }

    public String getDescription() {
        return this.desc.getUniqueID();
    }

    public ProtocolDescriptor getDescriptor() {
        return this.desc;
    }
}
