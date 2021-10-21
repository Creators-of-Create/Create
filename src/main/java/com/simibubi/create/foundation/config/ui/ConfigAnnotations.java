package com.simibubi.create.foundation.config.ui;

public class ConfigAnnotations {


	/**
	 * Changes the way the Integer value is display.
	 */
	public enum IntDisplay implements ConfigAnnotation {
		HEX("#"),
		ZERO_X("0x"),
		ZERO_B("0b");

		private final String value;

		IntDisplay(String value) {
			this.value = value;
		}

		@Override
		public String getName() {
			return "IntDisplay";
		}

		@Override
		public String getValue() {
			return value;
		}
	}

	/**
	 * Indicates to the player that changing this value will require a restart to take full effect
	 */
	public enum RequiresRestart implements ConfigAnnotation {
		CLIENT("client"),
		SERVER("server"),
		BOTH("both");

		private final String value;

		RequiresRestart(String value) {
			this.value = value;
		}

		@Override
		public String getName() {
			return "RequiresReload";
		}

		@Override
		public String getValue() {
			return value;
		}
	}

	/**
	 * Indicates to the player that changing this value will require them to relog to take full effect
	 */
	public enum RequiresRelog implements ConfigAnnotation {
		TRUE;

		@Override
		public String getName() {
			return "RequiresRelog";
		}
	}

	/**
	 * Changing a value that is annotated with Execute will cause the player to run the given command automatically.
	 */
	public static class Execute implements ConfigAnnotation {

		private final String command;

		public static Execute run(String command) {
			return new Execute(command);
		}

		private Execute(String command) {
			this.command = command;
		}

		@Override
		public String getName() {
			return "Execute";
		}

		@Override
		public String getValue() {
			return command;
		}
	}

	public interface ConfigAnnotation {
		String getName();

		default String getValue() {
			return null;
		}

		default String asComment() {
			String comment = "[@cui:" + getName();
			String value = getValue();
			if (value != null) {
				comment = comment + ":" + value;
			}
			comment = comment + "]";
			return comment;
		}

	}
}
