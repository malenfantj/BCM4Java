package fr.sorbonne_u.components.registry;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cvm.config.ConfigurationFileParser;
import fr.sorbonne_u.components.cvm.config.ConfigurationParameters;
import fr.sorbonne_u.components.cvm.config.exceptions.InvalidConfigurationFileFormatException;
import fr.sorbonne_u.components.helpers.CVMDebugModes;
import fr.sorbonne_u.components.helpers.Logger;
import fr.sorbonne_u.components.helpers.TracerWindow;
import fr.sorbonne_u.components.registry.protocol.Request;
import fr.sorbonne_u.components.registry.protocol.RequestI;
import fr.sorbonne_u.components.registry.protocol.ShutdownRequest;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>GlobalRegistry</code> implements the global registry for the
 * component model that registers connection information to remotely access
 * components through their ports.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The Registry implements a global registry for the component model allowing
 * to bind port URI to information required for the connection between
 * components through ports.  The registry must be run on one host which
 * name is given in the static variable <code>REGISTRY_HOSTNAME</code>.  It
 * listens to request on a port which number is given by the static variable
 * <code>REGISTRY_PORT</code>
 * </p>
 * <p>
 * Protocol (spaces are used to split the strings, so they are meaningful):
 * </p>
 * <pre>
 * Requests              Responses
 * 
 * lookup key            lookup ok value
 *                       lookup nok key
 * put key value         put ok
 *                       put nok key
 * remove key            remove ok
 *                       remove nok key
 * shutdown              shutdown ok
 * anything else         error request
 * </pre>
 * <p>
 * This protocol is implemented with two command design patterns, one for the
 * requests, one for the responses.
 * </p>
 * <p>
 * When the static variable <code>DEBUG</code> is set to true, the registry
 * provides with a log on STDOUT of the commands it executes.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true	// TODO
 * </pre>
 * 
 * <p>Created on : 2012-10-22</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class				GlobalRegistry
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// --+----------------------------------------------------------------------

	/** true if the global registry is logging its actions.					*/
	public static boolean				GLOBAL_REGISTRY_IS_LOGGING = true ;
	/** Default name of the host running the registry; is configurable.		*/
	public static String				REGISTRY_HOSTNAME = "localhost" ;
	/** Default port number listen for commands; is configurable.			*/
	public static int					REGISTRY_PORT = 55252 ;

	/** Directory of registred information.									*/
	protected ConcurrentHashMap<String,String>	directory ;
	/** Configuration parameters from the configuration file.				*/
	protected ConfigurationParameters	configurationParameters ;
	/** Number of JVM in the distributed component virtual machine.			*/
	protected final int					numberOfJVMsInDCVM ;

	/** The socket used to listen on the port number REGISTRY_PORT.			*/
	protected ServerSocket				ss ;

	protected static final int			WARNING_NUMBER_OF_THREADS = 2000 ; 
	/** The executor service in charge of handling component requests.		*/
	protected static ExecutorService	REQUEST_HANDLER ;
	/**	synchroniser to finish the execution of this global registry.		*/
	protected CountDownLatch			finished ;

	/** Execution log of the global registry.								*/
	protected final Logger				executionLog ;
	/** Tracing console for the global registry.							*/
	protected final TracerWindow		tracer ;

	// -------------------------------------------------------------------------
	// Task for the executor framework
	// -------------------------------------------------------------------------

	/**
	 * The class <code>ServiceTask</code> implements the behaviour of the
	 * registry exchanging with one client; its processes the requests from the
	 * clients until the latter explicitly disconnects with a "shutdown" request
	 * of implicitly with a null string request.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant		true
	 * </pre>
	 * 
	 * <p>Created on : 2014-01-30</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	protected static class	ServiceTask
	implements	Runnable
	{
		/** socket on which this task will accept the request coming from
		 *  one JVM.														*/
		protected Socket							s ;
		/** map holding the bindings of the global registry.				*/
		protected ConcurrentHashMap<String,String>	directory ;
		/** buffered reader to read from the socket s.						*/
		protected BufferedReader					br ;
		/** print stream to write on the socket s.							*/
		protected PrintStream						ps ;
		/** synchroniser detecting the end of the application.				*/
		protected CountDownLatch					finished ;
		/** global registry logger.											*/
		protected final Logger						executionLog ;
		/** global registry tracer.											*/
		protected final TracerWindow				tracer ;

		/**
		 * creating the service task.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true			// no precondition.
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @param s				socket on which this task will accept the request coming from one JVM.
		 * @param directory		map holding the bindings of the global registry.
		 * @param finished		synchroniser detecting the end of the application.
		 * @param executionLog	global registry logger.
		 * @param tracer		global registry tracer.
		 * @throws Exception	<i>to do.</i>
		 */
		public			ServiceTask(
			Socket 								s,
			ConcurrentHashMap<String,String>	directory,
			CountDownLatch						finished,
			Logger								executionLog,
			TracerWindow						tracer
			) throws Exception
		{
			if (GLOBAL_REGISTRY_IS_LOGGING) {
				if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.REGISTRY)) {
					executionLog.logMessage(
							"Global registry creating a service task");
					tracer.traceMessage(
							"Global registry creating a service task\n");
				}
			}

			this.s = s;
			this.directory = directory;
			this.finished = finished;
			this.br = new BufferedReader(
							new InputStreamReader(this.s.getInputStream()));
			this.ps = new PrintStream(s.getOutputStream(), true);
			this.executionLog = executionLog;
			this.tracer = tracer;

			if (GLOBAL_REGISTRY_IS_LOGGING) {
				if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.REGISTRY)) {
					this.executionLog.logMessage("...service runnable created");
					this.tracer.traceMessage("...service runnable created");
				}
			}
		}

		@Override
		public void		run()
		{
			if (GLOBAL_REGISTRY_IS_LOGGING &&
					AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.REGISTRY)) {
				this.executionLog.logMessage("GlobalRegistry task running...");
				this.tracer.traceMessage("GlobalRegistry task running...");
			}
			String request = null ;
			try {
				request = br.readLine() ;
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}

			if (GLOBAL_REGISTRY_IS_LOGGING) {
				this.executionLog.logMessage(
								"GlobalRegistry processing " + request);
				this.tracer.traceMessage(
								System.currentTimeMillis() + "|" +
								"GlobalRegistry processing " + request + "\n");
			}

			RequestI req = null;
			if (request == null) {
				req = new ShutdownRequest();
			} else {
				req = Request.string2request(request);
			}
			while(!req.isShutdownRequest()) {
				// process current request
				req.execute(this.ps, this.directory, this.executionLog);
				// read next request
				try {
					request = br.readLine() ;
					if (request == null) {
						req = new ShutdownRequest();
					} else {
						req = Request.string2request(request);
					}
				} catch (IOException e1) {
					e1.printStackTrace();
					throw new RuntimeException(e1) ;
				}
				if (GLOBAL_REGISTRY_IS_LOGGING) {
					this.executionLog.logMessage(
							"GlobalRegistry processing " + request) ;
					this.tracer.traceMessage(
							System.currentTimeMillis() + "|" +
							"GlobalRegistry processing " + request + "\n") ;
				}
			}

			if (GLOBAL_REGISTRY_IS_LOGGING &&
					AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.REGISTRY)) {
				this.executionLog.logMessage("GlobalRegistry task exits.") ;
				this.tracer.traceMessage("GlobalRegistry task exits.\n") ;
			}

			try {
				req.execute(this.ps, this.directory, this.executionLog);
				this.ps.close() ;
				this.br.close() ;
				s.close() ;
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			} finally {
				this.finished.countDown() ;
			}
		}
	}

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	/**
	 * create a registry object, using the configuration file to know the number
	 * of clients that will connect, and therefore that will have to disconnect
	 * for the registry to terminate its execution.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	configFileName != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param configFileName	name of the configuration file.
	 * @throws Exception		<i>to do.</i>
	 */
	public				GlobalRegistry(String configFileName) throws Exception
	{
		super() ;

		assert	configFileName != null :
					new PreconditionException("configFileName == null") ;

		if (GLOBAL_REGISTRY_IS_LOGGING) {
			this.executionLog = new Logger("globalRegistry");
			this.tracer = new TracerWindow("GlobalRegistry", 0, 0);
			this.executionLog.toggleLogging();
			this.tracer.toggleTracing();
		} else {
			this.executionLog = null;
			this.tracer = null;
		}

		File configFile = new File(configFileName);
		ConfigurationFileParser cfp = new ConfigurationFileParser();
		if (!cfp.validateConfigurationFile(configFile)) {
			throw new InvalidConfigurationFileFormatException(
							"invalid configuration file " + configFileName);
		}
		this.configurationParameters = cfp.parseConfigurationFile(configFile);
		this.numberOfJVMsInDCVM =
							this.configurationParameters.getJvmURIs().length;

		this.directory =
			new ConcurrentHashMap<String,String>(10*this.numberOfJVMsInDCVM);
		if (this.numberOfJVMsInDCVM > GlobalRegistry.WARNING_NUMBER_OF_THREADS) {
			String mes =
				"WARNING: very high number of threads in global registry (i.e., "
				+ this.numberOfJVMsInDCVM + "); may fail!";
			if (GLOBAL_REGISTRY_IS_LOGGING) {
				this.executionLog.logMessage(mes);
				this.tracer.traceMessage(
								System.currentTimeMillis() + "|" + mes + "\n");
			} else {
				System.out.println(mes);
			}
		}
		REQUEST_HANDLER = Executors.newFixedThreadPool(this.numberOfJVMsInDCVM);
		this.finished = new CountDownLatch(this.numberOfJVMsInDCVM);
		this.ss = new ServerSocket(REGISTRY_PORT);
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	/**
	 * runs the registry, repeated accepting connections on its server socket,
	 * decoding the request (in the format defined by the above protocol),
	 * executing it and returning the result (in the format defined by the
	 * above protocol) on the output stream of the socket.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 */
	public void			run()
	{
		if (GLOBAL_REGISTRY_IS_LOGGING) {
			this.executionLog.logMessage("Global registry up and running!");
			this.tracer.traceMessage(System.currentTimeMillis() + "|" +
										"Global registry up and running!\n");
		}
		int count = 0 ;
		while (count < this.numberOfJVMsInDCVM) {
			try {
				// block on this.ss.accept() and then create a new service
				// thread, one for each of the JVM participating in the
				// execution.
				REQUEST_HANDLER.submit(new ServiceTask(this.ss.accept(),
														   this.directory,
														   this.finished,
														   this.executionLog,
														   this.tracer)) ;
				count++ ;
				if (GLOBAL_REGISTRY_IS_LOGGING) {
					this.executionLog.logMessage(
							"Global registry accepted the " + count
														+ "th connection.") ;
				}
			} catch (Exception e) {
				try {
					ss.close() ;
				} catch (IOException e1) {
					;
				}
				e.printStackTrace();
			}
		}
		if (GLOBAL_REGISTRY_IS_LOGGING) {
			this.executionLog.logMessage("All (" + count + ") connected!");
			this.tracer.traceMessage(System.currentTimeMillis() + "|" +
										"All (" + count + ") connected!\n");
		}

		try {
			this.ss.close() ;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * terminating the global registry.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @throws FileNotFoundException	when the file to print the log does not exist.
	 */
	public void			closing() throws FileNotFoundException
	{
		if (GLOBAL_REGISTRY_IS_LOGGING) {
			this.executionLog.logMessage("Global registry shuts down!") ;
			this.tracer.traceMessage(System.currentTimeMillis() + "|" +
									 "Global registry shuts down!\n") ;
		}
		this.executionLog.printExecutionLog() ;
		REQUEST_HANDLER.shutdownNow() ;
	}

	// ------------------------------------------------------------------------
	// Main method
	// ------------------------------------------------------------------------

	/**
	 * initialise and run the registry.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param args	command-line arguments.
	 */
	public static void	main(String[] args)
	{
		try {
			GlobalRegistry reg = new GlobalRegistry(args[0]);
			reg.run() ;
			reg.finished.await() ;
			reg.closing() ;
			System.exit(0) ;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
// -----------------------------------------------------------------------------
