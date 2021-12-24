package com.simibubi.create.content.contraptions.solver;

public sealed class Value {
	public static final class Unknown extends Value {
		private static int nextID = 0;

		public final int id;

		public Unknown() {
			this.id = nextID++;
		}
	}

	public static final class Known extends Value {
		public final float value;

		public Known(float value) {
			this.value = value;
		}
	}
}
