package com.simibubi.create.content.contraptions.relays.belt;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.ITransformableBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.StructureTransform;
import com.simibubi.create.content.contraptions.processing.EmptyingByBasin;
import com.simibubi.create.content.contraptions.relays.belt.BeltSlicer.Feedback;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity.CasingType;
import com.simibubi.create.content.contraptions.relays.belt.transport.BeltMovementHandler.TransportedEntityInfo;
import com.simibubi.create.content.contraptions.relays.belt.transport.BeltTunnelInteractionHandler;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelBlock;
import com.simibubi.create.content.schematics.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.block.render.DestroyProgressRenderingHandler;
import com.simibubi.create.foundation.block.render.ReducedDestroyEffects;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IBlockRenderProperties;
import net.minecraftforge.common.Tags;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BeltBlock extends HorizontalKineticBlock implements ITE<BeltTileEntity>, ISpecialBlockItemRequirement, ITransformableBlock {

	public static final Property<BeltSlope> SLOPE = EnumProperty.create("slope", BeltSlope.class);
	public static final Property<BeltPart> PART = EnumProperty.create("part", BeltPart.class);
	public static final BooleanProperty CASING = BooleanProperty.create("casing");

	public BeltBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(SLOPE, BeltSlope.HORIZONTAL)
			.setValue(PART, BeltPart.START)
			.setValue(CASING, false));
	}

	@OnlyIn(Dist.CLIENT)
	public void initializeClient(Consumer<IBlockRenderProperties> consumer) {
		consumer.accept(new RenderProperties());
	}

	@Override
	public void fillItemCategory(CreativeModeTab p_149666_1_, NonNullList<ItemStack> p_149666_2_) {
		p_149666_2_.add(AllItems.BELT_CONNECTOR.asStack());
	}

	@Override
	protected boolean areStatesKineticallyEquivalent(BlockState oldState, BlockState newState) {
		return super.areStatesKineticallyEquivalent(oldState, newState)
			&& oldState.getValue(PART) == newState.getValue(PART);
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
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
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos,
		Player player) {
		return AllItems.BELT_CONNECTOR.asStack();
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<ItemStack> getDrops(BlockState state,
		net.minecraft.world.level.storage.loot.LootContext.Builder builder) {
		List<ItemStack> drops = super.getDrops(state, builder);
		BlockEntity tileEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		if (tileEntity instanceof BeltTileEntity && ((BeltTileEntity) tileEntity).hasPulley())
			drops.addAll(AllBlocks.SHAFT.getDefaultState()
				.getDrops(builder));
		return drops;
	}

	@Override
	public void spawnAfterBreak(BlockState state, ServerLevel worldIn, BlockPos pos, ItemStack p_220062_4_) {
		BeltTileEntity controllerTE = BeltHelper.getControllerTE(worldIn, pos);
		if (controllerTE != null)
			controllerTE.getInventory()
				.ejectAll();
	}

	@Override
	public boolean isFlammable(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return false;
	}

	@Override
	public void updateEntityAfterFallOn(BlockGetter worldIn, Entity entityIn) {
		super.updateEntityAfterFallOn(worldIn, entityIn);
		BlockPos entityPosition = entityIn.blockPosition();
		BlockPos beltPos = null;

		if (AllBlocks.BELT.has(worldIn.getBlockState(entityPosition)))
			beltPos = entityPosition;
		else if (AllBlocks.BELT.has(worldIn.getBlockState(entityPosition.below())))
			beltPos = entityPosition.below();
		if (beltPos == null)
			return;
		if (!(worldIn instanceof Level))
			return;

		entityInside(worldIn.getBlockState(beltPos), (Level) worldIn, beltPos, entityIn);
	}

	@Override
	public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
		if (!canTransportObjects(state))
			return;
		if (entityIn instanceof Player) {
			Player player = (Player) entityIn;
			if (player.isShiftKeyDown())
				return;
			if (player.getAbilities().flying)
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
					itemEntity.discard();
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
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand handIn,
		BlockHitResult hit) {
		if (player.isShiftKeyDown() || !player.mayBuild())
			return InteractionResult.PASS;
		ItemStack heldItem = player.getItemInHand(handIn);

		boolean isWrench = AllItems.WRENCH.isIn(heldItem);
		boolean isConnector = AllItems.BELT_CONNECTOR.isIn(heldItem);
		boolean isShaft = AllBlocks.SHAFT.isIn(heldItem);
		boolean isDye = heldItem.is(Tags.Items.DYES);
		boolean hasWater = EmptyingByBasin.emptyItem(world, heldItem, true)
			.getFirst()
			.getFluid()
			.isSame(Fluids.WATER);
		boolean isHand = heldItem.isEmpty() && handIn == InteractionHand.MAIN_HAND;

		if (isDye || hasWater) {
			if (!world.isClientSide)
				withTileEntityDo(world, pos, te -> te.applyColor(DyeColor.getColor(heldItem)));
			return InteractionResult.SUCCESS;
		}

		if (isConnector)
			return BeltSlicer.useConnector(state, world, pos, player, handIn, hit, new Feedback());
		if (isWrench)
			return BeltSlicer.useWrench(state, world, pos, player, handIn, hit, new Feedback());

		BeltTileEntity belt = BeltHelper.getSegmentTE(world, pos);
		if (belt == null)
			return InteractionResult.PASS;

		if (isHand) {
			BeltTileEntity controllerBelt = belt.getControllerTE();
			if (controllerBelt == null)
				return InteractionResult.PASS;
			if (world.isClientSide)
				return InteractionResult.SUCCESS;
			MutableBoolean success = new MutableBoolean(false);
			controllerBelt.getInventory()
				.applyToEachWithin(belt.index + .5f, .55f, (transportedItemStack) -> {
					player.getInventory()
						.placeItemBackInInventory(transportedItemStack.stack);
					success.setTrue();
					return TransportedResult.removeItem();
				});
			if (success.isTrue())
				world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, .2f,
					1f + Create.RANDOM.nextFloat());
		}

		if (isShaft) {
			if (state.getValue(PART) != BeltPart.MIDDLE)
				return InteractionResult.PASS;
			if (world.isClientSide)
				return InteractionResult.SUCCESS;
			if (!player.isCreative())
				heldItem.shrink(1);
			KineticTileEntity.switchToBlockState(world, pos, state.setValue(PART, BeltPart.PULLEY));
			return InteractionResult.SUCCESS;
		}

		if (AllBlocks.BRASS_CASING.isIn(heldItem)) {
			if (world.isClientSide)
				return InteractionResult.SUCCESS;
			withTileEntityDo(world, pos, te -> te.setCasingType(CasingType.BRASS));
			return InteractionResult.SUCCESS;
		}

		if (AllBlocks.ANDESITE_CASING.isIn(heldItem)) {
			if (world.isClientSide)
				return InteractionResult.SUCCESS;
			withTileEntityDo(world, pos, te -> te.setCasingType(CasingType.ANDESITE));
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		Level world = context.getLevel();
		Player player = context.getPlayer();
		BlockPos pos = context.getClickedPos();

		if (state.getValue(CASING)) {
			if (world.isClientSide)
				return InteractionResult.SUCCESS;
			withTileEntityDo(world, pos, te -> te.setCasingType(CasingType.NONE));
			return InteractionResult.SUCCESS;
		}

		if (state.getValue(PART) == BeltPart.PULLEY) {
			if (world.isClientSide)
				return InteractionResult.SUCCESS;
			KineticTileEntity.switchToBlockState(world, pos, state.setValue(PART, BeltPart.MIDDLE));
			if (player != null && !player.isCreative())
				player.getInventory()
					.placeItemBackInInventory(AllBlocks.SHAFT.asStack());
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(SLOPE, PART, CASING);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public BlockPathTypes getAiPathNodeType(BlockState state, BlockGetter world, BlockPos pos, Mob entity) {
		return BlockPathTypes.RAIL;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return BeltShapes.getShape(state);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		if (state.getBlock() != this)
			return Shapes.empty();

		VoxelShape shape = getShape(state, worldIn, pos, context);
		if (!(context instanceof EntityCollisionContext))
			return shape;

		return getTileEntityOptional(worldIn, pos).map(te -> {
			Entity entity = ((EntityCollisionContext) context).getEntity();
			if (entity == null)
				return shape;

			BeltTileEntity controller = te.getControllerTE();
			if (controller == null)
				return shape;
			if (controller.passengers == null || !controller.passengers.containsKey(entity))
				return BeltShapes.getCollisionShape(state);
			return shape;

		})
			.orElse(shape);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return state.getValue(CASING) ? RenderShape.MODEL : RenderShape.ENTITYBLOCK_ANIMATED;
	}

	public static void initBelt(Level world, BlockPos pos) {
		if (world.isClientSide)
			return;
		if (world instanceof ServerLevel && ((ServerLevel) world).getChunkSource()
			.getGenerator() instanceof DebugLevelSource)
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
			if (!world.isLoaded(nextSegmentPosition))
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
			BlockEntity tileEntity = world.getBlockEntity(beltPos);
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
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (world.isClientSide)
			return;
		if (state.getBlock() == newState.getBlock())
			return;
		if (isMoving)
			return;

		BlockEntity te = world.getBlockEntity(pos);
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
			BlockEntity tileEntity = world.getBlockEntity(currentPos);
			if (tileEntity instanceof BeltTileEntity) {
				BeltTileEntity belt = (BeltTileEntity) tileEntity;
				if (belt.isController())
					belt.getInventory()
						.ejectAll();

				hasPulley = belt.hasPulley();
			}

			world.removeBlockEntity(currentPos);
			BlockState shaftState = AllBlocks.SHAFT.getDefaultState()
				.setValue(BlockStateProperties.AXIS, getRotationAxis(currentState));
			world.setBlock(currentPos, hasPulley ? shaftState : Blocks.AIR.defaultBlockState(), 3);
			world.levelEvent(2001, currentPos, Block.getId(currentState));
		}
	}

	@Override
	public BlockState updateShape(BlockState state, Direction side, BlockState p_196271_3_, LevelAccessor world,
		BlockPos pos, BlockPos p_196271_6_) {
		if (side.getAxis()
			.isHorizontal())
			updateTunnelConnections(world, pos.above());
		return state;
	}

	private void updateTunnelConnections(LevelAccessor world, BlockPos pos) {
		Block tunnelBlock = world.getBlockState(pos)
			.getBlock();
		if (tunnelBlock instanceof BeltTunnelBlock)
			((BeltTunnelBlock) tunnelBlock).updateTunnel(world, pos);
	}

	public static List<BlockPos> getBeltChain(Level world, BlockPos controllerPos) {
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
	public BlockEntityType<? extends BeltTileEntity> getTileEntityType() {
		return AllTileEntities.BELT.get();
	}

	@Override
	public ItemRequirement getRequiredItems(BlockState state, BlockEntity te) {
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

	public BlockState transform(BlockState state, StructureTransform transform) {
		if (transform.mirror != null) {
			state = mirror(state, transform.mirror);
		}

		if (transform.rotationAxis == Direction.Axis.Y) {
			return rotate(state, transform.rotation);
		}
		return transformInner(state, transform);
	}

	protected BlockState transformInner(BlockState state, StructureTransform transform) {
		boolean halfTurn = transform.rotation == Rotation.CLOCKWISE_180;

		Direction initialDirection = state.getValue(HORIZONTAL_FACING);
		boolean diagonal =
			state.getValue(SLOPE) == BeltSlope.DOWNWARD || state.getValue(SLOPE) == BeltSlope.UPWARD;

		if (!diagonal) {
			for (int i = 0; i < transform.rotation.ordinal(); i++) {
				Direction direction = state.getValue(HORIZONTAL_FACING);
				BeltSlope slope = state.getValue(SLOPE);
				boolean vertical = slope == BeltSlope.VERTICAL;
				boolean horizontal = slope == BeltSlope.HORIZONTAL;
				boolean sideways = slope == BeltSlope.SIDEWAYS;

				Direction newDirection = direction.getOpposite();
				BeltSlope newSlope = BeltSlope.VERTICAL;

				if (vertical) {
					if (direction.getAxis() == transform.rotationAxis) {
						newDirection = direction.getCounterClockWise();
						newSlope = BeltSlope.SIDEWAYS;
					} else {
						newSlope = BeltSlope.HORIZONTAL;
						newDirection = direction;
						if (direction.getAxis() == Axis.Z)
							newDirection = direction.getOpposite();
					}
				}

				if (sideways) {
					newDirection = direction;
					if (direction.getAxis() == transform.rotationAxis)
						newSlope = BeltSlope.HORIZONTAL;
					else
						newDirection = direction.getCounterClockWise();
				}

				if (horizontal) {
					newDirection = direction;
					if (direction.getAxis() == transform.rotationAxis)
						newSlope = BeltSlope.SIDEWAYS;
					else if (direction.getAxis() != Axis.Z)
						newDirection = direction.getOpposite();
				}

				state = state.setValue(HORIZONTAL_FACING, newDirection);
				state = state.setValue(SLOPE, newSlope);
			}

		} else if (initialDirection.getAxis() != transform.rotationAxis) {
			for (int i = 0; i < transform.rotation.ordinal(); i++) {
				Direction direction = state.getValue(HORIZONTAL_FACING);
				Direction newDirection = direction.getOpposite();
				BeltSlope slope = state.getValue(SLOPE);
				boolean upward = slope == BeltSlope.UPWARD;
				boolean downward = slope == BeltSlope.DOWNWARD;

				// Rotate diagonal
				if (direction.getAxisDirection() == AxisDirection.POSITIVE ^ downward ^ direction.getAxis() == Axis.Z) {
					state = state.setValue(SLOPE, upward ? BeltSlope.DOWNWARD : BeltSlope.UPWARD);
				} else {
					state = state.setValue(HORIZONTAL_FACING, newDirection);
				}
			}

		} else if (halfTurn) {
			Direction direction = state.getValue(HORIZONTAL_FACING);
			Direction newDirection = direction.getOpposite();
			BeltSlope slope = state.getValue(SLOPE);
			boolean vertical = slope == BeltSlope.VERTICAL;

			if (diagonal) {
				state = state.setValue(SLOPE, slope == BeltSlope.UPWARD ? BeltSlope.DOWNWARD
					: slope == BeltSlope.DOWNWARD ? BeltSlope.UPWARD : slope);
			} else if (vertical) {
				state = state.setValue(HORIZONTAL_FACING, newDirection);
			}
		}

		return state;
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

	public static class RenderProperties extends ReducedDestroyEffects implements DestroyProgressRenderingHandler {
		@Override
		public boolean renderDestroyProgress(ClientLevel level, LevelRenderer renderer, int breakerId, BlockPos pos,
			int progress, BlockState blockState) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof BeltTileEntity belt) {
				for (BlockPos beltPos : BeltBlock.getBeltChain(level, belt.getController())) {
					renderer.destroyBlockProgress(beltPos.hashCode(), beltPos, progress);
				}
			}
			return false;
		}
	}

}
