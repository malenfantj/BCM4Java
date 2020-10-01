package fr.sorbonne_u.components.pre.dcc.ports;

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
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.components.pre.dcc.interfaces.DynamicComponentCreationCI;

// -----------------------------------------------------------------------------
/**
 * The class <code>DynamicComponentCreationOutboundPort</code> implements the
 * outbound port of a <code>DynamicCOmponentCreator</code> client component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2014-03-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			DynamicComponentCreationOutboundPort
extends		AbstractOutboundPort
implements	DynamicComponentCreationCI
{
	private static final long serialVersionUID = 1L;

	public 				DynamicComponentCreationOutboundPort(
		ComponentI owner
		) throws Exception
	{
		super(DynamicComponentCreationCI.class, owner);
	}

	public				DynamicComponentCreationOutboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, DynamicComponentCreationCI.class, owner);
	}

	/**
	 * @see fr.sorbonne_u.components.pre.dcc.interfaces.DynamicComponentCreationCI#createComponent(java.lang.String, java.lang.Object[])
	 */
	@Override
	public String		createComponent(
		String classname,
		Object[] constructorParams
		) throws Exception
	{
		return ((DynamicComponentCreationCI)this.getConnector()).
							createComponent(classname, constructorParams) ;
	}

	/**
	 * @see fr.sorbonne_u.components.pre.dcc.interfaces.DynamicComponentCreationCI#startComponent(java.lang.String)
	 */
	@Override
	public void			startComponent(String reflectionInboundPortURI)
	throws Exception
	{
		((DynamicComponentCreationCI)this.getConnector()).
								startComponent(reflectionInboundPortURI) ;
	}

	/**
	 * @see fr.sorbonne_u.components.pre.dcc.interfaces.DynamicComponentCreationCI#executeComponent(java.lang.String)
	 */
	@Override
	public void			executeComponent(String componentURI) throws Exception
	{
		((DynamicComponentCreationCI)this.getConnector()).
								executeComponent(componentURI) ;
	}

	/**
	 * @see fr.sorbonne_u.components.pre.dcc.interfaces.DynamicComponentCreationCI#finaliseComponent(java.lang.String)
	 */
	@Override
	public void			finaliseComponent(String componentURI) throws Exception
	{
		((DynamicComponentCreationCI)this.getConnector()).
								finaliseComponent(componentURI) ;
	}

	/**
	 * @see fr.sorbonne_u.components.pre.dcc.interfaces.DynamicComponentCreationCI#shutdownComponent(java.lang.String)
	 */
	@Override
	public void			shutdownComponent(String componentURI) throws Exception
	{
		((DynamicComponentCreationCI)this.getConnector()).
								shutdownComponent(componentURI) ;
	}

	/**
	 * @see fr.sorbonne_u.components.pre.dcc.interfaces.DynamicComponentCreationCI#shutdownNowComponent(java.lang.String)
	 */
	@Override
	public void			shutdownNowComponent(String componentURI)
	throws Exception
	{
		((DynamicComponentCreationCI)this.getConnector()).
								shutdownNowComponent(componentURI) ;
	}

	/**
	 * @see fr.sorbonne_u.components.pre.dcc.interfaces.DynamicComponentCreationCI#isDeployedComponent(java.lang.String)
	 */
	@Override
	public boolean		isDeployedComponent(String reflectionInboundPortURI)
	throws Exception
	{
		return ((DynamicComponentCreationCI)this.getConnector()).
								isDeployedComponent(reflectionInboundPortURI) ;
	}

	/**
	 * @see fr.sorbonne_u.components.pre.dcc.interfaces.DynamicComponentCreationCI#isStartedComponent(java.lang.String)
	 */
	@Override
	public boolean		isStartedComponent(String componentURI) throws Exception
	{
		return ((DynamicComponentCreationCI)this.getConnector()).
								isStartedComponent(componentURI) ;
	}

	/**
	 * @see fr.sorbonne_u.components.pre.dcc.interfaces.DynamicComponentCreationCI#isFinalisedComponent(java.lang.String)
	 */
	@Override
	public boolean		isFinalisedComponent(String componentURI)
	throws Exception
	{
		return ((DynamicComponentCreationCI)this.getConnector()).
								isFinalisedComponent(componentURI) ;
	}

	/**
	 * @see fr.sorbonne_u.components.pre.dcc.interfaces.DynamicComponentCreationCI#isShutdownComponent(java.lang.String)
	 */
	@Override
	public boolean		isShutdownComponent(String componentURI) throws Exception
	{
		return ((DynamicComponentCreationCI)this.getConnector()).
								isShutdownComponent(componentURI) ;
	}

	/**
	 * @see fr.sorbonne_u.components.pre.dcc.interfaces.DynamicComponentCreationCI#isTerminatedComponent(java.lang.String)
	 */
	@Override
	public boolean		isTerminatedComponent(String componentURI)
	throws Exception
	{
		return ((DynamicComponentCreationCI)this.getConnector()).
								isTerminatedComponent(componentURI) ;
	}
}
// -----------------------------------------------------------------------------
