package fr.sorbonne_u.components;

// Copyright Jacques Malenfant, Sorbonne Universite.
//
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

//------------------------------------------------------------------------------
/**
 * The class <code>AbstractComponentHelper</code>
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
	public static boolean		protectedConstructorsOnly(Class<?> cl)
	{
		boolean ret = true ;
		while (cl != Object.class && ret) {
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
	 * pre	cl != null and constructorParams != null
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
		Constructor<?> cons = null ;
		Class<?>[] parameterTypes = new Class[constructorParams.length] ;
		Class<?>[] parameterWithPrimitiveTypes =
									new Class[constructorParams.length] ;
		for (int i = 0 ; i < constructorParams.length ; i++) {
			parameterTypes[i] = constructorParams[i].getClass() ;
			parameterWithPrimitiveTypes[i] = parameterTypes[i] ;
			if (isWrapper(parameterWithPrimitiveTypes[i])) {
				parameterWithPrimitiveTypes[i] =
						class2type(parameterWithPrimitiveTypes[i]) ;
			}
		}
		boolean found = false ;
		while (cl != Object.class && !found) {
			try {
				cons = cl.getDeclaredConstructor(parameterTypes) ;
				found = true ;
			} catch (NoSuchMethodException e) {
				try {
					cons = cl.getDeclaredConstructor(
										parameterWithPrimitiveTypes) ;
					found = true ;
				} catch (NoSuchMethodException e1) {
					 ;
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
	 * return the class representing the primitive type for which
	 * <code>c</code> is the corresponding wrapper class.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	c != null
	 * pre	isWrapper(c)
	 * post	return != null
	 * </pre>
	 *
	 * @param c	a primitive type wrapper class.
	 * @return	the class representing the primitive type for which <code>c</code> is the corresponding wrapper class.
	 */
	public static Class<?> 		class2type(Class<?> c)
	{
		assert	c != null ;
		assert	isWrapper(c) ;

		Class<?> type = null ;
		if (c == Byte.class) {
			type = byte.class ;
		} else if (c == Short.class) {
			type = short.class ;
		} else if (c == Integer.class) {
			type = int.class ;
		} else if (c == Long.class) {
			type = long.class ;
		} else if (c == Character.class) {
			type = char.class ;
		} else if (c == Float.class) {
			type = float.class ;
		} else if (c == Double.class) {
			type = double.class ;
		} else if (c == Boolean.class) {
			type = boolean.class ;
		} else if (c == Void.class) {
			type = void.class ;
		}

		assert	type != null ;

		return type ;
	}

	/**
	 * return true if the class <code>c</code> is a wrapper class for a
	 * primitive type.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	c != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param c	class to be tested.
	 * @return	true if the class <code>c</code> is a wrapper class for a primitive type.
	 */
	public static boolean 	isWrapper(Class<?> c)
	{
		assert	c != null ;

		boolean ret = false ;
		if (c == Byte.class || c == Short.class || c == Integer.class ||
			c == Long.class || c == Character.class || c == Float.class ||
			c == Double.class || c == Boolean.class || c == Void.class)
		{
			ret = true ;
		}
		return ret ;
	}
}
//------------------------------------------------------------------------------
