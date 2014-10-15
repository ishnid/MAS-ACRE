package is.lill.acre.protocol;

import java.util.HashSet;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class to represent a state in a protocol.
 * 
 * @author daithi
 * @version $Id$
 */
public class State {

    // initialise logger
    private static Logger logger = Logger.getLogger( State.class.getName() );
    static {
        logger.setLevel( Level.OFF );
    }

    // set of transitions emerging from this state
    protected Set<Transition> outTransitions = new HashSet<Transition>();

    private boolean imported = false;

    // name of this state
    private String name;

    // set of transitions that travel from this node
    protected Set<Transition> inTransitions = new HashSet<Transition>();

    /**
     * Get the transitions that end at this state.
     * 
     * @return a set of {@link Transition} objects.
     */
    public Set<Transition> getInTransitions() {
        return inTransitions;
    }

    /**
     * Get the transitions that begin at this state.
     * 
     * @return a set of {@code Transition} objects.
     */

    public Set<Transition> getOutTransitions() {
        return outTransitions;
    }

    /**
     * Create a new state
     * 
     * @param name
     *            The name of the state
     */
    public State( String name ) {
        this.name = name;
    }

    /**
     * 
     * @return
     */
    public boolean isImported() {
        return this.imported;
    }

    /**
     * 
     * @param imported
     */
    public void setImported( boolean imported ) {
        this.imported = imported;
    }

    /**
     * Get the name of this State
     * 
     * @return State name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Add a transition that starts at this State
     * 
     * @param transition
     */
    public void addOutTransition( Transition transition ) {
        logger.info( "Adding outgoing transition to " + this.getName() );

        this.outTransitions.add( transition );
    }

    /**
     * Remove a transition that previously began at this state.
     * 
     * @param transition
     *            The transition to be removed.
     */
    public void removeOutTransition( Transition transition ) {
        logger.info( "Removing outgoing transition from " + this.getName() );
        this.outTransitions.remove( transition );
    }

    /**
     * Add a transition that ends at this state.
     * 
     * @param transition
     *            The transition to add.
     */
    public void addInTransition( Transition transition ) {
        logger.info( "Adding incoming transition to " + this.getName() );
        this.inTransitions.add( transition );
    }

    /**
     * Remove a transition that ends at this state.
     * 
     * @param transition
     *            The transition to remove.
     */
    public void removeInTransition( Transition transition ) {
        logger.info( "Removing incoming transition from " + this.getName() );
        this.inTransitions.remove( transition );
    }

    /**
     * Test whether this is the initial state of the protocol (i.e. it has no
     * incoming transitions).
     * 
     * @return {@code true} if it is the initial state, {@code false} otherwise.
     */
    public boolean isInitialState() {
        return this.inTransitions.isEmpty();
    }

    /**
     * Test whether this is a terminal state of the protocol (i.e. it has no
     * outgoing transitions).
     * 
     * @return {@code true} if it is a terminal state, {@code false} otherwise.
     */
    public boolean isTerminalState() {
        return this.outTransitions.isEmpty();
    }

    /**
     * Get the type of state
     * 
     * @return A {@link StateType} object representing whether this is an
     *         initial, terminal or middle state.
     */
    public StateType getType() {
        return this.isInitialState() ? StateType.INITIAL : this.isTerminalState() ? StateType.TERMINAL : StateType.MIDDLE;
    }

    /**
     * Find all the states that are reachable from this State. Can be used to
     * search further into the space.
     * 
     * @return A Set of State objects representing all available next states
     */
    public Set<State> getNextStates() {
        Set<State> toReturn = new HashSet<State>();
        for ( Transition t : this.outTransitions ) {
            toReturn.add( t.getEndState() );
        }
        return toReturn;
    }

    /**
     * Find all states from which it is possible to reach this state directly.
     * Useful for searching backwards through the search space.
     * 
     * @return A Set of State objects representing all possible previous states.
     */
    public Set<State> getPreviousStates() {
        Set<State> toReturn = new HashSet<State>();
        for ( Transition t : this.inTransitions ) {
            toReturn.add( t.getStartState() );
        }
        return toReturn;
    }

    /**
     * Generate a string representation of this state.
     * return A debugging string.
     */
    public String toString() {
        return "STATE\n\tName: [" + this.getName() + "]\n" + "\tType: [" + this.getType().toString() + "]\n";
    }
}
