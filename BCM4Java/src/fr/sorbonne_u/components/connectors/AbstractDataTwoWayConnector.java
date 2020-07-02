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

import fr.sorbonne_u.components.interfaces.DataTwoWayCI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>AbstractDataTwoWayConnector</code> partially implements a
 * basic data connector between components exchanging data in a peer to peer
 * way rather than calling each others services.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This basic implementation of the <code>DataTwoWayConnectorI</code> simply
 * assumes that the class representing the data to be exchanged implements
 * both data interfaces of the two data two-way interfaces.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2012-01-24</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	AbstractDataTwoWayConnector
extends		AbstractTwoWayConnector<DataTwoWayCI>
implements	DataTwoWayConnectorI
{
	/**
	 * <p><strong>Description</strong></p>
	 * 
	 * This implementation of the method assumes that the concrete data class
	 * implements both DataI interfaces.  Subclasses should redefine if this
	 * assumption is not true.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.connectors.DataTwoWayConnectorI#second2first(fr.sorbonne_u.components.interfaces.DataTwoWayCI.DataI)
	 */
	@Override
	public DataTwoWayCI.DataI	second2first(DataTwoWayCI.DataI d)
	{
		// the data class implements at least the DataTwoWayCI.DataI interface
		return d;
	}

	/**
	 * <p><strong>Description</strong></p>
	 * 
	 * This implementation of the method assumes that the concrete data class
	 * implements both DataI interfaces.  Subclasses should redefine if this
	 * assumption is not true.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.connectors.DataTwoWayConnectorI#first2second(fr.sorbonne_u.components.interfaces.DataTwoWayCI.DataI)
	 */
	@Override
	public DataTwoWayCI.DataI	first2second(DataTwoWayCI.DataI d)
	{
		// the data class implements at least the DataTwoWayCI.DataI interface
		return d;
	}

	/**
	 * connect data two way ports.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code peer1 instanceof DataTwoWayCI}
	 * pre	{@code peer2 instanceof DataTwoWayCI}
	 * post	true		// no postcondition.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.connectors.AbstractTwoWayConnector#connect(fr.sorbonne_u.components.interfaces.OfferedCI, fr.sorbonne_u.components.interfaces.RequiredCI)
	 */
	@Override
	public synchronized void	connect(OfferedCI peer1, RequiredCI peer2)
	throws	Exception
	{
		// the only reason to redefine this method is to test these
		assert	peer1 instanceof DataTwoWayCI :
					new PreconditionException("peer1 instanceof DataTwoWayCI");
		assert	peer2 instanceof DataTwoWayCI :
					new PreconditionException("peer2 instanceof DataTwoWayCI");

		super.connect(peer1, peer2);
	}
}
// -----------------------------------------------------------------------------
