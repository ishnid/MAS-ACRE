package is.lill.acre.group.monitor;

import is.lill.acre.group.ConversationGroup;
import is.lill.acre.group.GroupReasoner;
import is.lill.acre.group.MonitorConfigurationException;

import java.util.List;

/**
 * Interface to be implemented by any class that seeks to monitor a group and
 * raise events. Each implementing class should monitor for a particular event
 * occurring within the group.
 * 
 * @author daithi
 * @version $Id$
 * 
 */
public interface IGroupMonitor {
    /**
     * Set the {@link GroupReasoner} that manages the group
     * 
     * @param gr
     *            A group reasoner
     */
    public void setGroupReasoner( GroupReasoner gr );

    /**
     * Get a description of this GroupMonitor (typically the classname of the implementing class).
     * @return The description of the monitor.
     */
    public String getDescription();
    public void setDescription( String description );
    
    
    /**
     * Get the name of the group being monitored.
     * 
     * @return The group name
     */
    public String getGroupName();

    /**
     * Test whether the event being monitored for has occurred
     * 
     * @return {@code true} if the event has occurred, {@code false} otherwise.
     */
    public boolean event();

    /**
     * Get the value of the last event that was raised. Does not change anything internally within the monitor.
     * @return
     */
    public boolean lastEvent();
    
    /**
     * Initialise this group monitor.
     * 
     * @param group
     *            The {@link ConversationGroup} that this will monitor.
     * @param params
     *            A list of parameters to configure the group monitor.
     * @throws MonitorConfigurationException
     *             if the parameters are not appropriate for this group monitor.
     */
    public void init( ConversationGroup group, List<String> params )
            throws MonitorConfigurationException;

    /**
     * Test if the monitor is active
     * 
     * @return {@code true} if it is active, or {@code false} otherwise.
     */
    public boolean isActive();

    /**
     * Accessor for the parameters that were passed to this group monitor's
     * {@code init} method.
     * 
     * @return
     */
    public List<String> getParams();
}
