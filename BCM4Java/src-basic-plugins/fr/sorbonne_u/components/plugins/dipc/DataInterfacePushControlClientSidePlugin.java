package fr.sorbonne_u.components.plugins.dipc;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

// Copyright Jacques Malenfant, Sorbonne Universite.
//
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

import fr.sorbonne_u.components.plugins.dipc.connectors.PushControlConnector;
import fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlI;
import fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlImplementationI;
import fr.sorbonne_u.components.plugins.dipc.ports.PushControlOutboundPort;
import fr.sorbonne_u.components.plugins.helpers.AbstractClientSidePlugin;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.components.ports.OutboundPortI;

//------------------------------------------------------------------------------
/**
 * The class <code>DataInterfacePushControlClientSidePlugin</code> implements
 * the client-side role in this plug-in.
 *
 * <p><strong>Description</strong></p>
 * 
 * See the package documentation for detaled explanations.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2019-03-06</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class				DataInterfacePushControlClientSidePlugin
extends		AbstractClientSidePlugin
implements	PushControlImplementationI
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see fr.sorbonne_u.components.plugins.helpers.AbstractClientSidePlugin#getOutboundPort()
	 */
	@Override
	public PushControlOutboundPort	getOutboundPort()
	{
		AbstractOutboundPort p = super.getOutboundPort() ;
		assert	p instanceof PushControlOutboundPort ;
		return (PushControlOutboundPort) p ;
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlImplementationI#isPortExisting(java.lang.String)
	 */
	@Override
	public boolean			isPortExisting(String portURI)
	throws Exception
	{
		return ((PushControlI)this.pluginOutboundPort).
											isPortExisting(portURI) ;
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlImplementationI#startUnlimitedPushing(java.lang.String, long)
	 */
	@Override
	public void				startUnlimitedPushing(
		String portURI,
		long interval
		) throws Exception
	{
		((PushControlI)this.pluginOutboundPort).
							startUnlimitedPushing(portURI, interval) ;
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlImplementationI#startLimitedPushing(java.lang.String, long, int)
	 */
	@Override
	public void				startLimitedPushing(
		String portURI,
		long interval,
		int n
		) throws Exception
	{
		((PushControlI)this.pluginOutboundPort).
							startLimitedPushing(portURI, interval, n) ;
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlImplementationI#currentlyPushesData(java.lang.String)
	 */
	@Override
	public boolean			currentlyPushesData(String portURI)
	throws Exception
	{
		return ((PushControlI)this.pluginOutboundPort).currentlyPushesData(portURI) ;
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlImplementationI#stopPushing(java.lang.String)
	 */
	@Override
	public void				stopPushing(String portURI) throws Exception
	{
		((PushControlI)this.pluginOutboundPort).stopPushing(portURI) ;
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.helpers.AbstractClientSidePlugin#getRequiredInterface()
	 */
	@Override
	protected Class<? extends RequiredCI>	getRequiredInterface()
	{
		return PushControlI.class ;
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.helpers.AbstractClientSidePlugin#getOfferedInterface()
	 */
	@Override
	protected Class<? extends OfferedCI>	getOfferedInterface()
	{
		return PushControlI.class ;
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.helpers.AbstractClientSidePlugin#createOutboundPort()
	 */
	@Override
	protected OutboundPortI	createOutboundPort() throws Exception
	{
		return new PushControlOutboundPort(this.owner) ;
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.helpers.AbstractClientSidePlugin#getConnectorClassName()
	 */
	@Override
	protected String		getConnectorClassName()
	{
		return PushControlConnector.class.getCanonicalName() ;
	}
}
//------------------------------------------------------------------------------
