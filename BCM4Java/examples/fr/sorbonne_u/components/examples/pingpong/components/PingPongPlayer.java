package fr.sorbonne_u.components.examples.pingpong.components;

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
import fr.sorbonne_u.components.connectors.DataConnector;
import fr.sorbonne_u.components.connectors.DataTwoWayConnector;
import fr.sorbonne_u.components.examples.pingpong.CVM;
import fr.sorbonne_u.components.examples.pingpong.connectors.PingPongConnector;
import fr.sorbonne_u.components.examples.pingpong.connectors.PingPongTwoWayConnector;
import fr.sorbonne_u.components.examples.pingpong.interfaces.PingPongCI;
import fr.sorbonne_u.components.examples.pingpong.interfaces.PingPongTwoWayCI;
import fr.sorbonne_u.components.examples.pingpong.ports.PingPongDataInboundPort;
import fr.sorbonne_u.components.examples.pingpong.ports.PingPongDataOutboundPort;
import fr.sorbonne_u.components.examples.pingpong.ports.PingPongDataTwoWayPort;
import fr.sorbonne_u.components.examples.pingpong.ports.PingPongInboundPort;
import fr.sorbonne_u.components.examples.pingpong.ports.PingPongOutboundPort;
import fr.sorbonne_u.components.examples.pingpong.ports.PingPongTwoWayPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.components.interfaces.DataTwoWayCI;

import java.util.concurrent.TimeUnit;

