package is.lill.acre.group;

import is.lill.acre.conversation.Conversation;
import is.lill.acre.conversation.ConversationManager;
import is.lill.acre.group.monitor.IGroupMonitor;
import is.lill.acre.group.monitor.NoSuchGroupException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;

public class GroupReasoner {

    private static Logger logger = Logger.getLogger( GroupReasoner.class.getName() );
    static {
        logger.setLevel( Level.OFF );
    }

    private ConversationManager cm;
    private int counter = 0;

    private Map<String, ConversationGroup> conversationGroups = new HashMap<String, ConversationGroup>();

    private Set<IGroupMonitor> monitors = new HashSet<IGroupMonitor>();

    public GroupReasoner( ConversationManager cm ) {
        this.cm = cm;
        cm.setGroupReasoner( this );
    }

    public Set<IGroupMonitor> getGroupMonitors() {
        return this.monitors;
    }

    public Collection<ConversationGroup> getGroups() {
        return this.conversationGroups.values();
    }

    public void removeFromGroup( String groupName, String conversationIdentifier ) {
        ConversationGroup g = this.getGroup( groupName );
        Conversation c = cm.getConversationByID( conversationIdentifier );
        if ( c != null && g != null ) {
            g.removeConversation( c );
            if ( g.isEmpty() ) {
                this.conversationGroups.remove( g.getName() );
            }
        }
        else {
            logger.severe( "Unknown conversation or group" );
        }
    }

    public void addToGroup( String groupName, String conversationIdentifier ) {
        ConversationGroup g = this.getGroup( groupName );
        Conversation c = cm.getConversationByID( conversationIdentifier );
        if ( c != null && g != null ) {
            g.addConversation( c );
        }
        else {
            logger.severe( "Unknown conversation or group" );
        }
    }

    public void removeGroupMonitor( String monitorClassName, String groupName, List<String> params ) {

        // iterate this way to avoid a ConcurrentModificationException
        for ( Iterator<IGroupMonitor> it = monitors.iterator(); it.hasNext(); ) {
            IGroupMonitor element = it.next();
            try {
                if ( Class.forName( monitorClassName ).isInstance( element ) && element.getGroupName().equals( groupName ) ) {
                    boolean same = true;
                    if ( element.getParams().size() == params.size() ) {
                        for ( int i = 0 ; i < params.size(); i++ ) {
                            if ( ! element.getParams().get(i).equals(params.get(i))) {
                                same = false;
                            }
                        }
                    }
                    else {
                        same = false;
                    }
                    
                    if ( same ) {
                        it.remove();
                    }
                }
            }
            catch ( ClassNotFoundException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void removeGroupMonitors( String groupName ) {
        // iterate this way to avoid a ConcurrentModificationException
        for ( Iterator<IGroupMonitor> it = monitors.iterator(); it.hasNext(); ) {
            IGroupMonitor element = it.next();
            if ( element.getGroupName().equals( groupName ) ) {
                it.remove();
            }
        }
    }

    public void setTimeout( String groupName, int timeout ) {
        for ( Conversation c : getGroup( groupName ).getConversations() ) {
            cm.setTimeout( c, timeout );
        }
    }

    public void addGroupMonitor( String monitorClassName, String groupName, List<String> params ) throws MonitorConfigurationException, NoSuchGroupException {

        IGroupMonitor monitor;

        if ( this.conversationGroups.containsKey( groupName ) ) {

            try {
                Class<?> monitorClass = Class.forName( monitorClassName );

                Constructor<?> constructor = monitorClass.getConstructor();

                monitor = (IGroupMonitor) constructor.newInstance();
                monitor.setDescription( monitorClassName );
                monitor.setGroupReasoner( this );
                monitor.init( this.conversationGroups.get( groupName ), params );

                monitors.add( monitor );
            }
            catch ( ClassNotFoundException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch ( SecurityException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch ( NoSuchMethodException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch ( IllegalArgumentException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch ( InstantiationException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch ( IllegalAccessException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch ( InvocationTargetException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else {
            throw new NoSuchGroupException( groupName );
        }

    }

    public ConversationGroup createGroup( String agentGroupName, String protocolIdentifier ) {
        String id = agentGroupName + protocolIdentifier + counter++;
        return createGroup( id );
    }

    public ConversationGroup createGroup( String groupName ) {
        ConversationGroup toReturn = new ConversationGroup( groupName );
        this.conversationGroups.put( groupName, toReturn );
        return toReturn;
    }

    public ConversationGroup getGroup( String groupName ) {
        return conversationGroups.get( groupName );
    }
    
    public boolean hasGroup( String groupName ) {
        return conversationGroups.containsKey( groupName );
    }

    public Set<IGroupMonitor> getEvents() {
        Set<IGroupMonitor> toReturn = new HashSet<IGroupMonitor>();
        for ( IGroupMonitor gm : this.monitors ) {
            if ( gm.event() ) {
                logger.info( "Found an event for " + gm.getGroupName() );
                toReturn.add( gm );
            }
        }
        return toReturn;
    }

    public void forget( Conversation c ) {
        Iterator<String> it = this.conversationGroups.keySet().iterator();
        while(it.hasNext() ) {
            String name = it.next();
            ConversationGroup cg = conversationGroups.get( name );
            cg.removeConversation( c );
            if (cg.isEmpty()) {
                it.remove();
            }
        }
    }
}
