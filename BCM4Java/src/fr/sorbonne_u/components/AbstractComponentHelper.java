package fr.sorbonne_u.components;

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

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import fr.sorbonne_u.components.pre.dcc.DynamicComponentCreator;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>AbstractComponentHelper</code> defines a set of static
 * methods used in the class <code>AbstractComponent</code> but also in
 * some others.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2019-06-06</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			AbstractComponentHelper
{
	/** map of types that must be considered equivalent when looking for
	 *  a constructor to create a component.								*/
	private static final Map<Class<?>, Class<?>> equivalentTypeMap =
															new HashMap<>(18) ;

	static{
		equivalentTypeMap.put(boolean.class, Boolean.class);
		equivalentTypeMap.put(byte.class, Byte.class);
		equivalentTypeMap.put(char.class, Character.class);
		equivalentTypeMap.put(float.class, Float.class);
		equivalentTypeMap.put(int.class, Integer.class);
		equivalentTypeMap.put(long.class, Long.class);
		equivalentTypeMap.put(short.class, Short.class);
		equivalentTypeMap.put(double.class, Double.class);
		equivalentTypeMap.put(void.class, Void.class);
		equivalentTypeMap.put(Boolean.class, boolean.class);
		equivalentTypeMap.put(Byte.class, byte.class);
		equivalentTypeMap.put(Character.class, char.class);
		equivalentTypeMap.put(Float.class, float.class);
		equivalentTypeMap.put(Integer.class, int.class);
		equivalentTypeMap.put(Long.class, long.class);
		equivalentTypeMap.put(Short.class, short.class);
		equivalentTypeMap.put(Double.class, double.class);
		equivalentTypeMap.put(Void.class, void.class);
	}

	/**
	 * get the class equivalent to the provided one, either the wrapper of
	 * a primitive or the other way around.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param type	a Java predefined type as a class or a wrapper class.
	 * @return		the corresponding wrapper or type class.
	 */
	protected static Class<?>	getEquivalentType(Class<?> type)
	{
		if (!equivalentTypeMap.containsKey(type)) {
			throw new RuntimeException("unknown type or wrapper class: " +
											type.getCanonicalName()) ;
		}
		return equivalentTypeMap.get(type) ;
	}

	/**
	 * return true if the two classes are equivalent i.e., are either assignable
	 * from each other or one is the wrapper of the other.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code class1 != null && class2 != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param class1	a class to be tested.
	 * @param class2	a class to be tested.
	 * @return			true if the two classes are equivalent.
	 */
	protected static boolean	areEquivalentTypes(
		Class<?> class1,
		Class<?> class2
		)
	{
		assert	class1 != null && class2 != null :
					new PreconditionException(
							"class1 != null && class2 != null");

		if (class1.isPrimitive()) {
			class1 = getEquivalentType(class1) ;
		}
		if (class2.isPrimitive()) {
			class2 = getEquivalentType(class2) ;
		}
		return class1.isAssignableFrom(class2) ;
	}

	/**
	 * return true if the given class represents a component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code cl != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param cl	a class to be checked.
	 * @return		true if the class represents a component.
	 */
	public static boolean	isComponentClass(Class<?> cl)
	{
		assert	cl != null :
					new PreconditionException("cl != null");

		// All component classes must inherit from AbstractComponent
		boolean ret = AbstractComponent.class.isAssignableFrom(cl) ;
		// Component classes can only have protected constructors
		ret &= protectedConstructorsOnly(cl) ;
		return ret  ;
	}

	/**
	 * return true if the class and all of its superclasses up to
	 * <code>AbstractComponent</code> have protected constructors
	 * only.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code cl != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param cl	a class that represents a component to be checked.
	 * @return		true if the class and all of its superclasses have protected constructors only.
	 */
	public static boolean	protectedConstructorsOnly(Class<?> cl)
	{
		assert	cl != null :
					new PreconditionException("cl != null");

		boolean ret = true ;
		while (!cl.equals(Object.class) && ret) {
			Constructor<?>[] cons = cl.getDeclaredConstructors() ;
			for(int i = 0 ; i < cons.length && ret ; i++) {
				ret = Modifier.isProtected(cons[i].getModifiers()) ;
			}
			cl = cl.getSuperclass() ;
		}
		return ret ;
	}

	/**
	 * find the constructor in the provided class corresponding to the
	 * given constructor parameters.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code cl != null && constructorParams != null}
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param cl						class which constructor will be called.
	 * @param constructorParams			actual parameters of the constructor.
	 * @return							constructor corresponding to the parameters modulo the substitution of primitive types for the corresponding wrapper classes.
	 * @throws NoSuchMethodException	if no such constructor exists.
	 * @throws SecurityException		if the reflective access violates the access control status.
	 */
	public static Constructor<?>	getConstructor(
		Class<?> cl,
		Object[] constructorParams
		) throws NoSuchMethodException, SecurityException
	{
		assert	cl != null && constructorParams != null :
					new PreconditionException(
							"cl != null && constructorParams != null");

		Constructor<?> cons = null ;
		Class<?>[] actualsTypes = new Class[constructorParams.length] ;
		for (int i = 0 ; i < constructorParams.length ; i++) {
			actualsTypes[i] = constructorParams[i].getClass() ;
		}
		boolean found = false ;
		while (!cl.equals(Object.class) && !found) {
			Constructor<?>[] constructors = cl.getDeclaredConstructors() ;
			for (int i = 0 ; i < constructors.length && !found ; i++) {
				Class<?>[] formalsTypes = constructors[i].getParameterTypes() ;
				if (formalsTypes.length == actualsTypes.length) {
					boolean compatibleTypes = true ;
					for(int j = 0; j < formalsTypes.length &&
													compatibleTypes ; j++) {
						compatibleTypes =
								isAssignable(actualsTypes[j], formalsTypes[j]) ;
					}
					if (compatibleTypes) {
						found = true ;
						cons = constructors[i] ;
					}
				}
			}
			cl = cl.getSuperclass() ;
		}
		if (!found) {
			throw new NoSuchMethodException() ;
		} else {
			return cons ;
		}
	}

	/**
	 * return true if a value of the actual can be assigned to the formal.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code fromClass != null && toClass != null}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param fromClass	actual parameter type which values must be assigned to the formal parameter.
	 * @param toClass	class typing a formal parameter to be assigned.
	 * @return			true if a value of the actual can be assigned to the formal.
	 */
	private static boolean	isAssignable(Class<?> fromClass, Class<?> toClass)
	{
		assert	fromClass != null && toClass != null :
					new PreconditionException(
							"fromClass != null && toClass != null");

		return toClass.isAssignableFrom(fromClass) ||
									areEquivalentTypes(fromClass, toClass) ;
	}

	/**
	 * a simple test.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param args	command line arguments; here, nothing.
	 */
	public static void	main(String[] args)
	{
		System.out.println(isComponentClass(DynamicComponentCreator.class)) ;
	}
}
// -----------------------------------------------------------------------------
