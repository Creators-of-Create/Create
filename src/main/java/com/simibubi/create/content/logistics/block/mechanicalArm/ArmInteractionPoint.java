package com.simibubi.create.content.logistics.block.mechanicalArm;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionCallback;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import net.minecraft.server.level.ServerLevel;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterBlock;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterTileEntity;
import com.simibubi.create.content.contraptions.components.deployer.DeployerBlock;
import com.simibubi.create.content.contraptions.components.saw.SawBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.StructureTransform;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelBlock;
import com.simibubi.create.content.logistics.block.chute.AbstractChuteBlock;
import com.simibubi.create.content.logistics.block.funnel.AbstractFunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelBlock.Shape;
import com.simibubi.create.content.logistics.block.funnel.FunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.FunnelTileEntity;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import io.github.fabricators_of_create.porting_lib.util.LazyOptional;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public abstract class ArmInteractionPoint {
	public enum Mode {
		DEPOSIT, TAKE
	}

	protected BlockPos pos;
	protected BlockState state;
	protected Mode mode;

	protected Map<Level, BlockApiCache<Storage<ItemVariant>, Direction>> handlerCaches = new IdentityHashMap<>();
	protected ArmAngleTarget cachedAngles;

	protected static final HashMap<ArmInteractionPoint, Supplier<ArmInteractionPoint>> POINTS = new HashMap<>();

	static {
		addPoint(new Saw(), Saw::new);
		addPoint(new Belt(), Belt::new);
		addPoint(new Depot(), Depot::new);
		addPoint(new Chute(), Chute::new);
		addPoint(new Basin(), Basin::new);
		addPoint(new Funnel(), Funnel::new);
		addPoint(new Jukebox(), Jukebox::new);
		addPoint(new Crafter(), Crafter::new);
		addPoint(new Deployer(), Deployer::new);
		addPoint(new Composter(), Composter::new);
		addPoint(new Millstone(), Millstone::new);
		addPoint(new BlazeBurner(), BlazeBurner::new);
		addPoint(new CrushingWheels(), CrushingWheels::new);
	}

	public static void addPoint(ArmInteractionPoint instance, Supplier<ArmInteractionPoint> factory) {
		if (POINTS.containsKey(instance))
			Create.LOGGER.warn("Point for " + instance.getClass().getSimpleName() + " was overridden");
		POINTS.put(instance, factory);
	}

	public ArmInteractionPoint() {
	}

	@Environment(EnvType.CLIENT)
	protected void transformFlag(PoseStack stack) {}

	protected PartialModel getFlagType() {
		return mode == Mode.TAKE ? AllBlockPartials.FLAG_LONG_OUT : AllBlockPartials.FLAG_LONG_IN;
	}

	protected void cycleMode() {
		mode = mode == Mode.DEPOSIT ? Mode.TAKE : Mode.DEPOSIT;
	}

	protected Vec3 getInteractionPositionVector() {
		return VecHelper.getCenterOf(pos);
	}

	protected Direction getInteractionDirection() {
		return Direction.DOWN;
	}

	protected boolean isStillValid(BlockGetter reader) {
		return isValid(reader, pos, reader.getBlockState(pos));
	}

	protected void keepAlive(LevelAccessor world) {}

	protected abstract boolean isValid(BlockGetter reader, BlockPos pos, BlockState state);

	protected static boolean isInteractable(BlockGetter reader, BlockPos pos, BlockState state) {
		for (ArmInteractionPoint armInteractionPoint : POINTS.keySet())
			if (armInteractionPoint.isValid(reader, pos, state))
				return true;
		return false;
	}

	protected ArmAngleTarget getTargetAngles(BlockPos armPos, boolean ceiling) {
		if (cachedAngles == null)
			cachedAngles =
				new ArmAngleTarget(armPos, getInteractionPositionVector(), getInteractionDirection(), ceiling);

		return cachedAngles;
	}

	@Nullable
	protected Storage<ItemVariant> getHandler(Level world) {
		BlockApiCache<Storage<ItemVariant>, Direction> cache = getHandlerCache(world);
		return cache == null ? null : cache.find(Direction.UP);
	}

	protected BlockApiCache<Storage<ItemVariant>, Direction> getHandlerCache(Level level) {
		return handlerCaches.computeIfAbsent(level, $ -> TransferUtil.getItemCache(level, pos));
	}

	protected ItemStack insert(Level world, ItemStack stack, TransactionContext ctx) {
		Storage<ItemVariant> handler = getHandler(world);
		if (handler == null)
			return stack;
		long inserted = handler.insert(ItemVariant.of(stack), stack.getCount(), ctx);
		return ItemHandlerHelper.copyStackWithSize(stack, (int) (stack.getCount() - inserted));
	}

	protected ItemStack extract(Level world, int amount, TransactionContext ctx) {
		Storage<ItemVariant> handler = getHandler(world);
		if (handler == null)
			return ItemStack.EMPTY;
		return TransferUtil.extractAnyItem(handler, amount);
	}

	protected ItemStack extract(Level world, TransactionContext ctx) {
		return extract(world, 64, ctx);
	}

	@Nullable
	protected static ArmInteractionPoint createAt(BlockGetter world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		ArmInteractionPoint point = null;

		for (ArmInteractionPoint armInteractionPoint : POINTS.keySet())
			if (armInteractionPoint.isValid(world, pos, state))
				point = POINTS.get(armInteractionPoint)
					.get();

		if (point != null) {
			point.state = state;
			point.pos = pos;
			point.mode = Mode.DEPOSIT;
		}

		return point;
	}

	protected CompoundTag serialize(BlockPos anchor) {
		CompoundTag nbt = new CompoundTag();
		nbt.put("Pos", NbtUtils.writeBlockPos(pos.subtract(anchor)));
		NBTHelper.writeEnum(nbt, "Mode", mode);
		return nbt;
	}

	protected static ArmInteractionPoint deserialize(BlockGetter world, BlockPos anchor, CompoundTag nbt) {
		BlockPos pos = NbtUtils.readBlockPos(nbt.getCompound("Pos"));
		ArmInteractionPoint interactionPoint = createAt(world, pos.offset(anchor));
		if (interactionPoint == null)
			return null;
		interactionPoint.mode = NBTHelper.readEnum(nbt, "Mode", Mode.class);
		return interactionPoint;
	}

	protected static void transformPos(StructureTransform transform, CompoundTag nbt) {
		BlockPos pos = NbtUtils.readBlockPos(nbt.getCompound("Pos"));
		pos = transform.applyWithoutOffset(pos);
		nbt.put("Pos", NbtUtils.writeBlockPos(pos));
	}

	public static abstract class TopFaceArmInteractionPoint extends ArmInteractionPoint {

		@Override
		protected Vec3 getInteractionPositionVector() {
			return Vec3.atLowerCornerOf(pos).add(.5f, 1, .5f);
		}

	}

	public static class Depot extends ArmInteractionPoint {

		@Override
		protected Vec3 getInteractionPositionVector() {
			return Vec3.atLowerCornerOf(pos).add(.5f, 14 / 16f, .5f);
		}

		@Override
		protected boolean isValid(BlockGetter reader, BlockPos pos, BlockState state) {
			return AllBlocks.DEPOT.has(state) || AllBlocks.WEIGHTED_EJECTOR.has(state);
		}

	}

	public static class Saw extends Depot {

		@Override
		protected boolean isValid(BlockGetter reader, BlockPos pos, BlockState state) {
			return AllBlocks.MECHANICAL_SAW.has(state) && state.getValue(SawBlock.FACING) == Direction.UP
				&& ((KineticTileEntity) reader.getBlockEntity(pos)).getSpeed() != 0;
		}

	}

	public static class Millstone extends ArmInteractionPoint {

		@Override
		protected boolean isValid(BlockGetter reader, BlockPos pos, BlockState state) {
			return AllBlocks.MILLSTONE.has(state);
		}

	}

	public static class CrushingWheels extends TopFaceArmInteractionPoint {

		@Override
		protected boolean isValid(BlockGetter reader, BlockPos pos, BlockState state) {
			return AllBlocks.CRUSHING_WHEEL_CONTROLLER.has(state);
		}

	}

	public static class Composter extends TopFaceArmInteractionPoint {

		@Override
		protected Vec3 getInteractionPositionVector() {
			return Vec3.atLowerCornerOf(pos).add(.5f, 13 / 16f, .5f);
		}

		@Override
		protected boolean isValid(BlockGetter reader, BlockPos pos, BlockState state) {
			return Blocks.COMPOSTER.equals(state.getBlock());
		}

//		@Nullable
//		@Override
//		protected Storage<ItemVariant> getHandler(Level world) {
//			return new InvWrapper(
//				((ComposterBlock) Blocks.COMPOSTER).getContainer(world.getBlockState(pos), world, pos));
//		}
	}

	public static class Deployer extends ArmInteractionPoint {

		@Override
		protected boolean isValid(BlockGetter reader, BlockPos pos, BlockState state) {
			return AllBlocks.DEPLOYER.has(state);
		}

		@Override
		protected Direction getInteractionDirection() {
			return state.getValue(DeployerBlock.FACING)
				.getOpposite();
		}

		@Override
		protected Vec3 getInteractionPositionVector() {
			return super.getInteractionPositionVector()
				.add(Vec3.atLowerCornerOf(getInteractionDirection().getNormal()).scale(.65f));
		}

	}

	public static class BlazeBurner extends ArmInteractionPoint {

		@Override
		protected boolean isValid(BlockGetter reader, BlockPos pos, BlockState state) {
			return AllBlocks.BLAZE_BURNER.has(state);
		}

		@Override
		protected ItemStack extract(Level world, int amount, TransactionContext ctx) {
			return ItemStack.EMPTY;
		}

		@Override
		protected ItemStack insert(Level world, ItemStack stack, TransactionContext ctx) {
			ItemStack input = stack.copy();
			InteractionResultHolder<ItemStack> res = BlazeBurnerBlock.tryInsert(state, world, pos, input, false, false, ctx);
			if (!res.getObject().isEmpty())
				return stack;
			return res.getResult() == InteractionResult.SUCCESS
				? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - 1)
				: stack;
		}

		@Override
		protected void cycleMode() {}

	}

	public static class Crafter extends ArmInteractionPoint {

		@Override
		protected boolean isValid(BlockGetter reader, BlockPos pos, BlockState state) {
			return AllBlocks.MECHANICAL_CRAFTER.has(state);
		}

		@Override
		protected Direction getInteractionDirection() {
			return state.getValue(MechanicalCrafterBlock.HORIZONTAL_FACING)
				.getOpposite();
		}

		@Override
		protected ItemStack extract(Level world, int amount, TransactionContext ctx) {
			BlockEntity te = world.getBlockEntity(pos);
			if (!(te instanceof MechanicalCrafterTileEntity))
				return ItemStack.EMPTY;
			MechanicalCrafterTileEntity crafter = (MechanicalCrafterTileEntity) te;
			SmartInventory inventory = crafter.getInventory();
			inventory.allowExtraction();
			ItemStack extract = super.extract(world, amount, ctx);
			inventory.forbidExtraction();
			return extract;
		}

		@Override
		protected Vec3 getInteractionPositionVector() {
			return super.getInteractionPositionVector()
				.add(Vec3.atLowerCornerOf(getInteractionDirection().getNormal()).scale(.5f));
		}

	}

	public static class Basin extends ArmInteractionPoint {

		@Override
		protected boolean isValid(BlockGetter reader, BlockPos pos, BlockState state) {
			return AllBlocks.BASIN.has(state);
		}

	}

	public static class Jukebox extends TopFaceArmInteractionPoint {

		@Override
		protected boolean isValid(BlockGetter reader, BlockPos pos, BlockState state) {
			return state.getBlock() instanceof JukeboxBlock;
		}

		@Override
		protected ItemStack insert(Level world, ItemStack stack, TransactionContext ctx) {
			BlockEntity tileEntity = world.getBlockEntity(pos);
			if (!(tileEntity instanceof JukeboxBlockEntity))
				return stack;
			if (!(state.getBlock() instanceof JukeboxBlock))
				return stack;
			JukeboxBlock jukeboxBlock = (JukeboxBlock) state.getBlock();
			JukeboxBlockEntity jukeboxTE = (JukeboxBlockEntity) tileEntity;
			if (!jukeboxTE.getRecord()
				.isEmpty())
				return stack;
			if (!(stack.getItem() instanceof RecordItem))
				return stack;
			ItemStack remainder = stack.copy();
			ItemStack toInsert = remainder.split(1);
			if (!world.isClientSide) {
				TransactionCallback.onSuccess(ctx, () -> {
					jukeboxBlock.setRecord(world, pos, state, toInsert);
					world.levelEvent(null, 1010, pos, Item.getId(toInsert.getItem()));
					AllTriggers.triggerForNearbyPlayers(AllTriggers.MUSICAL_ARM, world, pos, 10);
				});
			}
			return remainder;
		}

		@Override
		protected ItemStack extract(Level world, int amount, TransactionContext ctx) {
			BlockEntity tileEntity = world.getBlockEntity(pos);
			if (!(tileEntity instanceof JukeboxBlockEntity))
				return ItemStack.EMPTY;
			if (!(state.getBlock() instanceof JukeboxBlock))
				return ItemStack.EMPTY;
			JukeboxBlockEntity jukeboxTE = (JukeboxBlockEntity) tileEntity;
			ItemStack itemstack = jukeboxTE.getRecord();
			if (itemstack.isEmpty())
				return ItemStack.EMPTY;
			if (!world.isClientSide) {
				TransactionCallback.onSuccess(ctx, () -> {
					world.levelEvent(1010, pos, 0);
					jukeboxTE.clearContent();
				});
				TransactionCallback.setBlock(ctx, world, pos, state.setValue(JukeboxBlock.HAS_RECORD, false), 2);
			}
			return itemstack;
		}

	}

	public static class Belt extends Depot {

		@Override
		protected boolean isValid(BlockGetter reader, BlockPos pos, BlockState state) {
			return AllBlocks.BELT.has(state) && !(reader.getBlockState(pos.above())
				.getBlock() instanceof BeltTunnelBlock);
		}

		@Override
		protected void keepAlive(LevelAccessor world) {
			super.keepAlive(world);
			BeltTileEntity beltTE = BeltHelper.getSegmentTE(world, pos);
			if (beltTE == null)
				return;
			TransportedItemStackHandlerBehaviour transport =
				beltTE.getBehaviour(TransportedItemStackHandlerBehaviour.TYPE);
			if (transport == null)
				return;
			MutableBoolean found = new MutableBoolean(false);
			transport.handleProcessingOnAllItems(tis -> {
				if (found.isTrue())
					return TransportedResult.doNothing();
				tis.lockedExternally = true;
				found.setTrue();
				return TransportedResult.doNothing();
			});
		}

	}

	public static class Chute extends TopFaceArmInteractionPoint {

		@Override
		protected boolean isValid(BlockGetter reader, BlockPos pos, BlockState state) {
			return AbstractChuteBlock.isChute(state);
		}
	}

	public static class Funnel extends ArmInteractionPoint {

		@Override
		protected Vec3 getInteractionPositionVector() {
			return VecHelper.getCenterOf(pos)
				.add(Vec3.atLowerCornerOf(FunnelBlock.getFunnelFacing(state)
					.getNormal()).scale(-.15f));
		}

		@Override
		protected ItemStack extract(Level world, int amount, TransactionContext ctx) {
			return ItemStack.EMPTY;
		}

		@Override
		protected Direction getInteractionDirection() {
			return FunnelBlock.getFunnelFacing(state)
				.getOpposite();
		}

		@Override
		protected ItemStack insert(Level world, ItemStack stack, TransactionContext ctx) {
			FilteringBehaviour filtering = TileEntityBehaviour.get(world, pos, FilteringBehaviour.TYPE);
			InvManipulationBehaviour inserter = TileEntityBehaviour.get(world, pos, InvManipulationBehaviour.TYPE);
			BlockState state = world.getBlockState(pos);
			if (state.getOptionalValue(BlockStateProperties.POWERED).orElse(false))
				return stack;
			if (inserter == null)
				return stack;
			if (filtering != null && !filtering.test(stack))
				return stack;
			inserter.simulate();
			ItemStack insert = inserter.insert(stack);
			if (insert.getCount() != stack.getCount()) {
				TransactionCallback.onSuccess(ctx, () -> {
					inserter.insert(stack);
					BlockEntity tileEntity = world.getBlockEntity(pos);
					if (tileEntity instanceof FunnelTileEntity) {
						FunnelTileEntity funnelTileEntity = (FunnelTileEntity) tileEntity;
						funnelTileEntity.onTransfer(stack);
						if (funnelTileEntity.hasFlap())
							funnelTileEntity.flap(true);
					}
				});
			}
			return insert;
		}

		@Override
		protected boolean isValid(BlockGetter reader, BlockPos pos, BlockState state) {
			return state.getBlock() instanceof AbstractFunnelBlock
				&& !(state.hasProperty(FunnelBlock.EXTRACTING) && state.getValue(FunnelBlock.EXTRACTING))
				&& !(state.hasProperty(BeltFunnelBlock.SHAPE) && state.getValue(BeltFunnelBlock.SHAPE) == Shape.PUSHING);
		}

		@Override
		protected void cycleMode() {}

	}
}
