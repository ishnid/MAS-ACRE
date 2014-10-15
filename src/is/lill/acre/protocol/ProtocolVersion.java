package is.lill.acre.protocol;

import is.lill.acre.exception.MalformedVersionException;

public class ProtocolVersion {
   private int majorVersion = -1;
   private int minorVersion = -1;
   
   private String version;
   
   public ProtocolVersion( String version ) {
     this.set( version );
   }
   
   public boolean isLaterThan( ProtocolVersion version ) {
      return version == null || this.majorVersion > version.majorVersion || (this.majorVersion == version.majorVersion && this.minorVersion > version.minorVersion);
   }
   
   public boolean equals( ProtocolVersion version ) {
      return this.version.equals(version.version);
   }
   
   public String toString() {
      return this.majorVersion + "." + this.minorVersion;
   }
   
   public ProtocolVersion clone() 
   {
      return new ProtocolVersion( this.toString() );
   }
   
   public void set( String version ) {
       if ( ! version.matches( "\\d+\\.\\d+" ) ) {
           throw new MalformedVersionException( version );
       }
       this.version = version;
       String[] pieces=  version.split("\\.");
       this.majorVersion = Integer.parseInt( pieces[0] );
       this.minorVersion = Integer.parseInt( pieces[1] );
   }
}
