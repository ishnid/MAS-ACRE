package is.lill.acre.gui;

import is.lill.acre.logic.ITermFormatter;
import is.lill.acre.logic.ITermParser;
import is.lill.acre.logic.MalformedTermException;
import is.lill.acre.logic.Utilities;
import is.lill.acre.protocol.Protocol;
import is.lill.acre.protocol.State;
import is.lill.acre.protocol.Transition;
import is.lill.acre.xml.XMLProtocolSerialiser;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;

import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings ( "serial")
public class ProtocolEditor extends JFrame {

   private static Logger logger = Logger.getLogger( ProtocolEditor.class.getName() );

   static {
      logger.setLevel( Level.ALL );
   }

   private static String VERSION = "0.1";

   public static final int FROM_STATE = 0;
   public static final int TO_STATE = 1;
   public static final int PERFORMATIVE = 2;
   public static final int SENDER = 3;
   public static final int RECEIVER = 4;
   public static final int CONTENT = 5;

   private ProtocolImageHandler handler = new ProtocolImageHandler();

   private Protocol protocol = new Protocol();

   protected DefaultListModel stateListModel = new DefaultListModel();
   protected DefaultListModel participantListModel = new DefaultListModel();

   protected JList stateList = new JList( stateListModel );
   private JList participantList = new JList( participantListModel );

   private ITermFormatter formatter = Utilities.getTermFormatter( "acre" );
   private List<Transition> transitions = new ArrayList<Transition>();

   private JTable transitionTable;

   public ProtocolEditor( Protocol p ) {
      this();
      this.protocol = p;
      this.transitions.addAll( protocol.getTransitions() );
      refresh();
   }

   public ProtocolEditor() {
      this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
      this.getContentPane().setLayout( new BoxLayout( this.getContentPane(), BoxLayout.X_AXIS ) );
      this.initComponents();
      this.setSize( 1200, 800 );
      this.setVisible( true );
      refresh();
   }

   public static void main( String[] args ) {

      ProtocolEditor pe = new ProtocolEditor();

      args =
            new String[] { "/home/daithi/.acre/is.lill.acre.test_pingpong_0.1.acr" };

      if ( args.length == 1 ) {
         logger.info( "Loading protocol from file: " + args[ 0 ] );
         pe.loadProtocol( new File( args[ 0 ] ) );
      }
   }

   public void loadProtocol( File protocolFile ) {
      Protocol p;
      try {
         p =
               XMLProtocolSerialiser.readProtocol( new FileInputStream( protocolFile ), null );
         this.setProtocol( p );
      }

      catch ( Exception e ) {
         JOptionPane.showMessageDialog( null, "Failed to load from file: "
               + protocolFile, "Error", JOptionPane.ERROR_MESSAGE );
      }
   }

   public void setProtocol( Protocol p ) {
      this.protocol = p;
      this.refresh();
   }

   private void refresh() {
      updateStates();
      transitions.clear();
      transitions.addAll( protocol.getTransitions() );
      handler.redrawGraph( protocol, 1.0f );

      participantListModel.clear();
      Set<String> pSet = new HashSet<String>();
      for ( Transition t : protocol.getTransitions() ) {
         String sender = formatter.format( t.getSender() );
         if ( !pSet.contains( sender ) ) {
            participantListModel.addElement( sender );
            pSet.add( sender );
         }

         String receiver = formatter.format( t.getReceiver() );
         if ( !pSet.contains( receiver ) ) {
            participantListModel.addElement( receiver );
            pSet.add( receiver );
         }
      }

   }

