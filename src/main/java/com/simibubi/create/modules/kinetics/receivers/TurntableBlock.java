package com.simibubi.create.modules.kinetics.receivers;

import com.simibubi.create.modules.kinetics.base.KineticBlock;
import com.simibubi.create.modules.kinetics.base.KineticTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
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
		super(Properties.create(Material.ROCK));
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
	public void onLanded(IBlockReader worldIn, Entity e) {
		TileEntity te = worldIn.getTileEntity(e.getPosition());
		if (!(te instanceof KineticTileEntity))
			return;

		float speed = ((KineticTileEntity) te).getSpeed() / 20;
		World world = e.getEntityWorld();

		if (speed == 0) {
			super.onLanded(worldIn, e);
			return;
		}
		if (world.isRemote) {
			super.onLanded(worldIn, e);
			return;
		}
		if ((e instanceof PlayerEntity)) {
			super.onLanded(worldIn, e);
			return;
		}
		if ((e instanceof LivingEntity)) {
			float offset = e.getRotationYawHead() - speed;
			e.setRenderYawOffset(offset);
			e.setRotationYawHead(offset);
			super.onLanded(worldIn, e);
			return;
		}

		e.rotationYaw -= speed;

		super.onLanded(worldIn, e);
	}

	// IRotate:

	@Override
	public boolean isAxisTowards(World world, BlockPos pos, BlockState state, Direction face) {
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
