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
 * The interface <code>CompositeEndPointI</code> defines the signatures of
 * methods that a composite of several end points connecting a client to a
 * server implementation must provide; a composite end point provides a way
 * to group together many end points defined for distinct implemented interfaces
 * connecting a single client software entity to a single server software entity.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The composite end point is meant to ease the passage of information among a
 * caller and a callee when the caller use many distinct interfaces to call the
 * callee. Compared to a single end point, it reuses all of the signatures
 * defined in {@code AbstractEndPointI} to perform the corresponding actions
 * on all of its embedded end points at once, and it provides a method
 * {@code getEndPoint} to retrieve each single end point from its implemented
 * interface.
 * </p>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2024-07-11</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		CompositeEndPointI
extends		AbstractEndPointI
{
	// -------------------------------------------------------------------------
	// From AbstractEndPointI, adding more precise information.
	// -------------------------------------------------------------------------

	/**
	 * initialise the server side of this multiple end points, performing
	 * whatever action needed to make this reference readily usable.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code complete()}
	 * post	{@code true}	// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.endpoints.AbstractEndPointI#initialiseServerSide(java.lang.Object)
	 */
	@Override
	public void			initialiseServerSide(Object serverSideEndPointOwner);

	/**
	 * initialise the client side references embedded in this multiple end
	 * points, performing whatever action needed to make this reference readily
	 * usable.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code complete()}
	 * post	{@code true}	// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.endpoints.AbstractEndPointI#initialiseClientSide(java.lang.Object)
	 */
	@Override
	public void			initialiseClientSide(Object clientSideEndPointOwner);

	// -------------------------------------------------------------------------
	// Local signatures
	// -------------------------------------------------------------------------

	/**
	 * return true if all expected end points are defined in this multiple end
	 * points and false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if all expected end points are defined in this multiple end points and false otherwise.
	 */
	public boolean		complete();

	/**
	 * return the end point proposing {@code inter} or the closest subclass of
	 * it.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code inter != null}
	 * pre	{@code clientSideInitialised()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param <I>	type of requested interface.
	 * @param <J>	type of interface proposed by the end point.
	 * @param inter	interface required by the end point.
	 * @return		the end point proposing {@code inter} or the closest subclass of it.
	 */
	public <I, J extends I> EndPointI<J>	getEndPoint(Class<I> inter);

	/**
	 * duplicate this composite end points except its transient information
	 * <i>i.e.</i>, keeping only the information that is shared among copies of
	 * the multi end point.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	a duplicate of this end point except its transient information.
	 */
	public CompositeEndPointI	copyWithSharable();
}
