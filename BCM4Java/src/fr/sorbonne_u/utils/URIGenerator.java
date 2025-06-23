package fr.sorbonne_u.utils;

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

import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>URIGenerator</code> provides two static methods to generate
 * unique resource identifiers.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * Uses a technique found here:
 * {@code http://www.asciiarmor.com/post/33736615/java-util-uuid-mini-faq}.
 * </p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2025-06-23</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	URIGenerator
{
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * generate a unique resource identifier.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret != null}
	 * </pre>
	 *
	 * @return	a distributed system-wide unique resource identifier.
	 */
	public static String	generateURI()
	{
		// see http://www.asciiarmor.com/post/33736615/java-util-uuid-mini-faq
		String ret = java.util.UUID.randomUUID().toString();

		assert	ret != null :
				new PostconditionException("Result shouldn't be null!");

		return ret;
	}

	/**
	 * generate a unique resource identifier which prefix is given by
	 * {@code prefix}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code prefix != null}
	 * post	{@code ret != null}
	 * </pre>
	 *
	 * @param prefix	string that will appear first in the URI.
	 * @return			a distributed system-wide unique resource identifier beginning with {@code prefix}.
	 */
	public static String	generateURIwithPrefix(String prefix)
	{
		assert	prefix != null : new PreconditionException("prefix != null");

		return prefix + "-" + generateURI();
	}
}
// -----------------------------------------------------------------------------
