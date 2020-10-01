package fr.sorbonne_u.components.examples.reflection.components;

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
import fr.sorbonne_u.components.examples.reflection.interfaces.MyServiceCI;
import fr.sorbonne_u.components.examples.reflection.ports.MyServiceInboundPort;
import fr.sorbonne_u.components.ports.PortI;

// -----------------------------------------------------------------------------
/**
 * The class <code>ReflectionServer</code> defines the component which is the
 * target of the example reflective processing.
 *
 * <p><strong>Description</strong></p>
 * 
 * The component defines only one service called <code>test</code> which
 * method will be reflectively changed by the client by adding to it two
 * lines.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2018-02-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			ReflectionServer
extends		AbstractComponent
{ 
	protected			ReflectionServer(
		String reflectionInboundPortURI
		) throws Exception
	{
		super(reflectionInboundPortURI, 1, 0) ;

		this.addOfferedInterface(MyServiceCI.class) ;
		MyServiceInboundPort ip = new MyServiceInboundPort(this) ;
		ip.publishPort() ;
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		PortI[] p = this.findPortsFromInterface(MyServiceCI.class) ;
		p[0].unpublishPort() ;

		super.finalise();
	}

	public void			myService(String message)
	{
		System.out.println("-------------------------------------------");
		System.out.println(message);
		this.test();
		System.out.println("-------------------------------------------") ;
	}

	public void 		test()
	{
		System.out.println("Code already there!") ;
	}
}
// -----------------------------------------------------------------------------
