package is.lill.acre.gui.swt;

import is.lill.acre.logic.ITermFormatter;
import is.lill.acre.logic.ITermParser;
import is.lill.acre.logic.MalformedTermException;
import is.lill.acre.logic.Term;
import is.lill.acre.logic.Utilities;
import is.lill.acre.protocol.Protocol;
import is.lill.acre.protocol.ProtocolDescriptor;
import is.lill.acre.protocol.ProtocolVersion;
import is.lill.acre.protocol.RepositoryException;
import is.lill.acre.protocol.State;
import is.lill.acre.protocol.Transition;
import is.lill.acre.protocol.util.ProtocolVerifier;
import is.lill.acre.xml.XMLProtocolSerialiser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * Editor for a single protocol
 * 
 * @author daithi
 */
public class ProtocolEditor {

   static Logger logger = Logger.getLogger( ProtocolEditor.class.getName() );

   static {
      logger.setLevel( Level.OFF );
   }

   Protocol protocol = new Protocol();

   private boolean changed = false;

   public boolean isChanged() {
      return changed;
   }

   public void setChanged( boolean changed ) {
      this.changed = changed;
      updateTitle();
   }

   public void updateTitle() {
      if ( changed ) {
         shell.setText( getTitle() + "*" );
      }
      else if ( !changed ) {
         shell.setText( getTitle() );
      }
   }

   private String getTitle() {
      ProtocolDescriptor pd = protocol.getDescriptor();
      return SHELL_TEXT + " " + pd.getNamespace() + "." + pd.getName() + " v" + pd.getVersion().toString();
   }

   // formatter and parser for dealing with ACRE terms
   ITermFormatter formatter = Utilities.getTermFormatter( "acre" );
   ITermParser parser = Utilities.getTermParser( "acre" );

   protected ProtocolImage it;
   protected ProtocolImageHandler pih;
   protected java.util.List<Transition> transitionList = new ArrayList<Transition>();;
   protected ProtocolEditor pe;

   protected Shell shell;

   String lastDir;
   
   // allow access to widgets throughout the application
   Text name;
   Text namespace;
   Text version;
   List states;
   GC protocolGC;
   Text ownerName;
   Text ownerPhone;
   TableColumn[] tcols;
   Table transitions;
   MenuItem openItem;
   Canvas protocolImg;
   MenuItem quitItem;
   List participants;
   Set<String> participantSet = new HashSet<String>();;
   MenuItem saveItem;
   MenuItem asItem;
   MenuItem newItem;
   MenuItem exportItem;
   Spinner spinner;

   String SHELL_TEXT = "ACRE Protocol Editor";

   RepositoryManager rm = null;

   // set this to true if the image failed to generate (probably means the 'dot'
   // utility is missing).
   private boolean noDot = false;

   MenuItem closeItem;

   private Button addState;

   private Button removeState;

   private Button addParticipant;

   private Button removeParticipant;

   private Button addTransition;

   private Button editTransition;

   private Button removeTransition;

   public static void main( String[] args ) {
      Display display = new Display();

      ProtocolEditor pe = new ProtocolEditor( display );

      pe.initComponents();
      pe.refresh();
      pe.shell.setSize( 1024, 600 );
      pe.shell.open();
      pe.setChanged( false );

      while ( !pe.shell.isDisposed() ) {
         if ( !display.readAndDispatch() )
            display.sleep();
      }
   }

   /**
    * Create a standalone protocol editor
    * 
    * @param display
    */
   public ProtocolEditor( Display display ) {
      this.shell = new Shell( display );
      this.pe = this;
      logger.info( "Created new standalone protocol editor" );
   }

   /**
    * Create a protocol editor based on a repository manager
    * 
    * @param rm
    */
   public ProtocolEditor( RepositoryManager rm ) {
      shell = new Shell( rm.shell.getDisplay() );
      this.pe = this;
      this.rm = rm;
      logger.info( "Created new protocol editor" );
   }

