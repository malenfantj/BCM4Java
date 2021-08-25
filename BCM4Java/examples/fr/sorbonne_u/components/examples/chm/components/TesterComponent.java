package fr.sorbonne_u.components.examples.chm.components;

import java.util.concurrent.TimeUnit;

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
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.examples.chm.connectors.MapReadingConnector;
import fr.sorbonne_u.components.examples.chm.connectors.MapWritingConnector;
import fr.sorbonne_u.components.examples.chm.interfaces.MapReadingCI;
import fr.sorbonne_u.components.examples.chm.interfaces.MapTestingCI;
import fr.sorbonne_u.components.examples.chm.interfaces.MapWritingCI;
import fr.sorbonne_u.components.examples.chm.ports.MapReadingOutboundPort;
import fr.sorbonne_u.components.examples.chm.ports.MapWritingOutboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.reflection.connectors.ReflectionConnector;
import fr.sorbonne_u.components.reflection.interfaces.ReflectionCI;
import fr.sorbonne_u.components.reflection.ports.ReflectionOutboundPort;

//------------------------------------------------------------------------------
/**
 * The class <code>TesterComponent</code> tests the
 * <code>ConcurrentMapComponent</code> by starting several reading tasks
 * and one writing task.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2019-02-11</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@RequiredInterfaces(required = {ReflectionCI.class,
								MapReadingCI.class,
								MapTestingCI.class,
								MapWritingCI.class})
public class				TesterComponent
extends		AbstractComponent
//-----------------------------------------------------------------------------
{
	// ------------------------------------------------------------------------
	// Constants and variables
	// ------------------------------------------------------------------------

	/** URI of the concurrent hash map component reflection inbound port.	*/
	protected final String						chmReflectionIBPUri ;
	/** outbound port to the reading services of the concurrent hash map.	*/
	protected final MapReadingOutboundPort<String,Integer>
												readingOutboundPort ;
	/** outbound port to the writing services of the concurrent hash map.	*/
	protected final MapWritingOutboundPort<String,Integer>
												writingOutboundPort ;

	protected int	numberOfVerify = 0 ;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	/**
	 * create the tester component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	chmReflectionIBPUri != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param chmReflectionIBPUri	URI of the concurrent map reflection inbound port.
	 * @throws Exception				<i>to do.</i>
	 */
	protected			TesterComponent(String chmReflectionIBPUri)
	throws Exception
	{
		super(1, 5) ;

		assert	chmReflectionIBPUri != null ;

		this.getTracer().setTitle("TesterComponent") ;
		this.getTracer().setRelativePosition(0, 1) ;

		this.chmReflectionIBPUri = chmReflectionIBPUri ;

		this.readingOutboundPort =
						new MapReadingOutboundPort<String,Integer>(this) ;
		this.readingOutboundPort.publishPort() ;

		this.writingOutboundPort =
						new MapWritingOutboundPort<String,Integer>(this) ;
		this.writingOutboundPort.publishPort() ;

		AbstractComponent.checkImplementationInvariant(this);
		AbstractComponent.checkInvariant(this);
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		super.execute() ;

		ReflectionOutboundPort rop = new ReflectionOutboundPort(this) ;
		rop.publishPort() ;

		this.doPortConnection(rop.getPortURI(),
							  chmReflectionIBPUri,
							  ReflectionConnector.class.getCanonicalName());
		String[] readingIBPURI =
				rop.findInboundPortURIsFromInterface(MapReadingCI.class) ;
		assert	readingIBPURI != null && readingIBPURI.length == 1 ;
		this.doPortConnection(
				this.readingOutboundPort.getPortURI(),
				readingIBPURI[0],
				MapReadingConnector.class.getCanonicalName()) ;

		String[] writingIBPURI =
				rop.findInboundPortURIsFromInterface(MapWritingCI.class) ;
		assert	writingIBPURI != null && writingIBPURI.length == 1 ;
		this.doPortConnection(
				this.writingOutboundPort.getPortURI(),
				writingIBPURI[0],
				MapWritingConnector.class.getCanonicalName()) ;

		this.doPortDisconnection(rop.getPortURI()) ;
		rop.unpublishPort() ;
		rop.destroyPort() ;

		this.add("k") ;
	}

	/**
	 * add 15 key/value pairs in the hash map and then schedule 15
	 * tasks to verify their presence and another 15 tasks to remove
	 * them.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param key			prefix of the keys to be used.
	 * @throws Exception		<i>to do.</i>
	 */
	protected synchronized void	add(String key) throws Exception
	{
		final TesterComponent tc = this ;
		for (int i = 1 ; i <= 15 ; i++) {
			this.writingOutboundPort.put(key + i, i) ;
			final int count = i ;
			this.scheduleTask(
				AbstractComponent.STANDARD_SCHEDULABLE_HANDLER_URI,
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							tc.verifyPresence(key + count, count) ;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, 100L, TimeUnit.MILLISECONDS) ;
		}

		for (int i = 1 ; i <= 15 ; i++) {
			final int count = i ;
			this.scheduleTask(
				AbstractComponent.STANDARD_SCHEDULABLE_HANDLER_URI,
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							tc.remove(key + count, count) ;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, 100L, TimeUnit.MILLISECONDS) ;
		}
	}

	/**
	 * remove the key/value pair from the hash map and then verify
	 * its absence.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	key != null and value != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param key		key under which the value has been put in the hash map.
	 * @param value		value that must be associated with the key.
	 * @throws Exception	<i>to do.</i>
	 */
	protected synchronized void	remove(String key, int value) throws Exception
	{
		this.writingOutboundPort.remove(key) ;
		final TesterComponent tc = this ;
		this.runTask(
				AbstractComponent.STANDARD_SCHEDULABLE_HANDLER_URI,
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							tc.verifyPresence(key, value) ;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}) ;
	}

	/**
	 * verify the presence of absence of a key/value pair in the hash map.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	key != null and value != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param key		key under which the value has been put in the hash map.
	 * @param value		value that must be associated with the key.
	 * @throws Exception	<i>to do.</i>
	 */
	protected synchronized void	verifyPresence(
		String key,
		Integer value
		) throws Exception
	{
		assert	key != null && value != null ;

		this.logMessage("verifyPresence " + this.numberOfVerify++ +
								"----------------------------------") ;
		this.logMessage("value = " + this.readingOutboundPort.get(key)) ;
		this.logMessage("presence key = " +
							this.readingOutboundPort.containsKey(key)) ;
		this.logMessage("presence value = " +
							this.readingOutboundPort.containsValue(value)) ;
		this.logMessage("empty = " + this.readingOutboundPort.isEmpty()) ;
		this.logMessage("size = " + this.readingOutboundPort.size()) ;
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		this.doPortDisconnection(this.readingOutboundPort.getPortURI()) ;
		this.readingOutboundPort.unpublishPort() ;
		this.doPortDisconnection(this.writingOutboundPort.getPortURI()) ;
		this.writingOutboundPort.unpublishPort() ;

		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public void			shutdown() throws ComponentShutdownException
	{
		try {
			this.readingOutboundPort.destroyPort() ;
			this.writingOutboundPort.destroyPort() ;
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}

		super.shutdown();
	}
}
//------------------------------------------------------------------------------
