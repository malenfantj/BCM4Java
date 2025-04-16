package fr.sorbonne_u.components.plugins.asynccall.connections;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide an
// implementation of the BCM component model.
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

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.helpers.CVMDebugModes;
import fr.sorbonne_u.components.plugins.asynccall.AbstractAsyncCall;
import fr.sorbonne_u.components.plugins.asynccall.AsyncCallCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

//------------------------------------------------------------------------------
/**
 * The class <code>AsyncCallOutboundPort</code> implements an outbound port
 * for the <code>AsyncCallCI</code> component interface.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2021-04-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			AsyncCallOutboundPort
extends		AbstractOutboundPort
implements	AsyncCallCI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create and initialise an outbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code AsyncCallCI.class.isAssignableFrom(getClass())}
	 * post	{@code !isDestroyed()}
	 * post	{@code AsyncCallCI.class.isAssignableFrom(getImplementedInterface())}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code !connected()}
	 * post {@code !isRemotelyConnected()}
	 * post	{@code owner.isPortExisting(getPortURI())}
	 * </pre>
	 *
	 * @param owner					component that owns this port.
	 * @throws Exception			<i>todo.</i>
	 */
	public				AsyncCallOutboundPort(ComponentI owner)
	throws Exception
	{
		super(AsyncCallCI.class, owner);
	}

	/**
	 * create and initialise an outbound port with a given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null && uri != null && implementedInterface != null}
	 * pre	{@code !owner.isPortExisting(uri)}
	 * pre	{@code AsyncCallResultReceptionCI.class.isAssignableFrom(getClass())}
	 * post	{@code !isDestroyed()}
	 * post	{@code AsyncCallCI.class.isAssignableFrom(getImplementedInterface())}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code !connected()}
	 * post {@code !isRemotelyConnected()}
	 * post	{@code owner.isPortExisting(uri)}
	 * </pre>
	 *
	 * @param uri			unique identifier of the port.
	 * @param owner			component that owns this port.
	 * @throws Exception 	<i>to do</i>.
	 */
	public				AsyncCallOutboundPort(String uri, ComponentI owner)
	throws Exception
	{
		super(uri, AsyncCallCI.class, owner);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.plugins.asynccall.AsyncCallCI#disconnectClient(java.lang.String)
	 */
	@Override
	public void			disconnectClient(String receptionPortURI)
	throws Exception
	{
		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.CALLING)) {
			System.out.println(
					"AsyncCallOutboundPort::disconnectClient "
					+ receptionPortURI);
		}

		((AsyncCallCI)this.getConnector()).disconnectClient(receptionPortURI);
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.asynccall.AsyncCallCI#asyncCall(fr.sorbonne_u.components.plugins.asynccall.AbstractAsyncCall)
	 */
	@Override
	public void			asyncCall(AbstractAsyncCall c) throws Exception
	{
		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.CALLING)) {
			System.out.println(
					"AsyncCallOutboundPort::asyncCall "
					+ c.getClass().getSimpleName());
		}

		((AsyncCallCI)this.getConnector()).asyncCall(c);
	}
}
// -----------------------------------------------------------------------------
