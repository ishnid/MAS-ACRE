package is.lill.acre.gui.swt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecentManager {

   private static Logger logger = Logger.getLogger( RecentManager.class.getName() );
   static {
      logger.setLevel( Level.OFF );
   }

   private Map<String, Date> recent = new HashMap<String, Date>();
   private List<String> repos = new ArrayList<String>();

   // check whether we're running a Mac or not
   private static boolean isMac;
   static {
      isMac = System.getProperty( "os.name" ).equals( "Mac OS X" );
   }

   public RecentManager() {
      loadRecent();
   }

   private File getRecentFile() {
      if ( isMac ) {
         return new File( System.getProperty( "user.home" ), "Library/ACRE Editor/recent.txt" );
      }
      else {
         return new File( System.getProperty( "user.home" ), ".acre/recent.txt" );
      }
   }

   private void loadRecent() {
      File recentFile = getRecentFile();
      try {
         if ( recentFile.exists() ) {
            BufferedReader in = new BufferedReader( new FileReader( recentFile ) );
            String line;
            while ( ( line = in.readLine() ) != null ) {

               String[] pieces = line.split( "\t" );
               if ( pieces.length == 2 && !this.recent.containsKey( pieces[ 0 ] ) ) {
                  this.recent.put( pieces[ 0 ], new Date( Long.valueOf( pieces[ 1 ] ) ) );
                  this.repos.add( pieces[ 0 ] );
               }
            }
            in.close();
         }
      }
      catch ( FileNotFoundException e ) {
         logger.warning( "Recent repo file not found: " + e );
      }
      catch ( IOException e ) {
         logger.warning( "Failed to read from recent repo file: " + e );
      }
   }

   private void writeRecent() {
      File recentFile = getRecentFile();
      try {
         PrintWriter out = new PrintWriter( new FileWriter( recentFile ) );
         for ( String s : this.repos ) {
            out.println( s + "\t" + this.recent.get( s ).getTime() );
         }
         out.close();
      }
      catch ( IOException e ) {
         logger.warning( "Failed to write to recent repo file: " + e );
      }
   }
   
   public void repositoryAccessed( String s ) {
      Date d = new Date();
      // remove existing instance of s
      if ( this.recent.containsKey( s ) ) {
         this.repos.remove( this.repos.indexOf(  s ) );
      }
      // remove last elements until size is 4, then add new one
      while ( this.repos.size() > 4 ) {
         this.repos.remove( this.repos.size() - 1 );
      }
      this.repos.add( 0, s );
      this.recent.put( s, d );
      writeRecent();
   }

   public List<String> getRecentRepositories() {
      return new ArrayList<String>( this.repos );
   }
}
