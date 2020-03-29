package com.simibubi.create.modules.contraptions.relays.belt;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.block.IHaveColorHandler;
import com.simibubi.create.foundation.block.IHaveNoBlockItem;
import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.modules.contraptions.relays.belt.BeltMovementHandler.TransportedEntityInfo;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.DiggingParticle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
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
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BeltBlock extends HorizontalKineticBlock
		implements IHaveNoBlockItem, IWithTileEntity<BeltTileEntity>, IHaveColorHandler {

	public static final IProperty<Slope> SLOPE = EnumProperty.create("slope", Slope.class);
	public static final IProperty<Part> PART = EnumProperty.create("part", Part.class);
	public static final BooleanProperty CASING = BooleanProperty.create("casing");

	public BeltBlock() {
		super(Properties.from(Blocks.BROWN_WOOL));
		setDefaultState(getDefaultState().with(SLOPE, Slope.HORIZONTAL).with(PART, Part.START).with(CASING, false));
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		if (face.getAxis() != getRotationAxis(state))
			return false;
		BeltTileEntity beltEntity = (BeltTileEntity) world.getTileEntity(pos);
		return beltEntity != null && beltEntity.hasPulley();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(HORIZONTAL_FACING).rotateY().getAxis();
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
		if (state.get(CASING))
			drops.addAll(AllBlocks.BRASS_CASING.getDefault().getDrops(builder));
		TileEntity tileEntity = builder.get(LootParameters.BLOCK_ENTITY);
		if (tileEntity instanceof BeltTileEntity && ((BeltTileEntity) tileEntity).hasPulley())
			drops.addAll(AllBlocks.SHAFT.getDefault().getDrops(builder));
		return drops;
	}

	@Override
	public void spawnAdditionalDrops(BlockState state, World worldIn, BlockPos pos, ItemStack stack) {
		withTileEntityDo(worldIn, pos, te -> {
			if (worldIn.isRemote)
				return;
			if (te.isController()) {
				BeltInventory inv = te.getInventory();
				for (TransportedItemStack s : inv.items)
					inv.eject(s);
			}
		});
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

		if (AllBlocks.BELT.typeOf(worldIn.getBlockState(entityPosition)))
			beltPos = entityPosition;
		else if (AllBlocks.BELT.typeOf(worldIn.getBlockState(entityPosition.down())))
			beltPos = entityPosition.down();
		if (beltPos == null)
			return;
		if (!(worldIn instanceof World))
			return;

		onEntityCollision(worldIn.getBlockState(beltPos), (World) worldIn, beltPos, entityIn);
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		BeltTileEntity belt = null;
		belt = (BeltTileEntity) worldIn.getTileEntity(pos);

		if (state.get(SLOPE) == Slope.VERTICAL)
			return;
		if (entityIn instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entityIn;
			if (player.isSneaking())
				return;
			if (player.abilities.isFlying)
				return;
		}
		if (belt == null || belt.getSpeed() == 0)
			return;
		if (entityIn instanceof ItemEntity && entityIn.isAlive()) {
			if (worldIn.isRemote)
				return;
			if (entityIn.getMotion().y > 0)
				return;
			withTileEntityDo(worldIn, pos, te -> {
				ItemEntity itemEntity = (ItemEntity) entityIn;
				IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
				if (handler == null)
					return;
				ItemStack remainder = handler.insertItem(0, itemEntity.getItem().copy(), false);
				if (remainder.isEmpty())
					itemEntity.remove();
			});
			return;
		}

		BeltTileEntity controller = (BeltTileEntity) worldIn.getTileEntity(belt.getController());
		if (controller == null || controller.passengers == null)
			return;
		if (controller.passengers.containsKey(entityIn)) {
			TransportedEntityInfo info = controller.passengers.get(entityIn);
			if (info.ticksSinceLastCollision != 0 || pos.equals(entityIn.getPosition()))
				info.refresh(pos, state);
		} else {
			controller.passengers.put(entityIn, new TransportedEntityInfo(pos, state));
			entityIn.onGround = true;
		}
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		updateNeighbouringTunnel(worldIn, pos, state);
		withTileEntityDo(worldIn, pos, te -> {
			te.attachmentTracker.findAttachments(te);
		});
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		if (player.isSneaking() || !player.isAllowEdit())
			return false;
		ItemStack heldItem = player.getHeldItem(handIn);
		boolean isShaft = heldItem.getItem() == AllBlocks.SHAFT.get().asItem();
		boolean isCasing = heldItem.getItem() == AllBlocks.BRASS_CASING.get().asItem();
		boolean isDye = Tags.Items.DYES.contains(heldItem.getItem());
		boolean isHand = heldItem.isEmpty() && handIn == Hand.MAIN_HAND;

		if (isDye) {
			if (worldIn.isRemote)
				return true;
			withTileEntityDo(worldIn, pos, te -> {
				DyeColor dyeColor = DyeColor.getColor(heldItem);
				if (dyeColor == null)
					return;
				te.applyColor(dyeColor);
			});
			if (!player.isCreative())
				heldItem.shrink(1);
			return true;
		}

		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null || !(te instanceof BeltTileEntity))
			return false;
		BeltTileEntity belt = (BeltTileEntity) te;

		if (isHand) {
			BeltTileEntity controllerBelt = belt.getControllerTE();
			if (controllerBelt == null)
				return false;
			if (worldIn.isRemote)
				return true;
			controllerBelt.getInventory().forEachWithin(belt.index + .5f, .55f, (transportedItemStack) -> {
				player.inventory.placeItemBackInInventory(worldIn, transportedItemStack.stack);
				return Collections.emptyList();
			});
		}

		if (isShaft) {
			if (state.get(PART) != Part.MIDDLE)
				return false;
			if (worldIn.isRemote)
				return true;
			if (!player.isCreative())
				heldItem.shrink(1);
			worldIn.setBlockState(pos, state.with(PART, Part.PULLEY), 2);
			belt.attachKinetics();
			return true;
		}

		if (isCasing) {
			if (state.get(CASING))
				return false;
			if (state.get(SLOPE) == Slope.VERTICAL)
				return false;
			if (!player.isCreative())
				heldItem.shrink(1);
			worldIn.setBlockState(pos, state.with(CASING, true), 2);
			return true;
		}

		return false;
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		World world = context.getWorld();
		TileEntity te = world.getTileEntity(context.getPos());
		if (te == null || !(te instanceof BeltTileEntity))
			return ActionResultType.PASS;
		BeltTileEntity belt = (BeltTileEntity) te;
		PlayerEntity player = context.getPlayer();

		if (state.get(CASING)) {
			if (world.isRemote)
				return ActionResultType.SUCCESS;
			world.setBlockState(context.getPos(), state.with(CASING, false), 3);
			if (!player.isCreative())
				player.inventory.placeItemBackInInventory(world, new ItemStack(AllBlocks.BRASS_CASING.get()));
			return ActionResultType.SUCCESS;
		}

		if (state.get(PART) == Part.PULLEY) {
			if (world.isRemote)
				return ActionResultType.SUCCESS;
			world.setBlockState(context.getPos(), state.with(PART, Part.MIDDLE), 2);
			belt.detachKinetics();
			belt.attachKinetics();
			if (!player.isCreative())
				player.inventory.placeItemBackInInventory(world, new ItemStack(AllBlocks.SHAFT.get()));
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
		// From Particle Manager, but reduced density for belts with lots of boxes
		VoxelShape voxelshape = state.getShape(world, pos);
		MutableInt amtBoxes = new MutableInt(0);
		voxelshape.forEachBox((x1, y1, z1, x2, y2, z2) -> amtBoxes.increment());
		double chance = 1d / amtBoxes.getValue();

		voxelshape.forEachBox((x1, y1, z1, x2, y2, z2) -> {
			double d1 = Math.min(1.0D, x2 - x1);
			double d2 = Math.min(1.0D, y2 - y1);
			double d3 = Math.min(1.0D, z2 - z1);
			int i = Math.max(2, MathHelper.ceil(d1 / 0.25D));
			int j = Math.max(2, MathHelper.ceil(d2 / 0.25D));
			int k = Math.max(2, MathHelper.ceil(d3 / 0.25D));

			for (int l = 0; l < i; ++l) {
				for (int i1 = 0; i1 < j; ++i1) {
					for (int j1 = 0; j1 < k; ++j1) {
						if (world.rand.nextDouble() > chance)
							continue;

						double d4 = ((double) l + 0.5D) / (double) i;
						double d5 = ((double) i1 + 0.5D) / (double) j;
						double d6 = ((double) j1 + 0.5D) / (double) k;
						double d7 = d4 * d1 + x1;
						double d8 = d5 * d2 + y1;
						double d9 = d6 * d3 + z1;
						manager.addEffect(
								(new DiggingParticle(world, (double) pos.getX() + d7, (double) pos.getY() + d8,
										(double) pos.getZ() + d9, d4 - 0.5D, d5 - 0.5D, d6 - 0.5D, state))
												.setBlockPos(pos));
					}
				}
			}

		});
		return true;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return BeltShapes.getShape(state);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos,
			ISelectionContext context) {
		VoxelShape shape = getShape(state, worldIn, pos, context);
		BeltTileEntity belt = (BeltTileEntity) worldIn.getTileEntity(pos);
		if (belt == null || context.getEntity() == null)
			return shape;
		BeltTileEntity controller = (BeltTileEntity) worldIn.getTileEntity(belt.getController());
		if (controller == null)
			return shape;
		if (controller.passengers == null || !controller.passengers.containsKey(context.getEntity())) {
			return BeltShapes.getCollisionShape(state);
		}

		return shape;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new BeltTileEntity();
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return state.get(CASING) && state.get(SLOPE) != Slope.VERTICAL ? BlockRenderType.MODEL
				: BlockRenderType.ENTITYBLOCK_ANIMATED;
	}

//	@Override // TODO 1.15 register layer
//	public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
//		return state.get(CASING) && state.get(SLOPE) != Slope.VERTICAL && layer == getRenderLayer();
//	}

	public static void initBelt(World world, BlockPos pos) {
		if (world.isRemote)
			return;

		BlockState state = world.getBlockState(pos);
		if (!AllBlocks.BELT.typeOf(state))
			return;
		// Find controller
		int limit = 1000;
		BlockPos currentPos = pos;
		while (limit-- > 0) {
			BlockState currentState = world.getBlockState(currentPos);
			if (!AllBlocks.BELT.typeOf(currentState)) {
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
		List<BlockPos> beltChain = getBeltChain(world, pos);
		if (beltChain.size() < 2) {
			world.destroyBlock(pos, true);
			return;
		}

		for (BlockPos beltPos : beltChain) {
			TileEntity tileEntity = world.getTileEntity(beltPos);
			if (tileEntity instanceof BeltTileEntity) {
				BeltTileEntity te = (BeltTileEntity) tileEntity;
				te.setController(pos);
				te.beltLength = beltChain.size();
				te.index = index;
				te.attachKinetics();
				te.markDirty();
				te.sendData();

				BlockState currentState = world.getBlockState(beltPos);
				boolean isVertical = currentState.get(BeltBlock.SLOPE) == Slope.VERTICAL;

				if (currentState.get(CASING) && isVertical) {
					Block.spawnAsEntity(world, beltPos, new ItemStack(AllBlocks.BRASS_CASING.get()));
					world.setBlockState(beltPos, currentState.with(CASING, false), 2);
				}

				if (te.isController() && isVertical) {
					BeltInventory inventory = te.getInventory();
					for (TransportedItemStack s : inventory.items)
						inventory.eject(s);
					inventory.items.clear();
				}
			} else {
				world.destroyBlock(pos, true);
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

		updateNeighbouringTunnel(world, pos, state);

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
			if (!AllBlocks.BELT.typeOf(currentState))
				continue;
			if (currentState.get(CASING))
				Block.spawnAsEntity(world, currentPos, new ItemStack(AllBlocks.BRASS_CASING.get()));

			boolean hasPulley = false;
			TileEntity tileEntity = world.getTileEntity(currentPos);
			if (tileEntity instanceof BeltTileEntity) {
				BeltTileEntity te = (BeltTileEntity) tileEntity;
				if (te.isController()) {
					BeltInventory inv = te.getInventory();
					for (TransportedItemStack stack : inv.items)
						inv.eject(stack);
				}

				te.remove();
				hasPulley = te.hasPulley();
			}

			BlockState shaftState =
				AllBlocks.SHAFT.get().getDefaultState().with(BlockStateProperties.AXIS, getRotationAxis(currentState));
			world.setBlockState(currentPos, hasPulley ? shaftState : Blocks.AIR.getDefaultState(), 3);
			world.playEvent(2001, currentPos, Block.getStateId(currentState));
		}
	}

	private void updateNeighbouringTunnel(World world, BlockPos pos, BlockState beltState) {
		boolean isEnd = beltState.get(PART) != Part.END;
		if (isEnd && beltState.get(PART) != Part.START)
			return;
		int offset = isEnd ? -1 : 1;
		BlockPos tunnelPos = pos.offset(beltState.get(HORIZONTAL_FACING), offset).up();
		if (AllBlocks.BELT_TUNNEL.typeOf(world.getBlockState(tunnelPos)))
			BeltTunnelBlock.updateTunnel(world, tunnelPos);
	}

	public enum Slope implements IStringSerializable {
		HORIZONTAL, UPWARD, DOWNWARD, VERTICAL;

		@Override
		public String getName() {
			return Lang.asId(name());
		}
	}

	public enum Part implements IStringSerializable {
		START, MIDDLE, END, PULLEY;

		@Override
		public String getName() {
			return Lang.asId(name());
		}
	}

	public static List<BlockPos> getBeltChain(World world, BlockPos controllerPos) {
		List<BlockPos> positions = new LinkedList<>();

		BlockState blockState = world.getBlockState(controllerPos);
		if (!AllBlocks.BELT.typeOf(blockState))
			return positions;

		int limit = 1000;
		BlockPos current = controllerPos;
		while (limit-- > 0 && current != null) {
			positions.add(current);
			BlockState state = world.getBlockState(current);
			if (!AllBlocks.BELT.typeOf(state))
				break;
			current = nextSegmentPosition(state, current, true);
		}

		return positions;
	}

	public static BlockPos nextSegmentPosition(BlockState state, BlockPos pos, boolean forward) {
		Direction direction = state.get(HORIZONTAL_FACING);
		Slope slope = state.get(SLOPE);
		Part part = state.get(PART);

		int offset = forward ? 1 : -1;

		if (part == Part.END && forward || part == Part.START && !forward)
			return null;
		if (slope == Slope.VERTICAL)
			return pos.up(direction.getAxisDirection() == AxisDirection.POSITIVE ? offset : -offset);
		pos = pos.offset(direction, offset);
		if (slope != Slope.HORIZONTAL)
			return pos.up(slope == Slope.UPWARD ? offset : -offset);
		return pos;
	}

	@Override
	protected boolean hasStaticPart() {
		return false;
	}

	public static boolean canAccessFromSide(Direction facing, BlockState belt) {
		if (facing == null)
			return true;
		if (!belt.get(BeltBlock.CASING))
			return false;
		Part part = belt.get(BeltBlock.PART);
		if (part != Part.MIDDLE && facing.getAxis() == belt.get(HORIZONTAL_FACING).rotateY().getAxis())
			return false;

		Slope slope = belt.get(BeltBlock.SLOPE);
		if (slope != Slope.HORIZONTAL) {
			if (slope == Slope.DOWNWARD && part == Part.END)
				return true;
			if (slope == Slope.UPWARD && part == Part.START)
				return true;
			Direction beltSide = belt.get(HORIZONTAL_FACING);
			if (slope == Slope.DOWNWARD)
				beltSide = beltSide.getOpposite();
			if (beltSide == facing)
				return false;
		}

		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public IBlockColor getColorHandler() {
		return new BeltColor();
	}

}
