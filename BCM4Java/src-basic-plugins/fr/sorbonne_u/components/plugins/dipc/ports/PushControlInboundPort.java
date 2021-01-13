package fr.sorbonne_u.components.plugins.dipc.ports;

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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlCI;
import fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlImplementationI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

// -----------------------------------------------------------------------------
/**
 * The class <code>PushControlInboundPort</code> implements an inbound port
 * for the interface <code>PushControlI</code> assuming that the component
 * that offers this interface will use the plug-in
 * <code>DataInterfacesPushControlPlugin</code>.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2018-02-16</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			PushControlInboundPort
extends		AbstractInboundPort
implements	PushControlCI
{
	private static final long serialVersionUID = 1L ;

	/**
	 * create the inbound port with a given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	uri != null and pluginURI != null and owner != null
	 * pre	owner.canScheduleTasks()
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param uri			URI of the port.
	 * @param pluginURI		URI of the plug-in to be called.
	 * @param owner			component owning the port.
	 * @throws Exception	<i>todo.</i>
	 */
	public				PushControlInboundPort(
		String uri,
		String pluginURI,
		ComponentI owner
		) throws Exception
	{
		super(uri, PushControlCI.class, owner, pluginURI, null) ;

		assert	uri != null && pluginURI != null && owner != null ;
		assert	owner.canScheduleTasks() ;
	}

	/**
	 * create the inbound port with a automatically generated URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	pluginURI != null and owner != null
	 * pre	owner.canScheduleTasks()
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param pluginURI		URI of the plug-in to be called.
	 * @param owner			component owning the port.
	 * @throws Exception	<i>todo.</i>
	 */
	public				PushControlInboundPort(
		String pluginURI,
		ComponentI owner
		) throws Exception
	{
		super(PushControlCI.class, owner, pluginURI, null) ;

		assert	pluginURI != null && owner != null ;
		assert	owner.canScheduleTasks() ;
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlCI#isPortExisting(java.lang.String)
	 */
	@Override
	public boolean		isPortExisting(String portURI) throws Exception
	{
		return this.owner.handleRequest(
			new AbstractComponent.AbstractService<Boolean>(this.getPluginURI()) {
				@Override
				public Boolean call() throws Exception {
					return ((PushControlImplementationI)
									this.getServiceProviderReference()).
										isPortExisting(portURI) ;
				}
			}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlImplementationI#startUnlimitedPushing(java.lang.String, long)
	 */
	@Override
	public void			startUnlimitedPushing(
		String portURI,
		long interval
		) throws Exception
	{
		this.owner.handleRequest(
			new AbstractComponent.AbstractService<Void>(this.getPluginURI()) {
				@Override
				public Void call() throws Exception {
					((PushControlImplementationI)
						this.getServiceProviderReference()).
							startUnlimitedPushing(portURI, interval) ;
					return null;
				}
			}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlImplementationI#startLimitedPushing(java.lang.String, long, int)
	 */
	@Override
	public void			startLimitedPushing(
		String portURI,
		long interval,
		int n
		) throws Exception
	{
		this.owner.handleRequest(
			new AbstractComponent.AbstractService<Void>(this.getPluginURI()) {
				@Override
				public Void call() throws Exception {
					((PushControlImplementationI)
							this.getServiceProviderReference()).
								startLimitedPushing(portURI, interval, n) ;
					return null;
				}
			}) ;								
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlCI#currentlyPushesData(java.lang.String)
	 */
	@Override
	public boolean		currentlyPushesData(String portURI)
	throws Exception
	{
		return this.owner.handleRequest(
			new AbstractComponent.AbstractService<Boolean>(this.getPluginURI()) {
				@Override
				public Boolean call() throws Exception {
					return ((PushControlImplementationI)
									this.getServiceProviderReference()).
										currentlyPushesData(portURI) ;
				}
			}) ;
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlCI#stopPushing(java.lang.String)
	 */
	@Override
	public void			stopPushing(String portURI)
	throws Exception
	{
		this.owner.handleRequest(
			new AbstractComponent.AbstractService<Void>(this.getPluginURI()) {
				@Override
				public Void call() throws Exception {
					((PushControlImplementationI)
							this.getServiceProviderReference()).
								stopPushing(portURI) ;
					return null;
				}
			}) ;
	}
}
// -----------------------------------------------------------------------------
