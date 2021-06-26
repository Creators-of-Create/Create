package com.simibubi.create.content.logistics.block.redstone;

import java.util.Random;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class NixieTubeBlock extends HorizontalBlock implements ITE<NixieTubeTileEntity> {

	public static final BooleanProperty CEILING = BooleanProperty.create("ceiling");

	public NixieTubeBlock(Properties properties) {
		super(properties);
		setDefaultState(getDefaultState().with(CEILING, false));
	}

	@Override
	public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult ray) {

		ItemStack heldItem = player.getHeldItem(hand);
		NixieTubeTileEntity nixie = getTileEntity(world, pos);

		if (nixie == null)
			return ActionResultType.PASS;
		if (player.isSneaking())
			return ActionResultType.PASS;

		if (heldItem.isEmpty()) {
			if (nixie.reactsToRedstone())
				return ActionResultType.PASS;
			nixie.clearCustomText();
			updateDisplayedRedstoneValue(state, world, pos);
			return ActionResultType.SUCCESS;
		}

		if (heldItem.getItem() == Items.NAME_TAG && heldItem.hasDisplayName()) {
			Direction left = state.get(HORIZONTAL_FACING)
					.rotateY();
			Direction right = left.getOpposite();

			if (world.isRemote)
				return ActionResultType.SUCCESS;

			BlockPos currentPos = pos;
			while (true) {
				BlockPos nextPos = currentPos.offset(left);
				if (world.getBlockState(nextPos) != state)
					break;
				currentPos = nextPos;
			}

			int index = 0;

			while (true) {
				final int rowPosition = index;
				withTileEntityDo(world, currentPos, te -> te.displayCustomNameOf(heldItem, rowPosition));
				BlockPos nextPos = currentPos.offset(right);
				if (world.getBlockState(nextPos) != state)
					break;
				currentPos = nextPos;
				index++;
			}
		}

		return ActionResultType.PASS;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder.add(CEILING, HORIZONTAL_FACING));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return (state.get(CEILING) ? AllShapes.NIXIE_TUBE_CEILING : AllShapes.NIXIE_TUBE)
			.get(state.get(HORIZONTAL_FACING)
				.getAxis());
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockPos pos = context.getPos();
		boolean ceiling = context.getFace() == Direction.DOWN;
		Vector3d hitVec = context.getHitVec();
		if (hitVec != null)
			ceiling = hitVec.y - pos.getY() > .5f;
		return getDefaultState().with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing()
			.getOpposite())
			.with(CEILING, ceiling);
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block p_220069_4_, BlockPos p_220069_5_,
		boolean p_220069_6_) {
		if (worldIn.isRemote)
			return;
		if (!worldIn.getPendingBlockTicks()
			.isTickPending(pos, this))
			worldIn.getPendingBlockTicks()
				.scheduleTick(pos, this, 0);
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random r) {
		updateDisplayedRedstoneValue(state, worldIn, pos);
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (state.getBlock() == oldState.getBlock() || isMoving)
			return;
		updateDisplayedRedstoneValue(state, worldIn, pos);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new NixieTubeTileEntity(AllTileEntities.NIXIE_TUBE.get());
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	private void updateDisplayedRedstoneValue(BlockState state, World worldIn, BlockPos pos) {
		if (worldIn.isRemote)
			return;
		withTileEntityDo(worldIn, pos, te -> {
			if (te.reactsToRedstone())
				te.displayRedstoneStrength(getPower(worldIn, pos));
		});
	}

	static boolean isValidBlock(IBlockReader world, BlockPos pos, boolean above) {
		BlockState state = world.getBlockState(pos.up(above ? 1 : -1));
		return !state.getShape(world, pos)
			.isEmpty();
	}

	private int getPower(World worldIn, BlockPos pos) {
		int power = 0;
		for (Direction direction : Iterate.directions)
			power = Math.max(worldIn.getRedstonePower(pos.offset(direction), direction), power);
		for (Direction direction : Iterate.directions)
			power = Math.max(worldIn.getRedstonePower(pos.offset(direction), Direction.UP), power);
		return power;
	}

	@Override
	public boolean allowsMovement(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		return side != null;
	}

	@Override
	public Class<NixieTubeTileEntity> getTileEntityClass() {
		return NixieTubeTileEntity.class;
	}

}
