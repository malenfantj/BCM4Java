package fr.sorbonne_u.exceptions;

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

import fr.sorbonne_u.components.AbstractComponent;

// -----------------------------------------------------------------------------
/**
 * The class <code>InvariantChecking</code> implements helper methods to check
 * invariants in objects and components.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Glass-box Invariant</strong></p>
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
	 * check a glass-box invariant expression and print a message if the
	 * expression evaluates to false.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code definingClass != null}
	 * pre	{@code instance == null || definingClass.isAssignableFrom(instance.getClass())}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param invariantExpression	result of the invariant expression.
	 * @param definingClass			class defining the invariant expression.
	 * @param instance				instance on which the invariant is checked.
	 * @param message				message to be printed on stdout if {@code invariantExpression} is false.
	 * @return						the value of {@code invariantExpression}.
	 */
	public static boolean	checkGlassBoxInvariant(
		boolean invariantExpression,
		Class<?> definingClass,
		Object instance,
		String message
		)
	{
		assert	definingClass != null :
				new PreconditionException("definingClass != null");
		assert	instance == null ||
						definingClass.isAssignableFrom(instance.getClass()) :
				new PreconditionException(
						"instance == null || "
						+ "definingClass.isAssignableFrom(instance.getClass())");

		if (!invariantExpression) {
			System.out.println(
				"Glass-box invariant violation in class "
				+ definingClass.getSimpleName()
				+ " for the object "
				+ (instance != null ? instance.toString() : "unknown instance")
				+ ": " + message);
		}
		return invariantExpression;
	}
	/**
	 * check a glass-box invariant expression for a component and print a
	 * message if the expression evaluates to false.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code definingClass != null}
	 * pre	{@code component == null || definingClass.isAssignableFrom(component.getClass())}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param invariantExpression	result of the invariant expression.
	 * @param definingClass			component class defining the invariant expression.
	 * @param component				component on which the invariant is checked.
	 * @param message				message to be printed on the trace window of {@code instance} if {@code invariantExpression} is false.
	 * @return						the value of {@code invariantExpression}.
	 */
	public static boolean	checkGlassBoxInvariant(
		boolean invariantExpression,
		Class<? extends AbstractComponent> definingClass,
		AbstractComponent component,
		String message
		)
	{
		assert	definingClass != null :
				new PreconditionException("definingClass != null");
		assert	component == null ||
						definingClass.isAssignableFrom(component.getClass()) :
				new PreconditionException(
						"component == null || "
						+ "definingClass.isAssignableFrom(component.getClass())");

		if (!invariantExpression) {
			component.traceMessage(
					"Glass-box invariant violation in class "
					+ definingClass.getSimpleName()
					+ " for the component "
					+ component
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
	 * pre	{@code definingClass != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param invariantExpression	result of the invariant expression.
	 * @param definingClass			class defining the invariant expression.
	 * @param instance				instance on which the invariant is checked.
	 * @param message				message to be printed on stdout if {@code invariantExpression} is false.
	 * @return						the value of {@code invariantExpression}.
	 */
	public static boolean	checkBlackBoxInvariant(
		boolean invariantExpression,
		Class<?> definingClass,
		Object instance,
		String message
		)
	{
		assert	definingClass != null :
				new PreconditionException("definingClass != null");
		assert	instance == null ||
						definingClass.isAssignableFrom(instance.getClass()) :
				new PreconditionException(
						"instance == null || "
						+ "definingClass.isAssignableFrom(instance.getClass())");

		if (!invariantExpression) {
			System.out.println(
				"Black-box invariant violation in class "
				+ definingClass.getSimpleName()
				+ " for the object "
				+ (instance != null ? instance.toString() : "unknown instance")
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
	 * pre	{@code definingClass != null}
	 * pre	{@code component == null || definingClass.isAssignableFrom(component.getClass())}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param invariantExpression	result of the invariant expression.
	 * @param definingClass			class defining the invariant expression.
	 * @param component				component on which the invariant is checked.
	 * @param message				message to be printed on the trace window of {@code instance} if {@code invariantExpression} is false.
	 * @return						the value of {@code invariantExpression}.
	 */
	public static boolean	checkBlackBoxInvariant(
		boolean invariantExpression,
		Class<? extends AbstractComponent> definingClass,
		AbstractComponent component,
		String message
		)
	{
		assert	definingClass != null :
				new PreconditionException("definingClass != null");
		assert	component == null ||
						definingClass.isAssignableFrom(component.getClass()) :
				new PreconditionException(
						"component != null || "
						+ "definingClass.isAssignableFrom(component.getClass())");

		if (!invariantExpression) {
			component.traceMessage(
					"Black-box invariant violation in class "
					+ definingClass.getSimpleName()
					+ " for the component "
					+ component
					+ ": " + message + "\n");
		}
		return invariantExpression;
	}
}
// -----------------------------------------------------------------------------