   public void initComponents() {

      shell.setImage( new Image( shell.getDisplay(), ClassLoader.class.getResourceAsStream( "/is/lill/acre/gui/swt/acre-icon.png" ) ) );

      // to handle the generation of FSMs
      pih = new ProtocolImageHandler( shell.getDisplay() );

      shell.setText( SHELL_TEXT );
      GridLayout gridLayout = new GridLayout();
      gridLayout.numColumns = 2;
      shell.setLayout( gridLayout );

      // create the menu
      createMenu();

      // group on the left-hand-side, with editing widgets
      Group left = new Group( shell, SWT.BORDER );
      left.setText( "Protocol Definition" );
      gridLayout = new GridLayout();
      gridLayout.numColumns = 2;
      left.setLayout( gridLayout );

      // create the section with name, namespace and version
      createNameComposite( left );

      // create the section with the list of states, with their add/remove
      // buttons
      createStatesGroup( left );

      // create the section with the list of participants, with their
      // add/remove
      // buttons
      createParticipantsGroup( left );

      // create group for all widgets relating to transitions
      createTransitionsGroup( left );

      GridData gridData = new GridData( GridData.FILL, GridData.FILL, true, true );
      left.setLayoutData( gridData );

      Composite right = new Composite( shell, SWT.BORDER );
      gridLayout = new GridLayout();
      gridLayout.numColumns = 1;
      right.setLayout( gridLayout );

      it = new ProtocolImage( right, shell.getDisplay() );

      protocolImg = it.getImageComposite();

      gridData = new GridData( GridData.FILL, GridData.FILL, true, true );
      gridData.widthHint = 400;

      protocolImg.setLayoutData( gridData );

      spinner = new Spinner( right, SWT.BORDER );
      spinner.setMinimum( 0 );
      spinner.setMaximum( 1000 );
      spinner.setSelection( 100 );
      spinner.setIncrement( 10 );

      gridData = new GridData( GridData.FILL, GridData.END, true, false );
      spinner.setLayoutData( gridData );

      spinner.addListener( SWT.Selection, spinnerListener() );

      spinner.addSelectionListener( new SelectionAdapter() {

      } );

      gridData = new GridData( GridData.FILL, GridData.FILL, true, true );
      right.setLayoutData( gridData );

      shell.pack();

      protocolImg.addListener( SWT.Resize, new Listener() {
         public void handleEvent( Event e ) {
            if ( !noDot && protocol != null && protocol.getStates().size() > 0 ) {
               try {
                  logger.info( "Resizing image: " + protocolImg.getClientArea() );
                  it.reload( pih.generateGraph( protocol, protocolImg.getClientArea().width, protocolImg.getClientArea().height, (float) spinner.getSelection() / 100 ) );
                  protocolImg.redraw();
               }
               catch ( IOException e1 ) {
                  errorBox( "Image Failed", "Failed to load image (have you installed graphviz?)" );
                  setNoDot( true );
               }
            }
         }
      } );
   }

   private void setNoDot( boolean noDot ) {
      this.noDot = noDot;
      exportItem.setEnabled( !noDot );
   }

   private Listener addTransitionListener() {
      return new Listener() {
         @Override
         public void handleEvent( Event arg0 ) {

            // pop up a dialog
            Transition t = new AddTransitionDialog( shell, SWT.NONE, pe ).open();

            // it will return non-null if everything's ok
            if ( t != null ) {
               // add the transition to the protocol and refresh the GUI
               protocol.addTransition( t );
               pe.setChanged( true );
               refresh();
            }
         }
      };
   }

   private Listener editTransitionListener() {
      return new Listener() {
         @Override
         public void handleEvent( Event arg0 ) {
            if ( transitions.getSelectionIndex() >= 0 ) {
               // refresh the GUI as soon as this returns
               if ( new EditTransitionDialog( shell, SWT.NONE, pe, transitionList.get( transitions.getSelectionIndex() ) ).open() ) {
                  pe.setChanged( true );
                  refresh();
               }
            }
         }
      };
   }

   private void createTransitionsGroup( Composite parent ) {

      Group tranGroup = new Group( parent, SWT.BORDER );
      tranGroup.setText( "Transitions" );
      GridLayout gridLayout = new GridLayout();
      gridLayout.numColumns = 3;
      gridLayout.makeColumnsEqualWidth = true;
      tranGroup.setLayout( gridLayout );

      transitions = new Table( tranGroup, SWT.BORDER );

      transitions.setHeaderVisible( true );
      transitions.setLinesVisible( true );

      String[] tLabels = new String[] { "From", "To", "Sender", "Receiver", "Performative", "Content" };
      tcols = new TableColumn[ tLabels.length ];

      for ( int i = 0; i < tcols.length; i++ ) {
         tcols[ i ] = new TableColumn( transitions, SWT.NONE );
         tcols[ i ].setText( tLabels[ i ] );
         tcols[ i ].pack();
      }

      fillTransitionsTable();

      GridData gridData = new GridData( GridData.FILL, GridData.FILL, true, true );
      gridData.horizontalSpan = 3;
      int listHeight = transitions.getItemHeight() * 6;
      Rectangle trim = transitions.computeTrim( 0, 0, 0, listHeight );
      gridData.heightHint = trim.height;
      transitions.setLayoutData( gridData );

      addTransition = new Button( tranGroup, SWT.PUSH );
      addTransition.setText( "Add" );
      gridData = new GridData( GridData.FILL, GridData.END, true, false );

      addTransition.addListener( SWT.Selection, addTransitionListener() );

      addTransition.setLayoutData( gridData );

      editTransition = new Button( tranGroup, SWT.PUSH );
      editTransition.setText( "Edit" );
      gridData = new GridData( GridData.FILL, GridData.END, true, false );

      editTransition.addListener( SWT.Selection, editTransitionListener() );

      editTransition.setLayoutData( gridData );

      removeTransition = new Button( tranGroup, SWT.PUSH );
      removeTransition.setText( "Remove" );
      gridData = new GridData( GridData.FILL, GridData.END, true, false );

      removeTransition.addListener( SWT.Selection, removeTransitionListener() );

      removeTransition.setLayoutData( gridData );

      gridData = new GridData( SWT.FILL, SWT.FILL, true, true );
      gridData.horizontalSpan = 2;
      tranGroup.setLayoutData( gridData );

   }

