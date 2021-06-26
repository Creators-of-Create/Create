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

public class CartAssemblerBlock extends AbstractRailBlock
	implements ITE<CartAssemblerTileEntity>, IWrenchable, ISpecialBlockItemRequirement {

	public static final Property<RailShape> RAIL_SHAPE =
		EnumProperty.create("shape", RailShape.class, RailShape.EAST_WEST, RailShape.NORTH_SOUTH);
	public static final Property<CartAssembleRailType> RAIL_TYPE =
		EnumProperty.create("rail_type", CartAssembleRailType.class);
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public CartAssemblerBlock(Properties properties) {
		super(true, properties);
		setDefaultState(getDefaultState().with(POWERED, false)
			.with(RAIL_TYPE, CartAssembleRailType.POWERED_RAIL));
	}

	public static BlockState createAnchor(BlockState state) {
		Axis axis = state.get(RAIL_SHAPE) == RailShape.NORTH_SOUTH ? Axis.Z : Axis.X;
		return AllBlocks.MINECART_ANCHOR.getDefaultState()
			.with(BlockStateProperties.HORIZONTAL_AXIS, axis);
	}

	private static Item getRailItem(BlockState state) {
		return state.get(RAIL_TYPE)
			.getItem();
	}

	public static BlockState getRailBlock(BlockState state) {
		AbstractRailBlock railBlock = (AbstractRailBlock) state.get(RAIL_TYPE)
			.getBlock();
		BlockState railState = railBlock.getDefaultState()
			.with(railBlock.getShapeProperty(), state.get(RAIL_SHAPE));
		if (railState.contains(ControllerRailBlock.BACKWARDS)) {
			railState = railState.with(ControllerRailBlock.BACKWARDS, state.get(RAIL_TYPE) == CartAssembleRailType.CONTROLLER_RAIL_BACKWARDS);
		}
		return railState;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(RAIL_SHAPE, POWERED, RAIL_TYPE);
		super.fillStateContainer(builder);
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
		if (world.isRemote)
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
		CartAssembleRailType type = state.get(RAIL_TYPE);
		boolean powered = state.get(POWERED);

		if (type == CartAssembleRailType.REGULAR)
			return powered ? CartAssemblerAction.ASSEMBLE : CartAssemblerAction.DISASSEMBLE;

		if (type == CartAssembleRailType.ACTIVATOR_RAIL)
			return powered ? CartAssemblerAction.DISASSEMBLE : CartAssemblerAction.PASS;

		if (type == CartAssembleRailType.POWERED_RAIL)
			return powered ? CartAssemblerAction.ASSEMBLE_ACCELERATE : CartAssemblerAction.DISASSEMBLE_BRAKE;

		if (type == CartAssembleRailType.DETECTOR_RAIL)
			return cart.getPassengers()
				.isEmpty() ? CartAssemblerAction.ASSEMBLE_ACCELERATE : CartAssemblerAction.DISASSEMBLE;

		if (type == CartAssembleRailType.CONTROLLER_RAIL || type == CartAssembleRailType.CONTROLLER_RAIL_BACKWARDS)
			return powered ? CartAssemblerAction.ASSEMBLE_ACCELERATE_DIRECTIONAL
				: CartAssemblerAction.DISASSEMBLE_BRAKE;

		return CartAssemblerAction.PASS;
	}

	public static boolean canAssembleTo(AbstractMinecartEntity cart) {
		return cart.canBeRidden() || cart instanceof FurnaceMinecartEntity || cart instanceof ChestMinecartEntity;
	}

	@Override
	@Nonnull
	public ActionResultType onUse(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos,
		PlayerEntity player, @Nonnull Hand hand, @Nonnull BlockRayTraceResult blockRayTraceResult) {

		ItemStack itemStack = player.getHeldItem(hand);
		Item previousItem = getRailItem(state);
		Item heldItem = itemStack.getItem();
		if (heldItem != previousItem) {

			CartAssembleRailType newType = null;
			for (CartAssembleRailType type : CartAssembleRailType.values())
				if (heldItem == type.getItem())
					newType = type;
			if (newType == null)
				return ActionResultType.PASS;
			world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1, 1);
			world.setBlockState(pos, state.with(RAIL_TYPE, newType));

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
		if (worldIn.isRemote)
			return;

		boolean previouslyPowered = state.get(POWERED);
		if (previouslyPowered != worldIn.isBlockPowered(pos)) {
			worldIn.setBlockState(pos, state.cycle(POWERED), 2);
		}

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
		return state.get(RAIL_SHAPE) == RailShape.NORTH_SOUTH ? Direction.Axis.Z : Direction.Axis.X;
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
		return VoxelShapes.fullCube();
	}

	@Override
	@Nonnull
	public PushReaction getPushReaction(@Nonnull BlockState state) {
		return PushReaction.BLOCK;
	}

	@Override
	public Class<CartAssemblerTileEntity> getTileEntityClass() {
		return CartAssemblerTileEntity.class;
	}

	@Override
	public boolean isValidPosition(@Nonnull BlockState state, @Nonnull IWorldReader world, @Nonnull BlockPos pos) {
		return false;
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

	@Override
	public ItemRequirement getRequiredItems(BlockState state, TileEntity te) {
		ArrayList<ItemStack> reuiredItems = new ArrayList<>();
		reuiredItems.add(new ItemStack(getRailItem(state)));
		reuiredItems.add(new ItemStack(asItem()));
		return new ItemRequirement(ItemUseType.CONSUME, reuiredItems);
	}

	@SuppressWarnings("deprecation")
	public List<ItemStack> getDropedAssembler(BlockState state, ServerWorld world, BlockPos pos,
		@Nullable TileEntity p_220077_3_, @Nullable Entity p_220077_4_, ItemStack p_220077_5_) {
		return super.getDrops(state, (new LootContext.Builder(world)).withRandom(world.rand)
			.withParameter(LootParameters.ORIGIN, Vector3d.of(pos))
			.withParameter(LootParameters.TOOL, p_220077_5_)
			.withNullableParameter(LootParameters.THIS_ENTITY, p_220077_4_)
			.withNullableParameter(LootParameters.BLOCK_ENTITY, p_220077_3_));
	}

	@Override
	public ActionResultType onSneakWrenched(BlockState state, ItemUseContext context) {
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		PlayerEntity player = context.getPlayer();
		if (world.isRemote)
			return ActionResultType.SUCCESS;

		if (player != null && !player.isCreative())
			getDropedAssembler(state, (ServerWorld) world, pos, world.getTileEntity(pos), player, context.getItem())
				.forEach(itemStack -> {
					player.inventory.placeItemBackInInventory(world, itemStack);
				});
		if(world instanceof ServerWorld)
			state.spawnAdditionalDrops((ServerWorld) world, pos, ItemStack.EMPTY);
		world.setBlockState(pos, getRailBlock(state));
		return ActionResultType.SUCCESS;
	}

	public static class MinecartAnchorBlock extends Block {

		public MinecartAnchorBlock(Properties p_i48440_1_) {
			super(p_i48440_1_);
		}

		@Override
		protected void fillStateContainer(Builder<Block, BlockState> builder) {
			builder.add(BlockStateProperties.HORIZONTAL_AXIS);
			super.fillStateContainer(builder);
		}

		@Override
		@Nonnull
		public VoxelShape getShape(@Nonnull BlockState p_220053_1_, @Nonnull IBlockReader p_220053_2_,
			@Nonnull BlockPos p_220053_3_, @Nonnull ISelectionContext p_220053_4_) {
			return VoxelShapes.empty();
		}
	}

	@Override
	public boolean allowsMovement(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		World world = context.getWorld();
		if (world.isRemote)
			return ActionResultType.SUCCESS;
		BlockPos pos = context.getPos();
		world.setBlockState(pos, rotate(state, Rotation.CLOCKWISE_90), 3);
		world.notifyNeighborsOfStateChange(pos.down(), this);
		return ActionResultType.SUCCESS;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		if (rotation == Rotation.NONE)
			return state;

		boolean is_controller_rail_backwards = state.get(RAIL_TYPE) == CartAssembleRailType.CONTROLLER_RAIL_BACKWARDS;
		boolean is_controller_rail = state.get(RAIL_TYPE) == CartAssembleRailType.CONTROLLER_RAIL || is_controller_rail_backwards;
		BlockState base = AllBlocks.CONTROLLER_RAIL.getDefaultState()
				.with(ControllerRailBlock.SHAPE, state.get(RAIL_SHAPE))
				.with(ControllerRailBlock.BACKWARDS, is_controller_rail_backwards)
				.rotate(rotation);
		if (is_controller_rail) {
			state = state.with(RAIL_TYPE,
					base.get(ControllerRailBlock.BACKWARDS) ? CartAssembleRailType.CONTROLLER_RAIL_BACKWARDS :
							CartAssembleRailType.CONTROLLER_RAIL
			);
		}
		return state.with(RAIL_SHAPE, base.get(ControllerRailBlock.SHAPE));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		if (mirror == Mirror.NONE)
			return state;

		boolean is_controller_rail_backwards = state.get(RAIL_TYPE) == CartAssembleRailType.CONTROLLER_RAIL_BACKWARDS;
		boolean is_controller_rail = state.get(RAIL_TYPE) == CartAssembleRailType.CONTROLLER_RAIL || is_controller_rail_backwards;
		BlockState base = AllBlocks.CONTROLLER_RAIL.getDefaultState()
				.with(ControllerRailBlock.SHAPE, state.get(RAIL_SHAPE))
				.with(ControllerRailBlock.BACKWARDS, is_controller_rail_backwards)
				.mirror(mirror);
		if (is_controller_rail) {
			state = state.with(RAIL_TYPE,
					base.get(ControllerRailBlock.BACKWARDS) ? CartAssembleRailType.CONTROLLER_RAIL_BACKWARDS :
							CartAssembleRailType.CONTROLLER_RAIL
			);
		}
		return state.with(RAIL_SHAPE, base.get(ControllerRailBlock.SHAPE));
	}
}