   private void initComponents() {

      this.setTitle( "ACRE Protocol Editor v" + VERSION );

      this.setJMenuBar( this.createMenu() );

      // try setting native look and feel
      try {
         UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
      }
      catch ( Exception e ) {
         logger.warning( "Error setting native look and feel: " + e );
      }

      this.add( statePanel() );

      JPanel transitionPanel = new JPanel();

      transitionPanel.setLayout( new BoxLayout( transitionPanel, BoxLayout.Y_AXIS ) );

      transitionTable = transitionTable();

      transitionTable.addMouseListener( new MouseAdapter() {
         public void mouseClicked( MouseEvent evt ) {
            if ( evt.getClickCount() == 2 ) {
               handler.redrawGraph( protocol, 1.0f );
            }
         }
      } );

      transitionPanel.add( new JScrollPane( transitionTable ) );

      JPanel transitionButtonPanel = new JPanel();

      JButton transitionAddButton = new JButton( "+" );

      transitionAddButton.addActionListener( new ActionListener() {

         @Override
         public void actionPerformed( ActionEvent arg0 ) {
            Vector<Object> participants = new Vector<Object>();

            for ( int i = 0; i < participantListModel.getSize(); i++ ) {
               participants.add( participantListModel.elementAt( i ) );
            }

            TransitionFrame tf =
                  new TransitionFrame( protocol, participants, null );

            tf.addWindowListener( new WindowListener() {

               @Override
               public void windowActivated( WindowEvent arg0 ) {}

               @Override
               public void windowClosed( WindowEvent arg0 ) {}

               @Override
               public void windowClosing( WindowEvent arg0 ) {}

               @Override
               public void windowDeactivated( WindowEvent arg0 ) {}

               @Override
               public void windowDeiconified( WindowEvent arg0 ) {}

               @Override
               public void windowIconified( WindowEvent arg0 ) {}

               @Override
               public void windowOpened( WindowEvent arg0 ) {}

            } );
         }
      } );

      transitionButtonPanel.add( transitionAddButton );

      JButton transitionRemoveButton = new JButton( "-" );

      transitionRemoveButton.addActionListener( new ActionListener() {

         @Override
         public void actionPerformed( ActionEvent arg0 ) {
            int selected = transitionTable.getSelectedRow();
            if ( selected > -1 ) {
               if ( JOptionPane.showConfirmDialog( null, "Delete this transition?", "Confirm Delete", JOptionPane.YES_NO_OPTION ) == JOptionPane.YES_OPTION ) {

                  Transition toRemove = transitions.remove( selected );
                  protocol.removeTransition( toRemove );
                  refresh();
               }
            }
         }
      } );

      transitionButtonPanel.add( transitionRemoveButton );

      transitionPanel.add( transitionButtonPanel );

      this.add( transitionPanel );

      JPanel visualPanel = new JPanel();

      visualPanel.setSize( 400, 400 );

      JLabel imageLabel = handler.getImageLabel();

      this.add( new JScrollPane( imageLabel ) );

   }

   private void updateStates() {
      this.stateListModel.clear();
      for ( State s : this.protocol.getStates() ) {
         stateListModel.addElement( s.getName() );
      }
      this.repaint();
   }

   private void removeState( String name ) {
      this.protocol.getStates().remove( this.protocol.getStateByName( name ) );
      this.updateStates();
   }

   private JMenuBar createMenu() {
      final ProtocolEditor pe = this;

      JMenuBar toReturn = new JMenuBar();

      JMenu protocolMenu = new JMenu( "Protocol" );

      JMenuItem openItem = new JMenuItem( "Open" );

      openItem.addActionListener( new ActionListener() {

         @Override
         public void actionPerformed( ActionEvent arg0 ) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileHidingEnabled( false );
            FileNameExtensionFilter filter =
                  new FileNameExtensionFilter( "ACRE Protocols", "acr" );
            chooser.setFileFilter( filter );
            int returnVal = chooser.showOpenDialog( pe );
            if ( returnVal == JFileChooser.APPROVE_OPTION ) {

               pe.loadProtocol( chooser.getSelectedFile() );
            }

         }
      } );

      protocolMenu.add( openItem );

      toReturn.add( protocolMenu );

