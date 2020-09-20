package com.simibubi.create.content.contraptions.components.structureMovement.mounted;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerTileEntity.CartMovementMode;
import com.simibubi.create.content.contraptions.components.structureMovement.train.MinecartCouplingHandler;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.content.schematics.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.VecHelper;

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
import net.minecraft.nbt.CompoundNBT;
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
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;

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
		return state.get(RAIL_TYPE).railItem;
	}

	public static BlockState getRailBlock(BlockState state) {
		AbstractRailBlock railBlock = (AbstractRailBlock) state.get(RAIL_TYPE).railBlock;
		return railBlock.getDefaultState()
			.with(railBlock.getShapeProperty(), state.get(RAIL_SHAPE));
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

		withTileEntityDo(world, pos, te -> {
			if (te.isMinecartUpdateValid()) {
				switch (state.get(RAIL_TYPE)) {
				case POWERED_RAIL:
					if (state.get(POWERED)) {
						assemble(world, pos, cart);
						Direction facing = cart.getAdjustedHorizontalFacing();
						float speed = getRailMaxSpeed(state, world, pos, cart);
						cart.setMotion(facing.getXOffset() * speed, facing.getYOffset() * speed,
							facing.getZOffset() * speed);
					} else {
						disassemble(world, pos, cart);
						Vector3d diff = VecHelper.getCenterOf(pos)
							.subtract(cart.getPositionVec());
						cart.setMotion(diff.x / 16f, 0, diff.z / 16f);
					}
					break;
				case REGULAR:
					if (state.get(POWERED)) {
						assemble(world, pos, cart);
					} else {
						disassemble(world, pos, cart);
					}
					break;
				case ACTIVATOR_RAIL:
					if (state.get(POWERED)) {
						disassemble(world, pos, cart);
					}
					break;
				case DETECTOR_RAIL:
					if (cart.getPassengers()
						.isEmpty()) {
						assemble(world, pos, cart);
						Direction facing = cart.getAdjustedHorizontalFacing();
						float speed = getRailMaxSpeed(state, world, pos, cart);
						cart.setMotion(facing.getXOffset() * speed, facing.getYOffset() * speed,
							facing.getZOffset() * speed);
					} else {
						disassemble(world, pos, cart);
					}
					break;
				default:
					break;
				}
				te.resetTicksSinceMinecartUpdate();
			}
		});
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
				if (heldItem == type.railItem)
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

	protected void assemble(World world, BlockPos pos, AbstractMinecartEntity cart) {
		if (!cart.getPassengers()
			.isEmpty())
			return;

		MountedContraption contraption = MountedContraption.assembleMinecart(world, pos);
		if (contraption == null)
			return;
		if (contraption.blocks.size() == 1)
			return;

		Direction facing = cart.getAdjustedHorizontalFacing();
		float initialAngle = facing.getHorizontalAngle();

		withTileEntityDo(world, pos, te -> contraption.rotationMode = CartMovementMode.values()[te.movementMode.value]);

		boolean couplingFound = contraption.connectedCart != null;
		if (couplingFound) {
			MinecartCouplingHandler.connectCarts(null, world, cart.getEntityId(),
				contraption.connectedCart.getEntityId());
			Vec3d diff = contraption.connectedCart.getPositionVec()
				.subtract(cart.getPositionVec());
			initialAngle = Direction.fromAngle(MathHelper.atan2(diff.z, diff.x) * 180 / Math.PI)
				.getHorizontalAngle();
		}

		ContraptionEntity entity = ContraptionEntity.createMounted(world, contraption, initialAngle, facing);

		if (couplingFound) {
			entity.setCouplingId(cart.getUniqueID());
			entity.setCoupledCart(contraption.connectedCart.getUniqueID());
		}

		entity.setPosition(pos.getX(), pos.getY(), pos.getZ());
		world.addEntity(entity);
		entity.startRiding(cart);

		if (cart instanceof FurnaceMinecartEntity) {
			CompoundNBT nbt = cart.serializeNBT();
			nbt.putDouble("PushZ", 0);
			nbt.putDouble("PushX", 0);
			cart.deserializeNBT(nbt);
		}
	}

	protected void disassemble(World world, BlockPos pos, AbstractMinecartEntity cart) {
		if (cart.getPassengers()
			.isEmpty())
			return;
		if (!(cart.getPassengers()
			.get(0) instanceof ContraptionEntity))
			return;
		cart.removePassengers();

		if (cart instanceof FurnaceMinecartEntity) {
			CompoundNBT nbt = cart.serializeNBT();
			nbt.putDouble("PushZ", cart.getMotion().x);
			nbt.putDouble("PushX", cart.getMotion().z);
			cart.deserializeNBT(nbt);
		}
	}

	@Override
	public void neighborChanged(@Nonnull BlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos,
		@Nonnull Block blockIn, @Nonnull BlockPos fromPos, boolean isMoving) {
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);

		if (worldIn.isRemote)
			return;

		boolean previouslyPowered = state.get(POWERED);
		if (previouslyPowered != worldIn.isBlockPowered(pos)) {
			worldIn.setBlockState(pos, state.cycle(POWERED), 2);
		}
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
		return AllShapes.CART_ASSEMBLER
			.get(state.get(RAIL_SHAPE) == RailShape.NORTH_SOUTH ? Direction.Axis.Z : Direction.Axis.X);
	}

	@Override
	@Nonnull
	public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos,
		ISelectionContext context) {
		if (context.getEntity() instanceof AbstractMinecartEntity)
			return VoxelShapes.empty();
		return VoxelShapes.fullCube();
	}

	@Override
	@Nonnull
	public PushReaction getPushReaction(@Nonnull BlockState state) {
		return PushReaction.BLOCK;
	}

	@Override
	public boolean isNormalCube(@Nonnull BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos) {
		return false;
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
	public ItemRequirement getRequiredItems(BlockState state) {
		ArrayList<ItemStack> reuiredItems = new ArrayList<ItemStack>();
		reuiredItems.add(new ItemStack(getRailItem(state)));
		reuiredItems.add(new ItemStack(asItem()));
		return new ItemRequirement(ItemUseType.CONSUME, reuiredItems);
	}

	@SuppressWarnings("deprecation")
	public List<ItemStack> getDropedAssembler(BlockState p_220077_0_, ServerWorld p_220077_1_, BlockPos p_220077_2_,
		@Nullable TileEntity p_220077_3_, @Nullable Entity p_220077_4_, ItemStack p_220077_5_) {
		return super.getDrops(p_220077_0_, (new LootContext.Builder(p_220077_1_)).withRandom(p_220077_1_.rand)
			.withParameter(LootParameters.POSITION, p_220077_2_)
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
		state.spawnAdditionalDrops(world, pos, ItemStack.EMPTY);
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
}
