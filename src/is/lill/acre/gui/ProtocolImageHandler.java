package is.lill.acre.gui;

import is.lill.acre.protocol.Protocol;
import is.lill.acre.protocol.util.GVProtocolFormatter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ProtocolImageHandler {

   private static Logger logger = Logger.getLogger( ProtocolImageHandler.class.getName() );
   static {
      logger.setLevel( Level.WARNING );
   }

   private JLabel imageLabel;
   private ImageIcon img;
   protected int dpi = 0;

   public ProtocolImageHandler() {
      this.img = new ImageIcon();

      this.imageLabel = new JLabel( img );
   }

   public void redrawGraph( Protocol toGraph, float multiplier ) {

      // we haven't yet figured out the dpi
      if ( dpi == 0 ) {
         logger.info( "Performing Swing calculations for DPI" );
         try {
            BufferedImage img = generateImage( toGraph, 1, 1 );
            dpi = Math.max( img.getWidth(), img.getHeight() );
            logger.info( "DPI Calculated as " + dpi );
         }
         catch ( IOException e ) {
            logger.severe( "Failed to calculate dpi: " + e );
         }
      }

      logger.info( "Generating FSM for protocol: " + toGraph.toString() );

      try {

         BufferedImage imageData = generateImage( toGraph, (float) this.imageLabel.getWidth() * multiplier / dpi, (float) this.imageLabel.getHeight() * multiplier / dpi );
         img.setImage( imageData );
         imageLabel.repaint();
      }
      catch ( IOException e ) {
         logger.severe( "Protocol Diagram generation failed: " + e );
      }
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

   private synchronized BufferedImage generateImage( Protocol toGraph, float width, float height ) throws IOException {

      // read dot's output into a BufferedImage
      return ImageIO.read( generateGraph( toGraph, width, height ) );
   }

   public JLabel getImageLabel() {
      return this.imageLabel;
   }
}
