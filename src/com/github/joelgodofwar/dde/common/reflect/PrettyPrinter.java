
package com.github.joelgodofwar.dde.common.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.joelgodofwar.dde.common.reflect.accessors.Accessors;
import com.google.common.primitives.Primitives;

/**
 * Used to print the content of an arbitrary class.
 *
 * @author Kristian
 */
public class PrettyPrinter {

	/**
	 * How far we will recurse.
	 */
	public static final int RECURSE_DEPTH = 3;

	/**
	 * Print the contents of an object.
	 *
	 * @param object - the object to serialize.
	 * @return String representation of the class.
	 * @throws IllegalAccessException If the object is null
	 */
	public static String printObject(Object object) throws IllegalAccessException {
		if (object == null) {
			throw new IllegalArgumentException("object cannot be NULL.");
		}

		return printObject(object, object.getClass(), Object.class);
	}

	/**
	 * Print the contents of an object.
	 *
	 * @param object - the object to serialize.
	 * @param start  - class to start at.
	 * @param stop   - superclass that will stop the process.
	 * @return String representation of the class
	 * @throws IllegalAccessException If the object is null
	 */
	public static String printObject(Object object, Class<?> start, Class<?> stop) throws IllegalAccessException {
		if (object == null) {
			throw new IllegalArgumentException("object cannot be NULL.");
		}

		return printObject(object, start, stop, RECURSE_DEPTH);
	}

	/**
	 * Print the contents of an object.
	 *
	 * @param object        - the object to serialize.
	 * @param start         - class to start at.
	 * @param stop          - superclass that will stop the process.
	 * @param hierachyDepth - maximum recursion level.
	 * @return String representation of the class.
	 * @throws IllegalAccessException If the object is null
	 */
	public static String printObject(Object object, Class<?> start, Class<?> stop, int hierachyDepth)
			throws IllegalAccessException {
		return printObject(object, start, stop, hierachyDepth, ObjectPrinter.DEFAULT);
	}

	/**
	 * Print the contents of an object.
	 *
	 * @param object        - the object to serialize.
	 * @param start         - class to start at.
	 * @param stop          - superclass that will stop the process.
	 * @param hierachyDepth - maximum recursion level.
	 * @param printer       - a generic object printer.
	 * @return String representation of the class.
	 * @throws IllegalAccessException If the object is null
	 */
	public static String printObject(Object object, Class<?> start, Class<?> stop, int hierachyDepth,
			ObjectPrinter printer) throws IllegalAccessException {
		if (object == null) {
			throw new IllegalArgumentException("object cannot be NULL.");
		}

		StringBuilder output = new StringBuilder();
		Set<Object> previous = new HashSet<>();

		// Start and stop
		output.append("{ ");
		printObject(output, object, start, stop, previous, hierachyDepth, true, printer);
		output.append(" }");

		return output.toString();
	}

	@SuppressWarnings("rawtypes")
	private static void printIterables(StringBuilder output, Iterable iterable, Class<?> stop,
			Set<Object> previous, int hierachyIndex, ObjectPrinter printer) throws IllegalAccessException {

		boolean first = true;
		output.append("(");

		for (Object value : iterable) {
			if (first) {
				first = false;
			} else {
				output.append(", ");
			}

			// Print value
			printValue(output, value, stop, previous, hierachyIndex - 1, printer);
		}

		output.append(")");
	}

	/**
	 * Print the content of a maps entries.
	 *
	 * @param output        - the output string builder.
	 * @param map           - the map to print.
	 * @param current       - the type of this map.
	 * @param stop          - the class that indicates we should stop printing.
	 * @param previous      - previous objects printed.
	 * @param hierachyIndex - hierachy index.
	 * @throws IllegalAccessException If any reflection went wrong.
	 */
	private static void printMap(StringBuilder output, Map<Object, Object> map, Class<?> current, Class<?> stop,
			Set<Object> previous, int hierachyIndex, ObjectPrinter printer) throws IllegalAccessException {

		boolean first = true;
		output.append("[");

		for (Entry<Object, Object> entry : map.entrySet()) {
			if (first) {
				first = false;
			} else {
				output.append(", ");
			}

			printValue(output, entry.getKey(), stop, previous, hierachyIndex - 1, printer);
			output.append(": ");
			printValue(output, entry.getValue(), stop, previous, hierachyIndex - 1, printer);
		}

		output.append("]");
	}

