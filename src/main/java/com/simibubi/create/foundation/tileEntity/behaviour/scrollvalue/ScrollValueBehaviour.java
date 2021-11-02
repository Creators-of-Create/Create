package com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ScrollValueBehaviour extends TileEntityBehaviour {

	public static BehaviourType<ScrollValueBehaviour> TYPE = new BehaviourType<>();

	ValueBoxTransform slotPositioning;
	Vec3 textShift;

	int min = 0;
	int max = 1;
	public int value;
	public int scrollableValue;
	int ticksUntilScrollPacket;
	boolean forceClientState;
	Component label;
	Consumer<Integer> callback;
	Consumer<Integer> clientCallback;
	Function<Integer, String> formatter;
	Function<Integer, Component> unit;
	Function<StepContext, Integer> step;
	private Supplier<Boolean> isActive;
	boolean needsWrench;

	public ScrollValueBehaviour(Component label, SmartTileEntity te, ValueBoxTransform slot) {
		super(te);
		this.setLabel(label);
		slotPositioning = slot;
		callback = i -> {
		};
		clientCallback = i -> {
		};
		textShift = Vec3.ZERO;
		formatter = i -> Integer.toString(i);
		step = (c) -> 1;
		value = 0;
		isActive = () -> true;
		ticksUntilScrollPacket = -1;
	}

	@Override
	public boolean isSafeNBT() { return true; }

	@Override
	public void write(CompoundTag nbt, boolean clientPacket) {
		nbt.putInt("ScrollValue", value);
		if (clientPacket && forceClientState) {
			nbt.putBoolean("ForceScrollable", true);
			forceClientState = false;
		}
		super.write(nbt, clientPacket);
	}

	@Override
	public void read(CompoundTag nbt, boolean clientPacket) {
		value = nbt.getInt("ScrollValue");
		if (nbt.contains("ForceScrollable")) {
			ticksUntilScrollPacket = -1;
			scrollableValue = value;
		}
		super.read(nbt, clientPacket);
	}

	@Override
	public void tick() {
		super.tick();

		if (!getWorld().isClientSide)
			return;
		if (ticksUntilScrollPacket == -1)
			return;
		if (ticksUntilScrollPacket > 0) {
			ticksUntilScrollPacket--;
			return;
		}

		AllPackets.channel.sendToServer(new ScrollValueUpdatePacket(getPos(), scrollableValue));
		ticksUntilScrollPacket = -1;
	}

	public ScrollValueBehaviour withClientCallback(Consumer<Integer> valueCallback) {
		clientCallback = valueCallback;
		return this;
	}

	public ScrollValueBehaviour withCallback(Consumer<Integer> valueCallback) {
		callback = valueCallback;
		return this;
	}

	public ScrollValueBehaviour between(int min, int max) {
		this.min = min;
		this.max = max;
		return this;
	}

	public ScrollValueBehaviour moveText(Vec3 shift) {
		textShift = shift;
		return this;
	}

	public ScrollValueBehaviour requiresWrench() {
		this.needsWrench = true;
		return this;
	}

	public ScrollValueBehaviour withFormatter(Function<Integer, String> formatter) {
		this.formatter = formatter;
		return this;
	}

	public ScrollValueBehaviour withUnit(Function<Integer, Component> unit) {
		this.unit = unit;
		return this;
	}

	public ScrollValueBehaviour onlyActiveWhen(Supplier<Boolean> condition) {
		isActive = condition;
		return this;
	}

	public ScrollValueBehaviour withStepFunction(Function<StepContext, Integer> step) {
		this.step = step;
		return this;
	}

	@Override
	public void initialize() {
		super.initialize();
		setValue(value);
		scrollableValue = value;
	}

	public void setValue(int value) {
		value = Mth.clamp(value, min, max);
		if (value == this.value)
			return;
		this.value = value;
		forceClientState = true;
		callback.accept(value);
		tileEntity.setChanged();
		tileEntity.sendData();
		scrollableValue = value;
	}

	public int getValue() {
		return value;
	}

	public String formatValue() {
		return formatter.apply(scrollableValue);
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	public boolean isActive() {
		return isActive.get();
	}

	public boolean testHit(Vec3 hit) {
		BlockState state = tileEntity.getBlockState();
		Vec3 localHit = hit.subtract(Vec3.atLowerCornerOf(tileEntity.getBlockPos()));
		return slotPositioning.testHit(state, localHit);
	}

	public void setLabel(Component label) {
		this.label = label;
	}

	public static class StepContext {
		public int currentValue;
		public boolean forward;
		public boolean shift;
		public boolean control;
	}

}
