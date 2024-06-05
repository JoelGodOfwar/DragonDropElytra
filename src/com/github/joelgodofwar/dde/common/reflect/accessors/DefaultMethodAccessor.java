package com.github.joelgodofwar.dde.common.reflect.accessors;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

final class DefaultMethodAccessor implements MethodAccessor {

	private final Method method;
	private final MethodHandle methodHandle;

	public DefaultMethodAccessor(Method method, MethodHandle methodHandle, boolean staticMethod) {
		this.method = method;
		this.methodHandle = methodHandle;
	}

	@Override
	public Object invoke(Object target, Object... args) {
		try {
			return this.methodHandle.invoke(target, args);
		} catch (Throwable throwable) {
			throw new IllegalStateException("Unable to invoke method " + this.method, throwable);
		}
	}

	@Override
	public Method getMethod() {
		return this.method;
	}
	=======
			package com.github.joelgodofwar.dde.common.reflect.accessors;

	import java.lang.invoke.MethodHandle;
	import java.lang.reflect.Method;

	final class DefaultMethodAccessor implements MethodAccessor {

		private final Method method;
		private final MethodHandle methodHandle;

		public DefaultMethodAccessor(Method method, MethodHandle methodHandle, boolean staticMethod) {
			this.method = method;
			this.methodHandle = methodHandle;
		}

		@Override
		public Object invoke(Object target, Object... args) {
			try {
				return this.methodHandle.invoke(target, args);
			} catch (Throwable throwable) {
				throw new IllegalStateException("Unable to invoke method " + this.method, throwable);
			}
		}

		@Override
		public Method getMethod() {
			return this.method;
		}

	}