package com.simibubi.create.foundation.utility;

import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.lang.Components;
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
					CreateLang.translateDirect("generic.unit.buckets")
			);
		}

		return Couple.create(
				Components.literal(String.valueOf(amount)),
				CreateLang.translateDirect("generic.unit.millibuckets")
		);
	}

}
