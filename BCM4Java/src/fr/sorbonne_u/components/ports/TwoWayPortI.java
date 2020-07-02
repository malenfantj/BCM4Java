package fr.sorbonne_u.components.ports;

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

import fr.sorbonne_u.components.connectors.AbstractTwoWayConnector;
import fr.sorbonne_u.components.interfaces.TwoWayCI;

//-----------------------------------------------------------------------------
/**
 * The interface <code>TwoWayPortI</code> represents the category of
 * <code>PortI</code> that cater for incoming and outgoing calls among
 * components that act both as clients and service providers towards each
 * others on the same interface.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : 2012-01-23</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		TwoWayPortI<TWI extends TwoWayCI>
extends		InboundPortI,
			OutboundPortI,
			TwoWayCI		// to be called both from the inside and outside
{
	/**
	 * @see fr.sorbonne_u.components.ports.InboundPortI#getImplementedInterface()
	 */
	@Override
	public Class<? extends TwoWayCI>	getImplementedInterface() throws Exception;

	/**
	 * @see fr.sorbonne_u.components.ports.OutboundPortI#getConnector()
	 */
	@Override
	public AbstractTwoWayConnector<TWI>	getConnector() throws Exception ;

	/**
	 * returns the proxy to be used by the owner component when calling
	 * components connected to this port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				proxy used when calling components connected to this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public TwoWayCI		getOut() throws Exception ;
}
//-----------------------------------------------------------------------------
