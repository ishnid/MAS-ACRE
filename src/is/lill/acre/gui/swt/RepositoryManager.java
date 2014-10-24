package is.lill.acre.gui.swt;

import is.lill.acre.exception.ProtocolParseException;
import is.lill.acre.protocol.HTTPRepository;
import is.lill.acre.protocol.IEditableRepository;
import is.lill.acre.protocol.IProtocolManager;
import is.lill.acre.protocol.Protocol;
import is.lill.acre.protocol.RepositoryException;
import is.lill.acre.protocol.RepositoryExporter;
import is.lill.acre.protocol.RepositoryFactory;
import is.lill.acre.xml.XMLProtocolSerialiser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class RepositoryManager {



   private static String APP_NAME = "ACRE Editor";

   private static Logger logger = Logger.getLogger( RepositoryManager.class.getName() );

   RepositoryManager rm;

   private IProtocolManager rep = null;

   Shell shell;
   private String VERSION = "1.1";

   private MenuItem newItem;

   private MenuItem openItem;

   // private MenuItem saveItem;

   // private MenuItem asItem;

   private MenuItem exportItem;

   private MenuItem quitItem;

   private List nameList;

   private List namespaceList;

   private Set<String> nameSpaces = new TreeSet<String>();

   private Table protocolTable;

   private TableColumn[] tcols;

   // buttons for mangaging protocols (new/edit/import/delete)
   private Button newProtocolButton;
   private Button editProtocolButton;
   private Button importProtocolButton;
   private Button deleteProtocolButton;

   private Label info;

   private MenuItem remoteItem;

   private MenuItem recentItem;

   private Menu recentMenu;
   
   private RecentManager recents = new RecentManager();

   java.util.List<Protocol> visibleProtocols = new ArrayList<Protocol>();

   String lastdir;

   Protocol preEdit;
   Protocol editing;

   // TODO: document this
   private boolean failed;

   static {
      logger.setLevel( Level.OFF );
   }

   public static void main( String[] args ) {

      Display display = new Display();
      RepositoryManager rm = new RepositoryManager( display );
      if ( args.length > 0 ) {
         if ( args[ 0 ].startsWith( "http://" ) ) {
            try {
               rm.openRepository( new URL( args[ 0 ] ) );
            }
            catch ( MalformedURLException e ) {
               e.printStackTrace();
            }
         }
         else {
            rm.openRepository( new File( args[ 0 ] ) );
         }
      }

      while ( !rm.shell.isDisposed() ) {
         if ( !display.readAndDispatch() )
            display.sleep();
      }
   }

   public void createShell( Display display ) {
      this.shell = new Shell( display );
      this.shell.setImage( new Image( display, ClassLoader.class.getResourceAsStream( "/is/lill/acre/gui/swt/acre-icon.png" ) ) );
      this.shell.setText( APP_NAME + " v" + VERSION );

      GridLayout gl = new GridLayout();
      gl.numColumns = 3;
      this.shell.setLayout( gl );

      createMenu();

      Composite left = new Composite( shell, SWT.NONE );
      left.setLayout( new GridLayout() );

      info = new Label( left, SWT.NONE );
      info.setText( "Repository:\n\n" );

      GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, true );
      info.setLayoutData( gridData );

      gridData = new GridData( SWT.FILL, SWT.FILL, true, true );
      gridData.verticalSpan = 2;

      left.setLayoutData( gridData );

      createButtonGroup( left );
      createNamespaceList( shell );
      createNameList( shell );
      createProtocolTable( shell );
   }

   public void createProtocolTable( Composite parent ) {

      Group g = new Group( parent, SWT.NONE );
      g.setLayout( new GridLayout() );

      protocolTable = new Table( g, SWT.BORDER );

      protocolTable.setHeaderVisible( true );
      protocolTable.setLinesVisible( true );

      String[] tLabels = new String[] { "Namespace", "Name", "Version" };
      tcols = new TableColumn[ tLabels.length ];

      for ( int i = 0; i < tcols.length; i++ ) {
         tcols[ i ] = new TableColumn( protocolTable, SWT.NONE );
         tcols[ i ].setText( tLabels[ i ] );
         tcols[ i ].pack();
      }

      int listHeight = protocolTable.getItemHeight() * 10;
      Rectangle trim = protocolTable.computeTrim( 0, 0, 0, listHeight );

      GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, true );
      gridData.heightHint = trim.height;
      gridData.horizontalSpan = 2;
      protocolTable.setLayoutData( gridData );
      protocolTable.addMouseListener( new MouseListener() {

         @Override
         public void mouseUp( MouseEvent arg0 ) {}

         @Override
         public void mouseDown( MouseEvent arg0 ) {}

         @Override
         public void mouseDoubleClick( MouseEvent arg0 ) {
            openEditor();
         }
      } );

      g.setLayoutData( gridData );
   }

   private void refreshProtocolTable() throws RepositoryException {
      logger.info( "Refreshing Protocols" );
      protocolTable.removeAll();

      visibleProtocols.clear();
      for ( Protocol p : this.rep.getProtocols() ) {
         if ( namespaceList.getSelectionIndex() < 1 || p.getDescriptor().getNamespace().equals( namespaceList.getItem( namespaceList.getSelectionIndex() ) ) ) {
            if ( nameList.getSelectionIndex() < 1 || p.getDescriptor().getName().equals( nameList.getItem( nameList.getSelectionIndex() ) ) ) {
               visibleProtocols.add( p );
            }
         }
      }

      for ( Protocol p : visibleProtocols ) {
         TableItem it = new TableItem( protocolTable, SWT.NONE );
         int c = 0;
         it.setText( c++, p.getDescriptor().getNamespace() );
         it.setText( c++, p.getDescriptor().getName() );
         it.setText( c++, p.getDescriptor().getVersion().toString() );
      }

      for ( int i = 0; i < tcols.length; i++ ) {
         tcols[ i ].pack();
      }

      protocolTable.setSelection( 0 );

   }

   public void createNameList( Composite parent ) {
      Group g = new Group( parent, SWT.NONE );
      g.setLayout( new GridLayout() );

      nameList = new List( g, SWT.NONE );
      int listHeight = nameList.getItemHeight() * 10;
      Rectangle trim = nameList.computeTrim( 0, 0, 0, listHeight );
      nameList.addListener( SWT.Selection, new Listener() {

         @Override
         public void handleEvent( Event arg0 ) {

            try {
               refreshProtocolTable();
            }
            catch ( RepositoryException e ) {
               e.printStackTrace();
            }
         }
      } );

      GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, true );
      gridData.heightHint = trim.height;
      nameList.setLayoutData( gridData );

      g.setLayoutData( gridData );
   }

   public void createNamespaceList( Composite parent ) {
      Group g = new Group( parent, SWT.NONE );
      g.setLayout( new GridLayout() );

      namespaceList = new List( g, SWT.NONE );
      int listHeight = namespaceList.getItemHeight() * 10;
      Rectangle trim = namespaceList.computeTrim( 0, 0, 0, listHeight );
      namespaceList.addListener( SWT.Selection, new Listener() {

         @Override
         public void handleEvent( Event arg0 ) {
            try {
               refreshNameList();
               refreshProtocolTable();
            }
            catch ( RepositoryException e ) {
               e.printStackTrace();
            }
         }
      } );

      GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, true );
      gridData.heightHint = trim.height;
      namespaceList.setLayoutData( gridData );

      g.setLayoutData( gridData );
   }

   public void createButtonGroup( Composite parent ) {
      Group g = new Group( parent, SWT.BORDER );
      g.setText( "Protocols" );
      g.setLayout( new GridLayout() );

      GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, false );
      newProtocolButton = new Button( g, SWT.PUSH );
      newProtocolButton.setText( "New" );
      newProtocolButton.setLayoutData( gridData );
      newProtocolButton.addListener( SWT.Selection, newButtonListener() );

      editProtocolButton = new Button( g, SWT.PUSH );
      editProtocolButton.setText( "Edit" );
      editProtocolButton.setLayoutData( gridData );
      editProtocolButton.addListener( SWT.Selection, editButtonListener() );

      importProtocolButton = new Button( g, SWT.PUSH );
      importProtocolButton.setText( "Import" );
      importProtocolButton.setLayoutData( gridData );
      importProtocolButton.addListener( SWT.Selection, importButtonListener() );

      deleteProtocolButton = new Button( g, SWT.PUSH );
      deleteProtocolButton.setText( "Delete" );
      deleteProtocolButton.setLayoutData( gridData );
      deleteProtocolButton.addListener( SWT.Selection, deleteButtonListener() );

      // initially all disabled
      this.newProtocolButton.setEnabled( false );
      this.deleteProtocolButton.setEnabled( false );
      this.importProtocolButton.setEnabled( false );
      this.editProtocolButton.setEnabled( false );

      gridData = new GridData( SWT.FILL, SWT.FILL, true, false );
      g.setLayoutData( gridData );

   }

   public void saveProtocol( boolean replace ) throws RepositoryException {

      IEditableRepository r = (IEditableRepository) rep;

      // it's not a new protocol
      if ( preEdit != null && replace ) {
         logger.info( "Deleting protocol " + preEdit.getDescriptor().toString() );
         r.deleteProtocol( preEdit );
      }
      logger.info( "Adding protocol " + editing.getDescriptor().toString() );
      preEdit = editing.clone();
      r.addProtocol( preEdit );

      refresh();
   }

   private Listener newButtonListener() {
      return new Listener() {

         @Override
         public void handleEvent( Event arg0 ) {
            preEdit = null;
            shell.setEnabled( false );
            ProtocolEditor pe = new ProtocolEditor( rm );
            pe.shell.setSize( 1024, 600 );
            pe.initComponents();
            pe.setProtocol( editing = new Protocol(), ( rep instanceof IEditableRepository ) );
            pe.shell.open();
            Display display = pe.shell.getDisplay();
            while ( !pe.shell.isDisposed() ) {
               if ( !display.readAndDispatch() )
                  display.sleep();
            }
            shell.setEnabled( true );

            editing = null;
         }
      };
   }

   private Listener editButtonListener() {
      return new Listener() {

         @Override
         public void handleEvent( Event arg0 ) {
            openEditor();
         }
      };
   }

   private void openEditor() {

      logger.info( "Opening protocol editor" );

      if ( protocolTable.getSelectionIndex() >= 0 ) {
         shell.setEnabled( false );
         preEdit = visibleProtocols.get( protocolTable.getSelectionIndex() );

         ProtocolEditor pe = new ProtocolEditor( rm );
         pe.initComponents();
         pe.setProtocol( editing = preEdit.clone(), ( rep instanceof IEditableRepository ) );
         pe.shell.setSize( 1024, 600 );
         pe.shell.open();
         Display display = pe.shell.getDisplay();
         while ( !pe.shell.isDisposed() ) {
            if ( !display.readAndDispatch() )
               display.sleep();
         }
         shell.setEnabled( true );
         preEdit = null;
         editing = null;
      }
   }

   private Listener deleteButtonListener() {
      return new Listener() {

         @Override
         public void handleEvent( Event arg0 ) {
            if ( protocolTable.getSelectionIndex() >= 0 ) {
               if ( yesNoBox( "Confirm Delete", "Are you sure you want to delete the protocol " + visibleProtocols.get( protocolTable.getSelectionIndex() ) ) ) {
                  try {
                     ( (IEditableRepository) rep ).deleteProtocol( visibleProtocols.get( protocolTable.getSelectionIndex() ) );
                  }
                  catch ( RepositoryException e ) {
                     errorBox( "Delete Failed", e.getMessage() );
                  }
                  refresh();
               }
            }
         }
      };
   }

   private Listener importButtonListener() {
      return new Listener() {

         @Override
         public void handleEvent( Event arg0 ) {
            FileDialog fd = new FileDialog( shell, SWT.OPEN );
            fd.setText( "Import Protocol" );
            if ( lastdir != null ) {
               fd.setFilterPath( lastdir );
            }
            String[] filterExt = { "*.acr", "*.xml", "*.*" };
            fd.setFilterExtensions( filterExt );
            String filename = fd.open();
            if ( filename != null ) {
               File file = new File( filename );

               lastdir = file.getParent();
               try {
                  ( (IEditableRepository) rep ).addProtocol( XMLProtocolSerialiser.readProtocol( new FileInputStream( file ), rep ) );
               }
               catch ( RepositoryException e ) {
                  errorBox( "Loading Failed", e.getMessage() );
               }
               catch ( FileNotFoundException e ) {
                  errorBox( "Loading Failed", e.getMessage() );
               }
               catch ( ProtocolParseException e ) {
                  errorBox( "Loading Failed", e.getMessage() );
               }
               catch ( IOException e ) {
                  errorBox( "Loading Failed", e.getMessage() );
               }
               refresh();
            }
         }
      };
   }

   public RepositoryManager( Display display, URL root ) {
      this( display );
      try {
         this.openRepository( new URL( "http://acre.lill.is" ) );

      }
      catch ( MalformedURLException e ) {
         e.printStackTrace();
      }
   }

   public RepositoryManager( Display display, File root ) {
      this( display );
      this.openRepository( root );
   }

   public RepositoryManager( Display display ) {
      this.createShell( display );
      this.shell.open();
      this.shell.setSize( 1024, 600 );

      this.rm = this;
   }

   private void failedRepository( String message ) {
      errorBox( "Repository Error", message );
      this.failed = true;
      this.clear();
   }

   private void clear() {
      this.nameList.removeAll();
      this.nameList.add( "All Names (0)" );
      this.namespaceList.removeAll();
      this.namespaceList.add( "All Namespaces (0)" );
      this.protocolTable.removeAll();

      this.info.setText( "Repository:\n\n" );
   }

   private void refresh() {

      if ( !failed ) {
         try {
            logger.info( "Refreshing GUI" );

            refreshNamespaceList();
            refreshNameList();
            refreshProtocolTable();

            if ( !( this.rep instanceof IEditableRepository ) ) {
               this.newProtocolButton.setEnabled( false );
               this.deleteProtocolButton.setEnabled( false );
               this.importProtocolButton.setEnabled( false );
               this.editProtocolButton.setEnabled( true );
               this.editProtocolButton.setText( "View" );
            }
            else {
               this.newProtocolButton.setEnabled( true );
               this.deleteProtocolButton.setEnabled( true );
               this.importProtocolButton.setEnabled( true );
               this.editProtocolButton.setEnabled( true );
               this.editProtocolButton.setText( "Edit" );
            }

            info.setText( "Repository:\n" + this.rep.getBase() + ( ( this.rep instanceof IEditableRepository ) ? "\n(Local)" : "\n(Remote)" ) );
         }
         catch ( RepositoryException e ) {
            failedRepository( e.getMessage() );
         }
      }
   }

   private void refreshNameList() throws RepositoryException {
      nameList.removeAll();

      Set<String> filtered = new TreeSet<String>();

      // only some names should be filtered
      if ( namespaceList.getSelectionIndex() > 0 ) {
         String selected = namespaceList.getItem( namespaceList.getSelectionIndex() );
         for ( Protocol p : this.rep.getProtocols() ) {
            if ( p.getDescriptor().getNamespace().equals( selected ) ) {
               filtered.add( p.getDescriptor().getName() );
            }
         }
      }
      else {
         nameList.removeAll();
         for ( Protocol p : this.rep.getProtocols() ) {
            filtered.add( p.getDescriptor().getName() );
         }
      }

      nameList.add( "All Names (" + filtered.size() + ")" );
      nameList.setSelection( 0 );
      for ( String n : filtered ) {
         nameList.add( n );
      }

   }

   private void refreshNamespaceList() throws RepositoryException {
      namespaceList.removeAll();

      this.nameSpaces.clear();

      // find the one previously selected
      int size = this.rep.getProtocols().size();
      logger.info( "Repository contains " + size + " protocol" + ( size != 1 ? "s" : "" ) );
      for ( Protocol p : this.rep.getProtocols() ) {
         // this.names.add(p.getDescriptor().getName());
         this.nameSpaces.add( p.getDescriptor().getNamespace() );
         // this.protocols.add( p );
      }

      this.namespaceList.add( "All Namespaces (" + this.nameSpaces.size() + ")" );
      this.namespaceList.setSelection( 0 );

      for ( String ns : this.nameSpaces ) {
         this.namespaceList.add( ns );
      }

   }

   private void openRepository( File root ) {
      logger.info( "Opening repository: " + root );

      try {// TODO: this is somewhat of a hack!
         this.rep = (IProtocolManager) RepositoryFactory.openRepository( root.toString() );
         this.recents.repositoryAccessed( root.toString() );
         this.makeRecentMenu();
         this.failed = false;
         this.refresh();
      }
      catch ( RepositoryException e ) {
         errorBox( "Failed to open repository", e.getMessage() );
      }
   }

   private void openRepository( URL root ) {
      logger.info( "Opening repository: " + root );
      this.rep = new HTTPRepository( root );
      this.recents.repositoryAccessed( root.toString() );
      this.makeRecentMenu();
      this.failed = false;
      this.refresh();
   }

   public void createMenu() {

      // set up the menu
      Menu menu = new Menu( shell, SWT.BAR );

      // first menu is the "Protocol" menu.
      MenuItem repositoryItem = new MenuItem( menu, SWT.CASCADE );
      repositoryItem.setText( "Repository" );
      Menu protocolMenu = new Menu( menu );
      repositoryItem.setMenu( protocolMenu );

      // New repository
      newItem = new MenuItem( protocolMenu, SWT.NONE );
      newItem.setText( "&New\tCtrl+N" );
      newItem.setAccelerator( SWT.MOD1 + 'N' );

      // Open local repository
      openItem = new MenuItem( protocolMenu, SWT.NONE );
      openItem.setText( "&Open Local\tCtrl+O" );
      openItem.setAccelerator( SWT.MOD1 + 'O' );

      // Open remote repository
      remoteItem = new MenuItem( protocolMenu, SWT.NONE );
      remoteItem.setText( "Open &Remote\tCtrl+R" );
      remoteItem.setAccelerator( SWT.MOD1 + 'R' );

      recentItem = new MenuItem( protocolMenu, SWT.CASCADE );
      recentItem.setText( "Open Recen&t\tCtrl+T" );
      recentItem.setAccelerator( SWT.MOD1 + 'T' );

      makeRecentMenu();

      // Save open protocol
      // saveItem = new MenuItem( protocolMenu, SWT.NONE );
      // saveItem.setText( "&Save\tCtrl+S" );
      // saveItem.setAccelerator( SWT.MOD1 + 'S' );
      // saveItem.addSelectionListener( ml );

      // Save as a different file
      // asItem = new MenuItem( protocolMenu, SWT.NONE );
      // asItem.setText( "Save &As\tCtrl+A" );
      // asItem.setAccelerator( SWT.MOD1 + 'A' );
      // asItem.addSelectionListener( ml );

      exportItem = new MenuItem( protocolMenu, SWT.NONE );
      exportItem.setText( "&Export\tCtrl+E" );
      exportItem.setAccelerator( SWT.MOD1 + 'E' );

      // Don't show Quit option on Macs
      if ( !SWT.getPlatform().equals( "cocoa" ) ) {

         // Line before the Quit option
         new MenuItem( protocolMenu, SWT.SEPARATOR );

         // Quit the editor
         quitItem = new MenuItem( protocolMenu, SWT.NONE );
         quitItem.setText( "&Quit\tCtrl+Q" );
         quitItem.setAccelerator( SWT.MOD1 + 'Q' );
      }

      attachListener( menu );

      // add the menu to the window
      shell.setMenuBar( menu );

   }

   // recursively attach a listener to all menu items
   private void attachListener( Menu menu ) {
      MenuListener ml = new MenuListener( this );
      attachListener( menu, ml );
   }

   private void attachListener( Menu menu, MenuListener ml ) {
      MenuItem[] items = menu.getItems();
      for ( MenuItem item : items ) {
         attachListener( item, ml );
      }
   }

   private void attachListener( MenuItem item, MenuListener ml ) {
      item.addSelectionListener( ml );
      if ( item.getMenu() != null ) {
         attachListener( item.getMenu(), ml );
      }
   }

   private void makeRecentMenu() {
      recentMenu = new Menu( shell, SWT.DROP_DOWN );
      recentItem.setMenu( recentMenu );

      for ( String s : this.recents.getRecentRepositories() ) {
         MenuItem mi = new MenuItem( recentMenu, SWT.NONE );
         mi.setText( s );
      }
   }

   public boolean yesNoBox( String title, String message ) {
      MessageBox mb = new MessageBox( this.shell, SWT.APPLICATION_MODAL | SWT.YES | SWT.NO );
      mb.setText( title );
      mb.setMessage( message );
      return mb.open() == SWT.YES;
   }

   public void errorBox( String title, String message ) {
      MessageBox mb = new MessageBox( this.shell, SWT.APPLICATION_MODAL | SWT.ERROR );
      mb.setText( title );
      mb.setMessage( message );
      mb.open();

   }

   public boolean okCancelBox( String title, String message ) {
      MessageBox mb = new MessageBox( this.shell, SWT.APPLICATION_MODAL | SWT.OK | SWT.CANCEL );
      mb.setText( title );
      mb.setMessage( message );
      return mb.open() == SWT.OK;
   }

   public void okBox( String title, String message ) {
      MessageBox mb = new MessageBox( this.shell, SWT.APPLICATION_MODAL | SWT.OK );
      mb.setText( title );
      mb.setMessage( message );
      mb.open();

   }

   class MenuListener extends SelectionAdapter {
      private RepositoryManager rm;

      private File file;

      public MenuListener( RepositoryManager repositoryManager ) {
         super();
         this.rm = repositoryManager;
      }

      /**
       * Process selection events
       * 
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      public void widgetSelected( SelectionEvent event ) {

         Object source = event.getSource();

         if ( source == newItem ) {

            DirectoryDialog dd = new DirectoryDialog( rm.shell, SWT.OPEN );
            dd.setText( "Select Location for new repository" );
            if ( rm.lastdir != null ) {
               dd.setFilterPath( rm.lastdir );
            }

            String filename = dd.open();

            if ( filename != null ) {

               File basedir = new File( filename );

               if ( basedir.list().length != 0 ) {
                  rm.errorBox( "Directory not empty", "A new ACRE repository can only be created in an empty directory." );
               }
               else {
                  try {
                     rm.rep = (IProtocolManager) RepositoryFactory.createRepository( basedir.toString() );
                     rm.failed = false;
                     rm.lastdir = basedir.toString();
                     rm.refresh();
                  }
                  catch ( RepositoryException e ) {
                     failedRepository( e.getMessage() );
                  }
               }
            }
         }

         else if ( source == openItem ) {
            DirectoryDialog dd = new DirectoryDialog( rm.shell, SWT.OPEN );
            dd.setText( "Open Local Repository" );
            if ( rm.lastdir != null ) {
               dd.setFilterPath( rm.lastdir );
            }
            else {
               dd.setFilterPath( System.getProperty( "user.home" ) );
            }

            String filename = dd.open();
            if ( filename != null ) {
               file = new File( filename );

               rm.lastdir = file.getParent();
               try {
                  rm.openRepository( file );
               }
               catch ( Exception e ) {
                  rm.errorBox( "Loading Failed", "Failed to open repository: " + file );

                  file = null;
               }
            }
         }
         else if ( source == remoteItem ) {
            URL remote = new OpenRemoteRepositoryDialog( shell, SWT.NONE, rm ).open();
            if ( remote != null ) {
               openRepository( remote );
            }
         }

         else if ( source == exportItem ) {
            RepositoryExporter.export( rm.rep );
         }

         else if ( source instanceof MenuItem && ( (MenuItem) source ).getParent() == recentMenu ) {
            String toOpen = ( (MenuItem) source ).getText();
            if ( toOpen.startsWith( "http://" ) ) {
               try {
                  openRepository( new URL( toOpen ) );
               }
               catch ( MalformedURLException e ) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }
            }
            else {
               rm.openRepository( new File( toOpen ) );
            }
         }

         else if ( ( (MenuItem) event.widget ) == rm.quitItem ) {
            rm.shell.close();
         }
      }
   }
}