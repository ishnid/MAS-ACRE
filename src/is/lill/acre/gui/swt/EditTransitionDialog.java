package is.lill.acre.gui.swt;

import is.lill.acre.logic.MalformedTermException;
import is.lill.acre.protocol.State;
import is.lill.acre.protocol.Transition;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class EditTransitionDialog extends Dialog {

   Transition transition;
   ProtocolEditor pe;
   private Combo perfCombo;
   private Text content;
   private Combo senderCombo;
   private Combo toCombo;
   private Combo fromCombo;
   private Combo receiverCombo;

   /**
    * @param parent
    */
   public EditTransitionDialog( Shell parent ) {
      super( parent );
   }

   /**
    * @param parent
    * @param style
    */
   public EditTransitionDialog( Shell parent, int style, ProtocolEditor pe, Transition t ) {
      super( parent, style );
      this.pe = pe;
      this.transition = t;
   }

   /**
    * Makes the dialog visible.
    * 
    * @return
    */
   public boolean open() {
      Shell parent = getParent();
      final Shell shell = new Shell( parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL );

      shell.setText( "Edit Transition" );

      shell.setLayout( new GridLayout( 2, true ) );

      GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, false );

      Label label = new Label( shell, SWT.NONE );
      label.setText( "From State:" );

      fromCombo = new Combo( shell, SWT.READ_ONLY );
      fromCombo.setItems( pe.states.getItems() );
      fromCombo.setText( transition.getStartState().getName() );

      fromCombo.setLayoutData( gridData );

      new Label( shell, SWT.NONE ).setText( "To State:" );

      toCombo = new Combo( shell, SWT.READ_ONLY );
      toCombo.setItems( pe.states.getItems() );
      toCombo.setLayoutData( gridData );
      toCombo.setText( transition.getEndState().getName() );

      new Label( shell, SWT.NONE ).setText( "Sender:" );

      senderCombo = new Combo( shell, SWT.READ_ONLY );
      senderCombo.setItems( pe.participants.getItems() );
      senderCombo.setLayoutData( gridData );
      senderCombo.setText( pe.formatter.format( transition.getSender() ) );

      new Label( shell, SWT.NONE ).setText( "Receiver:" );
      receiverCombo = new Combo( shell, SWT.READ_ONLY );
      receiverCombo.setItems( pe.participants.getItems() );
      receiverCombo.setLayoutData( gridData );
      receiverCombo.setText( pe.formatter.format( transition.getReceiver() ) );

      new Label( shell, SWT.NONE ).setText( "Performative:" );

      String[] performatives = new String[] {

      "accept-proposal", "agree", "cancel", "cfp", "confirm", "disconfirm", "failure", "inform", "inform-if", "inform-ref", "not-understood", "propagate", "propose", "proxy", "query-if", "query-ref", "refuse", "reject-proposal", "request",

      "request-when", "request-whenever", "subscribe" };

      perfCombo = new Combo( shell, SWT.READ_ONLY );
      perfCombo.setItems( performatives );
      perfCombo.setLayoutData( gridData );
      perfCombo.setText( transition.getPerformative() );

      new Label( shell, SWT.NONE ).setText( "Content:" );

      content = new Text( shell, SWT.SINGLE | SWT.BORDER );
      content.setText( pe.formatter.format( transition.getContent() ) );
      content.setLayoutData( gridData );

      final Button buttonOK = new Button( shell, SWT.PUSH );
      buttonOK.setText( "Ok" );

      gridData = new GridData( SWT.FILL, SWT.END, true, false );
      buttonOK.setLayoutData( gridData );
      Button buttonCancel = new Button( shell, SWT.PUSH );
      buttonCancel.setText( "Cancel" );
      buttonCancel.setLayoutData( gridData );
      buttonOK.addListener( SWT.Selection, new Listener() {
         public void handleEvent( Event event ) {

            State from = pe.protocol.getStateByName( fromCombo.getText() );
            if ( from == null ) {
               from = new State( fromCombo.getText() );
            }
            State to = pe.protocol.getStateByName( toCombo.getText() );
            if ( to == null ) {
               to = new State( toCombo.getText() );
            }

            try {
               transition.setStartState( from );
               transition.setEndState( to );
               transition.setSender( pe.parser.parse( senderCombo.getText() ) );
               transition.setReceiver( pe.parser.parse( receiverCombo.getText() ) );

               transition.setPerformative( perfCombo.getText() );
               transition.setContent( pe.parser.parse( content.getText() ) );

               shell.dispose();
            }
            catch ( MalformedTermException e ) {
               pe.errorBox( "Content Error",  "The content could not be parsed." );

            }

         }
      } );

      buttonCancel.addListener( SWT.Selection, new Listener() {
         public void handleEvent( Event event ) {
            // toReturn = null;
            shell.dispose();
         }
      } );

      shell.pack();
      shell.open();

      Display display = parent.getDisplay();
      while ( !shell.isDisposed() ) {
         if ( !display.readAndDispatch() )
            display.sleep();
      }

      return true;
   }
}
