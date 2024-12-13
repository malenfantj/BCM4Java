package fr.sorbonne_u.components.endpoints;

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

/**
 * The interface <code>EndPointI</code> defines an end point connecting two
 * software entities through an interface {@code I} in an
 * "implementation-agnostic" manner.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * An end point embed a reference connecting two entities to be used by a
 * client entity to call a server entity, a caller. The protocol is to create
 * the end point (with appropriate implementation-dependent information that is
 * shared among the caller and the callee). The protocol is the following,
 * actions being performed in the presented order:
 * <ol>
 * <li>Create the end point, providing the implementation-dependent information
 *   required by its actual implementation.</li>
 * <li>Pass a copy of the end point both to the server and client sides,
 *   using the method {@code copyWithSharable} to create the appropriate
 *   copies with the sharable information only.</li>
 * <li>On the server side: call {@code initialiseServerSide} passing it a
 *   reference to the server side software entity; this must perform whatever
 *   implementation-dependent actions required to establish the connection
 *   on the server side.</li>
 * <li>On the client side: after the initialisation on the server side, call
 *   {@code initialiseClientSide} passing it a reference to the client side
 *   software entity; this will perform whatever implementation-dependent
 *   actions required to establish the connection from the client side.</li>
 * <li>On the client side: call {@code getReference()} to get the reference
 *   implementing the interface {@code I} through which the cleint can call
 *   server side methods as long as needed.</li>
 * <li>On the client side: when the end point is no longer needed, call
 *   {@code cleanUpClientSide}; this will perform whatever
 *   implementation-dependent actions required to cut the connection
 *   on the client side.</li>
 * <li>On the server side: when the end point is no longer needed, call
 *   {@code cleanUpServerSide}; this will perform whatever
 *   implementation-dependent actions required to cut the connection
 *   on the server side.</li>
 * </ol>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2024-06-24</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		EndPointI<I>
extends		AbstractEndPointI
{
	/**
	 * return the interface implemented by this end point.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return		the interface proposed by this end point.
	 */
	public Class<I>		getImplementedInterface();

	/**
	 * return the reference embedded in this end point.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code clientSideInitialised()}
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the reference embedded in this end point.
	 */
	public I			getReference();

	/**
	 * copy this end point except its transient information <i>i.e.</i>,
	 * keeping only the information that is shared among copies of the end point.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * post	{@code return.getImplementedInterface().equals(getImplementedInterface())}
	 * </pre>
	 *
	 * @return	a duplicate of this end point except its transient information.
	 */
	public EndPointI<I>	copyWithSharable();
}