      return toReturn;
   }

   private JPanel statePanel() {

      JPanel statePanel = new JPanel();

      statePanel.setLayout( new BoxLayout( statePanel, BoxLayout.Y_AXIS ) );

      statePanel.add( new JLabel( "States" ) );

      JScrollPane stateNamePane = new JScrollPane( stateList );

      statePanel.add( stateNamePane );

      JPanel stateButtonPanel = new JPanel();

      // button to add states
      JButton addStateButton = new JButton( "+" );
      addStateButton.addActionListener( new ActionListener() {

         @Override
         public void actionPerformed( ActionEvent e ) {
            String stateName =
                  JOptionPane.showInputDialog( "Input new state name" );
            if ( protocol.getStateByName( stateName ) != null ) {
               JOptionPane.showMessageDialog( null, "Duplicate state name: "
                     + stateName, "Error", JOptionPane.ERROR_MESSAGE );
            }
            else {
               protocol.addState( new State( stateName ) );
               refresh();
            }
         }
      } );
      stateButtonPanel.add( addStateButton );

      // button to remove states
      JButton removeStateButton = new JButton( "-" );

      removeStateButton.addActionListener( new ActionListener() {

         @Override
         public void actionPerformed( ActionEvent arg0 ) {
            String stateName = (String) stateList.getSelectedValue();
            removeState( stateName );
         }
      } );

      stateButtonPanel.add( removeStateButton );
      statePanel.add( stateButtonPanel );

      // participants
      statePanel.add( new JLabel( "Participants" ) );

      JScrollPane participantNamePane = new JScrollPane( participantList );

      statePanel.add( participantNamePane );

      JPanel participantButtonPanel = new JPanel();

      // button to add participant
      JButton addParticipantButton = new JButton( "+" );
      addParticipantButton.addActionListener( new ActionListener() {

         @Override
         public void actionPerformed( ActionEvent e ) {
            String participantName =
                  JOptionPane.showInputDialog( "Input new participant name" );

            if ( participantListModel.contains( participantName ) ) {
               JOptionPane.showMessageDialog( null, "Duplicate participant name: "
                     + participantName, "Error", JOptionPane.ERROR_MESSAGE );
            }
            else {
               if ( participantName.startsWith( "?" )
                     || JOptionPane.showConfirmDialog( null, "Participant names generally begin with a `?' character.\nAre you sure you want to add the participant `"
                           + participantName + "'?", "Warning", JOptionPane.YES_NO_OPTION
                           | JOptionPane.WARNING_MESSAGE ) == JOptionPane.YES_OPTION ) {

                  participantListModel.addElement( participantName );
                  refresh();
               }
            }
         }
      } );
      participantButtonPanel.add( addParticipantButton );

      // button to remove participants
      JButton removeParticipantButton = new JButton( "-" );

      removeParticipantButton.addActionListener( new ActionListener() {

         @Override
         public void actionPerformed( ActionEvent arg0 ) {
            String stateName = (String) participantList.getSelectedValue();
            participantListModel.removeElement( stateName );
         }
      } );

      participantButtonPanel.add( removeParticipantButton );
      statePanel.add( participantButtonPanel );

      return statePanel;
   }

   private JTable transitionTable() {

      logger.info( transitions.size() + " transition(s) loaded" );

      AbstractTableModel tm = new AbstractTableModel() {
         public String getColumnName( int col ) {
            switch ( col ) {
               case FROM_STATE:
                  return "From State";
               case TO_STATE:
                  return "To State";
               case PERFORMATIVE:
                  return "Performative";
               case SENDER:
                  return "Sender";
               case RECEIVER:
                  return "Receiver";
               default:
                  return "Content";
            }
         }

         public int getRowCount() {

            return transitions.size();
         }

         public int getColumnCount() {
            return 6;
         }

         public Object getValueAt( int row, int col ) {
            Transition t = transitions.get( row );

            switch ( col ) {
               case FROM_STATE:
                  return t.getStartState().getName();
               case TO_STATE:
                  return t.getEndState().getName();
               case PERFORMATIVE:
                  return t.getPerformative();
               case SENDER:
                  return formatter.format( t.getSender() );
               case RECEIVER:
                  return formatter.format( t.getReceiver() );
               default:
                  return formatter.format( t.getContent() );
            }
         }

         public boolean isCellEditable( int row, int col ) {
            return false;
         }
      };

      return new JTable( tm );
   }

   public Protocol getProtocol() {
      return this.protocol;
   }
}

@SuppressWarnings ( "serial")
class TransitionFrame extends JFrame {

