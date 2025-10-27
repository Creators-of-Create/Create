package com.simibubi.create.content.kinetics.transmission.sequencer;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.lang.Lang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

public enum SequencerInstructions {
	TURN_ANGLE("angle", AllGuiTextures.SEQUENCER_INSTRUCTION, true, true, 360, 45, 90),
	TURN_DISTANCE("distance", AllGuiTextures.SEQUENCER_INSTRUCTION, true, true, 128, 5, 5),
	DELAY("duration", AllGuiTextures.SEQUENCER_DELAY, true, false, 600, 20, 10),
	AWAIT("", AllGuiTextures.SEQUENCER_AWAIT),
	END("", AllGuiTextures.SEQUENCER_END),

	;

	public static final StreamCodec<ByteBuf, SequencerInstructions> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(SequencerInstructions.class);

	public final String translationKey;
	public final String descriptiveTranslationKey;
	public final String parameterKey;
	public final boolean hasValueParameter;
	public final boolean hasSpeedParameter;
	public final AllGuiTextures background;
	public final int maxValue;
	public final int shiftStep;
	public final int defaultValue;

	SequencerInstructions(String parameterName, AllGuiTextures background) {
		this(parameterName, background, false, false, -1, -1, -1);
	}

	SequencerInstructions(String parameterName, AllGuiTextures background, boolean hasValueParameter,
			boolean hasSpeedParameter, int maxValue, int shiftStep, int defaultValue) {
		this.hasValueParameter = hasValueParameter;
		this.hasSpeedParameter = hasSpeedParameter;
		this.background = background;
		this.maxValue = maxValue;
		this.shiftStep = shiftStep;
		this.defaultValue = defaultValue;
		translationKey = "gui.sequenced_gearshift.instruction." + Lang.asId(name());
		descriptiveTranslationKey = translationKey + ".descriptive";
		parameterKey = translationKey + "." + parameterName;
	}

	public boolean needsPropagation() {
		return this == TURN_ANGLE || this == TURN_DISTANCE;
	}

	static List<Component> getOptions() {
		List<Component> options = new ArrayList<>();
		for (SequencerInstructions entry : values())
			options.add(CreateLang.translateDirect(entry.descriptiveTranslationKey));
		return options;
	}

	String formatValue(int value) {
		if (this == TURN_ANGLE)
			return value + CreateLang.translateDirect("generic.unit.degrees").getString();
		if (this == TURN_DISTANCE)
			return value + "m";
		if (this == DELAY) {
			if (value >= 20)
				return (value / 20) + "s";
			return value + "t";
		}
		return "" + value;
	}

}
