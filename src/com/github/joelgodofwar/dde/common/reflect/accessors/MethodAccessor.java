
package com.github.joelgodofwar.dde.common.reflect.accessors;

import java.lang.reflect.Method;

/**
 * Represents an interface for invoking a method.
 *
 * @author Kristian
 */
public interface MethodAccessor {

	/**
	 * Invoke the underlying method.
	 *
	 * @param target - the target instance, or NULL for a static method.
	 * @param args   - the arguments to pass to the method.
	 * @return The return value, or NULL for void methods.
	 */
	Object invoke(Object target, Object... args);

	/**
	 * Retrieve the underlying method.
	 *
	 * @return The method.
	 */
	Method getMethod();

}