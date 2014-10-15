package is.lill.acre.protocol;

import java.io.IOException;
import java.io.InputStream;


/**
 * Interface representing a source from which protocols can be read.
 * @author daithi
 *
 */
public interface IProtocolSource {
    /**
     * Get an {@code InputStream} from which one or more protocols may be read.
     * @return The stream
     * @throws IOException if a stream cannot be created.
     */
    public InputStream getInputStream() throws IOException;
    public String getDescription();
    public ProtocolDescriptor getDescriptor();
}
