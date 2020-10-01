package com.simibubi.create.content.contraptions.processing;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;

public class BasinMovementBehaviour extends MovementBehaviour {
	private static final Object NO_OR_EMPTY_BASIN = new Object();

	@Override
	public boolean hasSpecialMovementRenderer() {
		return false;
	}

	@Override
	public void tick(MovementContext context) {
		super.tick(context);
		if (context.temporaryData != NO_OR_EMPTY_BASIN) {
			Vec3d facingVec = VecHelper.rotate(new Vec3d(Direction.UP.getDirectionVec()), context.rotation.x, context.rotation.y, context.rotation.z);
			facingVec.normalize();
			if (Direction.getFacingFromVector(facingVec.x, facingVec.y, facingVec.z) == Direction.DOWN) {
				dump(context, facingVec);
			}
		}
	}

	private void dump(MovementContext context, Vec3d facingVec) {
		BasinTileEntity te = getOrCreate(context);
		if (te == null)
			return;
		te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(itemStackHandler -> {
				if (!(itemStackHandler instanceof IItemHandlerModifiable))
					return;
				for (int i = 0; i < itemStackHandler.getSlots(); i++) {
					if (itemStackHandler.getStackInSlot(i).isEmpty())
						continue;
					ItemEntity itemEntity = new ItemEntity(context.world, context.position.x, context.position.y, context.position.z, itemStackHandler.getStackInSlot(i));
					itemEntity.setMotion(facingVec.scale(.05));
					context.world.addEntity(itemEntity);
					((IItemHandlerModifiable) itemStackHandler).setStackInSlot(i, ItemStack.EMPTY);
				}
				te.write(context.tileData);
				context.temporaryData = NO_OR_EMPTY_BASIN;
			}
		);
	}

	@Nullable
	private BasinTileEntity getOrCreate(MovementContext context) {
		if (!(context.temporaryData instanceof BasinTileEntity || context.temporaryData == NO_OR_EMPTY_BASIN)) {
			if (context.contraption.customRenderTEs.isEmpty()) {
				// customRenderTEs are sometimes completely empty? Probably a server thing
				context.tileData.putInt("x", context.localPos.getX());
				context.tileData.putInt("y", context.localPos.getY());
				context.tileData.putInt("z", context.localPos.getZ());
				TileEntity te = TileEntity.create(context.tileData);
				if (te == null) {
					context.temporaryData = NO_OR_EMPTY_BASIN;
					return null;
				}
				te.setLocation(new WrappedWorld(context.world) {
					@Override
					public BlockState getBlockState(BlockPos pos) {
						if (!pos.equals(te.getPos()))
							return Blocks.AIR.getDefaultState();
						return context.state;
					}
				}, te.getPos());
				if (te instanceof KineticTileEntity)
					((KineticTileEntity) te).setSpeed(0);
				te.getBlockState();
				context.temporaryData = te;
			} else {
				for (TileEntity te : context.contraption.customRenderTEs) {
					if (te instanceof BasinTileEntity && te.getPos().equals(context.localPos)) {
						context.temporaryData = te;
						return ((BasinTileEntity) te);
					}
				}
				context.temporaryData = NO_OR_EMPTY_BASIN;
			}
		}
		if (!(context.temporaryData instanceof BasinTileEntity))
			return null;
		return ((BasinTileEntity) context.temporaryData);
	}
}
