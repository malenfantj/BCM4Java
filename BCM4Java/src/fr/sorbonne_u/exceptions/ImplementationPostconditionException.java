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
 * The class <code>ImplementationPostconditionException</code> defines the
 * exception type which instances are thrown when an implementation
 * postcondition violation occurs about the internal implementation of a
 * component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * In traditional contract-based programming, assertions are defined as
 * black-box expressions <i>i.e.</i>, using only publicly visible information
 * about the stakeholders. However, from the implementation point of view and
 * to enhance the quality of the software, it is also important to be able to
 * state assertions about the implementation of a piece of software <i>i.e.</i>,
 * assertions that use internal information about the stakeholders in a
 * white-box way and that deal with the implementation choices. When such an
 * assertion fails, an exception descendant of
 * <code>ImplementationContractException</code> is thrown.
 * </p>
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
 * <p>Created on : 2024-03-26</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			ImplementationPostconditionException
extends		ImplementationContractException
{
	private static final long serialVersionUID = 1L;
	private static final int	LEVEL = 4;

	public				ImplementationPostconditionException()
	{
		super(LEVEL);
	}

	public				ImplementationPostconditionException(String message)
	{
		super(LEVEL, message);
	}

	public				ImplementationPostconditionException(Throwable cause)
	{
		super(LEVEL, cause);
	}

	public				ImplementationPostconditionException(
		String message,
		Throwable cause
		)
	{
		super(LEVEL, message, cause);
	}

	public				ImplementationPostconditionException(
		String message,
		Throwable cause,
		boolean enableSuppression,
		boolean writableStackTrace
		)
	{
		super(LEVEL, message, cause, enableSuppression, writableStackTrace);
	}

	public				ImplementationPostconditionException(int level)
	{
		super(level);
	}

	public				ImplementationPostconditionException(
		int level,
		String message
		)
	{
		super(level, message);
	}

	public				ImplementationPostconditionException(
		int level,
		Throwable cause
		)
	{
		super(level, cause);
	}

	public				ImplementationPostconditionException(
		int level,
		String message,
		Throwable cause
		)
	{
		super(level, message, cause);
	}

	public				ImplementationPostconditionException(
		int level,
		String message,
		Throwable cause,
		boolean enableSuppression,
		boolean writableStackTrace
		)
	{
		super(level, message, cause, enableSuppression, writableStackTrace);
	}
}
// -----------------------------------------------------------------------------
