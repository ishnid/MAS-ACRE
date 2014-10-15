package is.lill.acre.gui.swt;

/*
 * Modified from a snippet sourced from:
 * http://dev.eclipse.org/viewcvs/index.cgi/%7Echeckout%7E/platform-swt-home/dev.html#snippets
 */
import java.io.InputStream;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

public class ProtocolImage {

   private static Logger logger = Logger.getLogger( ProtocolImage.class.getName() );
   static {
      logger.setLevel( Level.OFF );
   }

   private Composite parent;
   private Display display;
   private Image protocolImage;
   private Canvas canvas;

   public ProtocolImage( Composite parent, Display display ) {
      this.parent = parent;
      this.display = display;
   }

   public void reload( InputStream in ) {

      protocolImage = new Image( display, in );
      logger.info( "Image Width: " + protocolImage.getImageData().width );
      logger.info( "Image Height: " + protocolImage.getImageData().height );
   }

   public void text( String msg ) {
      int width = 100, height = 40;
      protocolImage = new Image( display, width, height );
      GC gc = new GC( protocolImage );
      gc.drawText( msg, 10, 10 );
      gc.dispose();
   }

   public Canvas getImageComposite() {
      canvas = new Canvas( parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL );
      canvas.setBackground( new Color( display, new RGB( 255, 255, 255 ) ) );

      int width = 100, height = 40;

      protocolImage = new Image( display, width, height );

      GC gc = new GC( protocolImage );

      gc.drawText( "No Protocol", 12, 12 );
      gc.dispose();

      final Point origin = new Point( 0, 0 );
      final ScrollBar hBar = canvas.getHorizontalBar();
      hBar.addListener( SWT.Selection, new Listener() {
         public void handleEvent( Event e ) {
            logger.info( "Horizontal Selection event" );

            int hSelection = hBar.getSelection();
            int destX = -hSelection - origin.x;
            Rectangle rect = protocolImage.getBounds();
            canvas.scroll( destX, 0, 0, 0, rect.width, rect.height, false );
            origin.x = -hSelection;
         }
      } );
      final ScrollBar vBar = canvas.getVerticalBar();
      vBar.addListener( SWT.Selection, new Listener() {
         public void handleEvent( Event e ) {
            logger.info( "Vertical Selection Event" );
            int vSelection = vBar.getSelection();
            int destY = -vSelection - origin.y;
            Rectangle rect = protocolImage.getBounds();
            canvas.scroll( 0, destY, 0, 0, rect.width, rect.height, false );
            origin.y = -vSelection;
         }
      } );
      
      canvas.addListener( SWT.Resize, new Listener() {
         public void handleEvent( Event e ) {
            logger.info( "Resize event" );

            Rectangle rect = protocolImage.getBounds();
            Rectangle client = canvas.getClientArea();
            hBar.setMaximum( rect.width );
            vBar.setMaximum( rect.height );
            hBar.setThumb( Math.min( rect.width, client.width ) );
            vBar.setThumb( Math.min( rect.height, client.height ) );
            int hPage = rect.width - client.width;
            int vPage = rect.height - client.height;
            int hSelection = hBar.getSelection();
            int vSelection = vBar.getSelection();
            if ( hSelection >= hPage ) {
               if ( hPage <= 0 )
                  hSelection = 0;
               origin.x = -hSelection;
            }
            if ( vSelection >= vPage ) {
               if ( vPage <= 0 )
                  vSelection = 0;
               origin.y = -vSelection;
            }
            canvas.redraw();
         }
      } );
      canvas.addListener( SWT.Paint, new Listener() {
         public void handleEvent( Event e ) {
            logger.info( "Paint event" );
            GC gc = e.gc;
            gc.drawImage( protocolImage, origin.x, origin.y );
            Rectangle rect = protocolImage.getBounds();
        
            Rectangle client = canvas.getClientArea();
            int marginWidth = client.width - rect.width;
            if ( marginWidth > 0 ) {
               gc.fillRectangle( rect.width, 0, marginWidth, client.height );
            }
            int marginHeight = client.height - rect.height;
            if ( marginHeight > 0 ) {
               gc.fillRectangle( 0, rect.height, client.width, marginHeight );
            }

            hBar.setMaximum( rect.width );
            vBar.setMaximum( rect.height );
            hBar.setThumb( Math.min( rect.width, client.width ) );
            vBar.setThumb( Math.min( rect.height, client.height ) );
            int hPage = rect.width - client.width;
            int vPage = rect.height - client.height;
            int hSelection = hBar.getSelection();
            int vSelection = vBar.getSelection();
            if ( hSelection >= hPage ) {
               if ( hPage <= 0 )
                  hSelection = 0;
               origin.x = -hSelection;
            }
            if ( vSelection >= vPage ) {
               if ( vPage <= 0 )
                  vSelection = 0;
               origin.y = -vSelection;
            }
         }
      } );

      return canvas;
   }

}
