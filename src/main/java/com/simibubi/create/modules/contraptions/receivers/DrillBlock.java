package com.simibubi.create.modules.contraptions.receivers;

import java.util.List;

import com.simibubi.create.modules.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.modules.contraptions.base.IRotate;
import com.simibubi.create.modules.contraptions.receivers.constructs.IHaveMovementBehavior;
import com.simibubi.create.modules.contraptions.receivers.constructs.MechanicalPistonTileEntity;
import com.simibubi.create.modules.contraptions.relays.ShaftBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class DrillBlock extends DirectionalKineticBlock implements IHaveMovementBehavior {

	protected static final VoxelShape CORE_SHAPE = makeCuboidShape(3, 3, 3, 13, 13, 13),
			DRILL_SHAPE_X = VoxelShapes.or(CORE_SHAPE, ShaftBlock.AXIS_X),
			DRILL_SHAPE_Y = VoxelShapes.or(CORE_SHAPE, ShaftBlock.AXIS_Y),
			DRILL_SHAPE_Z = VoxelShapes.or(CORE_SHAPE, ShaftBlock.AXIS_Z);

	public static final BooleanProperty FIXATED = BooleanProperty.create("fixated");

	public DrillBlock() {
		super(Properties.from(Blocks.IRON_BLOCK));
		setDefaultState(getDefaultState().with(FIXATED, true));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(FIXATED);
		super.fillStateContainer(builder);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return !state.get(FIXATED);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new DrillTileEntity();
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		Axis axis = state.get(FACING).getAxis();

		if (axis == Axis.X)
			return DRILL_SHAPE_X;
		if (axis == Axis.Y)
			return DRILL_SHAPE_Y;
		if (axis == Axis.Z)
			return DRILL_SHAPE_Z;

		return CORE_SHAPE;
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(FACING).getAxis();
	}

	@Override
	public boolean hasShaftTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return !state.get(FIXATED) && face == state.get(FACING).getOpposite();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return super.getStateForPlacement(context).with(FIXATED,
				!canConnectTo(context.getWorld(), context.getPos(), context.getFace().getOpposite()));
	}

	private boolean canConnectTo(IWorld world, BlockPos pos, Direction direction) {
		BlockPos offsetPos = pos.offset(direction);
		BlockState blockStateAttached = world.getBlockState(offsetPos);
		if (blockStateAttached.getBlock() instanceof IRotate && ((IRotate) blockStateAttached.getBlock())
				.hasShaftTowards(world.getWorld(), offsetPos, blockStateAttached, direction.getOpposite())) {
			return true;
		}
		return false;
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return state.get(FIXATED) ? PushReaction.NORMAL : PushReaction.BLOCK;
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		if (facing == stateIn.get(FACING).getOpposite()) {
			boolean connected = canConnectTo(worldIn, currentPos, facing);
			boolean fixated = stateIn.get(FIXATED);

			if (!fixated && !connected)
				worldIn.getWorld().removeTileEntity(currentPos);

			return stateIn.with(FIXATED, !connected);
		}

		if (facing != stateIn.get(FACING))
			return stateIn;

		DrillTileEntity te = (DrillTileEntity) worldIn.getTileEntity(currentPos);
		if (te != null)
			te.destroyNextTick();

		return stateIn;
	}

	@Override
	public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
		return state.get(FIXATED) && layer == BlockRenderLayer.SOLID;
	}

	@Override
	protected boolean hasStaticPart() {
		return false;
	}

	@Override
	public IMovementContext visitPosition(World world, BlockPos pos, BlockState block, Direction movement,
			MechanicalPistonTileEntity piston) {
		IMovementContext context = IdleMovementContext.INSTANCE;
		
		if (movement != block.get(FACING))
			return context;

		pos = pos.offset(movement);
		BlockState stateVisited = world.getBlockState(pos);

		if (stateVisited.getCollisionShape(world, pos).isEmpty())
			return context;
		if (stateVisited.getBlockHardness(world, pos) == -1)
			return context;

		world.playEvent(2001, pos, Block.getStateId(stateVisited));
		List<ItemStack> drops = Block.getDrops(stateVisited, (ServerWorld) world, pos, null);
		world.setBlockState(pos, Blocks.AIR.getDefaultState());

		for (ItemStack stack : drops) {
			ItemEntity itemEntity = new ItemEntity(world, pos.getX() + .5f, pos.getY() + .25f, pos.getZ() + .5f, stack);
			itemEntity.setMotion(
					new Vec3d(movement.getDirectionVec()).add(0, 0.5f, 0).scale(world.rand.nextFloat() * .3f));
			world.addEntity(itemEntity);
		}

		return context;
	}

}