	private static void printArray(StringBuilder output, Object array, Class<?> current, Class<?> stop,
			Set<Object> previous, int hierachyIndex, ObjectPrinter printer) throws IllegalAccessException {

		Class<?> component = current.getComponentType();
		boolean first = true;

		if (!component.isArray()) {
			output.append(component.getName());
		}
		output.append("[");

		for (int i = 0; i < Array.getLength(array); i++) {
			if (first) {
				first = false;
			} else {
				output.append(", ");
			}

			// Handle exceptions
			try {
				printValue(output, Array.get(array, i), component, stop, previous, hierachyIndex - 1, printer);
			} catch (ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
				break;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				break;
			}
		}

		output.append("]");
	}

	// Internal recursion method
	private static void printObject(StringBuilder output, Object object, Class<?> current, Class<?> stop,
			Set<Object> previous, int hierachyIndex, boolean first,
			ObjectPrinter printer) throws IllegalAccessException {

		// See if we're supposed to skip this class
		if ((current == null) || (current == Object.class) || ((stop != null) && current.equals(stop))) {
			return;
		}

		// Don't iterate twice
		previous.add(object);

		// Hard coded limit
		if (hierachyIndex < 0) {
			output.append("...");
			return;
		}

		for (Field field : current.getDeclaredFields()) {
			int mod = field.getModifiers();

			// Skip a good number of the fields
			if (!Modifier.isTransient(mod) && !Modifier.isStatic(mod)) {
				Class<?> type = field.getType();
				Object value = Accessors.getFieldAccessor(field).get(object);

				if (first) {
					first = false;
				} else {
					output.append(", ");
				}

				output.append(field.getName());
				output.append(" = ");
				printValue(output, value, type, stop, previous, hierachyIndex - 1, printer);
			}
		}

		// Recurse
		printObject(output, object, current.getSuperclass(), stop, previous, hierachyIndex, first, printer);
	}

	private static void printValue(StringBuilder output, Object value, Class<?> stop,
			Set<Object> previous, int hierachyIndex, ObjectPrinter printer) throws IllegalAccessException {
		// Handle the NULL case
		printValue(output, value, value != null ? value.getClass() : null, stop, previous, hierachyIndex, printer);
	}

	@SuppressWarnings("unchecked") private static void printValue(StringBuilder output, Object value, Class<?> type,
			Class<?> stop, Set<Object> previous, int hierachyIndex,
			ObjectPrinter printer) throws IllegalAccessException {

		// Just print primitive types
		if (printer.print(output, value)) {
			return;
		} else if (value == null) {
			output.append("NULL");
		} else if (type.isPrimitive() || Primitives.isWrapperType(type)) {
			output.append(value);
		} else if ((type == String.class) || (hierachyIndex <= 0)) {
			output.append("\"").append(value).append("\"");
		} else if (type.isArray()) {
			printArray(output, value, type, stop, previous, hierachyIndex, printer);
		} else if (Iterable.class.isAssignableFrom(type)) {
			printIterables(output, (Iterable<?>) value, stop, previous, hierachyIndex, printer);
		} else if (Map.class.isAssignableFrom(type)) {
			printMap(output, (Map<Object, Object>) value, type, stop, previous, hierachyIndex, printer);
		} else if (ClassLoader.class.isAssignableFrom(type) || previous.contains(value)) {
			// Don't print previous objects
			output.append("\"").append(value).append("\"");
		} else {
			output.append("{ ");
			printObject(output, value, value.getClass(), stop, previous, hierachyIndex, true, printer);
			output.append(" }");
		}
	}

	/**
	 * Represents a generic object printer.
	 *
	 * @author Kristian
	 */
	public interface ObjectPrinter {

		ObjectPrinter DEFAULT = (output, value) -> false;

		/**
		 * Print the content of the given object.
		 * <p>
		 * Return FALSE in order for let the default printer take over.
		 *
		 * @param output - where to print the output.
		 * @param value  - the value to print, may be NULL.
		 * @return TRUE if we processed the value and added to the output, FALSE otherwise.
		 */
		boolean print(StringBuilder output, Object value);
	}
}