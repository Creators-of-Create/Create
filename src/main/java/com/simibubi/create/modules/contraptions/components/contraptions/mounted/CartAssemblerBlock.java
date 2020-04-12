package com.simibubi.create.modules.contraptions.components.contraptions.mounted;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.RenderUtilityBlock;
import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.modules.contraptions.components.contraptions.Contraption;
import com.simibubi.create.modules.contraptions.components.contraptions.ContraptionEntity;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class CartAssemblerBlock extends AbstractRailBlock {

	public static IProperty<RailShape> RAIL_SHAPE =
		EnumProperty.create("shape", RailShape.class, RailShape.EAST_WEST, RailShape.NORTH_SOUTH);
	public static BooleanProperty POWERED = BlockStateProperties.POWERED;

	public CartAssemblerBlock() {
		super(true, Properties.from(Blocks.PISTON));
		setDefaultState(getDefaultState().with(POWERED, false));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(RAIL_SHAPE, POWERED);
		super.fillStateContainer(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		boolean alongX = context.getPlacementHorizontalFacing().getAxis() == Axis.X;
		boolean powered = context.getWorld().isBlockPowered(context.getPos());
		return super.getStateForPlacement(context).with(POWERED, Boolean.valueOf(powered)).with(RAIL_SHAPE,
				alongX ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH);
	}

	@Override
	public boolean canMakeSlopes(BlockState state, IBlockReader world, BlockPos pos) {
		return false;
	}

	@Override
	public void onMinecartPass(BlockState state, World world, BlockPos pos, AbstractMinecartEntity cart) {
		if (!cart.canBeRidden())
			return;
		if (state.get(POWERED))
			disassemble(world, pos, cart);
		else
			assemble(world, pos, cart);
	}

	protected void assemble(World world, BlockPos pos, AbstractMinecartEntity cart) {
		if (!cart.getPassengers().isEmpty())
			return;

		Contraption contraption = MountedContraption.assembleMinecart(world, pos);
		if (contraption == null)
			return;
		if (contraption.blocks.size() == 1)
			return;
		float initialAngle = ContraptionEntity.yawFromVector(cart.getMotion());
		ContraptionEntity entity = ContraptionEntity.createMounted(world, contraption, initialAngle);
		entity.setPosition(pos.getX(), pos.getY(), pos.getZ());
		world.addEntity(entity);
		entity.startRiding(cart);
	}

	protected void disassemble(World world, BlockPos pos, AbstractMinecartEntity cart) {
		if (cart.getPassengers().isEmpty())
			return;
		if (!(cart.getPassengers().get(0) instanceof ContraptionEntity))
			return;
		cart.removePassengers();
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);

		if (worldIn.isRemote)
			return;

		boolean previouslyPowered = state.get(POWERED);
		if (previouslyPowered != worldIn.isBlockPowered(pos)) {
			worldIn.setBlockState(pos, state.cycle(POWERED), 2);
		}
	}

	@Override
	public IProperty<RailShape> getShapeProperty() {
		return RAIL_SHAPE;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.CART_ASSEMBLER
				.get(state.get(RAIL_SHAPE) == RailShape.NORTH_SOUTH ? Direction.Axis.Z : Direction.Axis.X);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos,
			ISelectionContext context) {
		if (context.getEntity() instanceof AbstractMinecartEntity)
			return VoxelShapes.empty();
		return VoxelShapes.fullCube();
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}
	
	@Override
	public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return false;
	}

	public static class MinecartAnchorBlock extends RenderUtilityBlock {

		@Override
		protected void fillStateContainer(Builder<Block, BlockState> builder) {
			builder.add(BlockStateProperties.HORIZONTAL_AXIS);
			super.fillStateContainer(builder);
		}
	}

	public static BlockState createAnchor(BlockState state) {
		Axis axis = state.get(RAIL_SHAPE) == RailShape.NORTH_SOUTH ? Axis.Z : Axis.X;
		return AllBlocks.MINECART_ANCHOR.get().getDefaultState().with(BlockStateProperties.HORIZONTAL_AXIS, axis);
	}

}
