package com.simibubi.create.foundation.utility;

import net.minecraft.network.chat.MutableComponent;

public class FluidFormatter {

	public static String asString(long amount, boolean shorten) {
		Couple<MutableComponent> couple = asComponents(amount, shorten);
		return couple.getFirst().getString() + " " + couple.getSecond().getString();
	}

	public static Couple<MutableComponent> asComponents(long amount, boolean shorten) {
		if (shorten && amount >= 1000) {
			return Couple.create(
					Components.literal(String.format("%.1f" , amount / 1000d)),
					Lang.translateDirect("generic.unit.buckets")
			);
		}

		return Couple.create(
				Components.literal(String.valueOf(amount)),
				Lang.translateDirect("generic.unit.millibuckets")
		);
	}

}