   private Listener removeTransitionListener() {
      return new Listener() {
         @Override
         public void handleEvent( Event arg0 ) {
            // if something is selected
            if ( transitions.getSelectionIndex() >= 0 ) {
               // remove the transition from the protocol and refresh the
               // GUI
               protocol.removeTransition( transitionList.get( transitions.getSelectionIndex() ) );
               pe.setChanged( true );
               refresh();
            }
         }
      };
   }

   private Listener spinnerListener() {
      return new Listener() {
         @Override
         public void handleEvent( Event arg0 ) {
            if ( spinner.isEnabled() ) {
               spinner.setEnabled( false );

               if ( !noDot && !protocol.getStates().isEmpty() ) {
                  try {
                     it.reload( pih.generateGraph( protocol, protocolImg.getClientArea().width, protocolImg.getClientArea().height, (float) spinner.getSelection() / 100 ) );
                     protocolImg.redraw();
                  }
                  catch ( IOException e1 ) {
                     errorBox( "Image Failed", "Failed to load image (have you installed graphviz?)" );
                     setNoDot( true );
                  }
               }
               spinner.setEnabled( true );
            }
         }
      };
   }

   private void createNameComposite( Composite left ) {
      Composite nameComp = new Composite( left, SWT.NONE );
      GridLayout gridLayout = new GridLayout();
      gridLayout.numColumns = 3;
      nameComp.setLayout( gridLayout );

      // Protocol Name, Namespace, Version labels
      new Label( nameComp, SWT.NONE ).setText( "Namespace:" );
      new Label( nameComp, SWT.NONE ).setText( "Name:" );
      new Label( nameComp, SWT.NONE ).setText( "Version:" );

      namespace = new Text( nameComp, SWT.SINGLE | SWT.BORDER );

      GridData gridData = new GridData( GridData.FILL, GridData.FILL, true, false );
      namespace.setLayoutData( gridData );
      namespace.addListener( SWT.Modify, new Listener() {

         @Override
         public void handleEvent( Event arg0 ) {
            logger.info( "Namespace changed" );
            if ( !ProtocolVerifier.verifyField( ProtocolVerifier.NAMESPACE, namespace.getText() ) ) {
               namespace.setForeground( shell.getDisplay().getSystemColor( SWT.COLOR_RED ) );
            }
            else {
               namespace.setForeground( shell.getDisplay().getSystemColor( SWT.COLOR_BLACK ) );

               protocol.getDescriptor().setNamespace( namespace.getText() );
            }
            pe.setChanged( true );

         }
      } );

      name = new Text( nameComp, SWT.SINGLE | SWT.BORDER );
      gridData = new GridData( GridData.FILL, GridData.FILL, true, false );
      name.setLayoutData( gridData );
      name.addListener( SWT.Modify, new Listener() {

         @Override
         public void handleEvent( Event arg0 ) {

            if ( !ProtocolVerifier.verifyField( ProtocolVerifier.NAME, name.getText() ) ) {
               name.setForeground( shell.getDisplay().getSystemColor( SWT.COLOR_RED ) );
            }
            else {
               name.setForeground( shell.getDisplay().getSystemColor( SWT.COLOR_BLACK ) );

               protocol.getDescriptor().setName( name.getText() );
            }
            pe.setChanged( true );

         }
      } );

      version = new Text( nameComp, SWT.SINGLE | SWT.BORDER );
      gridData = new GridData( GridData.FILL, GridData.FILL, true, false );
      version.setLayoutData( gridData );
      version.addListener( SWT.Modify, new Listener() {

         @Override
         public void handleEvent( Event arg0 ) {

            if ( !ProtocolVerifier.verifyField( ProtocolVerifier.VERSION, version.getText() ) ) {
               version.setForeground( shell.getDisplay().getSystemColor( SWT.COLOR_RED ) );
            }
            else {
               version.setForeground( shell.getDisplay().getSystemColor( SWT.COLOR_BLACK ) );

               protocol.getDescriptor().getVersion().set( version.getText() );
            }
            pe.setChanged( true );

         }
      } );

      gridData = new GridData( GridData.FILL, GridData.FILL, true, false );
      gridData.horizontalSpan = 2;
      nameComp.setLayoutData( gridData );

   }

