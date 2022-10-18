package com.simibubi.create.content.logistics.trains.management.display;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import com.google.common.base.Strings;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class FlapDisplaySection {

	static final Map<String, String[]> LOADED_FLAP_CYCLES = new HashMap<>();
	static Random r = new Random();

	public static final float MONOSPACE = 7;
	public static final float WIDE_MONOSPACE = 9;

	float size;
	boolean singleFlap;
	boolean hasGap;
	boolean rightAligned;
	boolean wideFlaps;
	boolean sendTransition;
	String cycle;
	Component component;

	// Client
	String[] cyclingOptions;
	boolean[] spinning;
	int spinningTicks;
	String text;

	public FlapDisplaySection(float width, String cycle, boolean singleFlap, boolean hasGap) {
		this.size = width;
		this.cycle = cycle;
		this.hasGap = hasGap;
		this.singleFlap = singleFlap;
		this.spinning = new boolean[singleFlap ? 1 : Math.max(0, (int) (width / FlapDisplaySection.MONOSPACE))];
		this.text = Strings.repeat(" ", spinning.length);
		this.component = null;
	}

	public FlapDisplaySection rightAligned() {
		rightAligned = true;
		return this;
	}

	public FlapDisplaySection wideFlaps() {
		wideFlaps = true;
		return this;
	}

	public void setText(Component component) {
		this.component = component;
		sendTransition = true;
	}

	public void refresh(boolean transition) {
		if (component == null)
			return;

		String newText = component.getString();

		if (!singleFlap) {
			if (rightAligned)
				newText = newText.trim();
			newText = newText.toUpperCase(Locale.ROOT);
			newText = newText.substring(0, Math.min(spinning.length, newText.length()));
			String whitespace = Strings.repeat(" ", spinning.length - newText.length());
			newText = rightAligned ? whitespace + newText : newText + whitespace;
			if (!text.isEmpty())
				for (int i = 0; i < spinning.length; i++)
					spinning[i] |= transition && text.charAt(i) != newText.charAt(i);
		} else if (!text.isEmpty())
			spinning[0] |= transition && !newText.equals(text);

		text = newText;
		spinningTicks = 0;
	}

	public int tick(boolean instant) {
		if (cyclingOptions == null)
			return 0;
		int max = Math.max(4, (int) (cyclingOptions.length * 1.75f));
		if (spinningTicks > max)
			return 0;
		
		spinningTicks++;
		if (spinningTicks <= max && spinningTicks < 2)
			return spinningTicks == 1 ? 0 : spinning.length;
		
		int spinningFlaps = 0;
		for (int i = 0; i < spinning.length; i++) {
			int increasingChance = Mth.clamp(8 - spinningTicks, 1, 10);
			boolean continueSpin = !instant && r.nextInt(increasingChance * max / 4) != 0;
			continueSpin &= max > 5 || spinningTicks < 2;
			spinning[i] &= continueSpin;

			if (i > 0 && r.nextInt(3) > 0)
				spinning[i - 1] &= continueSpin;
			if (i < spinning.length - 1 && r.nextInt(3) > 0)
				spinning[i + 1] &= continueSpin;
			if (spinningTicks > max)
				spinning[i] = false;
			
			if (spinning[i])
				spinningFlaps++;
		}
		
		return spinningFlaps;
	}

	public float getSize() {
		return size;
	}

	public CompoundTag write() {
		CompoundTag tag = new CompoundTag();
		tag.putFloat("Width", size);
		tag.putString("Cycle", cycle);
		if (rightAligned)
			NBTHelper.putMarker(tag, "RightAligned");
		if (singleFlap)
			NBTHelper.putMarker(tag, "SingleFlap");
		if (hasGap)
			NBTHelper.putMarker(tag, "Gap");
		if (wideFlaps)
			NBTHelper.putMarker(tag, "Wide");
		if (component != null)
			tag.putString("Text", Component.Serializer.toJson(component));
		if (sendTransition)
			NBTHelper.putMarker(tag, "Transition");
		sendTransition = false;
		return tag;
	}

	public static FlapDisplaySection load(CompoundTag tag) {
		float width = tag.getFloat("Width");
		String cycle = tag.getString("Cycle");
		boolean singleFlap = tag.contains("SingleFlap");
		boolean hasGap = tag.contains("Gap");

		FlapDisplaySection section = new FlapDisplaySection(width, cycle, singleFlap, hasGap);
		section.cyclingOptions = getFlapCycle(cycle);
		section.rightAligned = tag.contains("RightAligned");
		section.wideFlaps = tag.contains("Wide");

		if (!tag.contains("Text"))
			return section;

		section.component = Component.Serializer.fromJson(tag.getString("Text"));
		section.refresh(tag.getBoolean("Transition"));
		return section;
	}

	public void update(CompoundTag tag) {
		component = Component.Serializer.fromJson(tag.getString("Text"));
		if (cyclingOptions == null)
			cyclingOptions = getFlapCycle(cycle);
		refresh(tag.getBoolean("Transition"));
	}

	public boolean renderCharsIndividually() {
		return !singleFlap;
	}

	public static String[] getFlapCycle(String key) {
		return LOADED_FLAP_CYCLES.computeIfAbsent(key, k -> Lang.translateDirect("flap_display.cycles." + key)
			.getString()
			.split(";"));
	}

}