package fr.sorbonne_u.components.tasks_management.interfaces;

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

import java.io.Serializable;
import fr.sorbonne_u.components.interfaces.OfferedCI;

// -----------------------------------------------------------------------------
/**
 * The interface <code>AbnormalTerminationNotificationImplI</code> declares
 * the signature of methods to be implemented by a client component that wants
 * to receive notification of abnormal termination of asynchronous service calls.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2026-03-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		AbnormalTerminationNotificationImplI
{
	// -------------------------------------------------------------------------
	// Signature and default methods
	// -------------------------------------------------------------------------

	/**
	 * notify a caller component the abnormal termination of a service call on
	 * the callee.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code offeredInterface != null}
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * pre	{@code serviceName != null && !serviceName.isEmpty()}
	 * pre	{@code actualParameters != null}
	 * pre	{@code t != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param offeredInterface		offered interface under which the service was called.
	 * @param serviceInboundPortURI	URI of the inbound port offering {@code offeredInterface}.
	 * @param serviceName			name of the called service (signature).
	 * @param actualParameters		actual parameters passed to the service.
	 * @param t						throwable thrown during the execution of the task.
	 * @throws Exception			<i>to do</i>.
	 */
	public void			notifyAbnormalTermination(
		Class<? extends OfferedCI> offeredInterface,
		String serviceInboundPortURI,
		String serviceName,
		Serializable[] actualParameters,
		Throwable t
		) throws Exception;
}
// -----------------------------------------------------------------------------
