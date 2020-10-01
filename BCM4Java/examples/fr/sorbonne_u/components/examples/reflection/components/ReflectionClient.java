package fr.sorbonne_u.components.examples.reflection.components;

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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.examples.reflection.connectors.MyServiceConnector;
import fr.sorbonne_u.components.examples.reflection.interfaces.MyServiceCI;
import fr.sorbonne_u.components.examples.reflection.ports.MyServiceOutboundPort;
import fr.sorbonne_u.components.reflection.connectors.ReflectionConnector;
import fr.sorbonne_u.components.reflection.interfaces.ReflectionCI;
import fr.sorbonne_u.components.reflection.ports.ReflectionOutboundPort;

//-----------------------------------------------------------------------------
/**
 * The class <code>ReflectionClient</code> defines the component that
 * exhibits the active behaviour in the example, calling the server
 * component reflectively to get information and change its service
 * implementation.
 *
 * <p><strong>Description</strong></p>
 * 
 * The component first connects itself to the reflection port of the
 * server to ask for the URI of its service inbound port. Then it call
 * the service <code>test</code> a first time to show its unchanged
 * behaviour, then it request two addition of code into the service
 * implementation and finallly calls the service again to show its
 * behaviour after the changes.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2018-02-18</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			ReflectionClient
extends		AbstractComponent
{
	protected ReflectionOutboundPort rObp ;
	protected String					serverRIPURI ;
	protected MyServiceOutboundPort	servicePort ;

	protected			ReflectionClient(
		String serverRIPURI
		) throws Exception
	{
		super(1, 0) ;
		assert	serverRIPURI != null ;

		this.serverRIPURI = serverRIPURI ;
		this.addRequiredInterface(ReflectionCI.class) ;
		this.rObp = new ReflectionOutboundPort(this) ;
		this.rObp.publishPort() ;

		this.addRequiredInterface(MyServiceCI.class) ;
		this.servicePort = new MyServiceOutboundPort(this) ;
		this.servicePort.publishPort() ;
	}

	@Override
	public void			execute() throws Exception
	{
		super.execute() ;

		this.doPortConnection(
					this.rObp.getPortURI(),
					this.serverRIPURI,
					ReflectionConnector.class.getCanonicalName());
		String[] uris =
				this.rObp.findInboundPortURIsFromInterface(
													MyServiceCI.class) ;
		this.doPortConnection(
					this.servicePort.getPortURI(),
					uris[0],
					MyServiceConnector.class.getCanonicalName()) ;

		this.servicePort.myService("Before change:") ;

		this.rObp.insertBeforeService(
						"test",
						new String[]{},
						"System.out.println(\"Yes!\") ;") ;
		this.rObp.insertAfterService(
						"test",
						new String[]{},
						"System.out.println(\"Again!\") ;");

		this.servicePort.myService("After change:") ;
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		this.servicePort.doDisconnection() ;
		this.servicePort.unpublishPort() ;
		this.rObp.doDisconnection() ;
		this.rObp.unpublishPort() ;

		super.finalise();
	}
}
//-----------------------------------------------------------------------------
