package is.lill.acre.gui.swt;

import is.lill.acre.protocol.util.ProtocolVerifier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AddStateDialog extends Dialog {
   String value;
   private ProtocolEditor pe;

   /**
    * @param parent
    */
   public AddStateDialog( Shell parent ) {
      super( parent );
   }

   /**
    * @param parent
    * @param style
    */
   public AddStateDialog( Shell parent, int style, ProtocolEditor pe ) {
      super( parent, style );
      this.pe = pe;
   }

   /**
    * Makes the dialog visible.
    * 
    * @return
    */
   public String open() {
      Shell parent = getParent();
      final Shell shell = new Shell( parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL );

      shell.setText( "Add State" );

      shell.setLayout( new GridLayout( 3, true ) );

      Label label = new Label( shell, SWT.NONE );
      label.setText( "Name of the new state:" );
      GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, false );
      gridData.horizontalSpan = 3;
      label.setLayoutData( gridData );

      final Text text = new Text( shell, SWT.SINGLE | SWT.BORDER );
      gridData = new GridData( SWT.FILL, SWT.FILL, true, false );
      gridData.horizontalSpan = 3;
      text.setLayoutData( gridData );

      // skip one position
      new Label( shell, SWT.NONE ).setText( "" );

      final Button buttonOK = new Button( shell, SWT.PUSH );
      buttonOK.setText( "Ok" );
      buttonOK.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
      Button buttonCancel = new Button( shell, SWT.PUSH );
      buttonCancel.setText( "Cancel" );
      buttonCancel.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );

      text.addListener( SWT.Traverse, new Listener() {
         public void handleEvent( Event event ) {
            if ( event.detail == SWT.TRAVERSE_RETURN ) {
               if ( verify( text.getText() ) ) {
                  value = text.getText();
                  shell.dispose();
               }
            }
         }
      } );

      buttonOK.addListener( SWT.Selection, new Listener() {
         public void handleEvent( Event event ) {
            if ( verify( text.getText() ) ) {
               value = text.getText();
               shell.dispose();
            }
         }
      } );

      buttonCancel.addListener( SWT.Selection, new Listener() {
         public void handleEvent( Event event ) {
            value = null;
            shell.dispose();
         }
      } );

      text.setText( "" );
      shell.pack();
      shell.open();

      Display display = parent.getDisplay();
      while ( !shell.isDisposed() ) {
         if ( !display.readAndDispatch() )
            display.sleep();
      }

      return value;
   }

   private boolean verify( String text ) {
      if ( !ProtocolVerifier.verifyField( ProtocolVerifier.STATE_NAME, text ) ) {
         pe.errorBox( "Invalid State Name", "The state name \"" + text + "\" could not be parsed.\nState names may only include alphanumeric characters or hyphens." );
         return false;
      }

      else if ( pe.protocol.getStateByName( text ) != null ) {

         pe.errorBox( "State Exists", "A state named \"" + text + "\" already exists" );
         return false;
      }
      else {
         return true;
      }
   }
}
