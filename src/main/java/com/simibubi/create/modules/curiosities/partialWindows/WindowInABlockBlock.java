package com.simibubi.create.modules.curiosities.partialWindows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.simibubi.create.foundation.block.IHaveColorHandler;
import com.simibubi.create.foundation.block.IHaveCustomBlockModel;
import com.simibubi.create.foundation.block.IHaveNoBlockItem;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FourWayBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext.Builder;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WindowInABlockBlock extends PaneBlock
		implements ITE<WindowInABlockTileEntity>, IHaveNoBlockItem, IHaveCustomBlockModel, IHaveColorHandler {

	public WindowInABlockBlock() {
		super(Properties.create(Material.ROCK));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new WindowInABlockTileEntity();
	}

	@Override
	public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player,
			boolean willHarvest, IFluidState fluid) {
		if (player == null)
			return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);

		Vec3d start = player.getEyePosition(1);
		Vec3d end = start.add(player.getLookVec().scale(player.getAttribute(PlayerEntity.REACH_DISTANCE).getValue()));
		BlockRayTraceResult target =
			world.rayTraceBlocks(new RayTraceContext(start, end, BlockMode.OUTLINE, FluidMode.NONE, player));
		if (target == null || target.getHitVec() == null)
			return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);

		try {
			WindowInABlockTileEntity tileEntity = getTileEntity(world, pos);
			BlockState windowBlock = tileEntity.getWindowBlock();
			for (AxisAlignedBB bb : windowBlock.getShape(world, pos).toBoundingBoxList()) {
				if (bb.grow(.1d).contains(target.getHitVec().subtract(new Vec3d(pos)))) {
					windowBlock.getBlock().onBlockHarvested(world, pos, windowBlock, player);
					Block.spawnDrops(windowBlock, world, pos, null, player, player.getHeldItemMainhand());
					BlockState partialBlock = tileEntity.getPartialBlock();
					world.setBlockState(pos, partialBlock);
					for (Direction d : Direction.values()) {
						BlockPos offset = pos.offset(d);
						BlockState otherState = world.getBlockState(offset);
						partialBlock = partialBlock.updatePostPlacement(d, otherState, world, pos, offset);
						world.notifyBlockUpdate(offset, otherState, otherState, 2);
					}
					if (partialBlock != world.getBlockState(pos))
						world.setBlockState(pos, partialBlock);
					return false;
				}
			}
		} catch (TileEntityException e) {}

		return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
	}

	@Override
	public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
		return true;
	}

	@Override
	public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
		return false;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
		return getSurroundingBlockState(reader, pos).propagatesSkylightDown(reader, pos);
	}

	@Override
	public boolean collisionExtendsVertically(BlockState state, IBlockReader world, BlockPos pos,
			Entity collidingEntity) {
		return getSurroundingBlockState(world, pos).collisionExtendsVertically(world, pos, collidingEntity);
	}

	@Override
	public float getBlockHardness(BlockState blockState, IBlockReader worldIn, BlockPos pos) {
		return getSurroundingBlockState(worldIn, pos).getBlockHardness(worldIn, pos);
	}

	@Override
	public float getExplosionResistance(BlockState state, IWorldReader world, BlockPos pos, Entity exploder,
			Explosion explosion) {
		return getSurroundingBlockState(world, pos).getExplosionResistance(world, pos, exploder, explosion);
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos,
			PlayerEntity player) {
		BlockState window = getWindowBlockState(world, pos);
		for (AxisAlignedBB bb : window.getShape(world, pos).toBoundingBoxList()) {
			if (bb.grow(.1d).contains(target.getHitVec().subtract(new Vec3d(pos))))
				return window.getPickBlock(target, world, pos, player);
		}
		BlockState surrounding = getSurroundingBlockState(world, pos);
		return surrounding.getPickBlock(target, world, pos, player);
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, Builder builder) {
		TileEntity tileentity = builder.get(LootParameters.BLOCK_ENTITY);
		if (!(tileentity instanceof WindowInABlockTileEntity))
			return Collections.emptyList();

		WindowInABlockTileEntity te = (WindowInABlockTileEntity) tileentity;
		List<ItemStack> drops = te.getPartialBlock().getDrops(builder);
		drops.addAll(te.getWindowBlock().getDrops(builder));
		return drops;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		VoxelShape shape1 = getSurroundingBlockState(worldIn, pos).getShape(worldIn, pos, context);
		VoxelShape shape2 = getWindowBlockState(worldIn, pos).getShape(worldIn, pos, context);
		return VoxelShapes.or(shape1, shape2);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos,
			ISelectionContext context) {
		return getShape(state, worldIn, pos, context);
	}

	@SuppressWarnings("deprecation")
	@Override
	public MaterialColor getMaterialColor(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return getSurroundingBlockState(worldIn, pos).getMaterialColor(worldIn, pos);
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		withTileEntityDo(worldIn, currentPos, te -> {
			te.setWindowBlock(
					te.getWindowBlock().updatePostPlacement(facing, facingState, worldIn, currentPos, facingPos));
			BlockState blockState =
				te.getPartialBlock().updatePostPlacement(facing, facingState, worldIn, currentPos, facingPos);
			if (blockState.getBlock() instanceof FourWayBlock) {
				for (BooleanProperty side : Arrays.asList(FourWayBlock.EAST, FourWayBlock.NORTH, FourWayBlock.SOUTH,
						FourWayBlock.WEST))
					blockState = blockState.with(side, false);
				te.setPartialBlock(blockState);
			}
			te.requestModelDataUpdate();
		});

		return stateIn;
	}

	private BlockState getSurroundingBlockState(IBlockReader reader, BlockPos pos) {
		try {
			return getTileEntity(reader, pos).getPartialBlock();
		} catch (TileEntityException e) {}
		return Blocks.AIR.getDefaultState();
	}

	private BlockState getWindowBlockState(IBlockReader reader, BlockPos pos) {
		try {
			return getTileEntity(reader, pos).getWindowBlock();
		} catch (TileEntityException e) {}
		return Blocks.AIR.getDefaultState();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public IBakedModel createModel(IBakedModel original) {
		return new WindowInABlockModel(original);
	}

	@Override
	public Class<WindowInABlockTileEntity> getTileEntityClass() {
		return WindowInABlockTileEntity.class;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public IBlockColor getColorHandler() {
		return (state, world, pos, index) -> {
			try {
				BlockState surrounding = getSurroundingBlockState(world, pos);
				return Minecraft.getInstance().getBlockColors().getColor(surrounding, world, pos, index);
			} catch (Exception e) {}
			return -1;
		};
	}

}
