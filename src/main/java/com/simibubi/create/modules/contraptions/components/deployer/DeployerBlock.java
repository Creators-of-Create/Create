package com.simibubi.create.modules.contraptions.components.deployer;

import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.base.DirectionalAxisKineticBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class DeployerBlock extends DirectionalAxisKineticBlock implements IWithTileEntity<DeployerTileEntity> {

	public DeployerBlock() {
		super(Properties.from(Blocks.ANDESITE));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new DeployerTileEntity();
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.SHORT_CASING_12_VOXEL.get(state.get(FACING));
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		if (context.getFace() == state.get(FACING)) {
			if (!context.getWorld().isRemote)
				withTileEntityDo(context.getWorld(), context.getPos(), DeployerTileEntity::changeMode);
			return ActionResultType.SUCCESS;
		}
		return super.onWrenched(state, context);
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		if (hit.getFace() == state.get(FACING)) {
			if (!worldIn.isRemote)
				withTileEntityDo(worldIn, pos, te -> {
					ItemStack heldItemMainhand = te.player.getHeldItemMainhand();
					if (heldItemMainhand.isEmpty())
						return;
					player.inventory.placeItemBackInInventory(worldIn, heldItemMainhand);
					te.player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
				});
			return true;
		}
		return false;
	}

	public static Vec3d getFilterSlotPosition(BlockState state) {
		Direction facing = state.get(FACING);
		Vec3d vec = VecHelper.voxelSpace(8f, 13.5f, 11.5f);

		float yRot = AngleHelper.horizontalAngle(facing);
		float zRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;
		vec = VecHelper.rotateCentered(vec, yRot, Axis.Y);
		vec = VecHelper.rotateCentered(vec, zRot, Axis.Z);

		return vec;
	}

	public static Vec3d getFilterSlotOrientation(BlockState state) {
		Direction facing = state.get(FACING);
		float yRot = AngleHelper.horizontalAngle(facing) + 180;
		float zRot = facing == Direction.UP ? 90 : facing == Direction.DOWN ? 270 : 0;
		return new Vec3d(0, yRot, zRot);
	}

}
