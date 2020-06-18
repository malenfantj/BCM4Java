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

import fr.sorbonne_u.components.registry.exceptions.GlobalRegistryResponseException;

// -----------------------------------------------------------------------------
/**
 * The abstract class <code>Response</code> is inherited by all classes
 * representing responses in the global registry protocol.
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
 * The subclasses of this abstract class represents the possible responses.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2020-06-17</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	Response
implements	ResponseI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// --+----------------------------------------------------------------------

	/** name of the lookup response.										*/
	public static final String	LOOKUP_RESPONSE_NAME = "lookup";
	/** name of the put response.											*/
	public static final String	PUT_RESPONSE_NAME = "put";
	/** name of the remove response.										*/
	public static final String	REMOVE_RESPONSE_NAME = "remove";
	/** name of the shutdown response.										*/
	public static final String	SHUTDOWN_RESPONSE_NAME = "shutdown";
	/** name of the error response.											*/
	public static final String	ERROR_RESPONSE_NAME = "error";
	/** name of the parameter saying that the request was successful.		*/
	public static final String	OK = "ok";
	/** name of the parameter saying that the request was erroneous.		*/
	public static final String	NOK = "nok";

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * convert a string representing a response to the corresponding
	 * response object, instance of a subclass of this abstract class. 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param response		the string representing the response.
	 * @return				the corresponding response object.
	 * @throws GlobalRegistryResponseException when the response is badly formatted.
	 */
	public static Response	string2response(String response) throws GlobalRegistryResponseException
	{
		if (response == null) {
			throw new GlobalRegistryResponseException(
									"Badly formatted response: " + response) ;
		}

		String[] tokens = response.split("\\s") ;
		switch (tokens[0]) {
		case LOOKUP_RESPONSE_NAME:
			if (tokens[1].equals(OK)) {
				return new LookupResponse(false, tokens[2]);
			} else {
				return new LookupResponse(true, tokens[2]);
			}
		case PUT_RESPONSE_NAME:
			if (tokens[1].equals(OK)) {
				return new PutResponse();
			} else {
				return new PutResponse(tokens[2]);
			}
		case REMOVE_RESPONSE_NAME:
			if (tokens[1].equals(OK)) {
				return new RemoveResponse();
			} else {
				return new RemoveResponse(tokens[2]);
			}
		case SHUTDOWN_RESPONSE_NAME:
			return new ShutdownResponse();
		case ERROR_RESPONSE_NAME:
			StringBuffer sb = new StringBuffer();
			for (int i = 1 ; i < tokens.length ; i++) {
				sb.append(tokens[i]);
				if (i < tokens.length - 1) {
					sb.append(" ");
				}
			}
			return new ErrorResponse(sb.toString());
		default:
			throw new GlobalRegistryResponseException(
									"Badly formatted response: " + response);
		}
	}
}
// -----------------------------------------------------------------------------
