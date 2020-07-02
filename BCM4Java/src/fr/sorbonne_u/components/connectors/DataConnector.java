package fr.sorbonne_u.components.connectors;

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

import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>DataConnector</code> partially implements a basic data
 * connector where components call each others using plain Java method calls
 * only.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * As there are two possible mode for transmitting data, push and pull, the
 * connector establishes a two way connection between the two components,
 * implementing the offering <code>PushCI</code> interface with methods calling
 * the requiring <code>PushCI</code> and implementing the requiring
 * <code>PullCI</code> interface with methods calling the offering
 * <code>PullCI</code> one.
 * </p>
 * <p>
 * The basic sequential data connector simply implements the offered push
 * interface <code>send</code> signature by a method calling the corresponding
 * required push interface signature <code>receive</code> that is then
 * implemented by the requiring component.  Conversely, this connector also
 * implements the required pull interface <code>request</code> signature by a
 * method that merely calls the corresponding offered pull interface
 * <code>get</code> signature then implemented by the offering component.  In
 * both cases, the translating method <code>from</code> is called to translate
 * the offered data type to the required one.
 * </p>
 * <p>
 * To complete this class, a subclass has only to implement the
 * <code>setConnectorOnComponents</code> method that is required when connecting
 * the two components to allow them to register the connector when needed.  In
 * the case of the basic sequential data connector, both components need to
 * register it to call it in both the push and pull modes.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2011-11-02</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			DataConnector
extends		AbstractDataConnector
{
	/**
	 * pass the request for a new datum from the requiring component to the
	 * offering component.
	 *
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code connected()}
	 * post	true		// no postcondition.
	 * </pre>
	 * 
	 * @throws Exception  <i>to do</i>.
	 * @see fr.sorbonne_u.components.interfaces.DataRequiredCI.PullCI#request()
	 */
	@Override
	public DataRequiredCI.DataI		request()
	throws	Exception
	{
		assert	this.connected() : new PreconditionException("connected()");

		return this.offered2required(((DataOfferedCI.PullCI)
												this.offering).get()) ;
	}

	/**
	 * push the datum sent by the offering component to the requiring one.
	 *
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code connected()}
	 * post	true		// no postcondition.
	 * </pre>
	 * 
	 * @throws Exception  <i>to do</i>.
	 * @see fr.sorbonne_u.components.interfaces.DataOfferedCI.PushCI#send(fr.sorbonne_u.components.interfaces.DataOfferedCI.DataI)
	 */
	@Override
	public void			send(DataOfferedCI.DataI d)
	throws	Exception
	{
		assert	this.connected() : new PreconditionException("connected()");

		((DataRequiredCI.PushCI) this.requiring).receive(
													this.offered2required(d)) ;
	}
}
// -----------------------------------------------------------------------------
