package fr.sorbonne_u.components.plugins.dipc.example.components;

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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.PluginI;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.plugins.dipc.DataInterfacesPushControlServerSidePlugin;
import fr.sorbonne_u.components.plugins.dipc.example.interfaces.PairDataCI;
import fr.sorbonne_u.components.plugins.dipc.example.ports.PairDataInboundPort;

//------------------------------------------------------------------------------
/**
 * The class <code>ServerComponent</code> implements a component that offers
 * the push control services using the corresponding plug-in.
 *
 * <p><strong>Description</strong></p>
 * 
 * Almost all of the behaviour is provided by the plug-in. When creating the
 * plug-in, the component implements the method <code>pushOnPort</code> as
 * required, making it calling the method <code>produceNextData</code> to
 * produce the next data item to be pushed.
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
public class			ServerComponent
extends		AbstractComponent
{
	// ------------------------------------------------------------------------
	// Component constants and variables
	// ------------------------------------------------------------------------

	/** reflection inbound port URI used to create an instance of
	 *  this component.													*/
	protected static final String	SC_ReflectionInboundPort_URI = "sc-rip-uri" ;
	/** URI of the push control server-side plug-in.					*/
	protected static final String	PUSH_CONTROL_SS_PLUGIN_URI = "sc-pcp-uri" ;
	/** URI of the data inound port through which the data item
	 *  are pushed.														*/
	protected static final String	SS_DATAINBOUNDPORT_URI = "sc-dibp-uri" ;

	/** the data inound port through which the data item are pushed.	*/
	protected PairDataInboundPort	pairDataIBP ;
	/** for the data item.												*/
	protected int					x ;
	/** for the data item.												*/
	protected int					y ;

	// ------------------------------------------------------------------------
	// Internal classes
	// ------------------------------------------------------------------------

	/**
	 * The class <code>IntPair</code> implements the data interface
	 * required for the component data required and offered interfaces.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * The data item is just a pair of integers.
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
	public static class		IntPair
	implements PairDataCI.PairI
	{
		private static final long serialVersionUID = 1L ;
		protected final int	x ;
		protected final int y ;

		public				IntPair(int x, int y)
		{
			super() ;
			this.x = x;
			this.y = y;
		}

		@Override
		public Object getFirst()	{ return this.x ; }
		@Override
		public Object getSecond()	{ return this.y ; }
		@Override
		public String toString()	{ return "(" + x + "," + y + ")" ; }
	}

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	/**
	 * component creation.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @throws Exception 	<i>to do.</i>
	 */
	protected			ServerComponent() throws Exception
	{
		super(SC_ReflectionInboundPort_URI, 1, 1) ;

		this.getTracer().setTitle("ServerComponent") ;
		this.getTracer().setRelativePosition(1,  0) ;

		// Add the standard component interfaces for data exchanging
		// components.
		this.addOfferedInterface(DataOfferedCI.PullCI.class) ;
		this.addRequiredInterface(DataOfferedCI.PushCI.class) ;

		// create and publish the data inbound port.
		this.pairDataIBP =
				new PairDataInboundPort(SS_DATAINBOUNDPORT_URI, this) ;
		this.pairDataIBP.publishPort() ;

		// create the server-side push control plug-in and install it
		// on the current component.
		final PairDataInboundPort pdIBP = this.pairDataIBP ;
		final ServerComponent o = this ;
		PluginI plugin =
			new DataInterfacesPushControlServerSidePlugin() {
				private static final long serialVersionUID = 1L;

				@Override
				protected void pushOnPort(String portURI)
				throws Exception
				{
					assert	portURI != null ;
					assert	pdIBP.getPortURI().equals(portURI) ;

					pdIBP.send(o.produceNextData()) ;
				}
			};
		plugin.setPluginURI(PUSH_CONTROL_SS_PLUGIN_URI) ;
		this.installPlugin(plugin) ;

		// component data item initialisations.
		this.x = 0 ;
		this.y = 0 ;
	}

	// ------------------------------------------------------------------------
	// Component life-cycle methods
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public void			shutdown() throws ComponentShutdownException
	{
		try {
			this.pairDataIBP.unpublishPort() ;
			this.pairDataIBP.destroyPort() ;
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}

		super.shutdown() ;
	}

	// ------------------------------------------------------------------------
	// Component service methods
	// ------------------------------------------------------------------------

	/**
	 * produce the next data item by making an <code>IntPair</code>
	 * from the <code>x</code> and <code>y</code> values and then
	 * incrementing them.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the next data item.
	 */
	public PairDataCI.PairI	produceNextData()
	{
		IntPair d = new IntPair(this.x++, this.y++) ;
		this.logMessage("server component produces next data: " + d) ;
		return d ;
	}
}
//------------------------------------------------------------------------------
