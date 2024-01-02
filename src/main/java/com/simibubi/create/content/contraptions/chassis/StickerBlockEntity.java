package com.simibubi.create.content.contraptions.chassis;

import java.util.List;

import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import com.simibubi.create.content.contraptions.glue.SuperGlueItem;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class StickerBlockEntity extends SmartBlockEntity {

	LerpedFloat piston;
	boolean update;

	public StickerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		piston = LerpedFloat.linear();
		update = false;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	@Override
	public void initialize() {
		super.initialize();
		if (!level.isClientSide)
			return;
		piston.startWithValue(isBlockStateExtended() ? 1 : 0);
	}

	public boolean isBlockStateExtended() {
		BlockState blockState = getBlockState();
		boolean extended = AllBlocks.STICKER.has(blockState) && blockState.getValue(StickerBlock.EXTENDED);
		return extended;
	}

	@Override
	public void tick() {
		super.tick();
		if (!level.isClientSide)
			return;
		piston.tickChaser();

		if (isAttachedToBlock() && piston.getValue(0) != piston.getValue() && piston.getValue() == 1) {
			SuperGlueItem.spawnParticles(level, worldPosition, getBlockState().getValue(StickerBlock.FACING), true);
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> playSound(true));
		}

		if (!update)
			return;
		update = false;
		int target = isBlockStateExtended() ? 1 : 0;
		if (isAttachedToBlock() && target == 0 && piston.getChaseTarget() == 1)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> playSound(false));
		piston.chase(target, .4f, Chaser.LINEAR);

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> VisualizationManager.queueUpdate(this));
	}

	public boolean isAttachedToBlock() {
		BlockState blockState = getBlockState();
		if (!AllBlocks.STICKER.has(blockState))
			return false;
		Direction direction = blockState.getValue(StickerBlock.FACING);
		return SuperGlueEntity.isValidFace(level, worldPosition.relative(direction), direction.getOpposite());
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		if (clientPacket)
			update = true;
	}

	@OnlyIn(Dist.CLIENT)
	public void playSound(boolean attach) {
		AllSoundEvents.SLIME_ADDED.play(level, Minecraft.getInstance().player, worldPosition, 0.35f, attach ? 0.75f : 0.2f);
	}


}
