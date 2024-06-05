
package com.github.joelgodofwar.dde.util;

public enum ClickAction {
	SUGGEST_COMMAND, RUN_COMMAND, OPEN_URL, NONE;

	private final String jsonValue;

	ClickAction() {
		jsonValue = name().toLowerCase();
	}

	@Override
	public String toString() {
		return jsonValue;
	}
	=======
			package com.github.joelgodofwar.dde.util;

	public enum ClickAction {
		SUGGEST_COMMAND, RUN_COMMAND, OPEN_URL, NONE;

		private final String jsonValue;

		ClickAction() {
			jsonValue = name().toLowerCase();
		}

		@Override
		public String toString() {
			return jsonValue;
		}

	}