   private void fillTransitionsTable() {

      logger.info( "Refreshing Transitions" );
      transitions.removeAll();
      transitionList.clear();

      for ( Transition t : this.protocol.getTransitions() ) {
         TableItem it = new TableItem( transitions, SWT.NONE );
         int c = 0;
         it.setText( c++, t.getStartState().getName() );
         it.setText( c++, t.getEndState().getName() );
         it.setText( c++, formatter.format( t.getSender() ) );
         it.setText( c++, formatter.format( t.getReceiver() ) );
         it.setText( c++, t.getPerformative() );
         if ( t.getContent() != null ) {
            it.setText( c++, formatter.format( t.getContent() ) );
         }
         else {
            it.setText( c++, "" );
         }

         if ( t.isImported() ) {
            it.setForeground( shell.getDisplay().getSystemColor( SWT.COLOR_GRAY ) );
         }

         transitionList.add( t );
      }

      for ( int i = 0; i < tcols.length; i++ ) {
         tcols[ i ].pack();
      }
      transitions.redraw();
   }

   private void createStatesGroup( Composite parent ) {

      // List of states
      Group statesGroup = new Group( parent, SWT.BORDER );
      statesGroup.setText( "States" );
      GridLayout gridLayout = new GridLayout();
      gridLayout.numColumns = 2;
      gridLayout.makeColumnsEqualWidth = true;
      statesGroup.setLayout( gridLayout );

      states = new List( statesGroup, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL );

      GridData gridData = new GridData( GridData.FILL, GridData.FILL, true, true );
      gridData.horizontalSpan = 2;
      int listHeight = states.getItemHeight() * 5;
      gridData.heightHint = listHeight;
      states.setLayoutData( gridData );

      addState = new Button( statesGroup, SWT.PUSH );
      addState.setText( "Add" );
      gridData = new GridData( GridData.FILL, GridData.END, true, false );

      addState.addListener( SWT.Selection, addStateListener() );

      addState.setLayoutData( gridData );

      removeState = new Button( statesGroup, SWT.PUSH );
      removeState.setText( "Remove" );
      gridData = new GridData( GridData.FILL, GridData.END, true, false );
      removeState.addListener( SWT.Selection, removeStateListener() );

      removeState.setLayoutData( gridData );

      gridData = new GridData( SWT.FILL, SWT.FILL, true, true );
      statesGroup.setLayoutData( gridData );
   }

   private Listener addStateListener() {
      return new Listener() {
         @Override
         public void handleEvent( Event arg0 ) {
            String newState = new AddStateDialog( shell, SWT.NONE, pe ).open();

            // will return non-null if all is ok
            if ( newState != null ) {
               // add the state to the Protocol and refresh the GUI
               protocol.addState( new State( newState ) );
               pe.setChanged( true );
               refresh();
            }
         }
      };
   }

   /**
    * A listener for the {@link removeStateButton}
    * 
    * @return
    */
   private Listener removeStateListener() {
      return new Listener() {

         @Override
         public void handleEvent( Event arg0 ) {
            if ( states.getSelectionIndex() >= 0 ) {
               State s = protocol.getStateByName( states.getSelection()[ 0 ] );
               int affected = s.getOutTransitions().size() + s.getInTransitions().size();
               StringBuilder msg = new StringBuilder( "Are you sure you want to delete the state \"" );
               msg.append( s.getName() ).append( "\"?\n" );
               if ( affected > 0 ) {
                  msg.append( affected ).append( " transition" );
                  if ( affected > 1 ) {
                     msg.append( "s" );
                  }
                  msg.append( " will also be deleted" );
               }

               if ( yesNoBox( "Confirm Delete", msg.toString() ) ) {
                  protocol.removeState( s );
                  pe.setChanged( true );
                  refresh();
               }
            }
         }
      };
   }

   private void createParticipantsGroup( Composite parent ) {

      // List of states
      Group participantsGroup = new Group( parent, SWT.BORDER );
      participantsGroup.setText( "Participants" );
      GridLayout gridLayout = new GridLayout();
      gridLayout.numColumns = 2;
      gridLayout.makeColumnsEqualWidth = true;
      participantsGroup.setLayout( gridLayout );

      participants = new List( participantsGroup, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL );

      GridData gridData = new GridData( GridData.FILL, GridData.FILL, true, true );
      gridData.horizontalSpan = 2;
      int listHeight = participants.getItemHeight() * 5;
      gridData.heightHint = listHeight;
      participants.setLayoutData( gridData );

      addParticipant = new Button( participantsGroup, SWT.PUSH );
      addParticipant.setText( "Add" );
      gridData = new GridData( GridData.FILL, GridData.END, true, false );

      addParticipant.addListener( SWT.Selection, addParticipantListener() );

      addParticipant.setLayoutData( gridData );

      removeParticipant = new Button( participantsGroup, SWT.PUSH );
      removeParticipant.setText( "Remove" );
      gridData = new GridData( GridData.FILL, GridData.END, true, false );
      removeParticipant.addListener( SWT.Selection, removeParticipantListener() );

      removeParticipant.setLayoutData( gridData );

      gridData = new GridData( SWT.FILL, SWT.FILL, true, true );
      participantsGroup.setLayoutData( gridData );

   }

