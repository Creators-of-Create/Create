package com.simibubi.create.foundation.utility;

import net.createmod.catnip.api.data.Couple;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class FluidFormatter {

	public static String asString(long amount, boolean shorten) {
		Couple<MutableComponent> couple = asComponents(amount, shorten);
		return couple.getFirst().getString() + " " + couple.getSecond().getString();
	}

	public static Couple<MutableComponent> asComponents(long amount, boolean shorten) {
		if (shorten && amount >= 1000) {
			return Couple.create(
					Component.literal(String.format("%.1f" , amount / 1000d)),
					CreateLang.translateDirect("generic.unit.buckets")
			);
		}

		return Couple.create(
				Component.literal(String.valueOf(amount)),
				CreateLang.translateDirect("generic.unit.millibuckets")
		);
	}

}
