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

public class AddTransitionDialog extends Dialog {

    Transition toReturn;
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
    public AddTransitionDialog( Shell parent ) {
        super( parent );
    }

    /**
     * @param parent
     * @param style
     */
    public AddTransitionDialog( Shell parent, int style, ProtocolEditor pe ) {
        super( parent, style );
        this.pe = pe;
    }

    /**
     * Makes the dialog visible.
     * 
     * @return
     */
    public Transition open() {
        Shell parent = getParent();
        final Shell shell = new Shell( parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL );

        shell.setText( "Add Transition" );

        shell.setLayout( new GridLayout( 2, true ) );

        GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, true );

        Label label = new Label( shell, SWT.NONE );
        label.setText( "From State:" );

        fromCombo = new Combo( shell, SWT.READ_ONLY );
        fromCombo.setItems( pe.states.getItems() );
        fromCombo.setLayoutData( gridData );

        new Label( shell, SWT.NONE ).setText( "To State:" );

        toCombo = new Combo( shell, SWT.READ_ONLY );
        toCombo.setItems( pe.states.getItems() );
        toCombo.setLayoutData( gridData );

        new Label( shell, SWT.NONE ).setText( "Sender:" );

        senderCombo = new Combo( shell, SWT.READ_ONLY );
        senderCombo.setItems( pe.participants.getItems() );
        senderCombo.setLayoutData( gridData );

        new Label( shell, SWT.NONE ).setText( "Receiver:" );
        receiverCombo = new Combo( shell, SWT.READ_ONLY );
        receiverCombo.setItems( pe.participants.getItems() );
        receiverCombo.setLayoutData( gridData );

        new Label( shell, SWT.NONE ).setText( "Performative:" );

        String[] performatives = new String[] {

        "accept-proposal", "agree", "cancel", "cfp", "confirm", "disconfirm", "failure", "inform", "inform-if", "inform-ref", "not-understood", "propagate", "propose", "proxy", "query-if", "query-ref", "refuse", "reject-proposal", "request",

        "request-when", "request-whenever", "subscribe" };

        perfCombo = new Combo( shell, SWT.READ_ONLY );
        perfCombo.setItems( performatives );
        perfCombo.setLayoutData( gridData );

        new Label( shell, SWT.NONE ).setText( "Content:" );

        content = new Text( shell, SWT.SINGLE | SWT.BORDER );
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

                Transition t = new Transition( from, to );
                try {
                    t.setSender( pe.parser.parse( senderCombo.getText() ) );
                    t.setReceiver( pe.parser.parse( receiverCombo.getText() ) );

                    t.setPerformative( perfCombo.getText() );
                    t.setContent( pe.parser.parse( content.getText() ) );

                    toReturn = t;

                    shell.dispose();
                }
                catch ( MalformedTermException e ) {
                    pe.errorBox( "Content Error", "The content could not be parsed." );
                }

            }
        } );

        buttonCancel.addListener( SWT.Selection, new Listener() {
            public void handleEvent( Event event ) {
                toReturn = null;
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

        return toReturn;
    }
}
