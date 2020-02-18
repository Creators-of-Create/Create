package com.simibubi.create.foundation.behaviour.linked;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.behaviour.base.IBehaviourType;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.modules.logistics.RedstoneLinkNetworkHandler;
import com.simibubi.create.modules.logistics.RedstoneLinkNetworkHandler.Frequency;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.Vec3d;

public class LinkBehaviour extends TileEntityBehaviour {

	public static IBehaviourType<LinkBehaviour> TYPE = new IBehaviourType<LinkBehaviour>() {
	};

	enum Mode {
		TRANSMIT, RECEIVE
	}

	Frequency frequencyFirst;
	Frequency frequencyLast;
	ValueBoxTransform firstSlot;
	ValueBoxTransform secondSlot;
	Vec3d textShift;

	private Mode mode;
	private Supplier<Boolean> transmission;
	private Consumer<Boolean> signalCallback;

	protected LinkBehaviour(SmartTileEntity te, Pair<ValueBoxTransform, ValueBoxTransform> slots) {
		super(te);
		frequencyFirst = new Frequency(ItemStack.EMPTY);
		frequencyLast = new Frequency(ItemStack.EMPTY);
		firstSlot = slots.getLeft();
		secondSlot = slots.getRight();
		textShift = Vec3d.ZERO;
	}

	public static LinkBehaviour receiver(SmartTileEntity te, Pair<ValueBoxTransform, ValueBoxTransform> slots,
			Consumer<Boolean> signalCallback) {
		LinkBehaviour behaviour = new LinkBehaviour(te, slots);
		behaviour.signalCallback = signalCallback;
		behaviour.mode = Mode.RECEIVE;
		return behaviour;
	}

	public static LinkBehaviour transmitter(SmartTileEntity te, Pair<ValueBoxTransform, ValueBoxTransform> slots,
			Supplier<Boolean> transmission) {
		LinkBehaviour behaviour = new LinkBehaviour(te, slots);
		behaviour.transmission = transmission;
		behaviour.mode = Mode.TRANSMIT;
		return behaviour;
	}

	public LinkBehaviour moveText(Vec3d shift) {
		textShift = shift;
		return this;
	}

	public void copyItemsFrom(LinkBehaviour behaviour) {
		if (behaviour == null)
			return;
		frequencyFirst = behaviour.frequencyFirst;
		frequencyLast = behaviour.frequencyLast;
	}

	public boolean isListening() {
		return mode == Mode.RECEIVE;
	}

	public boolean isTransmitting() {
		return mode == Mode.TRANSMIT && transmission.get();
	}

	public void updateReceiver(boolean networkPowered) {
		signalCallback.accept(networkPowered);
	}

	public void notifySignalChange() {
		Create.redstoneLinkNetworkHandler.updateNetworkOf(this);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (tileEntity.getWorld().isRemote)
			return;
		getHandler().addToNetwork(this);
	}

	public Pair<Frequency, Frequency> getNetworkKey() {
		return Pair.of(frequencyFirst, frequencyLast);
	}

	@Override
	public void remove() {
		super.remove();
		if (tileEntity.getWorld().isRemote)
			return;
		getHandler().removeFromNetwork(this);
	}

	@Override
	public void writeNBT(CompoundNBT compound) {
		super.writeNBT(compound);
		compound.put("FrequencyFirst", frequencyFirst.getStack().write(new CompoundNBT()));
		compound.put("FrequencyLast", frequencyLast.getStack().write(new CompoundNBT()));
	}

	@Override
	public void readNBT(CompoundNBT compound) {
		super.readNBT(compound);
		frequencyFirst = new Frequency(ItemStack.read(compound.getCompound("FrequencyFirst")));
		frequencyLast = new Frequency(ItemStack.read(compound.getCompound("FrequencyLast")));
	}

	public void setFrequency(boolean first, ItemStack stack) {
		stack = stack.copy();
		stack.setCount(1);
		ItemStack toCompare = first ? frequencyFirst.getStack() : frequencyLast.getStack();
		boolean changed =
			!ItemStack.areItemsEqual(stack, toCompare) || !ItemStack.areItemStackTagsEqual(stack, toCompare);

		if (changed)
			getHandler().removeFromNetwork(this);

		if (first)
			frequencyFirst = new Frequency(stack);
		else
			frequencyLast = new Frequency(stack);

		if (!changed)
			return;

		tileEntity.sendData();
		getHandler().addToNetwork(this);
	}

	@Override
	public IBehaviourType<?> getType() {
		return TYPE;
	}

	private RedstoneLinkNetworkHandler getHandler() {
		return Create.redstoneLinkNetworkHandler;
	}

	public static class SlotPositioning {
		Function<BlockState, Pair<Vec3d, Vec3d>> offsets;
		Function<BlockState, Vec3d> rotation;
		float scale;

		public SlotPositioning(Function<BlockState, Pair<Vec3d, Vec3d>> offsetsForState,
				Function<BlockState, Vec3d> rotationForState) {
			offsets = offsetsForState;
			rotation = rotationForState;
			scale = 1;
		}

		public SlotPositioning scale(float scale) {
			this.scale = scale;
			return this;
		}

	}

	public boolean testHit(Boolean first, Vec3d hit) {
		BlockState state = tileEntity.getBlockState();
		Vec3d localHit = hit.subtract(new Vec3d(tileEntity.getPos()));
		return (first ? firstSlot : secondSlot).testHit(state, localHit);
	}

}
