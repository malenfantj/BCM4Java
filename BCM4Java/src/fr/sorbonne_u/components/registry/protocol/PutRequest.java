package fr.sorbonne_u.components.registry.protocol;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// new implementation of the DEVS simulation standard for Java.
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

import java.io.PrintStream;
import java.util.concurrent.ConcurrentHashMap;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.helpers.CVMDebugModes;
import fr.sorbonne_u.components.helpers.Logger;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>PutRequest</code> represents a global registry put
 * request.
 *
 * <p><strong>Description</strong></p>
 * 
 * Part of a command design pattern implementation.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2020-06-16</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			PutRequest
extends		Request
{
	/** key of the entry to be added.										*/
	protected String	key;
	/** value of the entry to be added.										*/
	protected String	value;

	/**
	 * create a put request object.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code key != null && value != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param key	key of the entry to be added.
	 * @param value	value of the entry to be added.
	 */
	public				PutRequest(String key, String value)
	{
		super();
		assert	key != null && value != null :
					new PreconditionException(
							"badly formatted put request with key " + key +
							" and value " + value);

		this.key = key;
		this.value = value;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean		equals(Object obj)
	{
		if (obj != null && obj instanceof PutRequest) {
			PutRequest r = (PutRequest) obj;
			return this.key.equals(r.key) && this.value.equals(r.value);
		} else {
			return false;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.registry.protocol.RequestI#execute(java.io.PrintStream, java.util.concurrent.ConcurrentHashMap, fr.sorbonne_u.components.helpers.Logger)
	 */
	@Override
	public void			execute(
		PrintStream ps,
		ConcurrentHashMap<String, String> directory,
		Logger executionLog
		)
	{
		String result = directory.putIfAbsent(this.key, this.value);
		if (result != null) {
			// failed put, key already bound!
			ps.println((new PutResponse(this.key)).response2string()) ;
			if (executionLog != null &&
					AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.REGISTRY)) {
				executionLog.logMessage("Global registry can't bind " +
										this.key + " already bound to " +
										result + "!") ;
			}
		} else {
			// successful put
			ps.println((new PutResponse()).response2string()) ;
			if (executionLog != null &&
					AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.REGISTRY)) {
				executionLog.logMessage("Global registry binds " +
										this.key + " to " + result) ;
			}
		}
	}

	/**
	 * @see fr.sorbonne_u.components.registry.protocol.RequestI#request2string()
	 */
	@Override
	public String		request2string()
	{
		StringBuffer sb = new StringBuffer(Request.PUT_REQUEST_NAME);
		sb.append(" ").append(this.key).append(" ").append(this.value) ;
		return sb.toString() ;
	}

	/**
	 * @see fr.sorbonne_u.components.registry.protocol.Request#isPutRequest()
	 */
	@Override
	public boolean		isPutRequest()
	{
		return true;
	}
}
// -----------------------------------------------------------------------------
