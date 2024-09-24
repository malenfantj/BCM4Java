package fr.sorbonne_u.exceptions;

import fr.sorbonne_u.components.AbstractComponent;

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

// -----------------------------------------------------------------------------
/**
 * The class <code>InvariantChecking</code>
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2024-09-24</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	InvariantChecking
{
	/**
	 * check a white-box invariant expression and print a message if the
	 * expression evaluates to false.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param invariantExpression	result of the invariant expression.
	 * @param definingClass			class defining the invariant expression.
	 * @param message				message to be printed on stdout if {@code invariantExpression} is false.
	 * @return						the value of {@code invariantExpression}.
	 */
	public static boolean	checkWhiteBoxInvariant(
		boolean invariantExpression,
		Class<?> definingClass,
		String message
		)
	{
		if (!invariantExpression) {
			System.out.println(
					"White-box invariant violation in class "
					+ definingClass.getSimpleName()
					+ ": " + message);
		}
		return invariantExpression;
	}
	/**
	 * check a white-box invariant expression for a component and print a
	 * message if the expression evaluates to false.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param invariantExpression	result of the invariant expression.
	 * @param definingClass			component class defining the invariant expression.
	 * @param instance				component on which the invariant is checked.
	 * @param message				message to be printed on the trace window if {@code invariantExpression} is false.
	 * @return						the value of {@code invariantExpression}.
	 */
	public static boolean	checkWhiteBoxInvariant(
		boolean invariantExpression,
		Class<? extends AbstractComponent> definingClass,
		AbstractComponent instance,
		String message
		)
	{
		if (!invariantExpression) {
			instance.traceMessage(
					"White-box invariant violation in class "
					+ definingClass.getSimpleName()
					+ ": " + message + "\n");
		}
		return invariantExpression;
	}

	/**
	 * check a black-box invariant expression and print a message if the
	 * expression evaluates to false.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param invariantExpression	result of the invariant expression.
	 * @param definingClass			class defining the invariant expression.
	 * @param message				message to be printed on stdout if {@code invariantExpression} is false.
	 * @return						the value of {@code invariantExpression}.
	 */
	public static boolean	checkBlackBoxInvariant(
		boolean invariantExpression,
		Class<?> definingClass,
		String message
		)
	{
		if (!invariantExpression) {
			System.out.println(
					"Black-box invariant violation in class "
					+ definingClass.getSimpleName()
					+ ": " + message);
		}
		return invariantExpression;
	}

	/**
	 * check a black-box invariant expression and print a message if the
	 * expression evaluates to false.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param invariantExpression	result of the invariant expression.
	 * @param definingClass			class defining the invariant expression.
	 * @param instance				component on which the invariant is checked.
	 * @param message				message to be printed on stdout if {@code invariantExpression} is false.
	 * @return						the value of {@code invariantExpression}.
	 */
	public static boolean	checkBlackBoxInvariant(
		boolean invariantExpression,
		Class<? extends AbstractComponent> definingClass,
		AbstractComponent instance,
		String message
		)
	{
		if (!invariantExpression) {
			instance.traceMessage(
					"Black-box invariant violation in class "
					+ definingClass.getSimpleName()
					+ ": " + message + "\n");
		}
		return invariantExpression;
	}
}
// -----------------------------------------------------------------------------