   /*
    * A listener for the {@code removeParticipantButton}
    */
   private Listener removeParticipantListener() {
      return new Listener() {

         @Override
         public void handleEvent( Event arg0 ) {
            if ( participants.getSelectionIndex() >= 0 ) {
               
               // which participant is selected?
               String part = participants.getItem( participants.getSelectionIndex() );

               // build prompt
               StringBuilder msg = new StringBuilder( "Are you sure you want to delete the participant \"" );
               msg.append( part ).append( "\"?\n" );

               try {
                  // participant as a term
                  Term p = parser.parse( part );
                  
                  // get all affected transitions (these will be removed too)
                  Set<Transition> affected = new HashSet<Transition>();
                  for ( Transition t : transitionList ) {
                     if ( t.getSender().equals( p ) || t.getReceiver().equals( p ) ) {
                        affected.add( t );
                     }
                  }

                  // add to prompt if transitions will be deleted too
                  if ( affected.size() > 0 ) {
                     msg.append( affected.size() ).append( " transition" );
                     if ( affected.size() > 1 ) {
                        msg.append( "s" );
                     }
                     msg.append( " will also be deleted" );
                  }

                  // get confirmation of deletion?
                  if ( yesNoBox( "Confirm Delete", msg.toString() ) ) {
                     // remove affected transitions
                     for ( Transition t : affected ) {
                        protocol.removeTransition( t );
                     }
                     
                     int ind = participants.getSelectionIndex();
                     logger.info( "Removing participant" + participants.getSelection() + "(index " + participants.getSelectionIndex() + ")" );
                     participantSet.remove( participants.getItem( ind ) );
                     participants.remove( ind );
                     participants.redraw();
                     pe.setChanged( true );
                     refresh();
                  }

               }
               catch ( MalformedTermException e ) {
                  System.err.println( "Participant couldn't be parsed as a term: how did this happen?" );
               }
            }
         }

      };
   }

   /*
    * A listener for the {@link addParticipantButton}
    */
   private Listener addParticipantListener() {
      return new Listener() {

         @Override
         public void handleEvent( Event arg0 ) {
            String newParticipant = new AddParticipantDialog( shell, SWT.NONE, pe ).open();
            if ( newParticipant != null ) {
               participants.add( newParticipant );
               participantSet.add( newParticipant );
               pe.setChanged( true );
            }
         }
      };
   }

   private void fillStatesList() {
      states.removeAll();
      for ( State s : this.protocol.getStates() ) {
         states.add( s.getName() );
      }
      states.redraw();
   }

   protected void refresh() {
      this.name.setText( this.protocol.getDescriptor().getName() );
      this.namespace.setText( this.protocol.getDescriptor().getNamespace() );
      this.version.setText( this.protocol.getDescriptor().getVersion().toString() );
      this.fillStatesList();
      this.fillTransitionsTable();
      this.fillParticipantsList();
      try {
         if ( !noDot && !protocol.getStates().isEmpty() ) {
            logger.info( "Width: " + protocolImg.getClientArea().width );
            logger.info( "Height: " + protocolImg.getClientArea().height );

            it.reload( pih.generateGraph( this.protocol, protocolImg.getClientArea().width, protocolImg.getClientArea().height, 1.0f ) );
            protocolImg.redraw();
         }
         else if ( noDot ){
            it.text( "Install graphviz to see protocol images" );
            logger.warning( "Graphviz not found" );
            protocolImg.redraw();
         }
         else {
            it.text( "No States" );
            protocolImg.redraw();
         }

      }
      catch ( IOException e ) {
         logger.warning( e.toString() );
         
         // tried to run dot but failed: log environment
         Map<String, String> env = System.getenv();
         for (String envName : env.keySet()) {
              logger.warning( String.format("%s=%s%n", envName, env.get(envName)) );
         }
         it.text( "Install graphviz to ensure images are generated" );
         protocolImg.redraw();
         setNoDot( true );
      }

   }

