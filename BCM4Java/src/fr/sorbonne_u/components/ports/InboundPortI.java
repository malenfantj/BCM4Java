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

import fr.sorbonne_u.components.interfaces.OfferedCI;

//-----------------------------------------------------------------------------
/**
 * The interface <code>InboundPortI</code> represents the category of
 * <code>PortI</code> that cater for incoming calls from client components
 * towards the service providers.
 * 
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * An inbound port is a component port through which a provider component
 * is called to deliver its services.  It is therefore associated with an
 * offered interface that represents the services that are provided by the
 * component  but that it also implements so that its owner component is
 * called only through the inbound ports.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code getOwner().isOfferedInterface(getImplementedInterface()))}
 * </pre>
 * 
 * <p>Created on : 2011-11-07</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface			InboundPortI
extends		PortI,
			OfferedCI		// to be called by the client side to get the service
{

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#getImplementedInterface()
	 */
	@Override
	public Class<? extends OfferedCI> getImplementedInterface()
	throws Exception;
}
//-----------------------------------------------------------------------------
