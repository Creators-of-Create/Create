package com.simibubi.create.foundation.behaviour.inventory;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.simibubi.create.foundation.behaviour.base.IBehaviourType;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class SingleTargetAutoExtractingBehaviour extends AutoExtractingBehaviour {

	public static IBehaviourType<SingleTargetAutoExtractingBehaviour> TYPE = new IBehaviourType<SingleTargetAutoExtractingBehaviour>() {
	};

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

	public SingleTargetAutoExtractingBehaviour dontSynchronize() {
		synced = false;
		return this;
	}

	@Override
	public boolean extract() {
		if (synced) {
			BlockPos invPos = tileEntity.getPos().offset(attachmentDirection.get());
			return SynchronizedExtraction.extractSynchronized(getWorld(), invPos);
		} else
			return extractFromInventory();
	}

	public boolean extractFromInventory() {
		return super.extract();
	}

	@Override
	public IBehaviourType<?> getType() {
		return TYPE;
	}

}
