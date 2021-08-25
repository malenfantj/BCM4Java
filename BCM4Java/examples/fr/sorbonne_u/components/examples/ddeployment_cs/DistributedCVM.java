package fr.sorbonne_u.components.examples.ddeployment_cs;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;

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

import fr.sorbonne_u.components.cvm.AbstractDistributedCVM;
import fr.sorbonne_u.components.examples.ddeployment_cs.components.DynamicAssembler;
import fr.sorbonne_u.components.helpers.CVMDebugModes;

//-----------------------------------------------------------------------------
/**
 * The class <code>DistributedCVM</code> creates a component assembly for the
 * multiple-JVM execution of the dynamic deployment example.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2014-03-14</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class				DistributedCVM
extends		AbstractDistributedCVM
{
	/* The following must be the same as the ones in the confi.xml file.	*/
	protected static String		ASSEMBLER_JVM_URI = "assembler" ;
	protected static String		PROVIDER_JVM_URI = "provider" ;
	protected static String		CONSUMER_JVM_URI = "consumer" ;

	protected DynamicAssembler	da ;

	public				DistributedCVM(
		String[] args,
		int xLayout,
		int yLayout
		) throws Exception
	{
		super(args, xLayout, yLayout) ;
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.AbstractDistributedCVM#initialise()
	 */
	@Override
	public void			initialise() throws Exception
	{
		super.initialise() ;

		String[] jvmURIs = this.configurationParameters.getJvmURIs() ;
		boolean assemblerJVM_URI_OK = false ;
		boolean providerJVM_URI_OK = false ;
		boolean consumerJVM_URI_OK = false ;
		for (int i = 0 ; i < jvmURIs.length &&
										(!assemblerJVM_URI_OK ||
										!providerJVM_URI_OK ||
										!consumerJVM_URI_OK) ; i++) {
			if (jvmURIs[i].equals(ASSEMBLER_JVM_URI)) {
				assemblerJVM_URI_OK = true ;
			} else if (jvmURIs[i].equals(PROVIDER_JVM_URI)) {
				providerJVM_URI_OK = true ;
			} else if (jvmURIs[i].equals(CONSUMER_JVM_URI)) {
				consumerJVM_URI_OK = true ;
			}
		}
		assert	assemblerJVM_URI_OK && providerJVM_URI_OK &&
												consumerJVM_URI_OK ;
	}

	/**
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.cvm.AbstractDistributedCVM#instantiateAndPublish()
	 */
	@Override
	public void			instantiateAndPublish() throws Exception
	{
		// ---------------------------------------------------------------------
		// Configuration phase
		// ---------------------------------------------------------------------

		// debugging mode configuration; comment and uncomment the line to see
		// the difference
		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.LIFE_CYCLE);
		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.INTERFACES);
		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.PORTS);
		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.CONNECTING);
		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.CALLING);
		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.EXECUTOR_SERVICES);

		// ---------------------------------------------------------------------
		// Creation phase
		// ---------------------------------------------------------------------

		if (thisJVMURI.equals(ASSEMBLER_JVM_URI)) {

			@SuppressWarnings("unused")
			String daURI =
				AbstractComponent.createComponent(
						DynamicAssembler.class.getCanonicalName(),
						new Object[]{CONSUMER_JVM_URI, PROVIDER_JVM_URI}) ;

		}

		super.instantiateAndPublish();
	}

	public static void	main(String[] args)
	{
		try {
			DistributedCVM dda = new DistributedCVM(args, 2, 5) ;
			dda.startStandardLifeCycle(15000) ;
			Thread.sleep(5000L) ;
			System.exit(0) ;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
//-----------------------------------------------------------------------------
