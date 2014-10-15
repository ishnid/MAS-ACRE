package is.lill.acre.gui.swt;

import is.lill.acre.protocol.Protocol;
import is.lill.acre.protocol.util.GVProtocolFormatter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;

public class ProtocolImageHandler {

   private Image img;
   private Device device;
   private int dpi = 0;

   private static Logger logger = Logger.getLogger( ProtocolImageHandler.class.getName() );
   static {
      logger.setLevel( Level.OFF );
   }

   public ProtocolImageHandler( Device device ) {
      this.device = device;
   }

   protected synchronized InputStream generateGraph( Protocol toGraph, float width, float height ) throws IOException {

      logger.info( "Generating FSM for protocol: " + toGraph.toString() );
      logger.info( "Height: " + height + " Width: " + width );
      String gvCode = GVProtocolFormatter.formatProtocol( toGraph, width, height );

      // write graphviz code to temporary file
      // (dot won't read from stdin)
      logger.info( "Creating temporary file for GV code of protocol " + toGraph.getDescriptor().getName() );
      File tempFile = File.createTempFile( toGraph.getDescriptor().getName(), ".gv" );
      FileWriter out = new FileWriter( tempFile );
      out.write( gvCode );
      out.close();

      // run dot, piping the stdout
      Runtime rt = Runtime.getRuntime();
      Process dotProcess = rt.exec( "dot " + tempFile.getAbsolutePath() + " -Tpng" );
      return dotProcess.getInputStream();
   }
   
   public InputStream generateGraph( Protocol toGraph, int width, int height, float multiplier ) throws IOException {

      // we haven't yet figured out the dpi
      if ( dpi == 0 ) {
         try {
            this.img = new Image( device, generateGraph( toGraph, 1, 1 ) );

            dpi =
                  Math.max( img.getImageData().width, img.getImageData().height );

            logger.info( "DPI calculation uses width of "
                  + img.getImageData().width + " and height of "
                  + img.getImageData().height );

            logger.info( "DPI Calculated as " + dpi );
         }
         catch ( IOException e ) {
            logger.severe( "Failed to calculate dpi: " + e );
         }
      }

      logger.info( "Generating FSM for protocol: " + toGraph.toString()
            + " with DPI of " + dpi );

      return generateGraph( toGraph, (float) width * multiplier / dpi, (float) height
            * multiplier / dpi );

   }

}
