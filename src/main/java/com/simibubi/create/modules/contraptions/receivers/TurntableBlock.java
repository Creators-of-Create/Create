package com.simibubi.create.modules.contraptions.receivers;

import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.base.KineticBlock;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class TurntableBlock extends KineticBlock {

	protected static final VoxelShape SHAPE = VoxelShapes.or(
			Block.makeCuboidShape(1.0D, 6.0D, 1.0D, 15.0D, 8.0D, 15.0D),
			Block.makeCuboidShape(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D));

	public TurntableBlock() {
		super(Properties.from(Blocks.STRIPPED_SPRUCE_LOG));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new TurntableTileEntity();
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity e) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (!(te instanceof KineticTileEntity))
			return;
		if (!e.onGround)
			return;
		if (e.getMotion().y > 0)
			return;

		float speed = ((KineticTileEntity) te).getSpeed() / 20;
		World world = e.getEntityWorld();

		if (speed == 0)
			return;
		if (e.posY < pos.getY() + .5f)
			return;

		Vec3d origin = VecHelper.getCenterOf(pos);
		Vec3d offset = e.getPositionVec().subtract(origin);

		if (!world.isRemote && (e instanceof PlayerEntity))
			return;

		if (offset.length() > 1 / 4f) {
			offset = VecHelper.rotate(offset, speed / 1f, Axis.Y);
			Vec3d movement = origin.add(offset).subtract(e.getPositionVec());
			e.setMotion(e.getMotion().add(movement));
			e.velocityChanged = true;
		}

		if (world.isRemote)
			return;
		if ((e instanceof PlayerEntity))
			return;
		if ((e instanceof LivingEntity)) {
			float diff = e.getRotationYawHead() - speed;
			((LivingEntity) e).setIdleTime(20);
			e.setRenderYawOffset(diff);
			e.setRotationYawHead(diff);
			return;
		}

		e.rotationYaw -= speed;

	}

	// IRotate:

	@Override
	public boolean hasShaftTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return face == Direction.DOWN;
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return Axis.Y;
	}

	@Override
	protected boolean hasStaticPart() {
		return false;
	}

}
