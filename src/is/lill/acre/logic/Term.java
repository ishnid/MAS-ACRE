package is.lill.acre.logic;

import java.util.ArrayList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A logic term within an ACRE conversation
 * 
 * @author daithi
 */
public class Term {

    public static int VARIABLE = 0;
    public static int CONSTANT = 1;

    // predicates and functions behave the same
    public static int PREDICATE = 2;
    public static int FUNCTION = 2;

    private static Logger logger = Logger.getLogger( Term.class.getName() );
    static {
        logger.setLevel( Level.INFO );
    }

    private int type;
    private boolean mutable;
    private String functor;
    private List<Term> arguments;

    public void addArgument( Term arg ) {
        if ( this.arguments == null ) {
            this.arguments = new ArrayList<Term>();
        }
        this.arguments.add( arg );
    }

    /**
     * Check if this represents a variable term
     * 
     * @return {@code true} if this term is a variable, {@code false} otherwise
     */
    public boolean isVariable() {
        return this.type == VARIABLE;
    }

    public boolean isAnonymousVariable() {
        return this.isVariable() && this.functor.isEmpty();
    }

    /**
     * Check if this term is mutable
     * 
     * @return {@code true} if the term is mutable, {@code false} otherwise
     */
    public boolean isMutable() {
        return this.mutable;
    }

    public void setMutable( boolean mutable ) {
        this.mutable = mutable;
    }

    public void setFunctor( String functor ) {
        this.functor = functor;
    }

    public void setType( int type ) {
        this.type = type;
    }

    /**
     * Check if this term is a constant (i.e. a no-argument function)
     * 
     * @return {@code true} if the term is constant, {@code false} otherwise
     */
    public boolean isConstant() {
        return this.type == CONSTANT;
    }

    /**
     * Check if this term is a predicate
     * 
     * @return {@code true} if the term is a predicate, {@code false} otherwise
     */
    public boolean isPredicate() {
        return this.type == PREDICATE;
    }

    /**
     * Check if this term is a function
     * 
     * @return {@code true} if the term is a function, {@code false} otherwise
     */
    public boolean isFunction() {
        return this.type == FUNCTION;
    }

    /**
     * Check if this term matches another term. A match happens if:
     * <ul>
     * <li>Either term is a variable</li>
     * <li>Both terms are constants and are equal</li>
     * <li>Both terms are functions, the functors are equal and their arguments
     * match recursively</li>
     * <li>Both terms are predicates, the functors are equal and their arguments
     * match recursively</li>
     * </ul>
     * 
     * @param t
     *            A second term to check for a match against
     * @return {@code true} if the terms match, {@code false} otherwise.
     */
    public boolean matches( Term t ) {
        if ( this.isVariable() || t.isVariable() ) {
            return true;
        }
        else if ( this.isConstant() && t.isConstant() && this.getFunctor().equals( t.getFunctor() ) ) {
            return true;
        }
        else if ( this.isFunction() && t.isFunction() ) {
            if ( !this.functor.equals( t.functor ) || this.arguments.size() != t.arguments.size() ) {
                return false;
            }
            for ( int i = 0; i < this.arguments.size(); i++ ) {
                if ( !this.arguments.get( i ).matches( t.arguments.get( i ) ) ) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Get the functor of this term
     * 
     * @return The functor
     */
    public String getFunctor() {
        return this.functor;
    }

    /**
     * Apply a set of bindings to this term, replacing any variable with its
     * binding in {@code b}, if present.
     * 
     * @param b
     *            A set of bindings to apply
     * @return The term after applying the bindings provided.
     */
    public Term applyBindings( Bindings b ) {
        // constants and anonymous variables can't have bindings
        if ( this.isConstant() || ( this.isVariable() && this.isAnonymousVariable() ) ) {
            return this;
        }
        else if ( this.isVariable() ) {
            Term toReturn = b.getBindingFor( this );
            return toReturn == null ? this : toReturn;
        }
        else if ( this.isFunction() ) {
            Term toReturn = new Term();
            toReturn.type = this.type;
            toReturn.functor = this.functor;
            toReturn.mutable = this.mutable;

            for ( Term t : this.arguments ) {
                toReturn.addArgument( t.applyBindings( b ) );
            }
            return toReturn;
        }

        System.err.println( "UNKNOWN TERM TYPE?? UH OH THAT'S NOT SUPPOSED TO HAPPEN!!" );
        return null;

    }

    /**
     * Compare this term to another and generate a set of bindings between any
     * variables present within this term and any values they match in the given
     * term.
     * 
     * @param t
     *            Another term to match against
     * @return A set of bindings from matching the variables in this object with
     *         constant values in {@code t}.
     */
    public Bindings getBindings( Term t ) {
        Bindings toReturn = new Bindings();

        addBindings( this, t, toReturn );

        return toReturn;
    }

    private static void addBindings( Term t1, Term t2, Bindings b ) {
        if ( t1.matches( t2 ) ) {
            // t1 must be a non-anonymous variable and t2 shouldn't be any type
            // of
            // variable
            if ( t1.isVariable() && !t1.isAnonymousVariable() && !t2.isVariable() ) {
                b.addBinding( t1, t2 );
            }

            // if t1 and t2 are functions, try to bind their arguments
            // don't need to check if t2 is a function: it must be since they
            // match
            else if ( t1.isFunction() ) {
                for ( int i = 0; i < t1.arguments.size(); i++ ) {
                    addBindings( t1.arguments.get( i ), t2.arguments.get( i ), b );
                }
            }
        }
    }

    /**
     * Generate a clone of this term
     * 
     * @return A clone of this term
     */
    public Term clone() {
        // no point in cloning these: results in unnecessary object creation
        if ( this.isVariable() || this.isConstant() ) {
            return this;
        }

        Term toReturn = new Term();
        toReturn.type = this.type;
        toReturn.mutable = this.mutable;
        toReturn.functor = this.functor;
        for ( Term t : this.arguments ) {
            toReturn.addArgument( t.clone() );
        }
        return toReturn;
    }

    /**
     * Determine whether this term is equal to another. Terms are equal if:
     * <ul>
     * <li>They are variables of the same name</li>
     * <li>They are identical constants</li>
     * <li>They are both functions, with the same functors and an equal number
     * of arguments</li>
     * <li>They are both predicates, with the same functors and an equal number
     * of arguments</li>
     * </ul>
     * 
     * @param t
     *            Another term to compare with
     * @return {@code true} if the terms are equal, {@code false} otherwise
     */
    public boolean equals( Term t ) {
        if ( ( this.isVariable() && t.isVariable() ) || ( this.isConstant() && t.isConstant() ) ) {
            return this.functor.equals( t.functor );
        }
        else if ( this.isFunction() && t.isFunction() ) {
            if ( ( !this.functor.equals( t.functor ) ) || ( this.arguments.size() != t.arguments.size() ) ) {
                return false;
            }
            for ( int i = 0; i < this.arguments.size(); i++ ) {
                if ( !this.arguments.get( i ).equals( t.arguments.get( i ) ) ) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Get the arguments of this term
     * 
     * @return
     */
    public List<Term> getArguments() {
        return this.arguments;
    }

    /**
     * toString calls dotted around the code. Shouldn't be used as I've now
     * switched to formatters/parsers
     */
    public String toString() {
        throw new RuntimeException( "Term.toString() called" );
    }

    /**
     * To print debugging information on Terms
     * 
     * @return
     */
    public String toDebuggingString() {
        return Utilities.getTermFormatter( "acre" ).format( this );
    }
}
