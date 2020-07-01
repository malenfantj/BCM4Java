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

import fr.sorbonne_u.components.registry.ConnectionData;
import fr.sorbonne_u.components.registry.exceptions.BadConnectionDataException;
import fr.sorbonne_u.components.registry.exceptions.GlobalRegistryResponseException;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>LookupResponse</code> represents a response to a lookup
 * request from the global registry.
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
 * <p>Created on : 2020-06-17</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			LookupResponse
extends		Response
{
	/** true if the response is an error (key not bound).					*/
	protected boolean	isError = false;
	/** the error message (the queried key).								*/
	protected String	message;
	/** the string representing the RMI registry connection data.			*/
	protected String	connectionDataString;

	/**
	 * create a lookup response object.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code parameter != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param isError	true if the response is an error (key not bound).
	 * @param parameter	string that is in the response.
	 */
	public				LookupResponse(boolean isError, String parameter)
	{
		super();

		assert	!isError || parameter != null :
					new PreconditionException(
							"positivelookup response can't have a"
													+ " null parameter!");

		this.isError = isError;
		if (isError) {
			this.message = parameter;
		} else {
			this.connectionDataString = parameter;
		}
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean		equals(Object obj)
	{
		if (obj != null && obj instanceof LookupResponse) {
			LookupResponse r = (LookupResponse) obj;
			boolean ret = true;
			if (this.message == null) {
				ret = r.message == null;
			} else {
				ret = this.message.equals(r.message);
			}
			if (this.connectionDataString == null) {
				ret = ret && r.connectionDataString == null;
			} else {
				ret = ret && this.connectionDataString.
												equals(r.connectionDataString);
			}
			return	ret && this.isError == r.isError;
		} else {
			return false;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.registry.protocol.ResponseI#response2string()
	 */
	@Override
	public String		response2string()
	{
		StringBuffer sb = new StringBuffer(Response.LOOKUP_RESPONSE_NAME);
		sb.append(" ");
		if (this.isError) {
			sb.append(Response.NOK).append(" ").append(this.message);
		} else {
			sb.append(Response.OK).append(" ").
											append(this.connectionDataString);
		}
		return sb.toString();
	}

	@Override
	public Object		interpret()
	throws	GlobalRegistryResponseException,
			BadConnectionDataException
	{
		if (this.isError) {
			throw new GlobalRegistryResponseException(
												this.message + " not bound!");
		} else {
			return new ConnectionData(this.connectionDataString);
		}
	}
}
// -----------------------------------------------------------------------------
