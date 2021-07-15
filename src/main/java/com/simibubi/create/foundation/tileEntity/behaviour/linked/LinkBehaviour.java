package com.simibubi.create.foundation.tileEntity.behaviour.linked;

import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.IRedstoneLinkable;
import com.simibubi.create.content.logistics.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.logistics.RedstoneLinkNetworkHandler.Frequency;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class LinkBehaviour extends TileEntityBehaviour implements IRedstoneLinkable {

	public static BehaviourType<LinkBehaviour> TYPE = new BehaviourType<>();

	enum Mode {
		TRANSMIT, RECEIVE
	}

	Frequency frequencyFirst;
	Frequency frequencyLast;
	ValueBoxTransform firstSlot;
	ValueBoxTransform secondSlot;
	Vector3d textShift;

	public boolean newPosition;
	private Mode mode;
	private IntSupplier transmission;
	private IntConsumer signalCallback;

	protected LinkBehaviour(SmartTileEntity te, Pair<ValueBoxTransform, ValueBoxTransform> slots) {
		super(te);
		frequencyFirst = Frequency.EMPTY;
		frequencyLast = Frequency.EMPTY;
		firstSlot = slots.getLeft();
		secondSlot = slots.getRight();
		textShift = Vector3d.ZERO;
		newPosition = true;
	}

	public static LinkBehaviour receiver(SmartTileEntity te, Pair<ValueBoxTransform, ValueBoxTransform> slots,
		IntConsumer signalCallback) {
		LinkBehaviour behaviour = new LinkBehaviour(te, slots);
		behaviour.signalCallback = signalCallback;
		behaviour.mode = Mode.RECEIVE;
		return behaviour;
	}

	public static LinkBehaviour transmitter(SmartTileEntity te, Pair<ValueBoxTransform, ValueBoxTransform> slots,
		IntSupplier transmission) {
		LinkBehaviour behaviour = new LinkBehaviour(te, slots);
		behaviour.transmission = transmission;
		behaviour.mode = Mode.TRANSMIT;
		return behaviour;
	}

	public LinkBehaviour moveText(Vector3d shift) {
		textShift = shift;
		return this;
	}

	public void copyItemsFrom(LinkBehaviour behaviour) {
		if (behaviour == null)
			return;
		frequencyFirst = behaviour.frequencyFirst;
		frequencyLast = behaviour.frequencyLast;
	}

	@Override
	public boolean isListening() {
		return mode == Mode.RECEIVE;
	}

	@Override
	public int getTransmittedStrength() {
		return mode == Mode.TRANSMIT ? transmission.getAsInt() : 0;
	}

	@Override
	public void setReceivedStrength(int networkPower) {
		if (!newPosition)
			return;
		signalCallback.accept(networkPower);
	}

	public void notifySignalChange() {
		Create.REDSTONE_LINK_NETWORK_HANDLER.updateNetworkOf(getWorld(), this);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (getWorld().isClientSide)
			return;
		getHandler().addToNetwork(getWorld(), this);
		newPosition = true;
	}

	@Override
	public Pair<Frequency, Frequency> getNetworkKey() {
		return Pair.of(frequencyFirst, frequencyLast);
	}

	@Override
	public void remove() {
		super.remove();
		if (getWorld().isClientSide)
			return;
		getHandler().removeFromNetwork(getWorld(), this);
	}

	@Override
	public boolean isSafeNBT() {
		return true;
	}

	@Override
	public void write(CompoundNBT nbt, boolean clientPacket) {
		super.write(nbt, clientPacket);
		nbt.put("FrequencyFirst", frequencyFirst.getStack()
			.save(new CompoundNBT()));
		nbt.put("FrequencyLast", frequencyLast.getStack()
			.save(new CompoundNBT()));
		nbt.putLong("LastKnownPosition", tileEntity.getBlockPos()
			.asLong());
	}

	@Override
	public void read(CompoundNBT nbt, boolean clientPacket) {
		long positionInTag = tileEntity.getBlockPos()
			.asLong();
		long positionKey = nbt.getLong("LastKnownPosition");
		newPosition = positionInTag != positionKey;

		super.read(nbt, clientPacket);
		frequencyFirst = Frequency.of(ItemStack.of(nbt.getCompound("FrequencyFirst")));
		frequencyLast = Frequency.of(ItemStack.of(nbt.getCompound("FrequencyLast")));
	}

	public void setFrequency(boolean first, ItemStack stack) {
		stack = stack.copy();
		stack.setCount(1);
		ItemStack toCompare = first ? frequencyFirst.getStack() : frequencyLast.getStack();
		boolean changed =
			!ItemStack.isSame(stack, toCompare) || !ItemStack.tagMatches(stack, toCompare);

		if (changed)
			getHandler().removeFromNetwork(getWorld(), this);

		if (first)
			frequencyFirst = Frequency.of(stack);
		else
			frequencyLast = Frequency.of(stack);

		if (!changed)
			return;

		tileEntity.sendData();
		getHandler().addToNetwork(getWorld(), this);
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	private RedstoneLinkNetworkHandler getHandler() {
		return Create.REDSTONE_LINK_NETWORK_HANDLER;
	}

	public static class SlotPositioning {
		Function<BlockState, Pair<Vector3d, Vector3d>> offsets;
		Function<BlockState, Vector3d> rotation;
		float scale;

		public SlotPositioning(Function<BlockState, Pair<Vector3d, Vector3d>> offsetsForState,
			Function<BlockState, Vector3d> rotationForState) {
			offsets = offsetsForState;
			rotation = rotationForState;
			scale = 1;
		}

		public SlotPositioning scale(float scale) {
			this.scale = scale;
			return this;
		}

	}

	public boolean testHit(Boolean first, Vector3d hit) {
		BlockState state = tileEntity.getBlockState();
		Vector3d localHit = hit.subtract(Vector3d.atLowerCornerOf(tileEntity.getBlockPos()));
		return (first ? firstSlot : secondSlot).testHit(state, localHit);
	}

	@Override
	public boolean isAlive() {
		return !tileEntity.isRemoved() && getWorld().getBlockEntity(getPos()) == tileEntity;
	}

	@Override
	public BlockPos getLocation() {
		return getPos();
	}

}
