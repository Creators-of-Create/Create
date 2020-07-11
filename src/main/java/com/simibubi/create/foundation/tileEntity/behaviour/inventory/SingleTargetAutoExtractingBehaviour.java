package com.simibubi.create.foundation.tileEntity.behaviour.inventory;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class SingleTargetAutoExtractingBehaviour extends AutoExtractingBehaviour {

	public static BehaviourType<SingleTargetAutoExtractingBehaviour> TYPE = new BehaviourType<>();

	private Supplier<Direction> attachmentDirection;
	boolean synced;
	boolean advantageOnNextSync;

	public SingleTargetAutoExtractingBehaviour(SmartTileEntity te, Supplier<Direction> attachmentDirection,
		Consumer<ItemStack> onExtract, int delay) {
		super(te, Attachments.toward(attachmentDirection), onExtract, delay);
		this.attachmentDirection = attachmentDirection;
		synced = true;
		advantageOnNextSync = false;
	}

	public SingleTargetAutoExtractingBehaviour setSynchronized(boolean sync) {
		synced = sync;
		return this;
	}

	@Override
	public void writeNBT(CompoundNBT nbt) {
		nbt.putBoolean("Advantage", advantageOnNextSync);
		super.writeNBT(nbt);
	}

	@Override
	public void readNBT(CompoundNBT nbt) {
		advantageOnNextSync = nbt.getBoolean("Advantage");
		super.readNBT(nbt);
	}

	@Override
	public boolean extract() {
		if (synced) {
			BlockPos invPos = tileEntity.getPos()
				.offset(attachmentDirection.get());
			return SynchronizedExtraction.extractSynchronized(getWorld(), invPos);
		} else
			return extractFromInventory();
	}

	public boolean extractFromInventory() {
		return super.extract();
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

}
