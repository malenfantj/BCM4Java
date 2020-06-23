package fr.sorbonne_u.components.cvm.utils;

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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

// -----------------------------------------------------------------------------
/**
 * The class <code>DCVMCyclicBarrierClient</code> implements the client
 * side of the distributed cyclic wait barrier mechanism.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2014-01-21</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			DCVMCyclicBarrierClient
{
	/** socket used to exchange signals with the wait barrier.				*/
	protected Socket			cyclicBarrierSignalingSocket;
	/**	a buffered reader to read from the socket.							*/
	protected BufferedReader	cyclicBarrierBR;
	/** a print stream to write onto the socket.							*/
	protected PrintStream	cyclicBarrierPS;
	/**	name of the host that executes the process to be synchronised.		*/
	protected String			hostname;
	/**	URI of the JVM that is executing the process to be synchronised.	*/
	protected String			jvmURI;

	/**
	 * create the client side object implementing the wait behaviour.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param cyclicBarrierHostname	name of the host running the cyclic barrier.
	 * @param cyclicBarrierPort		port number listen by the cyclic barrier.
	 * @param hostname				name of the host that executes the process to be synchronised.
	 * @param jvmURI				URI of the JVM that is executing the process to be synchronised.
	 * @throws IOException			<i>todo.</i>
	 */
	public				DCVMCyclicBarrierClient(
		String cyclicBarrierHostname,
		int cyclicBarrierPort,
		String hostname,
		String jvmURI
		) throws IOException
	{
		super();
		this.hostname = hostname;
		this.jvmURI = jvmURI;
		this.cyclicBarrierSignalingSocket =
						new Socket(cyclicBarrierHostname, cyclicBarrierPort);
		this.cyclicBarrierPS =
			new PrintStream(
				this.cyclicBarrierSignalingSocket.getOutputStream(), true);
		this.cyclicBarrierBR =
			new BufferedReader(
				new InputStreamReader(
					this.cyclicBarrierSignalingSocket.getInputStream()));
	}

	/**
	 * signal the current virtual machine to the central distributed cyclic
	 * barrier and then wait for the signal from the barrier before resuming
	 * execution.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.cyclicBarrierSignalingSocket.isConnected()
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @throws IOException		<i>todo.</i>
	 */
	public void			waitBarrier()
	throws IOException
	{
		assert	this.cyclicBarrierSignalingSocket.isConnected() :
					new Exception("Cyclic barrier client socket disconnected!");

		// send the necessary information to allow the cyclic barrier to call
		// back to release the client process.
		this.cyclicBarrierPS.println(
			(new CyclicBarrierProtocol.
					WaitRequest(
							jvmURI,
							hostname,
							this.cyclicBarrierSignalingSocket.getLocalPort()).
				request2string()));
		// this call waits until something is written by the cyclic barrier.
		this.cyclicBarrierBR.readLine();
	}

	/**
	 * closing the connection with the central distributed cyclic barrier.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.cyclicBarrierSignalingSocket.isConnected()
	 * post	this.cyclicBarrierSignalingSocket.isClosed()
	 * </pre>
	 *
	 * @throws IOException		<i>todo.</i>
	 */
	public void			closeBarrier() throws IOException
	{
		assert	this.cyclicBarrierSignalingSocket.isConnected() :
					new Exception("Cyclic barrier client socket disconnected!");
		
		this.cyclicBarrierSignalingSocket.close();

		assert	this.cyclicBarrierSignalingSocket.isClosed() :
					new Exception("Cyclic barrier client socket not closed!");
	}
}
// -----------------------------------------------------------------------------
