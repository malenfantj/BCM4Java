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
import java.util.function.Supplier;

// -----------------------------------------------------------------------------
/**
 * The class <code>AssertionChecking</code> implements helper methods to check
 * assertions in objects and components.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2024-09-24</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	AssertionChecking
{
	/**
	 * check an implementation invariant expression and print a message if the
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
	public static boolean	checkImplementationInvariant(
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
				"Implementation invariant violation in class "
				+ definingClass.getSimpleName()
				+ " for the object "
				+ (instance != null ? instance.toString() : "unknown instance")
				+ ": " + message);
		}
		return invariantExpression;
	}
	/**
	 * check an implementation invariant expression for a component and print a
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
	public static boolean	checkImplementationInvariant(
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
					"Implementation invariant violation in class "
					+ definingClass.getSimpleName()
					+ " for the component "
					+ component
					+ ": " + message + "\n");
		}
		return invariantExpression;
	}

	/**
	 * check an invariant expression and print a message if the expression
	 * evaluates to false.
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
	public static boolean	checkInvariant(
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
				"Invariant violation in class "
				+ definingClass.getSimpleName()
				+ " for the object "
				+ (instance != null ? instance.toString() : "unknown instance")
				+ ": " + message);
		}
		return invariantExpression;
	}

	/**
	 * check an invariant expression and print a message if the expression
	 * evaluates to false.
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
	public static boolean	checkInvariant(
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
					"Invariant violation in class "
					+ definingClass.getSimpleName()
					+ " for the component "
					+ component
					+ ": " + message + "\n");
		}
		return invariantExpression;
	}

	/**
	 * assert that {@code value} is not null or throw the exception provided by
	 * {@code exceptionFactory}; meant to be used as an assert expression
	 * (rather than the standard Java assert instruction).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code exceptionFactory != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param <T>				type of the value to be tested and returned.
	 * @param value				value to be asserted non null.
	 * @param exceptionFactory	supplier of an exception to be thrown if {@code value} is null.
	 * @return					{@code value} if it is not null.
	 * @throws VerboseException	a {@code PreconditionException} if {@code exceptionFactory} is {@code null} or an exception provided by {@code exceptionFactory} if {@code value} is null.
	 */
	public static <T> T		assertNonNullOrThrow(
		T value,
		Supplier<? extends VerboseException> exceptionFactory
		) throws	VerboseException
	{
		assert	exceptionFactory != null :
				new PreconditionException("exceptionFactory != null");

		if (value != null) {
			return value;
		} else {
			throw exceptionFactory.get();
		}
	}

	/**
	 * assert that {@code expression} is true or throw the exception provided by
	 * {@code exceptionFactory}; meant to be used as an assert expression
	 * (rather than the standard Java assert instruction).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code exceptionFactory != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param expression		expression to be asserted true.
	 * @param exceptionFactory	supplier of an exception to be thrown if {@code expression} is false.
	 * @return					true if {@code expression} is.
	 * @throws VerboseException	a {@code PreconditionException} if {@code exceptionFactory} is {@code null} or an exception provided by {@code exceptionFactory} if {@code expression} is false.
	 */
	public static boolean	assertTrueOrThrow(
		boolean expression,
		Supplier<? extends VerboseException> exceptionFactory
		) throws	VerboseException
	{
		assert	exceptionFactory != null :
				new PreconditionException("exceptionFactory != null");

		if (expression) {
			return true;
		} else {
			throw exceptionFactory.get();
		}
	}

	/**
	 * assert that {@code expression} is true and then return {@code value}
	 * otherwise throw the exception provided by{@code exceptionFactory}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code exceptionFactory != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param <T>				type of the value to be tested and returned.
	 * @param expression		boolean expression; if true, return {@code value}, otherwise throw the exception supplied by {@code exceptionFactory}.
	 * @param value				value to be returned if {@code expression} is true.
	 * @param exceptionFactory	supplier of an exception to be thrown if {@code expression} is false.
	 * @return					{@code value} if {@code expression} is true.
	 * @throws VerboseException	a {@code PreconditionException} if {@code exceptionFactory} is {@code null} or an exception provided by {@code exceptionFactory} if {@code value} is null.
	 */
	public static <T> T		assertTrueAndReturnOrThrow(
		boolean expression,
		T value,
		Supplier<? extends VerboseException> exceptionFactory
		) throws	VerboseException
	{
		assert	exceptionFactory != null :
				new PreconditionException("exceptionFactory != null");

		if (expression) {
			return value;
		} else {
			throw exceptionFactory.get();
		}
	}
}
// -----------------------------------------------------------------------------