   protected void fillParticipantsList() {
      if ( this.participantSet.isEmpty() ) {
         for ( Transition t : protocol.getTransitions() ) {
            this.participantSet.add( formatter.format( t.getSender() ) );
            this.participantSet.add( formatter.format( t.getReceiver() ) );
         }
      }

      for ( String a : this.participantSet ) {
         if ( participants.indexOf( a ) < 0 ) {
            participants.add( a );
         }
      }
   }

   protected void loadProtocol( File protocolFile ) throws Exception {
      this.protocol = XMLProtocolSerialiser.readProtocol( new FileInputStream( protocolFile ), null );
   }

   private void setEditable( boolean editable ) {
      saveItem.setEnabled( editable );
      asItem.setEnabled( editable );
      name.setEnabled( editable );
      namespace.setEnabled( editable );
      version.setEnabled( editable );
      addState.setEnabled( editable );
      removeState.setEnabled( editable );
      addParticipant.setEnabled( editable );
      removeParticipant.setEnabled( editable );
      addTransition.setEnabled( editable );
      removeTransition.setEnabled( editable );
      editTransition.setEnabled( editable );
   }

   public void setProtocol( Protocol p, boolean editable ) {
      this.setEditable( editable );
      this.protocol = p;
      this.participantSet.clear();
      this.shell.setText( SHELL_TEXT + " " + p.getDescriptor().toString() );
      this.refresh();

      logger.info( "Changes reset" );
      pe.setChanged( false );
   }

   public void createMenu() {

      Menu menu = new Menu( shell, SWT.BAR );

      // first menu is the "Protocol" menu.
      MenuItem protocolItem = new MenuItem( menu, SWT.CASCADE );
      protocolItem.setText( "Protocol" );
      Menu protocolMenu = new Menu( menu );
      protocolItem.setMenu( protocolMenu );

      // different menus depending on whether there's a repository manager or
      // not

      // standalone
      if ( rm == null ) {

         // set up the menu
         StandaloneMenuListener ml = new StandaloneMenuListener( this );

         // New protocol
         newItem = new MenuItem( protocolMenu, SWT.NONE );
         newItem.setText( "&New\tCtrl+N" );
         newItem.setAccelerator( SWT.MOD1 + 'N' );
         newItem.addSelectionListener( ml );

         // Open existing protocol
         openItem = new MenuItem( protocolMenu, SWT.NONE );
         openItem.setText( "&Open\tCtrl+O" );
         openItem.setAccelerator( SWT.MOD1 + 'O' );
         openItem.addSelectionListener( ml );

         // Save open protocol
         saveItem = new MenuItem( protocolMenu, SWT.NONE );
         saveItem.setText( "&Save\tCtrl+S" );
         saveItem.setAccelerator( SWT.MOD1 + 'S' );
         saveItem.addSelectionListener( ml );

         // Save as a different file
         asItem = new MenuItem( protocolMenu, SWT.NONE );
         asItem.setText( "Save &As\tShift+Ctrl+S" );
         asItem.setAccelerator( SWT.SHIFT + SWT.MOD1 + 'S' );
         asItem.addSelectionListener( ml );

         // Export as image file
         exportItem = new MenuItem( protocolMenu, SWT.NONE );
         exportItem.setText( "&Export\tCtrl+E" );
         exportItem.setAccelerator( SWT.MOD1 + 'E' );
         exportItem.addSelectionListener( ml );

         // Quit the editor
         quitItem = new MenuItem( protocolMenu, SWT.NONE );
         quitItem.setText( "&Quit\tCtrl+Q" );
         quitItem.setAccelerator( SWT.MOD1 + 'Q' );
         quitItem.addSelectionListener( ml );

      }
      else {
         RepositoryMenuListener ml = new RepositoryMenuListener( pe, rm );

         // Save open protocol
         saveItem = new MenuItem( protocolMenu, SWT.NONE );
         saveItem.setText( "&Save\tCtrl+S" );
         saveItem.setAccelerator( SWT.MOD1 + 'S' );
         saveItem.addSelectionListener( ml );

         // Save as a different file
         asItem = new MenuItem( protocolMenu, SWT.NONE );
         asItem.setText( "Save &As New\tShift+Ctrl+S" );
         asItem.setAccelerator( SWT.SHIFT + SWT.MOD1 + 'S' );
         asItem.addSelectionListener( ml );

         
         // Export as image file
         exportItem = new MenuItem( protocolMenu, SWT.NONE );
         exportItem.setText( "&Export\tCtrl+E" );
         exportItem.setAccelerator( SWT.MOD1 + 'E' );
         exportItem.addSelectionListener( ml );
         
         // Line before the close option
         new MenuItem( protocolMenu, SWT.SEPARATOR );

         closeItem = new MenuItem( protocolMenu, SWT.NONE );
         closeItem.setText( "&Close\tCtrl+W" );
         closeItem.setAccelerator( SWT.MOD1 + 'W' );
         closeItem.addSelectionListener( ml );

      }

      // add the menu to the window
      shell.setMenuBar( menu );

   }