//----------------------------------------------------------------------------
/**
 * The class <code>PingPongPlayer</code> defines the ping pong players as
 * components.
 *
 * <p><strong>Description</strong></p>
 * 
 * This component illustrates the use of two way interfaces (inheriting
 * from <code>TwoWayCI</code>) and data two way interfaces (inheriting
 * from <code>DataTwoWayCI</code>.
 * 
 * The component offers/requires the <code>PingPongI</code> two way
 * interface defining a method <code>pingPong</code> used by the two
 * instances of <code>PingPongPlayer</code> to perform exchanges.
 * Each exchange will lead to a number of hits defined by the constant
 * <code>NUMBER_OF_HITS_IN_EXCHANGES</code>. The instance with the
 * service (constructor parameter <code>hasService</code> true)
 * will start the exchanges as defined in the method  <code>execute</code>.
 * It "serves" and the calls the method <code>pingPong</code> on the
 * other player through its two way port. As long as the number of hits
 * does not reach the defined one, the method <code>pingPong</code>
 * echos that the player hits the ball and the schedule a call to the
 * method <code>pingPong</code> on the other player after waiting 100
 * milliseconds.
 * 
 * When the method <code>pingPong</code> reaches the expected number of
 * hits in the first exchange,it starts a second exchange that will use
 * the standard data two way interface <code>DataTwoWayCI.PushCI</code>
 * implemented by the data two way port <code>PingPongDataTwoWayPort</code>
 * to send back and forth a ball (see the class <code>Ball</code>)  in
 * push mode. To perform this, the method <code>hit</code> is similar to
 * the method <code>pingPing</code>: it echos the hit, increment the number
 * of hits on the ball and until it reaches the expected number of hits,
 * schedules a push of the ball again to the other through its data two
 * way port.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2018-03-14</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			PingPongPlayer
extends		AbstractComponent
{
	/** The number of exchanges to be done before stopping.				*/
	public static final int				NUMBER_OF_HITS_IN_EXCHANGES = 10 ;

	/** URI of the component (player).									*/
	protected final String				uri ;
	/** True if the player has initially the service for the exchange.	*/
	protected final boolean				hasService ;
	/** The outbound port of the player.									*/
	protected PingPongOutboundPort		pingPongOutboundPort ;
	/** The inbound port URI of the player that has the service.			*/
	protected final String				player1PingPongInboundPortURI ;
	/** The inbound port URI of the player that does'nt have the service.	*/
	protected final String				player2PingPongInboundPortURI ;
	/** The inbound port of the player.									*/
	protected PingPongInboundPort		pingPongInboundPort ;
	/** The data outbound port URI of the player that has the service.		*/
	protected final String				player1PingPongDataOutboundPortURI ;
	/** The data inbound port URI of the player that has the service.		*/
	protected final String				player1PingPongDataInboundPortURI ;
	/** The data outbound port URI of the player that does'nt have the
	 *  service.															*/
	protected final String				player2PingPongDataOutboundPortURI ;
	/** The data inbound port URI of the player that does'nt have the
	 *  service.															*/
	protected final String				player2PingPongDataInboundPortURI ;
	/** Data outbound port of the component (player).						*/
	protected PingPongDataOutboundPort	pingPongDataOutboundPort ;
	/** Data inbound port of the component (player).						*/
	protected PingPongDataInboundPort	pingPongDataInboundPort ;
	/** The URI of the component, and the one used for the two way port.	*/
	protected final String				pingPongTwoWayPortURI ;
	/** URI of the other player's two way port.						 	*/
	protected final String				otherPingPongTwoWayPortURI ;
	/** This player two way port.										*/
	protected PingPongTwoWayPort			pingPongTwoWayPort ;
	/** This player data two way port.									*/
	protected PingPongDataTwoWayPort		pingpongDataTwoWayPort ;
	/** Counter for the number of hits still to be done in the exchange.	*/
	protected int						counter ;
	/** 	The ball, used in the pull parts.								*/
	protected Ball						ball ;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	/**
	 * create a ping-pong player.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	uri != null
	 * pre	player1PingPongInboundPortURI != null
	 * pre	player2PingPongInboundPortURI != null
	 * pre	player1PingPongDataInboundPortURI != null
	 * pre	player2PingPongDataInboundPortURI != null
	 * pre	twoWayPortURI != null
	 * pre	otherPingPongTwoWayPortURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param uri								URI of the component (player).
	 * @param hasService							true if the player has the service.
	 * @param player1PingPongInboundPortURI		inbound port URI of the player that has the service.
	 * @param player2PingPongInboundPortURI		inbound port URI of the player that does'nt have the service.
	 * @param player1PingPongDataOutboundPortURI	data outbound port URI of the player that has the service.
	 * @param player1PingPongDataInboundPortURI	data inbound port URI of the player that has the service.
	 * @param player2PingPongDataOutboundPortURI	data outbound port URI of the player that does'nt have the service.
	 * @param player2PingPongDataInboundPortURI	data inbound port URI of the player that does'nt have the service.
	 * @param pingPongTwoWayPortURI				URI of the two way port of the component.
	 * @param otherPingPongTwoWayPortURI			URI of the other player two way port.
	 * @throws Exception							<i>todo.</i>
	 */
	protected			PingPongPlayer(
		String uri,
		boolean hasService,
		String player1PingPongInboundPortURI,
		String player2PingPongInboundPortURI,
		String player1PingPongDataOutboundPortURI,
		String player1PingPongDataInboundPortURI,
		String player2PingPongDataOutboundPortURI,
		String player2PingPongDataInboundPortURI,
		String pingPongTwoWayPortURI,
		String otherPingPongTwoWayPortURI
		) throws Exception
	{
		// One standard thread to execute the internal methods like
		// start and execute and one schedulable thread to schedule
		// and execute the pingPong and hit.
		super(uri, 1, 1) ;

		assert	uri != null ;
		assert	pingPongTwoWayPortURI != null ;
		assert	player1PingPongInboundPortURI != null ;
		assert	player2PingPongInboundPortURI != null ;
		assert	otherPingPongTwoWayPortURI != null ;

		this.uri = uri ;
		this.hasService = hasService ;
		this.pingPongTwoWayPortURI = pingPongTwoWayPortURI ;
		this.player1PingPongInboundPortURI = player1PingPongInboundPortURI ;
		this.player2PingPongInboundPortURI = player2PingPongInboundPortURI ;
		this.player1PingPongDataOutboundPortURI = player1PingPongDataOutboundPortURI ;
		this.player1PingPongDataInboundPortURI = player1PingPongDataInboundPortURI ;
		this.player2PingPongDataOutboundPortURI = player2PingPongDataOutboundPortURI ;
		this.player2PingPongDataInboundPortURI = player2PingPongDataInboundPortURI ;
		this.otherPingPongTwoWayPortURI = otherPingPongTwoWayPortURI ;
		this.initialise(pingPongTwoWayPortURI) ;

		this.getTracer().setTitle(uri) ;
		if (uri.equals(CVM.PING_PONG_URI_1)) {
			this.getTracer().setRelativePosition(1, 0) ;
		} else {
			this.getTracer().setRelativePosition(1, 1) ;
		}

		AbstractComponent.checkImplementationInvariant(this);
		AbstractComponent.checkInvariant(this);
	}

	/**
	 * initialise the player (common to the constructors).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	pingPongTwoWayPortURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param pingPongTwoWayPortURI the URI of the port.
	 * @throws Exception		<i>todo.</i>
	 */
	protected void		initialise(
		String pingPongTwoWayPortURI
		) throws Exception
	{
		assert	pingPongTwoWayPortURI != null ;

		this.counter = PingPongPlayer.NUMBER_OF_HITS_IN_EXCHANGES ;
		this.ball = new Ball() ;
		this.ball.incrementNumberOfHits() ;

		// The standard offered and required interfaces and the ports.

		this.addRequiredInterface(PingPongCI.class) ;
		this.addOfferedInterface(PingPongCI.class) ;
		this.pingPongOutboundPort = new PingPongOutboundPort(this) ;
		this.pingPongOutboundPort.localPublishPort() ;
		if (this.hasService) {
			this.pingPongInboundPort =
				new PingPongInboundPort(
							this.player1PingPongInboundPortURI, this) ;
		} else {
			this.pingPongInboundPort =
				new PingPongInboundPort(
							this.player2PingPongInboundPortURI, this) ;
		}
		this.pingPongInboundPort.publishPort() ;

		// The data interfaces and ports.

		this.addOfferedInterface(DataOfferedCI.PullCI.class) ;
		this.addOfferedInterface(DataRequiredCI.PushCI.class) ;
		this.addRequiredInterface(DataOfferedCI.PushCI.class) ;
		this.addRequiredInterface(DataRequiredCI.PullCI.class) ;

		if (hasService) {
			this.pingPongDataOutboundPort =
				new PingPongDataOutboundPort(
							this.player1PingPongDataOutboundPortURI, this) ;
			this.pingPongDataInboundPort =
				new PingPongDataInboundPort(
							this.player1PingPongDataInboundPortURI, this) ;
		} else {
			this.pingPongDataOutboundPort =
				new PingPongDataOutboundPort(
							this.player2PingPongDataOutboundPortURI, this) ;
			this.pingPongDataInboundPort =
				new PingPongDataInboundPort(
							this.player2PingPongDataInboundPortURI, this) ;
		}
		this.pingPongDataOutboundPort.publishPort() ;
		this.pingPongDataInboundPort.publishPort() ;

		// The two way interface and port.

		this.addOfferedInterface(PingPongTwoWayCI.class) ;
		this.addRequiredInterface(PingPongTwoWayCI.class) ;
		this.pingPongTwoWayPort =
				new PingPongTwoWayPort(pingPongTwoWayPortURI, this) ;
		this.pingPongTwoWayPort.publishPort() ;

		// The data two way interface and port.

		this.addRequiredInterface(DataTwoWayCI.class) ;
		this.addOfferedInterface(DataTwoWayCI.class) ;
		// To simplify things, the URI is the same as the one of the
		// two way port, but with the suffix -dt
		this.pingpongDataTwoWayPort =
				new PingPongDataTwoWayPort(
									pingPongTwoWayPortURI + "-dtwp",
									this) ;
		this.pingpongDataTwoWayPort.publishPort() ;
	}

	// ------------------------------------------------------------------------
	// Component life-cycle
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public void			start() throws ComponentStartException
	{
		super.start() ;

		// The instance of player that has the service has arbitrarily
		// the responsibility to do the port connections and then the
		// disconnections.
		try {
			if (hasService) {
				this.doPortConnection(
						this.pingPongOutboundPort.getPortURI(),
						this.player2PingPongInboundPortURI,
						PingPongConnector.class.getCanonicalName()) ;
				// The next two are implementation alternatives: connect
				// from the data outbound port or from the data inbound port.
//				this.doPortConnection(
//						this.pingPongDataOutboundPort.getPortURI(),
//						this.player2PingPongDataInboundPortURI,
//						DataConnector.class.getCanonicalName()) ;
				this.doPortConnection(
						this.pingPongDataInboundPort.getPortURI(),
						this.player2PingPongDataOutboundPortURI,
						DataConnector.class.getCanonicalName()) ;
//				this.doPortConnection(
//						this.pingPongTwoWayPort.getPortURI(),
//						otherPingPongTwoWayPortURI,
//						PingPongTwoWayConnector.class.getCanonicalName()) ;
//				this.doPortConnection(
//						this.pingpongDataTwoWayPort.getPortURI(),
//						otherPingPongTwoWayPortURI + "-dtwp",
//						DataTwoWayConnector.class.getCanonicalName()) ;
			} else {
				this.doPortConnection(
						this.pingPongOutboundPort.getPortURI(),
						this.player1PingPongInboundPortURI,
						PingPongConnector.class.getCanonicalName()) ;
				// The next two are implementation alternatives: connect
				// from the data outbound port or from the data inbound port.
//				this.doPortConnection(
//						this.pingPongDataOutboundPort.getPortURI(),
//						this.player1PingPongDataInboundPortURI,
//						DataConnector.class.getCanonicalName()) ;
				this.doPortConnection(
						this.pingPongDataInboundPort.getPortURI(),
						this.player1PingPongDataOutboundPortURI,
						DataConnector.class.getCanonicalName()) ;
				// The next two are implementation alternatives of the
				// two way connections in the if-part: connect from
				// either side of the two way and data two way connections..
				this.doPortConnection(
						this.pingPongTwoWayPort.getPortURI(),
						otherPingPongTwoWayPortURI,
						PingPongTwoWayConnector.class.getCanonicalName()) ;
				this.doPortConnection(
						this.pingpongDataTwoWayPort.getPortURI(),
						otherPingPongTwoWayPortURI + "-dtwp",
						DataTwoWayConnector.class.getCanonicalName()) ;
			}
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
		//System.out.println("PingPongPlayer>>start 10") ;
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		super.execute() ;

		// The player that has the service starts the exchanges.
		this.counter = PingPongPlayer.NUMBER_OF_HITS_IN_EXCHANGES ;
		if (this.hasService) {
			this.traceMessage(this.uri + " serves the first exchange.\n") ;
			this.pingPongOutboundPort.play() ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		// The instance of player that has the service must do the
		// outbound port disconnection.
		this.doPortDisconnection(this.pingPongOutboundPort.getPortURI()) ;
		// The next two are implementation alternatives: disconnect
		// from the data outbound port or from the data inbound port.
//		this.doPortDisconnection(this.pingPongDataOutboundPort.getPortURI()) ;
		this.doPortDisconnection(this.pingPongDataInboundPort.getPortURI()) ;
		// The next two alternatives are implementation alternatives:
		// disconnect either side of the two way and data two ports..
		if (this.hasService) {
			this.doPortDisconnection(this.pingPongTwoWayPort.getPortURI()) ;
			this.doPortDisconnection(this.pingpongDataTwoWayPort.getPortURI()) ;
		} else {
//			this.doPortDisconnection(this.pingPongTwoWayPort.getPortURI()) ;
//			this.doPortDisconnection(this.pingpongDataTwoWayPort.getPortURI()) ;
		}

		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public void			shutdown() throws ComponentShutdownException
	{
		// Before shutting down (super call) unpublish the ports so that they
		// can be destroyed during the shut down process.
		try {
			this.doShutdownWork() ;
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		
		super.shutdown();
	}

	/**
	 * do the shutdown work for this component; allow to share it between
	 * <code>shutdown</code> and <code>shutdownNow</code>.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @throws Exception		<i>todo.</i>
	 */
	protected void		doShutdownWork() throws Exception
	{
		this.pingPongOutboundPort.unpublishPort() ;
		this.pingPongInboundPort.unpublishPort() ;
		this.pingPongDataOutboundPort.unpublishPort() ;
		this.pingPongDataInboundPort.unpublishPort() ;
		this.pingPongTwoWayPort.unpublishPort() ;
		this.pingpongDataTwoWayPort.unpublishPort() ;
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdownNow()
	 */
	@Override
	public void			shutdownNow() throws ComponentShutdownException
	{
		// Before shutting down (super call) unpublish the ports so that they
		// can be destroyed during the shut down process.
		try {
			this.doShutdownWork() ;
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		
		super.shutdownNow();
	}

	// ------------------------------------------------------------------------
	// Services
	// ------------------------------------------------------------------------


	/**
	 * play during the first exchange involving the offered and
	 * required interfaces as well as inbound and outbound ports.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @throws Exception		<i>todo.</i>
	 */
	public void			play() throws Exception
	{
		this.traceMessage(this.uri + " plays.\n") ;
		this.counter-- ;
		if (this.counter > 0) {
			this.scheduleTask(
					new AbstractComponent.AbstractTask() {
						@Override
						public void run() {
							try {
								Thread.sleep(500) ;
								((PingPongPlayer)this.getTaskOwner()).
											pingPongOutboundPort.play() ;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					},
					100L, TimeUnit.MILLISECONDS) ;
		} else {
			this.traceMessage(this.uri + " serves the second exchange.\n") ;
			this.counter = PingPongPlayer.NUMBER_OF_HITS_IN_EXCHANGES ;
			this.pingPongOutboundPort.goToService() ;
			this.counter-- ;
			this.pingPongOutboundPort.playOnDataPull() ;
		}
	}

	/**
	 * initialise the component to start a new exchange.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 */
	public void			goToService()
	{
		this.counter = PingPongPlayer.NUMBER_OF_HITS_IN_EXCHANGES ;
	}

	/**
	 * pull the ball during the second exchange involving the data offered
	 * and data required interfaces as well as data inbound and data
	 * outbound ports.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @throws Exception		<i>todo.</i>
	 */
	public void			playOnDataPull() throws Exception
	{
		assert	this.ball != null ;

		this.traceMessage(this.uri + " pulls the ball.\n") ;
		this.counter-- ;
		this.ball = (Ball) this.pingPongDataOutboundPort.request() ;
		this.ball.incrementNumberOfHits();
		if (this.counter >= 0) {
			this.scheduleTask(
					new AbstractComponent.AbstractTask() {
						@Override
						public void run() {
							try {
								Thread.sleep(500) ;
								((PingPongPlayer)this.getTaskOwner()).
									pingPongOutboundPort.playOnDataPull() ;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					},
					100L, TimeUnit.MILLISECONDS) ;
		} else {
			this.traceMessage(
				"The ball received " + this.ball.getNumberOfHits()
														+ " hits.\n") ;
			this.traceMessage(this.uri + " serves the third exchange.\n") ;
			this.counter = PingPongPlayer.NUMBER_OF_HITS_IN_EXCHANGES ;
			this.pingPongOutboundPort.goToService() ;
			Ball b = new Ball() ;
			b.incrementNumberOfHits() ;
			this.pingPongDataInboundPort.send(b) ;
		}
	}

	/**
	 * @return the ball
	 */
	public Ball			getBall()
	{
		return ball;
	}

	/**
	 * play the ball during the second exchange involving the data offered
	 * and data required interfaces as well as data inbound and data
	 * outbound ports.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	b != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param b	the ball.
	 * @throws Exception		<i>todo.</i>
	 */
	public void			playOnDataReception(Ball b) throws Exception
	{
		assert	b != null ;

		this.traceMessage(this.uri + " plays the ball.\n") ;
		this.counter-- ;
		b.incrementNumberOfHits() ;
		if (this.counter > 0) {
			this.scheduleTask(
					new AbstractComponent.AbstractTask() {
						@Override
						public void run() {
							try {
								Thread.sleep(500) ;
								((PingPongPlayer)this.getTaskOwner()).
									pingPongDataInboundPort.send(b) ;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					},
					100L, TimeUnit.MILLISECONDS) ;
		} else {
			this.traceMessage(
				"The ball received " + b.getNumberOfHits() + " hits.\n") ;
			this.traceMessage(this.uri + " serves the fourth exchange.\n") ;
			this.counter = PingPongPlayer.NUMBER_OF_HITS_IN_EXCHANGES ;
			this.pingPongOutboundPort.goToService() ;
			this.pingPongTwoWayPort.getOut().pingPong() ;
		}
	}

	/**
	 * play the player hit and, if the exchange is not terminated, schedule
	 * a call to the same method but on the other player through the two way
	 * port after waiting 100 milliseconds; when the first exchange is
	 * terminated, start the second by calling the method <code>send</code>
	 * to push the ball to the other player.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 * 
	 * @throws Exception		<i>todo.</i>
	 */
	public void			pingPong() throws Exception
	{
		this.logMessage(this.uri + " hits.") ;
		this.counter-- ;

		if (this.counter > 0) {
			// We are still in the first exchange.
			this.scheduleTask(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							Thread.sleep(500) ;
							((PingPongPlayer)this.getTaskOwner()).
								pingPongTwoWayPort.getOut().pingPong() ;
						} catch (Exception e) {
							e.printStackTrace();
						};
					}
				},
				100L, TimeUnit.MILLISECONDS) ;
		} else {
			this.counter = PingPongPlayer.NUMBER_OF_HITS_IN_EXCHANGES ;
			this.pingPongOutboundPort.goToService() ;
			this.traceMessage(this.uri + " serves the fifth exchange.\n");
			this.scheduleTask(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							Ball b = new Ball() ;
							b.incrementNumberOfHits() ;
							Thread.sleep(500) ;
							((PingPongPlayer)this.getTaskOwner()).
								pingpongDataTwoWayPort.getOut().send(b) ;
						} catch (Exception e) {
							e.printStackTrace();
						};
					}
				},
				100L, TimeUnit.MILLISECONDS) ;
		}
	}

	/**
	 * play the player hit and, if the exchange is not terminated, schedule
	 * a call to the method send to push the ball to the other player after
	 * waiting 100 milliseconds.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	b != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param b		the ball.
	 * @throws Exception		<i>todo.</i>
	 */
	public void			hit(Ball b) throws Exception
	{
		assert	b != null ;

		b.incrementNumberOfHits() ;
		this.counter-- ;

		this.traceMessage(this.uri + " hits the ball.\n") ;
		if (this.counter > 0) {
			this.scheduleTask(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							Thread.sleep(500) ;
							((PingPongPlayer)this.getTaskOwner()).
								pingpongDataTwoWayPort.getOut().send(b) ;
						} catch (Exception e) {
							e.printStackTrace();
						};
					}
				},
				100L, TimeUnit.MILLISECONDS) ;
		} else {
			this.traceMessage(
				"The ball received " + b.getNumberOfHits() + " hits.\n") ;
			this.traceMessage("End of the exchanges.\n");
		}
	}
}
//----------------------------------------------------------------------------
