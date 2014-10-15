package is.lill.acre.group.monitor;

import is.lill.acre.group.MonitorConfigurationException;
import is.lill.acre.group.ConversationGroup;
import is.lill.acre.group.GroupReasoner;

import java.util.List;

/**
 * Abstract implementation of the {@link IGroupMonitor} interface that should be
 * extended by concrete group monitors.
 * 
 * @author daithi
 * @version $Id$
 * 
 */
public abstract class AbstractGroupMonitor implements IGroupMonitor {

    protected ConversationGroup group;
    protected GroupReasoner groupReasoner;
    protected static int PARAMS = 0;
    protected List<String> params;
    private String description;
    private boolean last;
    
    protected AbstractGroupMonitor() {}

    public String getDescription() {
        return this.description;
    }
    
    public void setDescription( String description ) {
        this.description = description;
    }
    
    @Override
    public String getGroupName() {
        return this.group.getName();
    }

    @Override
    public List<String> getParams() {
        return this.params;
    }

    @Override
    public void init( ConversationGroup group, List<String> params )
            throws MonitorConfigurationException {
        this.group = group;
        if ( params.size() == PARAMS ) {
            this.params = params;
        }
        else {
            throw new MonitorConfigurationException( "Parameter mismatch for "
                    + this.getClass().getName() + " " + PARAMS + " != "
                    + params.size() );
        }
    }

    @Override
    public boolean isActive() {
        return this.group != null;
    }

    @Override
    public void setGroupReasoner( GroupReasoner gr ) {
        this.groupReasoner = gr;
    }


    public abstract boolean raiseEvent();

    @Override
    public final boolean event() {
        this.last = this.raiseEvent();
        return this.last;
    }
    
    @Override
    public final boolean lastEvent() {
        return this.last;
    }
}
