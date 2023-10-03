package fr.sorbonne_u.exceptions;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// basic component programming model to program with components
// distributed applications in the Java programming language.
//
// This software is governed by the CeCILL-C license under French law and
// abiding by the rules of distribution of free software.  You can use,
// modify and/ or redistribute the software under the terms of the
// CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
// URL "http://www.cecill.info".
//
// As a counterpart to the access to the source code and  rights to copy,
// modify and redistribute granted by the license, users are provided only
// with a limited warranty  and the software's author,  the holder of the
// economic rights,  and the successive licensors  have only  limited
// liability. 
//
// In this respect, the user's attention is drawn to the risks associated
// with loading,  using,  modifying and/or developing or reproducing the
// software by the user in light of its specific status of free software,
// that may mean  that it is complicated to manipulate,  and  that  also
// therefore means  that it is reserved for developers  and  experienced
// professionals having in-depth computer knowledge. Users are therefore
// encouraged to load and test the software's suitability as regards their
// requirements in conditions enabling the security of their systems and/or 
// data to be ensured and,  more generally, to use and operate it in the 
// same conditions as regards security. 
//
// The fact that you are presently reading this means that you have had
// knowledge of the CeCILL-C license and that you accept its terms.

// -----------------------------------------------------------------------------
/**
 * The class <code>ContractException</code> defines exceptions which leaves a
 * trace on the out stream when raised.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2020-06-29</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			VerboseException
extends		Exception
{
	private static final int	LEVEL = 2;
	private static final long	serialVersionUID = 1L;
	/** when true, print messages on sysout.								*/
	public static boolean		VERBOSE = false;

	public				VerboseException()
	{
		this(LEVEL);
	}

	public				VerboseException(String message)
	{
		this(LEVEL, message);
	}

	public				VerboseException(Throwable cause)
	{
		this(LEVEL, cause);
	}

	public				VerboseException(String message, Throwable cause)
	{
		this(LEVEL, message, cause);
	}

	public				VerboseException(
		String message,
		Throwable cause,
		boolean enableSuppression,
		boolean writableStackTrace
		)
	{
		this(LEVEL, message, cause, enableSuppression, writableStackTrace);
	}

	public				VerboseException(int level)
	{
		if (VERBOSE) {
			StackTraceElement ste = new Throwable().getStackTrace()[level];
			System.out.println(this.getClass().getSimpleName() + " raised at "
							   + ste.toString() + "!");
		}
	}

	public				VerboseException(int level, String message)
	{
		super(message);
		if (VERBOSE) {
			StackTraceElement ste = new Throwable().getStackTrace()[level];
			System.out.println(
					this.getClass().getSimpleName() + " raised with message \""
					+ message + "\" at " + ste.toString() + "!");
		}
	}

	public				VerboseException(int level, Throwable cause)
	{
		super(cause);
		if (VERBOSE) {
			StackTraceElement ste = new Throwable().getStackTrace()[level];
			System.out.println(
					this.getClass().getSimpleName() + " raised with cause "
					+ cause + " at " + ste.toString() + "!");
		}
	}

	public				VerboseException(
		int level,
		String message,
		Throwable cause)
	{
		super(message, cause);
		if (VERBOSE) {
			StackTraceElement ste = new Throwable().getStackTrace()[level];
			System.out.println(
					this.getClass().getSimpleName() + " raised with message \""
					+ message + "\" and cause " + cause + " at "
					+ ste.toString() + "!");
		}
	}

	public				VerboseException(
		int level,
		String message,
		Throwable cause,
		boolean enableSuppression,
		boolean writableStackTrace
		)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		if (VERBOSE) {
			StackTraceElement ste = new Throwable().getStackTrace()[level];
			System.out.println(
					this.getClass().getSimpleName() + " raised with message \""
					+ message + "\" and cause " + cause + " at "
					+ ste.toString() + "!");
		}
	}
}
// -----------------------------------------------------------------------------
