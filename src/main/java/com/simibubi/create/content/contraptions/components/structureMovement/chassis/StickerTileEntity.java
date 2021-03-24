package com.simibubi.create.content.contraptions.components.structureMovement.chassis;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueItem;
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import com.simibubi.create.foundation.render.backend.instancing.IInstanceRendered;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

import java.util.List;

public class StickerTileEntity extends SmartTileEntity implements IInstanceRendered {

	LerpedFloat piston;
	boolean update;

	public StickerTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		piston = LerpedFloat.linear();
		update = false;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	@Override
	public void initialize() {
		super.initialize();
		if (!world.isRemote)
			return;
		piston.startWithValue(isBlockStateExtended() ? 1 : 0);
	}

	public boolean isBlockStateExtended() {
		BlockState blockState = getBlockState();
		boolean extended = AllBlocks.STICKER.has(blockState) && blockState.get(StickerBlock.EXTENDED);
		return extended;
	}

	@Override
	public void tick() {
		super.tick();
		if (!world.isRemote)
			return;
		piston.tickChaser();

		if (isAttachedToBlock() && piston.getValue(0) != piston.getValue() && piston.getValue() == 1) {
			SuperGlueItem.spawnParticles(world, pos, getBlockState().get(StickerBlock.FACING), true);
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> playSound(true));
		}

		if (!update)
			return;
		update = false;
		int target = isBlockStateExtended() ? 1 : 0;
		if (isAttachedToBlock() && target == 0 && piston.getChaseTarget() == 1)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> playSound(false));
		piston.chase(target, .4f, Chaser.LINEAR);

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FastRenderDispatcher.enqueueUpdate(this));
	}

	public boolean isAttachedToBlock() {
		BlockState blockState = getBlockState();
		if (!AllBlocks.STICKER.has(blockState))
			return false;
		Direction direction = blockState.get(StickerBlock.FACING);
		return SuperGlueEntity.isValidFace(world, pos.offset(direction), direction.getOpposite());
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		if (clientPacket)
			update = true;
	}

	@OnlyIn(Dist.CLIENT)
	public void playSound(boolean attach) {
		world.playSound(Minecraft.getInstance().player, pos, AllSoundEvents.SLIME_ADDED.get(), SoundCategory.BLOCKS,
			0.35F, attach ? 0.75F : 0.2f);
	}

}
