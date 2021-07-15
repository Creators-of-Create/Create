package com.simibubi.create.content.contraptions.components.structureMovement.mounted;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.components.tracks.ControllerRailBlock;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.content.schematics.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.item.minecart.ChestMinecartEntity;
import net.minecraft.entity.item.minecart.FurnaceMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import net.minecraft.block.AbstractBlock.Properties;

public class CartAssemblerBlock extends AbstractRailBlock
	implements ITE<CartAssemblerTileEntity>, IWrenchable, ISpecialBlockItemRequirement {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final BooleanProperty BACKWARDS = BooleanProperty.create("backwards");
	public static final Property<RailShape> RAIL_SHAPE =
		EnumProperty.create("shape", RailShape.class, RailShape.EAST_WEST, RailShape.NORTH_SOUTH);
	public static final Property<CartAssembleRailType> RAIL_TYPE =
		EnumProperty.create("rail_type", CartAssembleRailType.class);

	public CartAssemblerBlock(Properties properties) {
		super(true, properties);
		registerDefaultState(defaultBlockState().setValue(POWERED, false)
			.setValue(BACKWARDS, false)
			.setValue(RAIL_TYPE, CartAssembleRailType.POWERED_RAIL));
	}

	public static BlockState createAnchor(BlockState state) {
		Axis axis = state.getValue(RAIL_SHAPE) == RailShape.NORTH_SOUTH ? Axis.Z : Axis.X;
		return AllBlocks.MINECART_ANCHOR.getDefaultState()
			.setValue(BlockStateProperties.HORIZONTAL_AXIS, axis);
	}

	private static Item getRailItem(BlockState state) {
		return state.getValue(RAIL_TYPE)
			.getItem();
	}

	public static BlockState getRailBlock(BlockState state) {
		AbstractRailBlock railBlock = (AbstractRailBlock) state.getValue(RAIL_TYPE)
			.getBlock();
		BlockState railState = railBlock.defaultBlockState()
			.setValue(railBlock.getShapeProperty(), state.getValue(RAIL_SHAPE));
		if (railState.hasProperty(ControllerRailBlock.BACKWARDS))
			railState = railState.setValue(ControllerRailBlock.BACKWARDS, state.getValue(BACKWARDS));
		return railState;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(RAIL_SHAPE, POWERED, RAIL_TYPE, BACKWARDS);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.CART_ASSEMBLER.create();
	}

	@Override
	public boolean canMakeSlopes(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
		return false;
	}

	@Override
	public void onMinecartPass(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos,
		AbstractMinecartEntity cart) {
		if (!canAssembleTo(cart))
			return;
		if (world.isClientSide)
			return;

		withTileEntityDo(world, pos, te -> te.assembleNextTick(cart));
	}

	public enum CartAssemblerAction {
		ASSEMBLE, DISASSEMBLE, ASSEMBLE_ACCELERATE, DISASSEMBLE_BRAKE, ASSEMBLE_ACCELERATE_DIRECTIONAL, PASS;

		public boolean shouldAssemble() {
			return this == ASSEMBLE || this == ASSEMBLE_ACCELERATE || this == ASSEMBLE_ACCELERATE_DIRECTIONAL;
		}

		public boolean shouldDisassemble() {
			return this == DISASSEMBLE || this == DISASSEMBLE_BRAKE;
		}
	}

	public static CartAssemblerAction getActionForCart(BlockState state, AbstractMinecartEntity cart) {
		CartAssembleRailType type = state.getValue(RAIL_TYPE);
		boolean powered = state.getValue(POWERED);
		switch (type) {
		case ACTIVATOR_RAIL:
			return powered ? CartAssemblerAction.DISASSEMBLE : CartAssemblerAction.PASS;
		case CONTROLLER_RAIL:
			return powered ? CartAssemblerAction.ASSEMBLE_ACCELERATE_DIRECTIONAL
				: CartAssemblerAction.DISASSEMBLE_BRAKE;
		case DETECTOR_RAIL:
			return cart.getPassengers()
				.isEmpty() ? CartAssemblerAction.ASSEMBLE_ACCELERATE : CartAssemblerAction.DISASSEMBLE;
		case POWERED_RAIL:
			return powered ? CartAssemblerAction.ASSEMBLE_ACCELERATE : CartAssemblerAction.DISASSEMBLE_BRAKE;
		case REGULAR:
			return powered ? CartAssemblerAction.ASSEMBLE : CartAssemblerAction.DISASSEMBLE;
		default:
			return CartAssemblerAction.PASS;
		}
	}

	public static boolean canAssembleTo(AbstractMinecartEntity cart) {
		return cart.canBeRidden() || cart instanceof FurnaceMinecartEntity || cart instanceof ChestMinecartEntity;
	}

	@Override
	@Nonnull
	public ActionResultType use(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos,
		PlayerEntity player, @Nonnull Hand hand, @Nonnull BlockRayTraceResult blockRayTraceResult) {

		ItemStack itemStack = player.getItemInHand(hand);
		Item previousItem = getRailItem(state);
		Item heldItem = itemStack.getItem();
		if (heldItem != previousItem) {

			CartAssembleRailType newType = null;
			for (CartAssembleRailType type : CartAssembleRailType.values())
				if (heldItem == type.getItem())
					newType = type;
			if (newType == null)
				return ActionResultType.PASS;
			world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundCategory.PLAYERS, 1, 1);
			world.setBlockAndUpdate(pos, state.setValue(RAIL_TYPE, newType));

			if (!player.isCreative()) {
				itemStack.shrink(1);
				player.inventory.placeItemBackInInventory(world, new ItemStack(previousItem));
			}
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}

	@Override
	public void neighborChanged(@Nonnull BlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos,
		@Nonnull Block blockIn, @Nonnull BlockPos fromPos, boolean isMoving) {
		if (worldIn.isClientSide)
			return;
		boolean previouslyPowered = state.getValue(POWERED);
		if (previouslyPowered != worldIn.hasNeighborSignal(pos))
			worldIn.setBlock(pos, state.cycle(POWERED), 2);
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
	}

	@Override
	@Nonnull
	public Property<RailShape> getShapeProperty() {
		return RAIL_SHAPE;
	}

	@Override
	@Nonnull
	public VoxelShape getShape(BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos,
		@Nonnull ISelectionContext context) {
		return AllShapes.CART_ASSEMBLER.get(getRailAxis(state));
	}

	protected Axis getRailAxis(BlockState state) {
		return state.getValue(RAIL_SHAPE) == RailShape.NORTH_SOUTH ? Direction.Axis.Z : Direction.Axis.X;
	}

	@Override
	@Nonnull
	public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos,
		ISelectionContext context) {
		Entity entity = context.getEntity();
		if (entity instanceof AbstractMinecartEntity)
			return VoxelShapes.empty();
		if (entity instanceof PlayerEntity)
			return AllShapes.CART_ASSEMBLER_PLAYER_COLLISION.get(getRailAxis(state));
		return VoxelShapes.block();
	}

	@Override
	@Nonnull
	public PushReaction getPistonPushReaction(@Nonnull BlockState state) {
		return PushReaction.BLOCK;
	}

	@Override
	public Class<CartAssemblerTileEntity> getTileEntityClass() {
		return CartAssemblerTileEntity.class;
	}

	@Override
	public boolean canSurvive(@Nonnull BlockState state, @Nonnull IWorldReader world, @Nonnull BlockPos pos) {
		return false;
	}

	@Override
	public ItemRequirement getRequiredItems(BlockState state, TileEntity te) {
		ArrayList<ItemStack> reuiredItems = new ArrayList<>();
		reuiredItems.add(new ItemStack(getRailItem(state)));
		reuiredItems.add(new ItemStack(asItem()));
		return new ItemRequirement(ItemUseType.CONSUME, reuiredItems);
	}

	@Override
	@SuppressWarnings("deprecation")
	@Nonnull
	public List<ItemStack> getDrops(@Nonnull BlockState state,
		@Nonnull net.minecraft.loot.LootContext.Builder builder) {
		List<ItemStack> drops = super.getDrops(state, builder);
		drops.addAll(getRailBlock(state).getDrops(builder));
		return drops;
	}

	@SuppressWarnings("deprecation")
	public List<ItemStack> getDropsNoRail(BlockState state, ServerWorld world, BlockPos pos,
		@Nullable TileEntity p_220077_3_, @Nullable Entity p_220077_4_, ItemStack p_220077_5_) {
		return super.getDrops(state, (new LootContext.Builder(world)).withRandom(world.random)
			.withParameter(LootParameters.ORIGIN, Vector3d.atLowerCornerOf(pos))
			.withParameter(LootParameters.TOOL, p_220077_5_)
			.withOptionalParameter(LootParameters.THIS_ENTITY, p_220077_4_)
			.withOptionalParameter(LootParameters.BLOCK_ENTITY, p_220077_3_));
	}

	@Override
	public ActionResultType onSneakWrenched(BlockState state, ItemUseContext context) {
		World world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		PlayerEntity player = context.getPlayer();
		if (world.isClientSide)
			return ActionResultType.SUCCESS;
		if (player != null && !player.isCreative())
			getDropsNoRail(state, (ServerWorld) world, pos, world.getBlockEntity(pos), player, context.getItemInHand())
				.forEach(itemStack -> player.inventory.placeItemBackInInventory(world, itemStack));
		if (world instanceof ServerWorld)
			state.spawnAfterBreak((ServerWorld) world, pos, ItemStack.EMPTY);
		world.setBlockAndUpdate(pos, getRailBlock(state));
		return ActionResultType.SUCCESS;
	}

	public static class MinecartAnchorBlock extends Block {

		public MinecartAnchorBlock(Properties p_i48440_1_) {
			super(p_i48440_1_);
		}

		@Override
		protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
			builder.add(BlockStateProperties.HORIZONTAL_AXIS);
			super.createBlockStateDefinition(builder);
		}

		@Override
		@Nonnull
		public VoxelShape getShape(@Nonnull BlockState p_220053_1_, @Nonnull IBlockReader p_220053_2_,
			@Nonnull BlockPos p_220053_3_, @Nonnull ISelectionContext p_220053_4_) {
			return VoxelShapes.empty();
		}
	}

	@Override
	public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		World world = context.getLevel();
		if (world.isClientSide)
			return ActionResultType.SUCCESS;
		BlockPos pos = context.getClickedPos();
		world.setBlock(pos, rotate(state, Rotation.CLOCKWISE_90), 3);
		world.updateNeighborsAt(pos.below(), this);
		return ActionResultType.SUCCESS;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		if (rotation == Rotation.NONE)
			return state;
		BlockState base = AllBlocks.CONTROLLER_RAIL.getDefaultState()
			.setValue(ControllerRailBlock.SHAPE, state.getValue(RAIL_SHAPE))
			.setValue(ControllerRailBlock.BACKWARDS, state.getValue(BACKWARDS))
			.rotate(rotation);
		return state.setValue(RAIL_SHAPE, base.getValue(ControllerRailBlock.SHAPE))
			.setValue(BACKWARDS, base.getValue(ControllerRailBlock.BACKWARDS));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		if (mirror == Mirror.NONE)
			return state;
		BlockState base = AllBlocks.CONTROLLER_RAIL.getDefaultState()
			.setValue(ControllerRailBlock.SHAPE, state.getValue(RAIL_SHAPE))
			.setValue(ControllerRailBlock.BACKWARDS, state.getValue(BACKWARDS))
			.mirror(mirror);
		return state.setValue(BACKWARDS, base.getValue(ControllerRailBlock.BACKWARDS));
	}

	public static Direction getHorizontalDirection(BlockState blockState) {
		if (!(blockState.getBlock() instanceof CartAssemblerBlock))
			return Direction.SOUTH;
		Direction pointingTo = getPointingTowards(blockState);
		return blockState.getValue(BACKWARDS) ? pointingTo.getOpposite() : pointingTo;
	}

	private static Direction getPointingTowards(BlockState state) {
		switch (state.getValue(RAIL_SHAPE)) {
		case EAST_WEST:
			return Direction.WEST;
		default:
			return Direction.NORTH;
		}
	}
}
