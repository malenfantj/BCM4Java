package fr.sorbonne_u.components.plugins.dipc;

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

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlCI;
import fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlImplementationI;
import fr.sorbonne_u.components.plugins.dipc.ports.PushControlInboundPort;
import fr.sorbonne_u.components.ports.AbstractDataInboundPort;
import fr.sorbonne_u.components.ports.PortI;

// -----------------------------------------------------------------------------
/**
 * The class <code>DataInterfacesPushControlServerSidePlugin</code>
 * implements the server-side of a push control service that allows to
 * start and stop the push of data via a
 * <code>DataOfferedInboundPort</code> (or subclasses thereof).
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
public abstract class	DataInterfacesPushControlServerSidePlugin
extends		AbstractPlugin
implements	PushControlImplementationI
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Plug-in internal constants and variables
	// -------------------------------------------------------------------------

	/** inbound port through which the push control calls are received.		*/
	protected PushControlInboundPort				pushControlInboundPort;
	/** future variables associated with the scheduled push tasks to
	 *  be able to cancel them when required.								*/
	protected HashMap<String,ScheduledFuture<?>>	futures;

	// -------------------------------------------------------------------------
	// Plug-in generic methods
	// -------------------------------------------------------------------------

	/**
	 * verify that the owner can schedule tasks, as this facility is
	 * used to schedule the pushing tasks (BCM plug-ins are usually
	 * not allowed to create their own threads but rather use the
	 * ones of their owner component).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null && owner.canScheduleTasks()}
	 * post	true		// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.AbstractPlugin#installOn(fr.sorbonne_u.components.ComponentI)
	 */
	@Override
	public void			installOn(ComponentI owner)
	throws Exception
	{
		assert	owner != null && owner.canScheduleTasks();

		super.installOn(owner);

		this.addOfferedInterface(PushControlCI.class);
	}

	/**
	 * @see fr.sorbonne_u.components.PluginI#initialise()
	 */
	@Override
	public void			initialise() throws Exception
	{
		this.pushControlInboundPort =
			new PushControlInboundPort(this.getPluginURI(), this.getOwner());
		this.pushControlInboundPort.publishPort();
		this.futures = new HashMap<String,ScheduledFuture<?>>();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractPlugin#isInitialised()
	 */
	@Override
	public boolean		isInitialised()
	{
		return super.isInitialised() && this.futures != null;
	}

	/**
	 * will attempt to stop all pushing tasks that have not been stopped
	 * yet, letting currently executing ones finish if they are running
	 * at finalisation time.
	 * 
	 * @see fr.sorbonne_u.components.PluginI#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		for (Entry<String,ScheduledFuture<?>> e : this.futures.entrySet()) {
			e.getValue().cancel(false);
		}
		this.futures.clear();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractPlugin#uninstall()
	 */
	@Override
	public void			uninstall() throws Exception
	{
		this.pushControlInboundPort.unpublishPort();
		this.pushControlInboundPort.destroyPort();
		this.removeOfferedInterface(PushControlCI.class);
	}

	// -------------------------------------------------------------------------
	// Plug-in specific methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlImplementationI#isPortExisting(java.lang.String)
	 */
	@Override
	public boolean		isPortExisting(String portURI)
	throws Exception
	{
		assert	this.isInitialised();
		assert	portURI != null;

		PortI p = this.findPortFromURI(portURI);
		return this.getOwner().isPortExisting(portURI) &&
								p instanceof AbstractDataInboundPort;
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlImplementationI#startUnlimitedPushing(java.lang.String, long)
	 */
	@Override
	public void			startUnlimitedPushing(
		final String portURI,
		final long interval
		) throws Exception
	{
		assert	this.isInitialised();
		assert	portURI != null;
		assert	interval > 0;
		assert	this.isPortExisting(portURI);

		final DataInterfacesPushControlServerSidePlugin plugin = this;
		ScheduledFuture<?> f =
			this.scheduleTaskAtFixedRateOnComponent(
					new AbstractComponent.AbstractTask() {					
						@Override
						public void run() {
							try {
								plugin.pushOnPort(portURI);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					},
					interval,
					interval,
					TimeUnit.MILLISECONDS);
		this.futures.put(portURI, f);
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlImplementationI#startLimitedPushing(java.lang.String, long, int)
	 */
	@Override
	public void			startLimitedPushing(
		final String portURI,
		final long interval,
		final int n
		) throws Exception
	{
		assert	this.isInitialised();
		assert	portURI != null;
		assert	this.isPortExisting(portURI);
		assert	interval > 0;
		assert	n > 0;

		final DataInterfacesPushControlServerSidePlugin plugin = this;
		ScheduledFuture<?> f =
			this.scheduleTaskOnComponent(
					new AbstractComponent.AbstractTask() {
						@Override
						public void run() {
							try {
								plugin.limitedPushingTask(portURI ,
														  interval,
														  n);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					},
					interval,
					TimeUnit.MILLISECONDS);
		this.futures.put(portURI, f);
	}

	/**
	 * execute <code>n</code> data pushes on the given port with an interval
	 * of <code>interval</code> milliseconds.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code portURI != null && isPortExisting(portURI)}
	 * pre	{@code interval > 0 && n >= 0}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param portURI		URI of the port through which the push is done.
	 * @param interval		delay between pushes (in milliseconds).
	 * @param n				remaining number of pushes to be done, unless stopped.
	 * @throws Exception 	<i>to do</i>.
	 */
	protected void		limitedPushingTask(	
		final String portURI,
		final long interval,
		final int n
		) throws Exception
	{
		assert	portURI != null;
		assert	this.isPortExisting(portURI);
		assert	interval > 0;
		assert	n >= 0;

		// perform the next push.
		try {
			this.pushOnPort(portURI);
		} catch (Exception e) {
			throw new Exception(e);
		}
		// remove the future that corresponds to the current execution of
		// the method (allows to stop the pushes when requested).
		if (this.futures.containsKey(portURI)) {
			this.futures.remove(portURI);
		}
		// if there are still pushed to be done, schedule the next
		// execution of the method.
		if (n > 1) {
			final DataInterfacesPushControlServerSidePlugin plugin = this;
			ScheduledFuture<?> f =
				this.scheduleTaskOnComponent(
						new AbstractComponent.AbstractTask() {
							@Override
							public void run() {
								try {
									plugin.limitedPushingTask(portURI ,
															  interval,
															  n - 1);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						},
						interval,
						TimeUnit.MILLISECONDS);
			this.futures.put(portURI, f) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlImplementationI#currentlyPushesData(java.lang.String)
	 */
	@Override
	public boolean		currentlyPushesData(String portURI) throws Exception
	{
		assert	this.isInitialised();
		assert	portURI != null;
		assert	this.isPortExisting(portURI);

		return this.futures.containsKey(portURI);
	}

	/**
	 * @see fr.sorbonne_u.components.plugins.dipc.interfaces.PushControlImplementationI#stopPushing(java.lang.String)
	 */
	@Override
	public void			stopPushing(String portURI) throws Exception
	{
		assert	this.isInitialised();
		assert	portURI != null;
		assert	this.isPortExisting(portURI);

		if (this.currentlyPushesData(portURI)) {
			ScheduledFuture<?> f = this.futures.remove(portURI);
			f.cancel(false);
		}
	}

	/**
	 * push one piece of data on the port which URI is given; component
	 * dependent hence must be implemented when creating a specific
	 * instance of the plug-in.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isInitialised()}
	 * pre	{@code portURI != null}
	 * pre	{@code currentlyPushesData(String portURI)}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param portURI		URI of the port on which data has to be pushed.
	 * @throws Exception	<i>to do</i>.
	 */
	protected abstract void	pushOnPort(String portURI) throws Exception ;	
}
// -----------------------------------------------------------------------------
