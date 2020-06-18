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

// -----------------------------------------------------------------------------
/**
 * The abstract class <code>Request</code> is inherited by all classes
 * representing a request in the global registry protocol.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * Part of an implementation of the Command design pattern.
 * </p>
 * <p>
 * The global registry uses a string-oriented socket-based protocol with
 * the following requests and responses: 
 * </p>
 * <pre>
 * Requests              Responses
 * 
 * lookup key            lookup ok value
 *                       lookup nok key
 * put key value         put ok
 *                       put nok key
 * remove key            remove ok
 *                       remove nok key
 * shutdown              shutdown ok
 * anything else         error request
 * </pre>
 * <p>
 * The subclasses of this abstract class represents the possible requests.
 * </p>
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
public abstract class	Request
implements	RequestI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// --+----------------------------------------------------------------------

	/** name of the lookup request.											*/
	protected static final String	LOOKUP_REQUEST_NAME = "lookup";
	/** name of the put request.											*/
	protected static final String	PUT_REQUEST_NAME = "put";
	/** name of the remove request.											*/
	protected static final String	REMOVE_REQUEST_NAME = "remove";
	/** name of the shutdown request.										*/
	protected static final String	SHUTDOWN_REQUEST_NAME = "shutdown";

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * deserialize the request into a request object.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param request	string representing the request.
	 * @return			the object representing the same request.
	 */
	public static Request	string2request(String request)
	{
		if (request == null) {
			return new UnknownRequest(request);
		}
		String[] tokens = request.split("\\s") ;
		Request r = null ;
		try {
			if (tokens[0].equals(LOOKUP_REQUEST_NAME)) {
				assert	tokens.length == 2;
				r = new LookupRequest(tokens[1]);
			} else if (tokens[0].equals(PUT_REQUEST_NAME)) {
				assert	tokens.length == 3;
				r = new PutRequest(tokens[1], tokens[2]);
			} else if (tokens[0].equals(REMOVE_REQUEST_NAME)) {
				assert	tokens.length == 2;
				r = new RemoveRequest(tokens[1]);
			} else if (tokens[0].equals(SHUTDOWN_REQUEST_NAME)) {
				assert	tokens.length == 1;
				r = new ShutdownRequest();
			} else {
				r = new UnknownRequest(request);
			}
		} catch(Throwable e) {
			r = new UnknownRequest(request);
		}
		return r;
	}

	/**
	 * @see fr.sorbonne_u.components.registry.protocol.RequestI#isLookupRequest()
	 */
	@Override
	public boolean		isLookupRequest()	{ return false; }

	/**
	 * @see fr.sorbonne_u.components.registry.protocol.RequestI#isPutRequest()
	 */
	@Override
	public boolean		isPutRequest()		{ return false; }

	/**
	 * @see fr.sorbonne_u.components.registry.protocol.RequestI#isRemoveRequest()
	 */
	@Override
	public boolean		isRemoveRequest()	{ return false; }

	/**
	 * @see fr.sorbonne_u.components.registry.protocol.RequestI#isShutdownRequest()
	 */
	@Override
	public boolean		isShutdownRequest()	{ return false; }

	/**
	 * @see fr.sorbonne_u.components.registry.protocol.RequestI#isUnknownRequest()
	 */
	@Override
	public boolean		isUnknownRequest()	{ return false; }
}
// -----------------------------------------------------------------------------
