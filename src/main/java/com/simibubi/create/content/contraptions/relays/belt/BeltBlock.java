package com.simibubi.create.content.contraptions.relays.belt;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.processing.EmptyingByBasin;
import com.simibubi.create.content.contraptions.relays.belt.BeltSlicer.Feedback;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity.CasingType;
import com.simibubi.create.content.contraptions.relays.belt.transport.BeltMovementHandler.TransportedEntityInfo;
import com.simibubi.create.content.contraptions.relays.belt.transport.BeltTunnelInteractionHandler;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelBlock;
import com.simibubi.create.content.schematics.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.DebugChunkGenerator;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BeltBlock extends HorizontalKineticBlock implements ITE<BeltTileEntity>, ISpecialBlockItemRequirement {

	public static final Property<BeltSlope> SLOPE = EnumProperty.create("slope", BeltSlope.class);
	public static final Property<BeltPart> PART = EnumProperty.create("part", BeltPart.class);
	public static final BooleanProperty CASING = BooleanProperty.create("casing");

	public BeltBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(SLOPE, BeltSlope.HORIZONTAL)
			.setValue(PART, BeltPart.START)
			.setValue(CASING, false));
	}

	@Override
	public void fillItemCategory(ItemGroup p_149666_1_, NonNullList<ItemStack> p_149666_2_) {
		p_149666_2_.add(AllItems.BELT_CONNECTOR.asStack());
	}

	@Override
	protected boolean areStatesKineticallyEquivalent(BlockState oldState, BlockState newState) {
		return super.areStatesKineticallyEquivalent(oldState, newState)
			&& oldState.getValue(PART) == newState.getValue(PART);
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		if (face.getAxis() != getRotationAxis(state))
			return false;
		return getTileEntityOptional(world, pos).map(BeltTileEntity::hasPulley)
			.orElse(false);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		if (state.getValue(SLOPE) == BeltSlope.SIDEWAYS)
			return Axis.Y;
		return state.getValue(HORIZONTAL_FACING)
			.getClockWise()
			.getAxis();
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos,
		PlayerEntity player) {
		return AllItems.BELT_CONNECTOR.asStack();
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<ItemStack> getDrops(BlockState state, net.minecraft.loot.LootContext.Builder builder) {
		List<ItemStack> drops = super.getDrops(state, builder);
		TileEntity tileEntity = builder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
		if (tileEntity instanceof BeltTileEntity && ((BeltTileEntity) tileEntity).hasPulley())
			drops.addAll(AllBlocks.SHAFT.getDefaultState()
				.getDrops(builder));
		return drops;
	}

	@Override
	public void spawnAfterBreak(BlockState state, ServerWorld worldIn, BlockPos pos, ItemStack p_220062_4_) {
		BeltTileEntity controllerTE = BeltHelper.getControllerTE(worldIn, pos);
		if (controllerTE != null)
			controllerTE.getInventory()
				.ejectAll();
	}

	@Override
	public boolean isFlammable(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
		return false;
	}

	@Override
	public void updateEntityAfterFallOn(IBlockReader worldIn, Entity entityIn) {
		super.updateEntityAfterFallOn(worldIn, entityIn);
		BlockPos entityPosition = entityIn.blockPosition();
		BlockPos beltPos = null;

		if (AllBlocks.BELT.has(worldIn.getBlockState(entityPosition)))
			beltPos = entityPosition;
		else if (AllBlocks.BELT.has(worldIn.getBlockState(entityPosition.below())))
			beltPos = entityPosition.below();
		if (beltPos == null)
			return;
		if (!(worldIn instanceof World))
			return;

		entityInside(worldIn.getBlockState(beltPos), (World) worldIn, beltPos, entityIn);
	}

	@Override
	public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		if (!canTransportObjects(state))
			return;
		if (entityIn instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entityIn;
			if (player.isShiftKeyDown())
				return;
			if (player.abilities.flying)
				return;
		}

		if (AllItems.DIVING_BOOTS.get()
			.isWornBy(entityIn))
			return;

		BeltTileEntity belt = BeltHelper.getSegmentTE(worldIn, pos);
		if (belt == null)
			return;
		if (entityIn instanceof ItemEntity && entityIn.isAlive()) {
			if (worldIn.isClientSide)
				return;
			if (entityIn.getDeltaMovement().y > 0)
				return;
			if (!entityIn.isAlive())
				return;
			if (BeltTunnelInteractionHandler.getTunnelOnPosition(worldIn, pos) != null)
				return;
			withTileEntityDo(worldIn, pos, te -> {
				ItemEntity itemEntity = (ItemEntity) entityIn;
				IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
					.orElse(null);
				if (handler == null)
					return;
				ItemStack remainder = handler.insertItem(0, itemEntity.getItem()
					.copy(), false);
				if (remainder.isEmpty())
					itemEntity.remove();
			});
			return;
		}

		BeltTileEntity controller = BeltHelper.getControllerTE(worldIn, pos);
		if (controller == null || controller.passengers == null)
			return;
		if (controller.passengers.containsKey(entityIn)) {
			TransportedEntityInfo info = controller.passengers.get(entityIn);
			if (info.getTicksSinceLastCollision() != 0 || pos.equals(entityIn.blockPosition()))
				info.refresh(pos, state);
		} else {
			controller.passengers.put(entityIn, new TransportedEntityInfo(pos, state));
			entityIn.setOnGround(true);
		}
	}

	public static boolean canTransportObjects(BlockState state) {
		if (!AllBlocks.BELT.has(state))
			return false;
		BeltSlope slope = state.getValue(SLOPE);
		return slope != BeltSlope.VERTICAL && slope != BeltSlope.SIDEWAYS;
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn,
		BlockRayTraceResult hit) {
		if (player.isShiftKeyDown() || !player.mayBuild())
			return ActionResultType.PASS;
		ItemStack heldItem = player.getItemInHand(handIn);

		boolean isWrench = AllItems.WRENCH.isIn(heldItem);
		boolean isConnector = AllItems.BELT_CONNECTOR.isIn(heldItem);
		boolean isShaft = AllBlocks.SHAFT.isIn(heldItem);
		boolean isDye = Tags.Items.DYES.contains(heldItem.getItem());
		boolean hasWater = EmptyingByBasin.emptyItem(world, heldItem, true)
			.getFirst()
			.getFluid()
			.isSame(Fluids.WATER);
		boolean isHand = heldItem.isEmpty() && handIn == Hand.MAIN_HAND;

		if (isDye || hasWater) {
			if (!world.isClientSide)
				withTileEntityDo(world, pos, te -> te.applyColor(DyeColor.getColor(heldItem)));
			return ActionResultType.SUCCESS;
		}

		if (isConnector)
			return BeltSlicer.useConnector(state, world, pos, player, handIn, hit, new Feedback());
		if (isWrench)
			return BeltSlicer.useWrench(state, world, pos, player, handIn, hit, new Feedback());

		BeltTileEntity belt = BeltHelper.getSegmentTE(world, pos);
		if (belt == null)
			return ActionResultType.PASS;

		if (isHand) {
			BeltTileEntity controllerBelt = belt.getControllerTE();
			if (controllerBelt == null)
				return ActionResultType.PASS;
			if (world.isClientSide)
				return ActionResultType.SUCCESS;
			MutableBoolean success = new MutableBoolean(false);
			controllerBelt.getInventory()
				.applyToEachWithin(belt.index + .5f, .55f, (transportedItemStack) -> {
					player.inventory.placeItemBackInInventory(world, transportedItemStack.stack);
					success.setTrue();
					return TransportedResult.removeItem();
				});
			if (success.isTrue())
				world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundCategory.PLAYERS, .2f,
					1f + Create.RANDOM.nextFloat());
		}

		if (isShaft) {
			if (state.getValue(PART) != BeltPart.MIDDLE)
				return ActionResultType.PASS;
			if (world.isClientSide)
				return ActionResultType.SUCCESS;
			if (!player.isCreative())
				heldItem.shrink(1);
			KineticTileEntity.switchToBlockState(world, pos, state.setValue(PART, BeltPart.PULLEY));
			return ActionResultType.SUCCESS;
		}

		if (AllBlocks.BRASS_CASING.isIn(heldItem)) {
			if (world.isClientSide)
				return ActionResultType.SUCCESS;
			AllTriggers.triggerFor(AllTriggers.CASING_BELT, player);
			withTileEntityDo(world, pos, te -> te.setCasingType(CasingType.BRASS));
			return ActionResultType.SUCCESS;
		}

		if (AllBlocks.ANDESITE_CASING.isIn(heldItem)) {
			if (world.isClientSide)
				return ActionResultType.SUCCESS;
			AllTriggers.triggerFor(AllTriggers.CASING_BELT, player);
			withTileEntityDo(world, pos, te -> te.setCasingType(CasingType.ANDESITE));
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		World world = context.getLevel();
		PlayerEntity player = context.getPlayer();
		BlockPos pos = context.getClickedPos();

		if (state.getValue(CASING)) {
			if (world.isClientSide)
				return ActionResultType.SUCCESS;
			withTileEntityDo(world, pos, te -> te.setCasingType(CasingType.NONE));
			return ActionResultType.SUCCESS;
		}

		if (state.getValue(PART) == BeltPart.PULLEY) {
			if (world.isClientSide)
				return ActionResultType.SUCCESS;
			KineticTileEntity.switchToBlockState(world, pos, state.setValue(PART, BeltPart.MIDDLE));
			if (player != null && !player.isCreative())
				player.inventory.placeItemBackInInventory(world, AllBlocks.SHAFT.asStack());
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(SLOPE, PART, CASING);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public PathNodeType getAiPathNodeType(BlockState state, IBlockReader world, BlockPos pos, MobEntity entity) {
		return PathNodeType.RAIL;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager manager) {
		BlockHelper.addReducedDestroyEffects(state, world, pos, manager);
		return true;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return BeltShapes.getShape(state);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos,
		ISelectionContext context) {
		if (state.getBlock() != this)
			return VoxelShapes.empty();

		VoxelShape shape = getShape(state, worldIn, pos, context);
		return getTileEntityOptional(worldIn, pos).map(te -> {
			if (context.getEntity() == null)
				return shape;

			BeltTileEntity controller = te.getControllerTE();
			if (controller == null)
				return shape;
			if (controller.passengers == null || !controller.passengers.containsKey(context.getEntity()))
				return BeltShapes.getCollisionShape(state);
			return shape;

		})
			.orElse(shape);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.BELT.create();
	}

	@Override
	public BlockRenderType getRenderShape(BlockState state) {
		return state.getValue(CASING) ? BlockRenderType.MODEL : BlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	public static void initBelt(World world, BlockPos pos) {
		if (world.isClientSide)
			return;
		if (world instanceof ServerWorld && ((ServerWorld) world).getChunkSource()
			.getGenerator() instanceof DebugChunkGenerator)
			return;

		BlockState state = world.getBlockState(pos);
		if (!AllBlocks.BELT.has(state))
			return;
		// Find controller
		int limit = 1000;
		BlockPos currentPos = pos;
		while (limit-- > 0) {
			BlockState currentState = world.getBlockState(currentPos);
			if (!AllBlocks.BELT.has(currentState)) {
				world.destroyBlock(pos, true);
				return;
			}
			BlockPos nextSegmentPosition = nextSegmentPosition(currentState, currentPos, false);
			if (nextSegmentPosition == null)
				break;
			if (!world.isAreaLoaded(nextSegmentPosition, 0))
				return;
			currentPos = nextSegmentPosition;
		}

		// Init belts
		int index = 0;
		List<BlockPos> beltChain = getBeltChain(world, currentPos);
		if (beltChain.size() < 2) {
			world.destroyBlock(currentPos, true);
			return;
		}

		for (BlockPos beltPos : beltChain) {
			TileEntity tileEntity = world.getBlockEntity(beltPos);
			BlockState currentState = world.getBlockState(beltPos);

			if (tileEntity instanceof BeltTileEntity && AllBlocks.BELT.has(currentState)) {
				BeltTileEntity te = (BeltTileEntity) tileEntity;
				te.setController(currentPos);
				te.beltLength = beltChain.size();
				te.index = index;
				te.attachKinetics();
				te.setChanged();
				te.sendData();

				if (te.isController() && !canTransportObjects(currentState))
					te.getInventory()
						.ejectAll();
			} else {
				world.destroyBlock(currentPos, true);
				return;
			}
			index++;
		}

	}

	@Override
	public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (world.isClientSide)
			return;
		if (state.getBlock() == newState.getBlock())
			return;
		if (isMoving)
			return;

		TileEntity te = world.getBlockEntity(pos);
		if (te instanceof BeltTileEntity) {
			BeltTileEntity beltTileEntity = (BeltTileEntity) te;
			if (beltTileEntity.isController())
				beltTileEntity.getInventory()
					.ejectAll();
			world.removeBlockEntity(pos);
		}

		// Destroy chain
		for (boolean forward : Iterate.trueAndFalse) {
			BlockPos currentPos = nextSegmentPosition(state, pos, forward);
			if (currentPos == null)
				continue;
			world.destroyBlockProgress(currentPos.hashCode(), currentPos, -1);
			BlockState currentState = world.getBlockState(currentPos);
			if (!AllBlocks.BELT.has(currentState))
				continue;

			boolean hasPulley = false;
			TileEntity tileEntity = world.getBlockEntity(currentPos);
			if (tileEntity instanceof BeltTileEntity) {
				BeltTileEntity belt = (BeltTileEntity) tileEntity;
				if (belt.isController())
					belt.getInventory()
						.ejectAll();

				belt.setRemoved();
				hasPulley = belt.hasPulley();
			}

			BlockState shaftState = AllBlocks.SHAFT.getDefaultState()
				.setValue(BlockStateProperties.AXIS, getRotationAxis(currentState));
			world.setBlock(currentPos, hasPulley ? shaftState : Blocks.AIR.defaultBlockState(), 3);
			world.levelEvent(2001, currentPos, Block.getId(currentState));
		}
	}

	@Override
	public BlockState updateShape(BlockState state, Direction side, BlockState p_196271_3_, IWorld world, BlockPos pos,
		BlockPos p_196271_6_) {
		if (side.getAxis()
			.isHorizontal())
			updateTunnelConnections(world, pos.above());
		return state;
	}

	private void updateTunnelConnections(IWorld world, BlockPos pos) {
		Block tunnelBlock = world.getBlockState(pos)
			.getBlock();
		if (tunnelBlock instanceof BeltTunnelBlock)
			((BeltTunnelBlock) tunnelBlock).updateTunnel(world, pos);
	}

	public static List<BlockPos> getBeltChain(World world, BlockPos controllerPos) {
		List<BlockPos> positions = new LinkedList<>();

		BlockState blockState = world.getBlockState(controllerPos);
		if (!AllBlocks.BELT.has(blockState))
			return positions;

		int limit = 1000;
		BlockPos current = controllerPos;
		while (limit-- > 0 && current != null) {
			BlockState state = world.getBlockState(current);
			if (!AllBlocks.BELT.has(state))
				break;
			positions.add(current);
			current = nextSegmentPosition(state, current, true);
		}

		return positions;
	}

	public static BlockPos nextSegmentPosition(BlockState state, BlockPos pos, boolean forward) {
		Direction direction = state.getValue(HORIZONTAL_FACING);
		BeltSlope slope = state.getValue(SLOPE);
		BeltPart part = state.getValue(PART);

		int offset = forward ? 1 : -1;

		if (part == BeltPart.END && forward || part == BeltPart.START && !forward)
			return null;
		if (slope == BeltSlope.VERTICAL)
			return pos.above(direction.getAxisDirection() == AxisDirection.POSITIVE ? offset : -offset);
		pos = pos.relative(direction, offset);
		if (slope != BeltSlope.HORIZONTAL && slope != BeltSlope.SIDEWAYS)
			return pos.above(slope == BeltSlope.UPWARD ? offset : -offset);
		return pos;
	}

	public static boolean canAccessFromSide(Direction facing, BlockState belt) {
		return true;
	}

	@Override
	public Class<BeltTileEntity> getTileEntityClass() {
		return BeltTileEntity.class;
	}

	@Override
	public ItemRequirement getRequiredItems(BlockState state, TileEntity te) {
		List<ItemStack> required = new ArrayList<>();
		if (state.getValue(PART) != BeltPart.MIDDLE)
			required.add(AllBlocks.SHAFT.asStack());
		if (state.getValue(PART) == BeltPart.START)
			required.add(AllItems.BELT_CONNECTOR.asStack());
		if (required.isEmpty())
			return ItemRequirement.NONE;
		return new ItemRequirement(ItemUseType.CONSUME, required);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		BlockState rotate = super.rotate(state, rot);

		if (state.getValue(SLOPE) != BeltSlope.VERTICAL)
			return rotate;
		if (state.getValue(HORIZONTAL_FACING)
			.getAxisDirection() != rotate.getValue(HORIZONTAL_FACING)
				.getAxisDirection()) {
			if (state.getValue(PART) == BeltPart.START)
				return rotate.setValue(PART, BeltPart.END);
			if (state.getValue(PART) == BeltPart.END)
				return rotate.setValue(PART, BeltPart.START);
		}

		return rotate;
	}

	@Override
	public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}

}