   public boolean yesNoBox( String title, String message ) {
      MessageBox mb = new MessageBox( pe.shell, SWT.APPLICATION_MODAL | SWT.YES | SWT.NO );
      mb.setText( title );
      mb.setMessage( message );
      return mb.open() == SWT.YES;
   }

   public void errorBox( String title, String message ) {
      MessageBox mb = new MessageBox( pe.shell, SWT.APPLICATION_MODAL | SWT.ERROR );
      mb.setText( title );
      mb.setMessage( message );
      mb.open();

   }

   public boolean okCancelBox( String title, String message ) {
      MessageBox mb = new MessageBox( pe.shell, SWT.APPLICATION_MODAL | SWT.OK | SWT.CANCEL );
      mb.setText( title );
      mb.setMessage( message );
      return mb.open() == SWT.OK;
   }

   public void okBox( String title, String message ) {
      MessageBox mb = new MessageBox( pe.shell, SWT.APPLICATION_MODAL | SWT.OK );
      mb.setText( title );
      mb.setMessage( message );
      mb.open();

   }
   
   boolean exportProtocol( File filename ) {
      try {
         FileOutputStream out = new FileOutputStream( filename );

         // use -1 to indicate that we don't want a width/height actually stored in the dotfile
         InputStream in = pe.pih.generateGraph( pe.protocol, -1, -1, 1.0f );
         byte[] buf = new byte[1024];
         int read = 0;
         while( ( read = in.read( buf ) ) != -1 ) {
            out.write( buf, 0, read );
         }
         in.close();
         out.close();
         
         pe.okBox("Export Successful", "Protocol Successfully exported to " + filename.toString() );
         
         return true;
      }
      catch ( IOException e ) {
         pe.errorBox( "Export Error", "Could not export protocol: " + filename.toString() );
         return false;
      }
   }

   boolean saveProtocolWithPrompt() {
      FileDialog fd = new FileDialog( pe.shell, SWT.SAVE );
      fd.setText( "Save Protocol" );
      if ( lastDir != null ) {
         fd.setFilterPath( lastDir );
      }
      String filename = fd.open();
      if ( filename != null ) {
         return saveProtocol( new File( filename ) );
      }
      else {
         return false;
      }
   }
   
   boolean saveProtocol( File file ) {

      if ( !ProtocolVerifier.verifyField( ProtocolVerifier.NAMESPACE, pe.namespace.getText() ) ) {
         pe.errorBox( "Invalid Namespace", "The namespace is invalid" );
      }
      else if ( !ProtocolVerifier.verifyField( ProtocolVerifier.NAME, pe.name.getText() ) ) {
         pe.errorBox( "Invalid Name", "The protocol name is invalid" );
      }
      else if ( !ProtocolVerifier.verifyField( ProtocolVerifier.VERSION, pe.version.getText() ) ) {
         pe.errorBox( "Invalid Vame", "The protocol version is invalid" );
      }
      else {

         pe.protocol.getDescriptor().setNamespace( pe.namespace.getText() );
         pe.protocol.getDescriptor().setName( pe.name.getText() );
         pe.protocol.getDescriptor().setVersion( new ProtocolVersion( pe.version.getText() ) );
         try {
            XMLProtocolSerialiser.writeProtocol( pe.protocol, new FileOutputStream( file ) );
            pe.setChanged( false );
            return true;
         }
         catch ( FileNotFoundException e ) {
            pe.errorBox( "Save Error", "Could not find file: " + file.toString() );
         }
         catch ( IOException e ) {
            pe.errorBox( "Save Error", "Failed to write to file: " + file.toString() );
         }
      }
      return false;
   }
}

class RepositoryMenuListener extends SelectionAdapter {
   private static Logger logger = ProtocolEditor.logger;

   private ProtocolEditor pe;
   private RepositoryManager rm;
   private boolean saved = false;

   public RepositoryMenuListener( ProtocolEditor pe, RepositoryManager rm ) {
      this.pe = pe;
      this.rm = rm;
   }

