package fr.sorbonne_u.components.ports.forplugins;

//Copyright Jacques Malenfant, Sorbonne Universite.
//
//Jacques.Malenfant@lip6.fr
//
//This software is a computer program whose purpose is to provide a
//basic component programming model to program with components
//distributed applications in the Java programming language.
//
//This software is governed by the CeCILL-C license under French law and
//abiding by the rules of distribution of free software.  You can use,
//modify and/ or redistribute the software under the terms of the
//CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
//URL "http://www.cecill.info".
//
//As a counterpart to the access to the source code and  rights to copy,
//modify and redistribute granted by the license, users are provided only
//with a limited warranty  and the software's author,  the holder of the
//economic rights,  and the successive licensors  have only  limited
//liability. 
//
//In this respect, the user's attention is drawn to the risks associated
//with loading,  using,  modifying and/or developing or reproducing the
//software by the user in light of its specific status of free software,
//that may mean  that it is complicated to manipulate,  and  that  also
//therefore means  that it is reserved for developers  and  experienced
//professionals having in-depth computer knowledge. Users are therefore
//encouraged to load and test the software's suitability as regards their
//requirements in conditions enabling the security of their systems and/or 
//data to be ensured and,  more generally, to use and operate it in the 
//same conditions as regards security. 
//
//The fact that you are presently reading this means that you have had
//knowledge of the CeCILL-C license and that you accept its terms.

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.exceptions.PreconditionException;
import fr.sorbonne_u.components.ports.AbstractDataInboundPort;

//----------------------------------------------------------------------------
/**
 * The class <code>AbstractDataInboundPortForPlugin</code> extends the standard
 * data inbound port for the case where the pull services to be called is
 * implemented by a plug-in.
 *
 * <p><strong>Description</strong></p>
 * 
 * The data inbound port must directly call a plug-in with a specific URI, so
 * the plug-in URI is passed at the creation of the port and used at each
 * call.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2017-12-22</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	AbstractDataInboundPortForPlugin
extends		AbstractDataInboundPort
{
	// ------------------------------------------------------------------------
	// Port instance variables and constructors
	// ------------------------------------------------------------------------

	private static final long	serialVersionUID = 1L;
	protected final String		pluginURI ;

	public				AbstractDataInboundPortForPlugin(
		Class<?> implementedInterface,
		String pluginURI,
		ComponentI owner
		) throws Exception
	{
		super(implementedInterface, owner);
		throw new RuntimeException("AbstractDataInboundPortForPlugin: must use"
				+ " the four or five parameters version of the constructor.") ;
	}

	public				AbstractDataInboundPortForPlugin(
		String uri,
		Class<?> implementedInterface,
		String pluginURI,
		ComponentI owner
		) throws Exception
	{
		super(uri, implementedInterface, owner);
		throw new RuntimeException("AbstractDataInboundPortForPlugin: must use"
				+ " the four or five parameters version of the constructor.") ;
	}

	/**
	 * create and initialise data inbound ports, with a given URI and a given
	 * plug-in URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	uri != null and owner != null
	 * pre	DataOfferedI.PullI.class.isAssignableFrom(implementedPullInterface)
	 * pre	DataOfferedI.PushI.class.isAssignableFrom(implementedPushInterface)
	 * pre	pluginURI != null and owner.isInstalled(pluginURI) ;
	 * post	this.getPortURI().equals(uri)
	 * post	this.getOwner().equals(owner)
	 * post	this.getImplementedInterface().equals(implementedPullInterface)
	 * post	this.getImplementedPushInterface().equals(implementedPushInterface)
	 * </pre>
	 *
	 * @param uri						unique identifier of the port.
	 * @param implementedPullInterface	pull interface implemented by this port.
	 * @param implementedPushInterface	push interface implemented by this port.
	 * @param pluginURI					URI of the plug-in to be called.
	 * @param owner						component that owns this port.
	 * @throws Exception					<i>todo.</i>
	 */
	public				AbstractDataInboundPortForPlugin(
		String uri,
		Class<?> implementedPullInterface,
		Class<?> implementedPushInterface,
		String pluginURI,
		ComponentI owner
		) throws Exception
	{
		super(uri, implementedPullInterface, implementedPushInterface, owner);

		assert	pluginURI != null :
					new PreconditionException("pluginURI != null") ;
		assert	owner.isInstalled(pluginURI) :
					new PreconditionException("owner.isInstalled(pluginURI)") ;

		this.pluginURI = pluginURI ;
	}

	/**
	 * create and initialise data inbound ports with an automatically generated
	 * URI but a given plug-in URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	owner != null
	 * pre	DataOfferedI.PullI.class.isAssignableFrom(implementedPullInterface)
	 * pre	DataOfferedI.PushI.class.isAssignableFrom(implementedPushInterface)
	 * pre	owner.isInstalled(pluginURI) ;
	 * post	this.getPortURI().equals(uri)
	 * post	this.getOwner().equals(owner)
	 * post	this.getImplementedInterface().equals(implementedPullInterface)
	 * post	this.getImplementedPushInterface().equals(implementedPushInterface)
	 * </pre>
	 *
	 * @param implementedPullInterface	pull interface implemented by this port.
	 * @param implementedPushInterface	push interface implemented by this port.
	 * @param pluginURI					URI of the plug-in to be called.
	 * @param owner						component that owns this port.
	 * @throws Exception 				<i>todo.</i>
	 */
	public				AbstractDataInboundPortForPlugin(
		Class<?> implementedPullInterface,
		Class<?> implementedPushInterface,
		String pluginURI,
		ComponentI owner
		) throws Exception
	{
		super(implementedPullInterface, implementedPushInterface, owner);

		assert	pluginURI != null :
					new PreconditionException("pluginURI != null") ;
		assert	owner.isInstalled(pluginURI) :
					new PreconditionException("owner.isInstalled(pluginURI)");

		this.pluginURI = pluginURI ;
	}
}
//----------------------------------------------------------------------------
