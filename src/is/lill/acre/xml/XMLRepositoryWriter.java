package is.lill.acre.xml;

import is.lill.acre.protocol.IEditableRepository;
import is.lill.acre.protocol.ProtocolDescriptor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLRepositoryWriter {

   private static Logger logger = Logger.getLogger( XMLRepositoryWriter.class.getName() );
   static {
      logger.setLevel( Level.WARNING );
   }

   public static void writeRepository( IEditableRepository r, OutputStream out ) throws IOException {

      try {
         logger.info( "Creating XML document" );
         DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
         Document doc = docBuilder.newDocument();
         doc.setXmlStandalone( true );

         Element rep = doc.createElement( "repository" );

         rep.setAttribute( "xmlns", "http://acre.lill.is" );
         rep.setAttribute( "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance" );
         rep.setAttribute( "xsi:schemaLocation", "http://acre.lill.is http://acre.lill.is/repository.xsd" );
         doc.appendChild( rep );

         Element base = doc.createElement( "base" );
         base.setTextContent( r.getBase() );
         rep.appendChild( base );

         Element namespaces = doc.createElement( "namespaces" );
         rep.appendChild( namespaces );

         for ( String ns : r.getNamespaces() ) {
            Element nspace = doc.createElement( "namespace" );
            nspace.setAttribute( "name", ns );
            namespaces.appendChild( nspace );

            for ( ProtocolDescriptor pd : r.getDescriptorsByNamespace( ns ) ) {
               Element p = doc.createElement( "protocol" );
               p.setAttribute( "name", pd.getName() );
               p.setAttribute( "version", pd.getVersion().toString() );
               nspace.appendChild( p );
            }

         }

         // set up a transformer
         TransformerFactory transfac = TransformerFactory.newInstance();
         transfac.setAttribute( "indent-number", 3 );

         Transformer trans = transfac.newTransformer();
         trans.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "no" );
         trans.setOutputProperty( OutputKeys.INDENT, "yes" );

         // create string from xml tree
         StringWriter sw = new StringWriter();
         StreamResult result = new StreamResult( sw );
         DOMSource source = new DOMSource( doc );
         trans.transform( source, result );

         out.write( sw.toString().getBytes() );
      }
      catch ( ParserConfigurationException e ) {
         logger.severe( "Exception when writing repository description to XML:" + e );
         e.printStackTrace();
      }
      catch ( TransformerConfigurationException e ) {
         logger.severe( "Exception when writing repository description to XML:" + e );
         e.printStackTrace();
      }
      catch ( TransformerException e ) {
         logger.severe( "Exception when writing repository description to XML:" + e );
         e.printStackTrace();
      }

   }
}
