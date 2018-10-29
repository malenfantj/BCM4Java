package fr.sorbonne_u.components.examples.cps;

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

import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.examples.cps.components.ContinuationExamples;
import fr.sorbonne_u.components.examples.cps.components.RandomValueProvider;
import fr.sorbonne_u.components.examples.cps.components.ValueConsumer;

//-----------------------------------------------------------------------------
/**
 * The class <code>CVM</code> deploys and executes  the continuation-passing
 * style example on a single JVM.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2018-03-17</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class				CVM
extends		AbstractCVM
{
	protected static final String	VALUE_PROVIDER_INBOUND_PORT_URI =
											"value-provider-inbound-port" ;

	public CVM() throws Exception {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.AbstractCVM#deploy()
	 */
	@Override
	public void			deploy() throws Exception {
		
		RandomValueProvider s =
			new RandomValueProvider(VALUE_PROVIDER_INBOUND_PORT_URI) ;
		this.addDeployedComponent(s) ;
		s.toggleTracing() ;
		
		ValueConsumer c =
			new ValueConsumer(VALUE_PROVIDER_INBOUND_PORT_URI) ;
		this.addDeployedComponent(c);
		c.toggleTracing();

		ContinuationExamples ce = new ContinuationExamples() ;
		this.addDeployedComponent(ce) ;
		ce.toggleTracing() ;

		super.deploy();
	}

	public static void main(String[] args) {
		try {
			CVM a = new CVM() ;
			a.startStandardLifeCycle(10000L) ;
			Thread.sleep(600000L);
			System.exit(0) ;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
//-----------------------------------------------------------------------------
