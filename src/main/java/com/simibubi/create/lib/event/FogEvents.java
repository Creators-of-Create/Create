package com.simibubi.create.lib.event;

import com.mojang.math.Vector3f;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Camera;

@Environment(EnvType.CLIENT)
public class FogEvents {
	public static final Event<SetDensity> SET_DENSITY = EventFactory.createArrayBacked(SetDensity.class, callbacks -> (info, density) -> {
		for (SetDensity callback : callbacks) {
			return callback.setDensity(info, density);
		}
		return density;
	});

	public static final Event<SetColor> SET_COLOR = EventFactory.createArrayBacked(SetColor.class, callbacks -> data -> {
		for (SetColor callback : callbacks) {
			callback.setColor(data);
		}
	});

	private FogEvents() {
	}

	@FunctionalInterface
	public interface SetDensity {
		float setDensity(Camera activeRenderInfo, float density);
	}

	@FunctionalInterface
	public interface SetColor {
		void setColor(ColorData d);
	}

	public static class ColorData {
		private final Camera camera;
		private float red;
		private float green;
		private float blue;

		public ColorData(Camera camera, float r, float g, float b) {
			this.camera = camera;
			this.red = r;
			this.green = g;
			this.blue = b;
		}

		public Camera getCamera() {
			return camera;
		}

		public float getRed() {
			return red;
		}

		public float getGreen() {
			return green;
		}

		public float getBlue() {
			return blue;
		}

		public void setRed(float red) {
			this.red = red;
		}

		public void setGreen(float green) {
			this.green = green;
		}

		public void setBlue(float blue) {
			this.blue = blue;
		}
	}
}
