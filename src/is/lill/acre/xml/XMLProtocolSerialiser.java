package is.lill.acre.xml;

import is.lill.acre.exception.InvalidProtocolException;
import is.lill.acre.exception.InvalidSchemaException;
import is.lill.acre.exception.ProtocolParseException;
import is.lill.acre.logic.ITermFormatter;
import is.lill.acre.logic.ITermParser;
import is.lill.acre.logic.MalformedTermException;
import is.lill.acre.logic.Term;
import is.lill.acre.logic.Utilities;
import is.lill.acre.protocol.IProtocolManager;
import is.lill.acre.protocol.Protocol;
import is.lill.acre.protocol.ProtocolDescriptor;
import is.lill.acre.protocol.ProtocolVersion;
import is.lill.acre.protocol.RepositoryException;
import is.lill.acre.protocol.State;
import is.lill.acre.protocol.Transition;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class to write/read ACRE Protocols to/from XML files
 * 
 * @author daithi
 * @version $Id$
 */
public class XMLProtocolSerialiser {

    private static final Logger logger = Logger.getLogger( XMLProtocolSerialiser.class.getName() );

    static {
        logger.setLevel( Level.WARNING );
    }

    /**
     * Read a protocol from an InputStream. A ProtocolManager should be provided
     * as it will be consulted if any imported protocols are found.
     * 
     * @param in
     * @param pm
     *            The{@code ProtocolManager} to be used if imports are required.
     *            No imports will occur if this is {@code null}.
     * @return
     * @throws IOException
     */
    public static Protocol readProtocol( InputStream in, IProtocolManager pm ) throws ProtocolParseException, IOException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware( true );