   public void widgetSelected( SelectionEvent event ) {
      if ( event.widget == pe.closeItem ) {
         if ( saved || !pe.isChanged() || pe.okCancelBox( "Protocol Not Saved", "You have not saved your protocol. Really quit?" ) ) {
            pe.shell.close();
         }
      }
      else if ( event.widget == pe.saveItem ) {

         try {
            logger.info( "Saving to repository" );
            pe.protocol.getDescriptor().setNamespace( pe.namespace.getText() );
            pe.protocol.getDescriptor().setName( pe.name.getText() );
            pe.protocol.getDescriptor().setVersion( new ProtocolVersion( pe.version.getText() ) );
            rm.saveProtocol( true );
            pe.setChanged( false );
         }
         catch ( RepositoryException e ) {
            logger.info( "Repository Exception " + e );
            pe.errorBox( "Failed to save", e.getMessage() );
         }
      }
      else if ( event.widget == pe.asItem ) {

         try {
            pe.protocol.getDescriptor().setNamespace( pe.namespace.getText() );
            pe.protocol.getDescriptor().setName( pe.name.getText() );
            pe.protocol.getDescriptor().setVersion( new ProtocolVersion( pe.version.getText() ) );
            rm.saveProtocol( false );
            pe.setChanged( false );
         }
         catch ( RepositoryException e ) {
            pe.errorBox( "Failed to save", e.getMessage() );
         }
      }
      else if ( ( (MenuItem) event.widget ) == pe.exportItem ) {
         logger.info( "Export menu item clicked" );
         FileDialog fd = new FileDialog( pe.shell, SWT.SAVE );
         fd.setText( "Export Protocol" );
         if ( pe.lastDir != null ) {
            fd.setFilterPath( pe.lastDir );
         }
         fd.setFilterExtensions( new String[]{ "*.png" } );
         String filename = fd.open();
         if ( filename != null ) {
            pe.exportProtocol( new File( filename ) );
         }
      }
   }
}

class StandaloneMenuListener extends SelectionAdapter {
   private ProtocolEditor pe;
   private File file;
   private boolean saved;

   private static Logger logger = ProtocolEditor.logger;

   public StandaloneMenuListener( ProtocolEditor pe ) {
      super();
      this.pe = pe;
      logger.info( "Creating menu listener for standalone editor" );

   }

   public void widgetSelected( SelectionEvent event ) {
      if ( ( (MenuItem) event.widget ) == pe.quitItem ) {

         if ( saved || !pe.isChanged() || pe.okCancelBox( "Protocol Not Saved", "You have not saved your protocol. Really quit?" ) ) {
            pe.shell.close();
         }
      }
      else if ( ( (MenuItem) event.widget ) == pe.openItem ) {
         FileDialog fd = new FileDialog( pe.shell, SWT.OPEN );
         fd.setText( "Open Protocol" );
         if ( pe.lastDir != null ) {
            fd.setFilterPath( pe.lastDir );
         }
         else {
            fd.setFilterPath( System.getProperty( "user.home" ) );
         }
         String[] filterExt = { "*.acr", "*.xml", "*.*" };
         fd.setFilterExtensions( filterExt );
         String filename = fd.open();
         if ( filename != null ) {
            file = new File( filename );

            pe.lastDir = file.getParent();
            try {
               pe.loadProtocol( file );
            }
            catch ( Exception e ) {
               pe.errorBox( "Loading Failed", "Failed to open protocol: " + file );

               // don't overwrite this file if they click 'save'
               file = null;
            }
            pe.refresh();
            pe.setChanged( false );
         }
      }
      else if ( ( (MenuItem) event.widget ) == pe.saveItem ) {
         if ( file != null ) {

            logger.info( "Saving protocol as " + file.toString() );
            pe.protocol.getDescriptor().setNamespace( pe.namespace.getText() );
            pe.protocol.getDescriptor().setName( pe.name.getText() );
            pe.protocol.getDescriptor().setVersion( new ProtocolVersion( pe.version.getText() ) );
            saved = pe.saveProtocol( file );

         }
         else {
            saved = pe.saveProtocolWithPrompt();
         }
      }
      else if ( ( (MenuItem) event.widget ) == pe.asItem ) {

         saved = pe.saveProtocolWithPrompt();

      }
      else if ( ( (MenuItem) event.widget ) == pe.newItem ) {
         if ( !saved ) {

            if ( pe.yesNoBox( "Protocol Not Saved", "You have not saved your protocol. Do you want to save it now?" ) ) {

               if ( file != null ) {
                  saved = pe.saveProtocol( file );
               }
               else {
                  saved = pe.saveProtocolWithPrompt();
               }
               if ( saved ) {
                  pe.setProtocol( new Protocol(), true );
               }
            }
            else {
               pe.setProtocol( new Protocol(), true );
            }
         }
         else {
            pe.setProtocol( new Protocol(), true );
         }
      }
      else if ( ( (MenuItem) event.widget ) == pe.exportItem ) {
         logger.info( "Export menu item clicked" );
         FileDialog fd = new FileDialog( pe.shell, SWT.SAVE );
         fd.setText( "Export Protocol" );
         if ( pe.lastDir != null ) {
            fd.setFilterPath( pe.lastDir );
         }
         fd.setFilterExtensions( new String[]{ "*.png" } );
         String filename = fd.open();
         if ( filename != null ) {
            pe.exportProtocol( new File( filename ) );
         }
      }
   }
}
