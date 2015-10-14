package is.lill.acre.protocol;

import is.lill.acre.exception.MalformedVersionException;

import java.util.logging.Logger;

public class ProtocolVersion {

   private static Logger logger = Logger.getLogger( ProtocolVersion.class.getName() );

   private int majorVersion = -1;
   private int minorVersion = -1;

   private String version;

   public ProtocolVersion( String version ) {
      this.set( version );
   }

   public boolean isLaterThan( ProtocolVersion version ) {
      return version == null || this.majorVersion > version.majorVersion || ( this.majorVersion == version.majorVersion && this.minorVersion > version.minorVersion );
   }

   public boolean equals( ProtocolVersion version ) {
      return this.version.equals( version.version );
   }

   public String toString() {
      return this.majorVersion + "." + this.minorVersion;
   }

   public ProtocolVersion clone() {
      return new ProtocolVersion( this.toString() );
   }

   public void set( String version ) {
      if ( !version.matches( "\\d+\\.\\d+" ) ) {
         throw new MalformedVersionException( version );
      }
      this.version = version;
      String[] pieces = version.split( "\\." );
      if ( pieces.length == 2 ) {
         try {
            this.majorVersion = Integer.parseInt( pieces[ 0 ] );
         }
         catch ( NumberFormatException e ) {
            logger.warning( "Cannot parse version: " + pieces[ 0 ] );
         }
         try {
            this.minorVersion = Integer.parseInt( pieces[ 1 ] );
         }
         catch ( NumberFormatException e ) {
            logger.warning( "Cannot parse version: " + pieces[ 1 ] );
         }
      }
      else {
         logger.warning( "Unparseable version: " + pieces );
      }
   }
}
