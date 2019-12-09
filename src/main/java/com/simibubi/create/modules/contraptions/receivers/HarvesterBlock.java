package com.simibubi.create.modules.contraptions.receivers;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.IRenderUtilityBlock;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.VoxelShapers;
import com.simibubi.create.modules.contraptions.receivers.constructs.IHaveMovementBehavior;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IPlantable;

public class HarvesterBlock extends HorizontalBlock implements IHaveMovementBehavior {

	public HarvesterBlock() {
		super(Properties.from(Blocks.IRON_BLOCK));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new HarvesterTileEntity();
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.PUSH_ONLY;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		Direction direction = state.get(HORIZONTAL_FACING);
		return VoxelShapers.HARVESTER_BASE.get(direction);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING);
		super.fillStateContainer(builder);
	}

	@Override
	@OnlyIn(value = Dist.CLIENT)
	public SuperByteBuffer renderInContraption(MovementContext context) {
		return HarvesterTileEntityRenderer.renderInContraption(context);
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		Direction direction = state.get(HORIZONTAL_FACING);
		BlockPos offset = pos.offset(direction.getOpposite());
		return Block.hasSolidSide(worldIn.getBlockState(offset), worldIn, offset, direction);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction facing;
		if (context.getFace().getAxis().isVertical())
			facing = context.getPlacementHorizontalFacing().getOpposite();
		else {
			BlockState blockState = context.getWorld()
					.getBlockState(context.getPos().offset(context.getFace().getOpposite()));
			if (AllBlocks.HARVESTER.typeOf(blockState))
				facing = blockState.get(HORIZONTAL_FACING);
			else
				facing = context.getFace();
		}
		return getDefaultState().with(HORIZONTAL_FACING, facing);
	}

	@Override
	public void visitPosition(MovementContext context) {
		Direction movement = context.getMovementDirection();
		World world = context.world;
		BlockState block = context.state;
		BlockPos pos = context.currentGridPos;

		if (movement != block.get(HORIZONTAL_FACING))
			return;

		BlockState stateVisited = world.getBlockState(pos);
		boolean notCropButCuttable = false;

		if (stateVisited.getBlock() == Blocks.SUGAR_CANE) {
			notCropButCuttable = true;
			pos = pos.up();
			stateVisited = world.getBlockState(pos);
		}

		if (!isValidCrop(world, pos, stateVisited)) {
			if (isValidOther(world, pos, stateVisited))
				notCropButCuttable = true;
			else
				return;
		}

		List<ItemStack> drops = Block.getDrops(stateVisited, (ServerWorld) world, pos, null);
		world.playEvent(2001, pos, Block.getStateId(stateVisited));
		world.setBlockState(pos, cutCrop(world, pos, stateVisited));

		boolean seedSubtracted = notCropButCuttable;
		for (ItemStack stack : drops) {
			if (!seedSubtracted && stack.isItemEqual(new ItemStack(stateVisited.getBlock()))) {
				stack.shrink(1);
				seedSubtracted = true;
			}
			ItemEntity itemEntity = new ItemEntity(world, pos.getX() + .5f, pos.getY() + .25f, pos.getZ() + .5f, stack);
			itemEntity.setMotion(
					new Vec3d(movement.getDirectionVec()).add(0, 0.5f, 0).scale(world.rand.nextFloat() * .3f));
			world.addEntity(itemEntity);
		}
	}

	private boolean isValidCrop(World world, BlockPos pos, BlockState state) {
		if (state.getBlock() instanceof CropsBlock) {
			CropsBlock crop = (CropsBlock) state.getBlock();
			if (!crop.isMaxAge(state))
				return false;
			return true;
		}
		if (state.getCollisionShape(world, pos).isEmpty()) {
			for (IProperty<?> property : state.getProperties()) {
				if (!(property instanceof IntegerProperty))
					continue;
				if (!property.getName().equals(BlockStateProperties.AGE_0_1.getName()))
					continue;
				if (((IntegerProperty) property).getAllowedValues().size() - 1 != state.get((IntegerProperty) property)
						.intValue())
					continue;
				return true;
			}
		}

		return false;
	}

	private boolean isValidOther(World world, BlockPos pos, BlockState state) {
		if (state.getBlock() instanceof CropsBlock)
			return false;
		if (state.getBlock() instanceof SugarCaneBlock)
			return true;

		if (state.getCollisionShape(world, pos).isEmpty()) {
			for (IProperty<?> property : state.getProperties()) {
				if (!(property instanceof IntegerProperty))
					continue;
				if (!property.getName().equals(BlockStateProperties.AGE_0_1.getName()))
					continue;
				return false;
			}

			if (state.getBlock() instanceof IPlantable)
				return true;
		}

		return false;
	}

	private BlockState cutCrop(World world, BlockPos pos, BlockState state) {
		if (state.getBlock() instanceof CropsBlock) {
			CropsBlock crop = (CropsBlock) state.getBlock();
			return crop.withAge(0);
		}
		if (state.getBlock() == Blocks.SUGAR_CANE) {
			return Blocks.AIR.getDefaultState();
		}
		if (state.getCollisionShape(world, pos).isEmpty()) {
			for (IProperty<?> property : state.getProperties()) {
				if (!(property instanceof IntegerProperty))
					continue;
				if (!property.getName().equals(BlockStateProperties.AGE_0_1.getName()))
					continue;
				return state.with((IntegerProperty) property, Integer.valueOf(0));
			}
		}

		return Blocks.AIR.getDefaultState();
	}

	public static class HarvesterBladeBlock extends HorizontalBlock implements IRenderUtilityBlock {

		public HarvesterBladeBlock() {
			super(Properties.from(Blocks.AIR));
		}

		@Override
		protected void fillStateContainer(Builder<Block, BlockState> builder) {
			builder.add(HORIZONTAL_FACING);
			super.fillStateContainer(builder);
		}

		@Override
		public BlockRenderLayer getRenderLayer() {
			return BlockRenderLayer.SOLID;
		}

	}

}
