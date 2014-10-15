package is.lill.acre.protocol;

import is.lill.acre.xml.XMLProtocolSerialiser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class HTTPRepository extends AbstractRepository implements IRepository {

    private static final Logger logger = Logger.getLogger( HTTPRepository.class.getName() );
    static {
        logger.setLevel( Level.OFF );
    }

    private URL base;

    public HTTPRepository( URL base ) {
        this.base = base;
    }

    public String getBase() {
        return this.base.toString();
    }

    public void refresh() throws RepositoryException {
        this.setReadSources( true );
        try {
            URL address = new URL( this.base.toString() + "/repository.xml" );

            logger.info( "Opening repository.xml file at " + address.toString() );

            URLConnection uCon = address.openConnection();

            InputStream in;

            if ( uCon instanceof HttpURLConnection ) {

                logger.info( "Opening repository at: " + address.toString() );

                if ( ( (HttpURLConnection) uCon ).getResponseCode() == HttpURLConnection.HTTP_OK ) {

                    logger.info( "Successfully opened repository.xml file" );

                    in = address.openStream();
                }
                else {
                    logger.info( "Failed to open repository.xml file" );
                    throw new RepositoryException( "Failed to open repository.xml file:\n" + address.toString() );
                }
            }
            else {
                try {
                    in = address.openStream();
                }
                catch ( Throwable t ) {
                    throw new RepositoryException( "Invalid URL" );
                }
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware( true );

            DocumentBuilder parser = factory.newDocumentBuilder();

            Document doc = parser.parse( in );

            // validate the repository xml file
            Source source = new DOMSource( doc );
            SchemaFactory fac = SchemaFactory.newInstance( "http://www.w3.org/2001/XMLSchema" );

            URL schemaURL = XMLProtocolSerialiser.class.getResource( "/is/lill/acre/xml/repository.xsd" );

            try {
                Schema schema = fac.newSchema( schemaURL );
                Validator val = schema.newValidator();
                try {
                    val.validate( source );
                }
                catch ( SAXException e ) {
                    throw new RepositoryException( "Invalid Repository XML File: " + e.getMessage() );
                }
            }
            catch ( SAXException e ) {
                throw new RepositoryException( "Invalid Repository Schema: " + e.getMessage() );
            }
            logger.info( "Protocol validated" );

            // NodeList baseNodes = doc.getElementsByTagName( "base" );
            NodeList namespaceNodes = doc.getElementsByTagName( "namespace" );

            int i = 0;
            while ( i < namespaceNodes.getLength() ) {
                Node nsNode = namespaceNodes.item( i );
                String namespace = nsNode.getAttributes().getNamedItem( "name" ).getNodeValue();

                NodeList children = nsNode.getChildNodes();
                int j = 0;
                while ( j < children.getLength() ) {
                    Node child = children.item( j );
                    if ( child.getNodeName().equals( "protocol" ) ) {
                        ProtocolDescriptor desc = new ProtocolDescriptor();
                        // source.setBase( base );s
                        desc.setNamespace( namespace );
                        desc.setName( child.getAttributes().getNamedItem( "name" ).getNodeValue() );
                        desc.setVersion( new ProtocolVersion( child.getAttributes().getNamedItem( "version" ).getNodeValue() ) );
                        this.sources.put( desc, new URLProtocolSource( base, desc ) );
                    }
                    j++;
                }
                i++;
            }

        }

        catch ( ParserConfigurationException e ) {
            throw new RepositoryException( "Failed to parse repository XML file for " + base.toString() );
        }
        catch ( MalformedURLException e ) {
            throw new RepositoryException( "Repository file 'repository.xml' not found at " + base.toString() );
        }
        catch ( IOException e ) {
            throw new RepositoryException( "Failed to read repository XML file for " + base.toString() );
        }
        catch ( SAXException e ) {
            throw new RepositoryException( "Failed to parse repository XML file for " + base.toString() );
        }
    }

    @Override
    public IProtocolSource getSourceFor( ProtocolDescriptor desc ) {
        return this.sources.get( desc );
    }
}
