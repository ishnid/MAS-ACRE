package is.lill.acre.protocol;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RepositoryFactory {

    private static final Logger logger = Logger.getLogger( RepositoryFactory.class.toString() );
    static {
        logger.setLevel( Level.WARNING );
    }

    public static IRepository openRepository( String description ) throws RepositoryException {
        try {
            URL u = new URL( description );
            return new HTTPRepository( u );
        }
        catch ( MalformedURLException e ) {
            File base = new File( description );
            if ( !base.exists() ) {
                if ( !description.startsWith( "/" ) ) {
                    description = "/" + description;
                }

                URL u = RepositoryFactory.class.getResource( description );
                if ( u == null ) {
                    throw new RepositoryException( "Repository could not be found: " + description );
                }
                else {
                    return new ZipFileRepository( u );
                }
            }
            else if ( base.isDirectory() ) {
                return new LocalRepository( base );
            }
            else if ( description.endsWith( ".zip" ) ) {
                try {
                    URL u = base.toURI().toURL();
                    return new ZipFileRepository( u );
                }
                catch ( MalformedURLException e1 ) {
                    // coming from a file that is confirmed to exist
                    // if this exception is ever thrown, it's a Java bug
                    e1.printStackTrace();
                    return null;
                }
            }
            else {
                throw new RepositoryException( "Repository type could not be identified: " + description );
            }
        }
    }
}
