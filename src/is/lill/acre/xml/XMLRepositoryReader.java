package is.lill.acre.xml;

import is.lill.acre.exception.InvalidSchemaException;
import is.lill.acre.protocol.ProtocolDescriptor;
import is.lill.acre.protocol.ProtocolVersion;
import is.lill.acre.protocol.RepositoryException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLRepositoryReader {

   private static Logger logger = Logger.getLogger( XMLRepositoryReader.class.getName() );

   public static Set<ProtocolDescriptor> readRepository( InputStream in ) throws RepositoryException, IOException {

      Set<ProtocolDescriptor> descriptors = new HashSet<>();
      
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware( true );

      Document doc;
      try {
         DocumentBuilder builder = factory.newDocumentBuilder();
         doc = builder.parse( in );
      }
      catch ( SAXException e ) {
         throw new RepositoryException( "Invalid XML" );
      }
      catch ( ParserConfigurationException e ) {
         throw new RepositoryException( "Failed to set up XML parser" );
      }
      catch ( IOException e ) {
         throw new RepositoryException( "Failed to read protocol XML" );
      }

      // only import protocols that are valid according to the schema
      Source source = new DOMSource( doc );
      SchemaFactory fac = SchemaFactory.newInstance( "http://www.w3.org/2001/XMLSchema" );

      // the URL to this is in the XML file, but
      // a) if it doesn't follow this schema, this code won't be able to
      // parse it anyway AND
      // b) this doesn't require an internet connection
      URL schemaURL = XMLProtocolSerialiser.class.getResource( "/is/lill/acre/xml/repository.xsd" );

      try {
         Schema schema = fac.newSchema( schemaURL );
         Validator val = schema.newValidator();
         try {
            val.validate( source );
         }
         catch ( SAXException e ) {
            e.printStackTrace();
            throw new RepositoryException( e.getMessage() );
         }
      }
      catch ( SAXException e ) {
         throw new InvalidSchemaException( e );
      }
      logger.info( "Repository validated" );

      NodeList nameSpaces = doc.getElementsByTagName( "namespace" );
      for ( int i = 0; i < nameSpaces.getLength(); i++ ) {
         Node nsNode = nameSpaces.item( i );
         Node nameNode = nsNode.getAttributes().getNamedItem( "name" );
         String nsName = nameNode.getTextContent();
         NodeList children = nsNode.getChildNodes();
         for ( int j = 0; j < children.getLength(); j++ ) {
            Node protocolNode = children.item( j );
            if ( protocolNode.getNodeType() == Node.ELEMENT_NODE ) {
               NamedNodeMap attrs = protocolNode.getAttributes();
               String name = attrs.getNamedItem( "name" ).getTextContent();
               String version = attrs.getNamedItem( "version" ).getTextContent();

               ProtocolDescriptor pd = new ProtocolDescriptor();
               pd.setName( name );
               pd.setNamespace( nsName );
               pd.setVersion( new ProtocolVersion( version ) );
               
               descriptors.add( pd );
            }
         }

      }

      return descriptors;
   }
}