   private JComboBox fromStateBox;
   private JComboBox toStateBox;
   private JComboBox senderBox;
   private JComboBox receiverBox;
   private JComboBox performativeBox;
   private JTextField contentField;

   private ITermParser parser = Utilities.getTermParser( "acre" );
   public TransitionFrame( final Protocol p, Vector<Object> participants, final Transition t ) {

      setSize( 400, 400 );

      setLayout( new GridLayout( 7, 2 ) );

      // find all the available states in the protocol currently
      Vector<String> states = new Vector<String>();
      for ( State s : p.getStates() ) {
         states.add( s.getName() );
      }

      add( new JLabel( "From State:" ) );
      fromStateBox = new JComboBox( states );
      add( fromStateBox );

      add( new JLabel( "To State:" ) );
      toStateBox = new JComboBox( states );
      add( toStateBox );

      add( new JLabel( "Sender:" ) );

      senderBox = new JComboBox( participants );
      add( senderBox );
      add( new JLabel( "Receiver:" ) );

      receiverBox = new JComboBox( participants );
      add( receiverBox );

      // list of performatives
      String[] performatives =
            new String[] {

            "accept-proposal", "agree", "cancel", "cfp", "confirm",
                  "disconfirm", "failure", "inform", "inform-if", "inform-ref",
                  "not-understood", "propagate", "propose", "proxy",
                  "query-if", "query-ref", "refuse", "reject-proposal",
                  "request",

                  "request-when", "request-whenever", "subscribe" };

      add( new JLabel( "Performative:" ) );

      performativeBox = new JComboBox( performatives );
      add( performativeBox );

      // define the content
      add( new JLabel( "Content:" ) );

      contentField = new JTextField();

      add( contentField );

      JButton okButton = new JButton( "Ok" );

      // we are in "Add" mode, so add a new transition
      if ( t == null ) {

         okButton.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent arg0 ) {
               try {
                  Transition t =
                        new Transition( p.getStateByName( (String) fromStateBox.getSelectedItem() ), p.getStateByName( (String) toStateBox.getSelectedItem() ) );

                  t.setContent( parser.parse( contentField.getText() ) );
                  t.setPerformative( (String) performativeBox.getSelectedItem() );
                  t.setReceiver( parser.parse( (String) receiverBox.getSelectedItem() ) );
                  t.setSender( parser.parse( (String) senderBox.getSelectedItem() ) );

                  p.addTransition( t );
                  close();

               }
               catch ( MalformedTermException e1 ) {
                  JOptionPane.showMessageDialog( null, "Content is an invalid ACRE Term: "
                        + contentField.getText(), "Error", JOptionPane.ERROR_MESSAGE );
               }
            }
         } );
      }
      // we were editing
      else {

         okButton.addActionListener( new ActionListener() {

            public void actionPerformed( ActionEvent arg0 ) {

               try {
                  t.setContent( parser.parse( contentField.getText() ) );
                  t.setPerformative( (String) performativeBox.getSelectedItem() );
                  t.setReceiver( parser.parse( (String) receiverBox.getSelectedItem() ) );
                  t.setSender( parser.parse( (String) senderBox.getSelectedItem() ) );
                  t.setStartState( p.getStateByName( (String) fromStateBox.getSelectedItem() ) );
                  t.setEndState( p.getStateByName( (String) toStateBox.getSelectedItem() ) );
                  close();
               }
               catch ( MalformedTermException e1 ) {
                  JOptionPane.showMessageDialog( null, "Content is an invalid ACRE Term: "
                        + contentField.getText(), "Error", JOptionPane.ERROR_MESSAGE );
               }
            }
         } );
      }

      JButton cancelButton = new JButton( "Cancel" );
      cancelButton.addActionListener( new ActionListener() {

         @Override
         public void actionPerformed( ActionEvent e ) {
            dispose();
         }
      } );
      add( okButton );
      add( cancelButton );

      setVisible( true );

   }

   public void close() {
      this.dispatchEvent( new WindowEvent( this, WindowEvent.WINDOW_CLOSING ) );

      this.setVisible( false );
      this.dispose();

   }
}