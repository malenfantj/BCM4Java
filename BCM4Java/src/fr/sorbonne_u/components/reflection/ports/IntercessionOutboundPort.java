package fr.sorbonne_u.components.reflection.ports;

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

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.PluginI;
import fr.sorbonne_u.components.helpers.Logger;
import fr.sorbonne_u.components.helpers.TracerWindow;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.components.reflection.interfaces.IntercessionCI;

// -----------------------------------------------------------------------------
/**
 * The class <code>IntercessionOutboundPort</code> defines the outbound port
 * associated the interface <code>IntercessionI</code>.
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
public class			IntercessionOutboundPort
extends		AbstractOutboundPort
implements	IntercessionCI
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public				IntercessionOutboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, IntercessionCI.class, owner) ;
	}

	public				IntercessionOutboundPort(
		ComponentI owner
		) throws Exception
	{
		super(IntercessionCI.class, owner) ;
	}


	// -------------------------------------------------------------------------
	// Plug-ins facilities
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#installPlugin(fr.sorbonne_u.components.PluginI)
	 */
	@Override
	public void			installPlugin(PluginI plugin) throws Exception
	{
		((IntercessionCI)this.getConnector()).installPlugin(plugin) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#initialisePlugin(java.lang.String)
	 */
	@Override
	public void			initialisePlugin(String pluginURI) throws Exception
	{
		((IntercessionCI)this.getConnector()).initialisePlugin(pluginURI) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#finalisePlugin(java.lang.String)
	 */
	@Override
	public void			finalisePlugin(String pluginURI) throws Exception
	{
		((IntercessionCI)this.getConnector()).finalisePlugin(pluginURI) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#uninstallPlugin(java.lang.String)
	 */
	@Override
	public void			uninstallPlugin(String pluginId) throws Exception
	{
		((IntercessionCI)this.getConnector()).uninstallPlugin(pluginId) ;
	}

	// -------------------------------------------------------------------------
	// Logging facilities
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#toggleLogging()
	 */
	@Override
	public void			toggleLogging() throws Exception
	{
		((IntercessionCI)this.getConnector()).toggleLogging() ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#toggleTracing()
	 */
	@Override
	public void			toggleTracing() throws Exception
	{
		((IntercessionCI)this.getConnector()).toggleTracing() ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#setTracer(fr.sorbonne_u.components.helpers.TracerWindow)
	 */
	@Override
	public void			setTracer(TracerWindow tracer) throws Exception
	{
		((IntercessionCI)this.getConnector()).setTracer(tracer) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#traceMessage(java.lang.String)
	 */
	@Override
	public void			traceMessage(String message) throws Exception
	{
		((IntercessionCI)this.getConnector()).traceMessage(message) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#setLogger(fr.sorbonne_u.components.helpers.Logger)
	 */
	@Override
	public void			setLogger(Logger logger) throws Exception
	{
		((IntercessionCI)this.getConnector()).setLogger(logger) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#logMessage(java.lang.String)
	 */
	@Override
	public void			logMessage(String message) throws Exception
	{
		((IntercessionCI)this.getConnector()).logMessage(message) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#printExecutionLog()
	 */
	@Override
	public void			printExecutionLog() throws Exception
	{
		((IntercessionCI)this.getConnector()).printExecutionLog() ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#printExecutionLogOnFile(java.lang.String)
	 */
	@Override
	public void			printExecutionLogOnFile(String fileName)
	throws Exception
	{
		((IntercessionCI)this.getConnector()).printExecutionLogOnFile(fileName) ;
	}

	// -------------------------------------------------------------------------
	// Implemented interfaces management
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#addRequiredInterface(java.lang.Class)
	 */
	@Override
	public void			addRequiredInterface(Class<? extends RequiredCI> inter)
	throws Exception
	{
		((IntercessionCI)this.getConnector()).addRequiredInterface(inter) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#removeRequiredInterface(java.lang.Class)
	 */
	@Override
	public void			removeRequiredInterface(
		Class<? extends RequiredCI> inter
		) throws Exception
	{
		((IntercessionCI)this.getConnector()).removeRequiredInterface(inter) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#addOfferedInterface(java.lang.Class)
	 */
	@Override
	public void			addOfferedInterface(Class<? extends OfferedCI> inter)
	throws Exception
	{
		((IntercessionCI)this.getConnector()).addOfferedInterface(inter) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#removeOfferedInterface(java.lang.Class)
	 */
	@Override
	public void			removeOfferedInterface(Class<? extends OfferedCI> inter)
	throws Exception
	{
		((IntercessionCI)this.getConnector()).removeOfferedInterface(inter) ;
	}

	// -------------------------------------------------------------------------
	// Port management
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#doPortConnection(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void			doPortConnection(
		String portURI,
		String otherPortURI,
		String ccname
		) throws Exception
	{
		((IntercessionCI)this.getConnector()).
							doPortConnection(portURI, otherPortURI, ccname) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#doPortDisconnection(java.lang.String)
	 */
	@Override
	public void			doPortDisconnection(String portURI)
	throws Exception
	{
		((IntercessionCI)this.getConnector()).doPortDisconnection(portURI) ;
	}

	// -------------------------------------------------------------------------
	// Reflection facility
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#invokeService(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object		invokeService(String name, Object[] params)
	throws Exception
	{
		return ((IntercessionCI)this.getConnector()).invokeService(name, params) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#invokeServiceSync(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object		invokeServiceSync(String name, Object[] params)
	throws Exception
	{
		return ((IntercessionCI)this.getConnector()).invokeServiceSync(name, params) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#invokeServiceAsync(java.lang.String, java.lang.Object[])
	 */
	@Override
	public void			invokeServiceAsync(String name, Object[] params)
	throws Exception
	{
		((IntercessionCI)this.getConnector()).invokeServiceAsync(name, params) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		((IntercessionCI)this.getConnector()).execute() ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#runTask(fr.sorbonne_u.components.reflection.interfaces.IntercessionCI.AbstractRemoteComponentTask)
	 */
	@Override
	public void			runTask(AbstractRemoteComponentTask t) throws Exception
	{
		((IntercessionCI)this.getConnector()).runTask(t) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#runTask(java.lang.String, fr.sorbonne_u.components.reflection.interfaces.IntercessionCI.AbstractRemoteComponentTask)
	 */
	@Override
	public void			runTask(
		String executorServiceURI,
		AbstractRemoteComponentTask t
		) throws Exception
	{
		((IntercessionCI)this.getConnector()).runTask(executorServiceURI, t) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#insertBeforeService(java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public void			insertBeforeService(
		String methodName,
		String[] parametersCanonicalClassNames,
		String code
		) throws Exception
	{
		((IntercessionCI)this.getConnector()).
			insertBeforeService(
					methodName, parametersCanonicalClassNames, code) ;
	}

	/**
	 * @see fr.sorbonne_u.components.reflection.interfaces.IntercessionCI#insertAfterService(java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public void			insertAfterService(
		String methodName,
		String[] parametersCanonicalClassNames,
		String code
		) throws Exception
	{
		((IntercessionCI)this.getConnector()).
			insertAfterService(
					methodName, parametersCanonicalClassNames, code) ;
	}
}
// -----------------------------------------------------------------------------
