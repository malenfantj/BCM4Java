package fr.sorbonne_u.components.interfaces;

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

import java.io.Serializable;

// -----------------------------------------------------------------------------
/**
 * The component interface <code>TwoWayDataCI</code> enables two components to
 * exchange data in a peer-to-peer way, that is the two components implement
 * the same component interface and have both the sender and receiver roles.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The component interface defines the type of data it will allow to exchange
 * and two methods <code>send</code> and <code>receive</code>. Components use
 * the <code>send</code> method to send a piece of data to the other component,
 * while they implement the <code>receive</code> to accept a piece of data
 * from the other component. The connector implements the <code>send</code>
 * method in such a way to call the <code>receive</code> of the other component
 * to pass the data to it.
 * </p>
 * <p>
 * Contrary to the data exchange offered and required component interfaces,
 * that assume that one component has the sender role and the other the receiver
 * one, in this case, the two have both roles, so they can send and receive data.
 * Hence, for the connector to be able to distinguish between the sender and
 * the receiver, the <code>send</code> and <code>receive</code> methods are
 * called through a proxy when going to the other component, as with two way
 * ports.
 * </p>
 * 
 * <p>Created on : 2012-01-23</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		DataTwoWayCI
extends		TwoWayCI
{

	/**
	 * The component interface <code>TwoWayDataCI.DataI</code> is a marker for
	 * data that can be exchanged between components using a two-way
	 * component data interface.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Created on : 2012-01-23</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public interface	DataI extends Serializable { }

	/**
	 * used in push mode to send a piece of data to the other component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param d				piece of data sent to the other component.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			send(DataI d) throws	Exception;

	/**
	 * used in pull mode to request a piece of data from the other component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				piece of data produced by the other component.
	 * @throws Exception	<i>to do</i>.
	 */
	public DataI		request() throws	Exception;
}
// -----------------------------------------------------------------------------
