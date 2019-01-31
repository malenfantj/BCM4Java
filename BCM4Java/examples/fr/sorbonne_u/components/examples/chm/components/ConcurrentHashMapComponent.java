package fr.sorbonne_u.components.examples.chm.components;

// Copyright Jacques Malenfant, Sorbonne Universite.
//
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

import fr.sorbonne_u.components.AbstractComponent;
import java.util.concurrent.ConcurrentHashMap;

//------------------------------------------------------------------------------
/**
 * The class <code>ConcurrentHashMapComponent</code>
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2019-01-22</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class				ConcurrentHashMapComponent
extends		AbstractComponent
{
	// ------------------------------------------------------------------------
	// Constants and variables
	// ------------------------------------------------------------------------

	public static final String	READ_ACCESS_HANDLER_URI = "rah" ;
	public static final String	WRITE_ACCESS_HANDLER_URI = "wah" ;
	protected final ConcurrentHashMap<String,Integer> chm ;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	/**
	 * 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param reflectionInboundPortURI
	 * @param nbThreads
	 * @param nbSchedulableThreads
	 */
	public				ConcurrentHashMapComponent(
		String reflectionInboundPortURI
		)
	{
		super(reflectionInboundPortURI, 1, 0) ;

		this.chm = new ConcurrentHashMap<String,Integer>() ;
		this.createNewExecutorService(READ_ACCESS_HANDLER_URI, 1, false) ;
		this.createNewExecutorService(WRITE_ACCESS_HANDLER_URI, 1, false) ;
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	public boolean		contains(int value)
	{
		return this.chm.contains(value) ;
	}

	public boolean		containsKey(String key)
	{
		return this.chm.containsKey(key) ;
	}

	public int			get(String key)
	{
		return this.chm.get(key) ;
	}

	public boolean		isEmpty()
	{
		return this.chm.isEmpty() ;
	}

	public int			put(String key, int value)
	{
		return this.chm.put(key, value) ;
	}

	public int			remove(String key)
	{
		return this.chm.remove(key) ;
	}

	public int			size()
	{
		return this.chm.size() ;
	}
}
//------------------------------------------------------------------------------
