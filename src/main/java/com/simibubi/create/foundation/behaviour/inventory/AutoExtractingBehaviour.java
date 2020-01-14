package com.simibubi.create.foundation.behaviour.inventory;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.foundation.behaviour.base.IBehaviourType;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class AutoExtractingBehaviour extends ExtractingBehaviour {

	public static IBehaviourType<AutoExtractingBehaviour> TYPE = new IBehaviourType<AutoExtractingBehaviour>() {
	};

	private int delay;
	private int timer;
	Supplier<Boolean> shouldExtract;
	Supplier<Boolean> shouldPause;
	private boolean ticking;

	public AutoExtractingBehaviour(SmartTileEntity te, Supplier<List<Pair<BlockPos, Direction>>> attachments,
			Consumer<ItemStack> onExtract, int delay) {
		super(te, attachments, onExtract);
		shouldPause = () -> false;
		shouldExtract = () -> true;
		ticking = true;
		this.delay = delay;
	}

	public AutoExtractingBehaviour pauseWhen(Supplier<Boolean> condition) {
		shouldPause = condition;
		return this;
	}

	public ExtractingBehaviour waitUntil(Supplier<Boolean> condition) {
		this.shouldExtract = condition;
		return this;
	}

	public void setDelay(int delay) {
		this.delay = delay;
		this.timer = delay;
	}

	@Override
	public boolean extract(int amount) {
		timer = delay;
		return super.extract(amount);
	}

	@Override
	public void tick() {
		super.tick();
		
		if (!ticking)
			return;
		
		if (getWorld().isRemote)
			return;

		if (getShouldPause().get()) {
			timer = 0;
			return;
		}

		if (timer > 0) {
			timer--;
			return;
		}

		if (!getShouldExtract().get())
			return;

		extract();
	}
	
	public void setTicking(boolean ticking) {
		this.ticking = ticking;
	}

	@Override
	public IBehaviourType<?> getType() {
		return TYPE;
	}

	public Supplier<Boolean> getShouldExtract() {
		return shouldExtract;
	}

	public Supplier<Boolean> getShouldPause() {
		return shouldPause;
	}

}
