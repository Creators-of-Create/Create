package com.simibubi.create.content.contraptions.relays.belt;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity.CasingType;
import com.simibubi.create.content.contraptions.relays.belt.transport.BeltMovementHandler.TransportedEntityInfo;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelBlock;
import com.simibubi.create.content.schematics.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
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
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BeltBlock extends HorizontalKineticBlock implements ITE<BeltTileEntity>, ISpecialBlockItemRequirement {

	public static final IProperty<BeltSlope> SLOPE = EnumProperty.create("slope", BeltSlope.class);
	public static final IProperty<BeltPart> PART = EnumProperty.create("part", BeltPart.class);
	public static final BooleanProperty CASING = BooleanProperty.create("casing");

	public BeltBlock(Properties properties) {
		super(properties);
		setDefaultState(getDefaultState().with(SLOPE, BeltSlope.HORIZONTAL)
			.with(PART, BeltPart.START)
			.with(CASING, false));
	}

	@Override
	public void fillItemGroup(ItemGroup p_149666_1_, NonNullList<ItemStack> p_149666_2_) {
		p_149666_2_.add(AllItems.BELT_CONNECTOR.asStack());
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		if (face.getAxis() != getRotationAxis(state))
			return false;
		try {
			return getTileEntity(world, pos).hasPulley();
		} catch (TileEntityException e) {
		}
		return false;
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		if (state.get(SLOPE) == BeltSlope.SIDEWAYS)
			return Axis.Y;
		return state.get(HORIZONTAL_FACING)
			.rotateY()
			.getAxis();
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos,
		PlayerEntity player) {
		return AllItems.BELT_CONNECTOR.asStack();
	}

	@Override
	public Material getMaterial(BlockState state) {
		return state.get(CASING) ? Material.WOOD : Material.WOOL;
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<ItemStack> getDrops(BlockState state, net.minecraft.world.storage.loot.LootContext.Builder builder) {
		List<ItemStack> drops = super.getDrops(state, builder);
		TileEntity tileEntity = builder.get(LootParameters.BLOCK_ENTITY);
		if (tileEntity instanceof BeltTileEntity && ((BeltTileEntity) tileEntity).hasPulley())
			drops.addAll(AllBlocks.SHAFT.getDefaultState()
				.getDrops(builder));
		return drops;
	}

	@Override
	public void spawnAdditionalDrops(BlockState state, World worldIn, BlockPos pos, ItemStack stack) {
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
	public void onLanded(IBlockReader worldIn, Entity entityIn) {
		super.onLanded(worldIn, entityIn);
		BlockPos entityPosition = entityIn.getPosition();
		BlockPos beltPos = null;

		if (AllBlocks.BELT.has(worldIn.getBlockState(entityPosition)))
			beltPos = entityPosition;
		else if (AllBlocks.BELT.has(worldIn.getBlockState(entityPosition.down())))
			beltPos = entityPosition.down();
		if (beltPos == null)
			return;
		if (!(worldIn instanceof World))
			return;

		onEntityCollision(worldIn.getBlockState(beltPos), (World) worldIn, beltPos, entityIn);
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		if (!canTransportObjects(state))
			return;
		if (entityIn instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entityIn;
			if (player.isSneaking())
				return;
			if (player.abilities.isFlying)
				return;
		}

		BeltTileEntity belt = BeltHelper.getSegmentTE(worldIn, pos);
		if (belt == null)
			return;
		if (entityIn instanceof ItemEntity && entityIn.isAlive()) {
			if (worldIn.isRemote)
				return;
			if (entityIn.getMotion().y > 0)
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
			if (info.getTicksSinceLastCollision() != 0 || pos.equals(entityIn.getPosition()))
				info.refresh(pos, state);
		} else {
			controller.passengers.put(entityIn, new TransportedEntityInfo(pos, state));
			entityIn.onGround = true;
		}
	}

	public static boolean canTransportObjects(BlockState state) {
		if (!AllBlocks.BELT.has(state))
			return false;
		BeltSlope slope = state.get(SLOPE);
		return slope != BeltSlope.VERTICAL && slope != BeltSlope.SIDEWAYS;
	}

	@Override
	public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn,
		BlockRayTraceResult hit) {
		if (player.isSneaking() || !player.isAllowEdit())
			return ActionResultType.PASS;
		ItemStack heldItem = player.getHeldItem(handIn);
		boolean isShaft = AllBlocks.SHAFT.isIn(heldItem);
		boolean isDye = Tags.Items.DYES.contains(heldItem.getItem());
		boolean isHand = heldItem.isEmpty() && handIn == Hand.MAIN_HAND;

		if (isDye) {
			if (world.isRemote)
				return ActionResultType.SUCCESS;
			withTileEntityDo(world, pos, te -> {
				DyeColor dyeColor = DyeColor.getColor(heldItem);
				if (dyeColor == null)
					return;
				te.applyColor(dyeColor);
			});
			if (!player.isCreative())
				heldItem.shrink(1);
			return ActionResultType.SUCCESS;
		}

		BeltTileEntity belt = BeltHelper.getSegmentTE(world, pos);
		if (belt == null)
			return ActionResultType.PASS;

		if (isHand) {
			BeltTileEntity controllerBelt = belt.getControllerTE();
			if (controllerBelt == null)
				return ActionResultType.PASS;
			if (world.isRemote)
				return ActionResultType.SUCCESS;
			controllerBelt.getInventory()
				.applyToEachWithin(belt.index + .5f, .55f, (transportedItemStack) -> {
					player.inventory.placeItemBackInInventory(world, transportedItemStack.stack);
					return TransportedResult.removeItem();
				});
		}

		if (isShaft) {
			if (state.get(PART) != BeltPart.MIDDLE)
				return ActionResultType.PASS;
			if (world.isRemote)
				return ActionResultType.SUCCESS;
			if (!player.isCreative())
				heldItem.shrink(1);
			KineticTileEntity.switchToBlockState(world, pos, state.with(PART, BeltPart.PULLEY));
			return ActionResultType.SUCCESS;
		}

		if (AllBlocks.BRASS_CASING.isIn(heldItem)) {
			if (world.isRemote)
				return ActionResultType.SUCCESS;
			withTileEntityDo(world, pos, te -> te.setCasingType(CasingType.BRASS));
			return ActionResultType.SUCCESS;
		}

		if (AllBlocks.ANDESITE_CASING.isIn(heldItem)) {
			if (world.isRemote)
				return ActionResultType.SUCCESS;
			withTileEntityDo(world, pos, te -> te.setCasingType(CasingType.ANDESITE));
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		World world = context.getWorld();
		PlayerEntity player = context.getPlayer();
		BlockPos pos = context.getPos();

		if (state.get(CASING)) {
			if (world.isRemote)
				return ActionResultType.SUCCESS;
			withTileEntityDo(world, pos, te -> te.setCasingType(CasingType.NONE));
			return ActionResultType.SUCCESS;
		}

		if (state.get(PART) == BeltPart.PULLEY) {
			if (world.isRemote)
				return ActionResultType.SUCCESS;
			KineticTileEntity.switchToBlockState(world, pos, state.with(PART, BeltPart.MIDDLE));
			if (player != null && !player.isCreative())
				player.inventory.placeItemBackInInventory(world, AllBlocks.SHAFT.asStack());
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.FAIL;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(SLOPE, PART, CASING);
		super.fillStateContainer(builder);
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
		try {
			if (context.getEntity() == null)
				return shape;

			BeltTileEntity belt = getTileEntity(worldIn, pos);
			BeltTileEntity controller = belt.getControllerTE();

			if (controller == null)
				return shape;
			if (controller.passengers == null || !controller.passengers.containsKey(context.getEntity())) {
				return BeltShapes.getCollisionShape(state);
			}

		} catch (TileEntityException e) {
		}
		return shape;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.BELT.create();
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return state.get(CASING) ? BlockRenderType.MODEL : BlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	public static void initBelt(World world, BlockPos pos) {
		if (world.isRemote || world.getWorldType() == WorldType.DEBUG_ALL_BLOCK_STATES)
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
			TileEntity tileEntity = world.getTileEntity(beltPos);
			BlockState currentState = world.getBlockState(beltPos);

			if (tileEntity instanceof BeltTileEntity && AllBlocks.BELT.has(currentState)) {
				BeltTileEntity te = (BeltTileEntity) tileEntity;
				te.setController(currentPos);
				te.beltLength = beltChain.size();
				te.index = index;
				te.attachKinetics();
				te.markDirty();
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
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (world.isRemote)
			return;
		if (state.getBlock() == newState.getBlock())
			return;
		if (isMoving)
			return;
		TileEntity belt = world.getTileEntity(pos);
		if (belt instanceof BeltTileEntity)
			belt.remove();

		// Destroy chain
		for (boolean forward : Iterate.trueAndFalse) {
			BlockPos currentPos = nextSegmentPosition(state, pos, forward);
			if (currentPos == null)
				continue;
			BlockState currentState = world.getBlockState(currentPos);
			if (!AllBlocks.BELT.has(currentState))
				continue;

			boolean hasPulley = false;
			TileEntity tileEntity = world.getTileEntity(currentPos);
			if (tileEntity instanceof BeltTileEntity) {
				BeltTileEntity te = (BeltTileEntity) tileEntity;
				if (te.isController())
					te.getInventory()
						.ejectAll();

				te.remove();
				hasPulley = te.hasPulley();
			}

			BlockState shaftState = AllBlocks.SHAFT.getDefaultState()
				.with(BlockStateProperties.AXIS, getRotationAxis(currentState));
			world.setBlockState(currentPos, hasPulley ? shaftState : Blocks.AIR.getDefaultState(), 3);
			world.playEvent(2001, currentPos, Block.getStateId(currentState));
		}
	}

	@Override
	public BlockState updatePostPlacement(BlockState state, Direction side, BlockState p_196271_3_, IWorld world,
		BlockPos pos, BlockPos p_196271_6_) {
		if (side.getAxis()
			.isHorizontal())
			updateTunnelConnections(world, pos.up());
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
		Direction direction = state.get(HORIZONTAL_FACING);
		BeltSlope slope = state.get(SLOPE);
		BeltPart part = state.get(PART);

		int offset = forward ? 1 : -1;

		if (part == BeltPart.END && forward || part == BeltPart.START && !forward)
			return null;
		if (slope == BeltSlope.VERTICAL)
			return pos.up(direction.getAxisDirection() == AxisDirection.POSITIVE ? offset : -offset);
		pos = pos.offset(direction, offset);
		if (slope != BeltSlope.HORIZONTAL && slope != BeltSlope.SIDEWAYS)
			return pos.up(slope == BeltSlope.UPWARD ? offset : -offset);
		return pos;
	}

	public static boolean canAccessFromSide(Direction facing, BlockState belt) {
//		if (facing == null)
//			return true;
//		if (!belt.get(BeltBlock.CASING))
//			return false;
//		BeltPart part = belt.get(BeltBlock.PART);
//		if (part != BeltPart.MIDDLE && facing.getAxis() == belt.get(HORIZONTAL_FACING)
//			.rotateY()
//			.getAxis())
//			return false;
//
//		BeltSlope slope = belt.get(BeltBlock.SLOPE);
//		if (slope != BeltSlope.HORIZONTAL) {
//			if (slope == BeltSlope.DOWNWARD && part == BeltPart.END)
//				return true;
//			if (slope == BeltSlope.UPWARD && part == BeltPart.START)
//				return true;
//			Direction beltSide = belt.get(HORIZONTAL_FACING);
//			if (slope == BeltSlope.DOWNWARD)
//				beltSide = beltSide.getOpposite();
//			if (beltSide == facing)
//				return false;
//		}

		return true;
	}

	@Override
	public Class<BeltTileEntity> getTileEntityClass() {
		return BeltTileEntity.class;
	}

	@Override
	public ItemRequirement getRequiredItems(BlockState state) {
		List<ItemStack> required = new ArrayList<>();
		if (state.get(PART) != BeltPart.MIDDLE)
			required.add(AllBlocks.SHAFT.asStack());
		if (state.get(PART) == BeltPart.START)
			required.add(AllItems.BELT_CONNECTOR.asStack());
		if (required.isEmpty())
			return ItemRequirement.NONE;
		return new ItemRequirement(ItemUseType.CONSUME, required);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		BlockState rotate = super.rotate(state, rot);

		if (state.get(SLOPE) != BeltSlope.VERTICAL)
			return rotate;
		if (state.get(HORIZONTAL_FACING)
			.getAxisDirection() != rotate.get(HORIZONTAL_FACING)
				.getAxisDirection()) {
			if (state.get(PART) == BeltPart.START)
				return rotate.with(PART, BeltPart.END);
			if (state.get(PART) == BeltPart.END)
				return rotate.with(PART, BeltPart.START);
		}

		return rotate;
	}

}
