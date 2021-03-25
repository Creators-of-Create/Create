package com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ScrollValueBehaviour extends TileEntityBehaviour {

	public static BehaviourType<ScrollValueBehaviour> TYPE = new BehaviourType<>();

	ValueBoxTransform slotPositioning;
	Vec3d textShift;

	int min = 0;
	int max = 1;
	public int value;
	public int scrollableValue;
	int ticksUntilScrollPacket;
	boolean forceClientState;
	String label;
	Consumer<Integer> callback;
	Consumer<Integer> clientCallback;
	Function<Integer, String> formatter;
	Function<Integer, String> unit;
	Function<StepContext, Integer> step;
	private Supplier<Boolean> isActive;
	boolean needsWrench;

	public ScrollValueBehaviour(String label, SmartTileEntity te, ValueBoxTransform slot) {
		super(te);
		this.setLabel(label);
		slotPositioning = slot;
		callback = i -> {
		};
		clientCallback = i -> {
		};
		textShift = Vec3d.ZERO;
		formatter = i -> Integer.toString(i);
		step = (c) -> 1;
		value = 0;
		isActive = () -> true;
		ticksUntilScrollPacket = -1;
	}

	@Override
	public void write(CompoundNBT nbt, boolean clientPacket) {
		nbt.putInt("ScrollValue", value);
		if (clientPacket && forceClientState) {
			nbt.putBoolean("ForceScrollable", true);
			forceClientState = false;
		}
		super.write(nbt, clientPacket);
	}

	@Override
	public void read(CompoundNBT nbt, boolean clientPacket) {
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

		if (!getWorld().isRemote)
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

	public ScrollValueBehaviour moveText(Vec3d shift) {
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

	public ScrollValueBehaviour withUnit(Function<Integer, String> unit) {
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
		value = MathHelper.clamp(value, min, max);
		if (value == this.value)
			return;
		this.value = value;
		forceClientState = true;
		callback.accept(value);
		tileEntity.markDirty();
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

	public boolean testHit(Vec3d hit) {
		BlockState state = tileEntity.getBlockState();
		Vec3d localHit = hit.subtract(new Vec3d(tileEntity.getPos()));
		return slotPositioning.testHit(state, localHit);
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public static class StepContext {
		public int currentValue;
		public boolean forward;
		public boolean shift;
		public boolean control;
	}

}
