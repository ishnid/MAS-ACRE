package is.lill.acre.protocol;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to store details of a protocol descriptor, consisting of a namespace, a name and a version number.
 * @author daithi
 * @version $Id$
 */
public class ProtocolDescriptor {

   private ProtocolVersion version = null;
   private String namespace = null;
   private String name = null;

   private static final Logger logger = Logger.getLogger( ProtocolDescriptor.class.getName() );

   static {
      logger.setLevel( Level.WARNING );
   }

   /**
    * Set the namespace associated with this protocol
    * @param namespace The namespace, in a similar format to Java package names
    */
   public void setNamespace( String namespace ) {
      this.namespace = namespace;
   }

   /**
    * Test if this is a valid protocol descriptor, in which all three elements are present
    * @return {@code true} if the descriptor is valid, {@code false} otherwise.
    */
   public boolean isValid() {
      return this.getNamespace() != null && this.getName() != null && this.getVersion() != null;
   }

   /**
    * Get the protocol's name
    * 
    * @return The protocol's name
    */
   public String getName() {
      return this.name;
   }

   /**
    * Set the protocol's name
    * 
    * @param name
    *           A name to give the protocol
    */
   public void setName( String name ) {
      if ( name == null ) {
         logger.warning( "Null name provided for protocol" );
      }
      // this.imports.add( name );
      this.name = name;
   }

   /**
    * Get the namespace this protocol belongs to
    * @return
    */
   public String getNamespace() {
      return namespace;
   }

   /**
    * Set the protocol version
    * @param version
    */
   public void setVersion( ProtocolVersion version ) {
      this.version = version;
   }

   /**
    * Get the protocol version
    * @return
    */
   public ProtocolVersion getVersion() {
      return this.version;
   }

   /**
    * Get the unique identifier of this protocol
    * @return The unique identifier, in the form <i>namespace</i>_<i>name</i>_<i>version</i>
    */
   public String getUniqueID() {
      return this.getNamespace() + "_" + this.getName() + "_" + this.getVersion().toString();
   }

   /**
    * Check if this describes the same protocol as the object provided
    * @param o The object to compare, which should be an instance of {@link ProtocolDescriptor}
    */
   @Override
   public boolean equals( Object o ) {
      if ( o instanceof ProtocolDescriptor ) {
         return this.getUniqueID().equals( ( (ProtocolDescriptor) o ).getUniqueID() );
      }
      return false;
   }

   /**
    * Return the hashCode
    */
   @Override
   public int hashCode() {
      return this.getUniqueID().hashCode();
   }

   /**
    * Get a string representation of this object
    * @return 
    */
   @Override
   public String toString() {
      return this.getNamespace() + "_" + this.getName() + "_" + this.getVersion();
   }

   /**
    * Parse a string to create a new {@link ProtocolDescriptor} object.
    * @param string
    * @return
    */
   public static ProtocolDescriptor parseString( String string ) {

      String[] parts = string.split( "_" );

      // not valid
      if ( parts.length > 3 ) {
         return null;
      }
      else {
         ProtocolDescriptor toReturn = new ProtocolDescriptor();

         // all three fields set, so namespace_name_version
         if ( parts.length == 3 ) {
            toReturn.setNamespace( parts[ 0 ] );
            toReturn.setName( parts[ 1 ] );
            toReturn.setVersion( new ProtocolVersion( parts[ 2 ] ) );
         }
         else if ( parts.length == 2 ) {
            try {
               // second part parseable as a double, so it must be the version
               Double.parseDouble( parts[ 1 ] );
               toReturn.setName( parts[ 0 ] );
               toReturn.setVersion( new ProtocolVersion( parts[ 1 ] ) );
            }
            // it wasn't parseable so we assume there's no version number
            catch ( NumberFormatException e ) {
               toReturn.setNamespace( parts[ 0 ] );
               toReturn.setName( parts[ 1 ] );
            }
         }
         // just one set: assumed to be the protocol name
         else if ( parts.length == 1 ) {
            toReturn.setName( parts[ 0 ] );
         }
         return toReturn;
      }
   }

   /**
    * Test if this matches another {@link ProtocolDescriptor} (i.e. the namespace, name and version are all equal).
    * @param pd
    * @return
    */
   public boolean matches( ProtocolDescriptor pd ) {
      return ( this.name == null || pd.name == null || this.name.equals( pd.name ) ) && ( this.namespace == null || pd.namespace == null || this.namespace.equals( pd.namespace ) ) && ( this.version == null || pd.version == null || this.version.equals( pd.version ) );
   }

   /**
    * Get a clone of this object
    */
   public ProtocolDescriptor clone() {
      ProtocolDescriptor toReturn = new ProtocolDescriptor();
      toReturn.setName( this.getName() );
      toReturn.setNamespace( this.getNamespace() );
      toReturn.setVersion( this.getVersion().clone() );
      return toReturn;
   }
}
