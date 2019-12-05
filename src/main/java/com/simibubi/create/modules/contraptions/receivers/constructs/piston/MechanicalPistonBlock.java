package com.simibubi.create.modules.contraptions.receivers.constructs.piston;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateConfig;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.base.DirectionalAxisKineticBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

public class MechanicalPistonBlock extends DirectionalAxisKineticBlock {

	public static final EnumProperty<PistonState> STATE = EnumProperty.create("state", PistonState.class);

	protected static final VoxelShape BASE_SHAPE_UP = makeCuboidShape(0, 0, 0, 16, 12, 16),
			BASE_SHAPE_DOWN = makeCuboidShape(0, 4, 0, 16, 16, 16),
			BASE_SHAPE_EAST = makeCuboidShape(0, 0, 0, 12, 16, 16),
			BASE_SHAPE_WEST = makeCuboidShape(4, 0, 0, 16, 16, 16),
			BASE_SHAPE_SOUTH = makeCuboidShape(0, 0, 0, 16, 16, 12),
			BASE_SHAPE_NORTH = makeCuboidShape(0, 0, 4, 16, 16, 16),

			EXTENDED_SHAPE_UP = VoxelShapes.or(BASE_SHAPE_UP, MechanicalPistonHeadBlock.AXIS_SHAPE_Y),
			EXTENDED_SHAPE_DOWN = VoxelShapes.or(BASE_SHAPE_DOWN, MechanicalPistonHeadBlock.AXIS_SHAPE_Y),
			EXTENDED_SHAPE_EAST = VoxelShapes.or(BASE_SHAPE_EAST, MechanicalPistonHeadBlock.AXIS_SHAPE_X),
			EXTENDED_SHAPE_WEST = VoxelShapes.or(BASE_SHAPE_WEST, MechanicalPistonHeadBlock.AXIS_SHAPE_X),
			EXTENDED_SHAPE_SOUTH = VoxelShapes.or(BASE_SHAPE_SOUTH, MechanicalPistonHeadBlock.AXIS_SHAPE_Z),
			EXTENDED_SHAPE_NORTH = VoxelShapes.or(BASE_SHAPE_NORTH, MechanicalPistonHeadBlock.AXIS_SHAPE_Z);

	protected boolean isSticky;

	public MechanicalPistonBlock(boolean sticky) {
		super(Properties.from(Blocks.PISTON));
		setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(STATE, PistonState.RETRACTED));
		isSticky = sticky;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(STATE);
		super.fillStateContainer(builder);
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		if (state.get(STATE) != PistonState.RETRACTED)
			return false;
		if (!player.isAllowEdit())
			return false;
		if (!player.getHeldItem(handIn).getItem().isIn(Tags.Items.SLIMEBALLS))
			return false;
		Direction direction = state.get(FACING);
		if (hit.getFace() != direction)
			return false;
		if (((MechanicalPistonBlock) state.getBlock()).isSticky)
			return false;
		if (worldIn.isRemote) {
			Vec3d vec = hit.getHitVec();
			worldIn.addParticle(ParticleTypes.ITEM_SLIME, vec.x, vec.y, vec.z, 0, 0, 0);
			return true;
		}
		worldIn.playSound(null, pos, SoundEvents.BLOCK_SLIME_BLOCK_PLACE, SoundCategory.BLOCKS, .5f, 1);
		if (!player.isCreative())
			player.getHeldItem(handIn).shrink(1);
		worldIn.setBlockState(pos, AllBlocks.STICKY_MECHANICAL_PISTON.get().getDefaultState().with(FACING, direction)
				.with(AXIS_ALONG_FIRST_COORDINATE, state.get(AXIS_ALONG_FIRST_COORDINATE)));
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new MechanicalPistonTileEntity();
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}

	public enum PistonState implements IStringSerializable {
		RETRACTED, MOVING, EXTENDED;

		@Override
		public String getName() {
			return Lang.asId(name());
		}
	}

	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
		Direction direction = state.get(FACING);
		BlockPos pistonHead = null;
		BlockPos pistonBase = pos;
		boolean dropBlocks = player == null || !player.isCreative();

		Integer maxPoles = CreateConfig.parameters.maxPistonPoles.get();
		for (int offset = 1; offset < maxPoles; offset++) {
			BlockPos currentPos = pos.offset(direction, offset);
			BlockState block = worldIn.getBlockState(currentPos);

			if (AllBlocks.PISTON_POLE.typeOf(block)
					&& direction.getAxis() == block.get(BlockStateProperties.FACING).getAxis())
				continue;

			if (AllBlocks.MECHANICAL_PISTON_HEAD.typeOf(block) && block.get(BlockStateProperties.FACING) == direction) {
				pistonHead = currentPos;
			}

			break;
		}

		if (pistonHead != null && pistonBase != null) {
			BlockPos.getAllInBox(pistonBase, pistonHead).filter(p -> !p.equals(pos))
					.forEach(p -> worldIn.destroyBlock(p, dropBlocks));
		}

		for (int offset = 1; offset < maxPoles; offset++) {
			BlockPos currentPos = pos.offset(direction.getOpposite(), offset);
			BlockState block = worldIn.getBlockState(currentPos);

			if (AllBlocks.PISTON_POLE.typeOf(block)
					&& direction.getAxis() == block.get(BlockStateProperties.FACING).getAxis()) {
				worldIn.destroyBlock(currentPos, dropBlocks);
				continue;
			}

			break;
		}

		super.onBlockHarvested(worldIn, pos, state, player);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {

		if (state.get(STATE) == PistonState.EXTENDED)
			switch (state.get(FACING)) {
			case DOWN:
				return EXTENDED_SHAPE_DOWN;
			case EAST:
				return EXTENDED_SHAPE_EAST;
			case NORTH:
				return EXTENDED_SHAPE_NORTH;
			case SOUTH:
				return EXTENDED_SHAPE_SOUTH;
			case UP:
				return EXTENDED_SHAPE_UP;
			case WEST:
				return EXTENDED_SHAPE_WEST;
			}

		if (state.get(STATE) == PistonState.MOVING)
			switch (state.get(FACING)) {
			case DOWN:
				return BASE_SHAPE_DOWN;
			case EAST:
				return BASE_SHAPE_EAST;
			case NORTH:
				return BASE_SHAPE_NORTH;
			case SOUTH:
				return BASE_SHAPE_SOUTH;
			case UP:
				return BASE_SHAPE_UP;
			case WEST:
				return BASE_SHAPE_WEST;
			}

		return VoxelShapes.fullCube();
	}

}