        Document doc;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse( in );
        }
        catch ( SAXException e ) {
            throw new ProtocolParseException( "Invalid XML" );
        }
        catch ( ParserConfigurationException e ) {
            throw new ProtocolParseException( "Failed to set up XML parser" );
        }
        catch ( IOException e ) {
            throw new ProtocolParseException( "Failed to read protocol XML" );
        }

        // only import protocols that are valid according to the schema
        Source source = new DOMSource( doc );
        SchemaFactory fac = SchemaFactory.newInstance( "http://www.w3.org/2001/XMLSchema" );

        // the URL to this is in the XML file, but
        // a) if it doesn't follow this schema, this code won't be able to
        // parse it anyway AND
        // b) this doesn't require an internet connection
        URL schemaURL = XMLProtocolSerialiser.class.getResource( "/is/lill/acre/xml/protocol.xsd" );

        try {
            Schema schema = fac.newSchema( schemaURL );
            Validator val = schema.newValidator();
            try {
                val.validate( source );
            }
            catch ( SAXException e ) {
                e.printStackTrace();
                throw new InvalidProtocolException( e.getMessage() );
            }
        }
        catch ( SAXException e ) {
            throw new InvalidSchemaException( e );
        }
        logger.info( "Protocol validated" );

        NodeList protocolNodes = doc.getElementsByTagName( "protocol" );
        if ( protocolNodes.getLength() > 1 ) {
            logger.warning( "Multiple protocols defined in one file: only the first will be read" );
        }
        else if ( protocolNodes.getLength() == 0 ) {
            logger.warning( "No <protocol> tag found: aborting" );
        }
        else {
            Protocol toReturn = new Protocol();
            ProtocolDescriptor desc = new ProtocolDescriptor();

            NodeList children = protocolNodes.item( 0 ).getChildNodes();
            NodeList states = null;
            NodeList transitions = null;

            // transitions may not be above states in the protocol
            // but states must be processed first, so find the relevant
            // node lists first and process later
            for ( int i = 0; i < children.getLength(); i++ ) {
                Node child = children.item( i );

                if ( child.getNodeName().equals( "namespace" ) && child.hasChildNodes() ) {
                    desc.setNamespace( child.getFirstChild().getTextContent().trim() );
                    logger.info( "Protocol namespace: " + desc.getNamespace() );
                }
                else if ( child.getNodeName().equals( "name" ) && child.hasChildNodes() ) {
                    desc.setName( child.getFirstChild().getTextContent().trim() );
                    logger.info( "Protocol name: " + desc.getName() );
                }
                else if ( child.getNodeName().equals( "version" ) && child.hasChildNodes() ) {
                    desc.setVersion( new ProtocolVersion( child.getFirstChild().getTextContent().trim() ) );
                    logger.info( "Protocol version: " + desc.getVersion() );
                }
                else if ( child.getNodeName().equals( "description" ) && child.hasChildNodes() ) {
                    toReturn.setDescription( child.getFirstChild().getTextContent().trim() );
                    logger.info( "Protocol description: " + toReturn.getDescription() );
                }
                else if ( child.getNodeName().equals( "states" ) ) {
                    states = child.getChildNodes();
                }
                else if ( child.getNodeName().equals( "transitions" ) ) {
                    transitions = child.getChildNodes();
                }
                else if ( child.getNodeName().equals( "import" ) ) {

                    ProtocolDescriptor toImport = new ProtocolDescriptor();
                    NodeList importDetails = child.getChildNodes();
                    for ( int j = 0; j < importDetails.getLength(); j++ ) {
                        Node importChild = importDetails.item( j );
                        if ( importChild.getNodeName().equals( "namespace" ) && importChild.hasChildNodes() ) {
                            toImport.setNamespace( importChild.getFirstChild().getTextContent().trim() );
                        }
                        else if ( importChild.getNodeName().equals( "name" ) && importChild.hasChildNodes() ) {
                            toImport.setName( importChild.getFirstChild().getTextContent().trim() );
                        }
                        else if ( importChild.getNodeName().equals( "version" ) && importChild.hasChildNodes() ) {
                            toImport.setVersion( new ProtocolVersion( importChild.getFirstChild().getTextContent().trim() ) );
                        }
                        else if ( importChild.getNodeType() == Node.ELEMENT_NODE ) {
                            logger.warning( "Unrecognised tag in <import>: " + importChild.getNodeName() );
                        }
                    }
                    if ( toImport.isValid() && pm != null ) {
                        logger.info( "Importing protocol " + toImport.getUniqueID() );
                        try {
                            Protocol imported = pm.getProtocolByDescriptor( toImport );
                            if ( imported != null ) {
                                toReturn.importProtocol( imported );
                            }
                            else {
                                logger.severe( "Import failed due to unknown protocol: " + toImport.toString() );
                                throw new ProtocolParseException( "Unknown import: " + toImport.toString() );
                            }
                        }
                        catch ( RepositoryException e ) {
                            logger.severe( "Import failed due to repository exceptions: " + toImport.toString() );
                            throw new ProtocolParseException( "Unable to import: " + toImport.toString() );
                        }
                    }
                    else {
                        logger.severe( "Import failed due to invalid descriptor: " + toImport.toString() );
                        throw new ProtocolParseException( "Invalid import descriptor: " + toImport.toString() );
                    }
                }
                else if ( child.getNodeType() == Node.ELEMENT_NODE ) {
                    logger.warning( "Unhandled XML tag in protocol: [" + child.getNodeName() + "]: ignoring" );
                }
            }

            toReturn.setDescriptor( desc );

            // add any states found in this file
            if ( states != null ) {
                for ( int i = 0; i < states.getLength(); i++ ) {
                    if ( states.item( i ).getNodeType() == Node.ELEMENT_NODE ) {

                        NamedNodeMap attrs = states.item( i ).getAttributes();
                        Node nameNode = attrs.getNamedItem( "name" );
                        if ( nameNode != null ) {
                            toReturn.addState( new State( nameNode.getNodeValue() ) );
                            logger.info( "State [" + nameNode.toString() + "] added" );
                        }
                        else {
                            logger.severe( "State has no name (and SHOULD NOT HAVE VALIDATED): skipping" );
                        }
                    }
                }
            }

            // between this file's states and imports, the protocol needs to
            // have
            // states
            if ( toReturn.getStates().isEmpty() ) {
                logger.severe( "Protocol has no states" );
                throw new ProtocolParseException( "No states" );
            }

            // add any transitions found in this file
            if ( transitions != null ) {
                for ( int i = 0; i < transitions.getLength(); i++ ) {

                    if ( transitions.item( i ).getNodeType() == Node.ELEMENT_NODE ) {

                        NamedNodeMap attrs = transitions.item( i ).getAttributes();
                        Node startNode = attrs.getNamedItem( Transition.FROM_STATE );
                        Node endNode = attrs.getNamedItem( Transition.TO_STATE );
                        Node senderNode = attrs.getNamedItem( Transition.SENDER );
                        Node recipientNode = attrs.getNamedItem( Transition.RECEIVER );
                        Node performativeNode = attrs.getNamedItem( Transition.PERFORMATIVE );
                        Node contentNode = attrs.getNamedItem( Transition.CONTENT );
                        if ( startNode == null ) {
                            logger.severe( "No 'from-state' attribute in Transtion: skipping" );
                        }
                        else if ( endNode == null ) {
                            logger.severe( "No 'to-state' attribute in Transition: skipping" );
                        }
                        else if ( performativeNode == null ) {
                            logger.severe( "No 'performative' attribute in Transition: skipping" );
                        }
                        else {
                            try {
                                if ( senderNode == null ) {
                                    logger.info( "No 'sender' attribute in Transition: using default value" );
                                }
                                else if ( recipientNode == null ) {
                                    logger.info( "No 'receiver' attribute in Transition: using default value" );
                                }

                                else if ( contentNode == null ) {
                                    logger.info( "No 'content' attribute in Transition: using default value" );
                                }

                                ITermParser p = Utilities.getTermParser( "acre" );
                                Term startState = p.parse( startNode.getNodeValue() );
                                if ( startState.isConstant() ) {
                                    String startStateName = startNode.getNodeValue();

                                    // start state is a regexp
                                    if ( startStateName.startsWith( "/" ) ) {

                                    }

                                    String endStateName = endNode.getNodeValue();

                                    if ( toReturn.getStateByName( startStateName ) != null && toReturn.getStateByName( endStateName ) != null ) {

                                        Transition t = new Transition( toReturn.getStateByName( startStateName ), toReturn.getStateByName( endStateName ) );
                                        t.setPerformative( performativeNode.getNodeValue() );
                                        t.setSender( p.parse( senderNode == null ? "?" : senderNode.getNodeValue() ) );
                                        t.setReceiver( p.parse( recipientNode == null ? "?" : recipientNode.getNodeValue() ) );
                                        t.setContent( p.parse( contentNode == null ? "?" : contentNode.getNodeValue() ) );
                                        toReturn.addTransition( t );
                                        logger.info( "Transition [" + startStateName + " -> " + endStateName + "] added" );
                                    }
                                    else {
                                        logger.severe( "Transition [" + startStateName + " -> " + endStateName + "] references unknown state" );
                                        throw new ProtocolParseException( "Unknown state in transition" );
                                    }
                                }

                                // anonymous variable used, so create transition
                                // from all states except the one that's the end
                                // state of this transition
                                else if ( startState.isAnonymousVariable() ) {
                                    for ( State s : toReturn.getStates() ) {
                                        // don't create loops, but match any
                                        // other
                                        // node
                                        if ( !s.getName().equals( endNode.getNodeValue() ) ) {
                                            Transition t = new Transition( s, toReturn.getStateByName( endNode.getNodeValue() ) );
                                            t.setPerformative( performativeNode.getNodeValue() );
                                            t.setSender( p.parse( senderNode.getNodeValue() ) );
                                            t.setReceiver( p.parse( recipientNode.getNodeValue() ) );
                                            t.setContent( p.parse( contentNode.getNodeValue() ) );
                                            toReturn.addTransition( t );
                                            logger.info( "Transition [" + t.getStartState().getName() + " -> " + t.getEndState().getName() + "] added" );
                                        }
                                    }
                                }
                                else {
                                    logger.severe( "Transition [" + startNode.getNodeValue() + " -> " + endNode.getNodeValue() + "] failed: " + startNode.getNodeValue() + " must be a constant or the anonymous variable" );
                                }

                            }
                            catch ( MalformedTermException e ) {
                                logger.severe( "Transition [" + startNode.getNodeValue() + " -> " + endNode.getNodeValue() + "] failed" );
                            }
                        }
                    }
                }
            }

            // no protocol without transitions
            if ( toReturn.getTransitions().isEmpty() ) {
                logger.severe( "Protocol has no transitions" );
                throw new ProtocolParseException( "No transitions" );
            }

            return toReturn;
        }

        return null;
    }

    public static void writeProtocol( Protocol p, OutputStream out ) throws IOException {

        ITermFormatter formatter = Utilities.getTermFormatter( "acre" );

        try {
            logger.info( "Creating XML document" );
            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            doc.setXmlStandalone( true );

            Element prot = doc.createElement( "protocol" );

            prot.setAttribute( "xmlns", "http://acre.lill.is" );
            prot.setAttribute( "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance" );
            prot.setAttribute( "xsi:schemaLocation", "http://acre.lill.is http://acre.lill.is/protocol.xsd" );
            doc.appendChild( prot );

            Element namespace = doc.createElement( "namespace" );
            namespace.setTextContent( p.getDescriptor().getNamespace() );
            prot.appendChild( namespace );

            Element name = doc.createElement( "name" );
            name.setTextContent( p.getDescriptor().getName() );
            prot.appendChild( name );

            Element version = doc.createElement( "version" );
            version.setTextContent( p.getDescriptor().getVersion().toString() );
            prot.appendChild( version );
            
            if ( p.getDescription() != null ) {
              Element description = doc.createElement( "description" );
              description.setTextContent( p.getDescription() );
              prot.appendChild( description );
            }

            if ( p.getImport() != null ) {
                Element imported = doc.createElement( "import" );

                Element iNamespace = doc.createElement( "namespace" );
                iNamespace.setTextContent( p.getImport().getNamespace() );
                imported.appendChild( iNamespace );

                Element iName = doc.createElement( "name" );
                iName.setTextContent( p.getImport().getName() );
                imported.appendChild( iName );

                Element iVersion = doc.createElement( "version" );
                iVersion.setTextContent( p.getImport().getVersion().toString() );
                imported.appendChild( iVersion );

                prot.appendChild( imported );
            }

            Element states = doc.createElement( "states" );
            prot.appendChild( states );

            Element transitions = doc.createElement( "transitions" );
            prot.appendChild( transitions );

            logger.info( "Creating states" );
            for ( State s : p.getStates() ) {
                if ( !s.isImported() ) {
                    Element state = doc.createElement( "state" );
                    state.setAttribute( "name", s.getName() );
                    states.appendChild( state );
                }
            }

            logger.info( "Creating transitions" );
            for ( Transition pt : p.getTransitions() ) {
                if ( !pt.isImported() ) {
                    Element trans = doc.createElement( "transition" );
                    trans.setAttribute( Transition.PERFORMATIVE, pt.getPerformative() );
                    trans.setAttribute( Transition.FROM_STATE, pt.getStartState().getName() );
                    trans.setAttribute( Transition.TO_STATE, pt.getEndState().getName() );
                    trans.setAttribute( Transition.SENDER, formatter.format( pt.getSender() ) );
                    trans.setAttribute( Transition.RECEIVER, formatter.format( pt.getReceiver() ) );
                    trans.setAttribute( Transition.CONTENT, formatter.format( pt.getContent() ) );

                    transitions.appendChild( trans );
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
            logger.severe( "Exception when writing protocol to XML:" + e );
            e.printStackTrace();
        }
        catch ( TransformerConfigurationException e ) {
            logger.severe( "Exception when writing protocol to XML:" + e );
            e.printStackTrace();
        }
        catch ( TransformerException e ) {
            logger.severe( "Exception when writing protocol to XML:" + e );
            e.printStackTrace();
        }

    }
}
