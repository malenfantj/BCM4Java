package fr.sorbonne_u.components.plugins.dipc.interfaces;

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

//-----------------------------------------------------------------------------
/**
 * The interface <code>PushControlImplementationI</code> declares basic
 * component services to control the regular exchanges of data via a
 * <code>DataOfferedCI</code> interface in push mode.
 *
 * <p><strong>Description</strong></p>
 * 
 * The interface defines the services to start and stop the pushing
 * of data. The two start methods allow either to perform a fixed or
 * an unbounded number of pushes with a specified interval of time
 * between each. The stop service stops the pushes that were previously
 * started. If an unbounded number of pushes have been started, the stop
 * service is the only way to stop them, but stop can also be used to
 * stop a fixed number of pushes before the end.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2018-01-26</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface			PushControlImplementationI
{
	/**
	 * return true if the owner component has a data offered inbound port of
	 * the given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isInitialised()
	 * pre	portURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param portURI		URI of a port to be tested.
	 * @return				true if the owner component has a data offered inbound port of the given URI.
	 * @throws Exception	<i>to do.</i>
	 */
	public boolean			isPortExisting(String portURI) throws Exception ;

	/**
	 * start, after <code>interval</code> period of time, the pushing of data
	 * and force the pushes to be done indefinitely each <code>interval</code>
	 * period of time. 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isInitialised()
	 * pre	portURI != null
	 * pre	this.isPortExisting(portURI)
	 * pre	interval &gt; 0
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param portURI		URI of the port through which the data is pushed.
	 * @param interval		delay between pushes (in milliseconds).
	 * @throws Exception		<i>to do.</i>
	 */
	public void				startUnlimitedPushing(
		String portURI,
		final long interval
		) throws Exception ;

	/**
	 * start, after <code>interval</code> period of time, <code>n</code>
	 * pushes of data and force the pushes to be done each
	 * <code>interval</code> period of time. 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isInitialised()
	 * pre	portURI != null
	 * pre	this.isPortExisting(portURI)
	 * pre	interval &gt; 0
	 * pre	n &gt; 0
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param portURI		URI of the port through which the data is pushed.
	 * @param interval		delay between pushes (in milliseconds).
	 * @param n				total number of pushes to be done, unless stopped.
	 * @throws Exception	<i>to do.</i>
	 */
	public void				startLimitedPushing(
		String portURI,
		final long interval,
		final int n
		) throws Exception ;

	/**
	 * return true if a series of pushes are done on the port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isInitialised()
	 * pre	portURI != null
	 * pre	this.isPortExisting(portURI)
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param portURI		URI of the port through which the data would be pushed.
	 * @return				true if a series of pushes are done on the port.
	 * @throws Exception	<i>to do.</i>
	 */
	public boolean			currentlyPushesData(String portURI)
	throws Exception ;

	/**
	 * stop the pushing of data on the given port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isInitialised()
	 * pre	portURI != null
	 * pre	this.isPortExisting(portURI)
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param portURI		URI of the port through which the data is pushed.
	 * @throws Exception	<i>to do.</i>
	 */
	public void				stopPushing(String portURI) throws Exception ;
}
//-----------------------------------------------------------------------------
