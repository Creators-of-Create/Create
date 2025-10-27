package com.simibubi.create.content.kinetics.transmission.sequencer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;

import net.createmod.catnip.lang.Lang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

public enum InstructionSpeedModifiers {

	FORWARD_FAST(2, ">>"), FORWARD(1, "->"), BACK(-1, "<-"), BACK_FAST(-2, "<<"),

	;

	public static final StreamCodec<ByteBuf, InstructionSpeedModifiers> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(InstructionSpeedModifiers.class);

	String translationKey;
	int value;
	Component label;

	private InstructionSpeedModifiers(int modifier, Component label) {
		this.label = label;
		translationKey = "gui.sequenced_gearshift.speed." + Lang.asId(name());
		value = modifier;
	}

	private InstructionSpeedModifiers(int modifier, String label) {
		this.label = Component.literal(label);
		translationKey = "gui.sequenced_gearshift.speed." + Lang.asId(name());
		value = modifier;
	}

	static List<Component> getOptions() {
		List<Component> options = new ArrayList<>();
		for (InstructionSpeedModifiers entry : values())
			options.add(CreateLang.translateDirect(entry.translationKey));
		return options;
	}

	public static InstructionSpeedModifiers getByModifier(int modifier) {
		return Arrays.stream(InstructionSpeedModifiers.values())
				.filter(speedModifier -> speedModifier.value == modifier)
				.findAny()
				.orElse(InstructionSpeedModifiers.FORWARD);
	}

}
