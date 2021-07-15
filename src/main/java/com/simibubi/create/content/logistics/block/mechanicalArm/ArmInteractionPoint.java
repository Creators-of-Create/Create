package com.simibubi.create.content.logistics.block.mechanicalArm;

import java.util.HashMap;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.matrix.MatrixStack;
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

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.JukeboxTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;

public abstract class ArmInteractionPoint {
	public enum Mode {
		DEPOSIT, TAKE
	}

	protected BlockPos pos;
	protected BlockState state;
	protected Mode mode;

	protected LazyOptional<IItemHandler> cachedHandler;
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
		cachedHandler = LazyOptional.empty();
	}

	@OnlyIn(Dist.CLIENT)
	protected void transformFlag(MatrixStack stack) {}

	protected PartialModel getFlagType() {
		return mode == Mode.TAKE ? AllBlockPartials.FLAG_LONG_OUT : AllBlockPartials.FLAG_LONG_IN;
	}

	protected void cycleMode() {
		mode = mode == Mode.DEPOSIT ? Mode.TAKE : Mode.DEPOSIT;
	}

	protected Vector3d getInteractionPositionVector() {
		return VecHelper.getCenterOf(pos);
	}

	protected Direction getInteractionDirection() {
		return Direction.DOWN;
	}

	protected boolean isStillValid(IBlockReader reader) {
		return isValid(reader, pos, reader.getBlockState(pos));
	}

	protected void keepAlive(IWorld world) {}

	protected abstract boolean isValid(IBlockReader reader, BlockPos pos, BlockState state);

	protected static boolean isInteractable(IBlockReader reader, BlockPos pos, BlockState state) {
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
	protected IItemHandler getHandler(World world) {
		if (!cachedHandler.isPresent()) {
			TileEntity te = world.getBlockEntity(pos);
			if (te == null)
				return null;
			cachedHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);
		}
		return cachedHandler.orElse(null);
	}

	protected ItemStack insert(World world, ItemStack stack, boolean simulate) {
		IItemHandler handler = getHandler(world);
		if (handler == null)
			return stack;
		return ItemHandlerHelper.insertItem(handler, stack, simulate);
	}

	protected ItemStack extract(World world, int slot, int amount, boolean simulate) {
		IItemHandler handler = getHandler(world);
		if (handler == null)
			return ItemStack.EMPTY;
		return handler.extractItem(slot, amount, simulate);
	}

	protected ItemStack extract(World world, int slot, boolean simulate) {
		return extract(world, slot, 64, simulate);
	}

	protected int getSlotCount(World world) {
		IItemHandler handler = getHandler(world);
		if (handler == null)
			return 0;
		return handler.getSlots();
	}

	@Nullable
	protected static ArmInteractionPoint createAt(IBlockReader world, BlockPos pos) {
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

	protected CompoundNBT serialize(BlockPos anchor) {
		CompoundNBT nbt = new CompoundNBT();
		nbt.put("Pos", NBTUtil.writeBlockPos(pos.subtract(anchor)));
		NBTHelper.writeEnum(nbt, "Mode", mode);
		return nbt;
	}

	protected static ArmInteractionPoint deserialize(IBlockReader world, BlockPos anchor, CompoundNBT nbt) {
		BlockPos pos = NBTUtil.readBlockPos(nbt.getCompound("Pos"));
		ArmInteractionPoint interactionPoint = createAt(world, pos.offset(anchor));
		if (interactionPoint == null)
			return null;
		interactionPoint.mode = NBTHelper.readEnum(nbt, "Mode", Mode.class);
		return interactionPoint;
	}

	protected static void transformPos(StructureTransform transform, CompoundNBT nbt) {
		BlockPos pos = NBTUtil.readBlockPos(nbt.getCompound("Pos"));
		pos = transform.applyWithoutOffset(pos);
		nbt.put("Pos", NBTUtil.writeBlockPos(pos));
	}

	public static abstract class TopFaceArmInteractionPoint extends ArmInteractionPoint {

		@Override
		protected Vector3d getInteractionPositionVector() {
			return Vector3d.atLowerCornerOf(pos).add(.5f, 1, .5f);
		}

	}

	public static class Depot extends ArmInteractionPoint {

		@Override
		protected Vector3d getInteractionPositionVector() {
			return Vector3d.atLowerCornerOf(pos).add(.5f, 14 / 16f, .5f);
		}

		@Override
		protected boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return AllBlocks.DEPOT.has(state) || AllBlocks.WEIGHTED_EJECTOR.has(state);
		}

	}

	public static class Saw extends Depot {

		@Override
		protected boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return AllBlocks.MECHANICAL_SAW.has(state) && state.getValue(SawBlock.FACING) == Direction.UP
				&& ((KineticTileEntity) reader.getBlockEntity(pos)).getSpeed() != 0;
		}

	}

	public static class Millstone extends ArmInteractionPoint {

		@Override
		protected boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return AllBlocks.MILLSTONE.has(state);
		}

	}

	public static class CrushingWheels extends TopFaceArmInteractionPoint {

		@Override
		protected boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return AllBlocks.CRUSHING_WHEEL_CONTROLLER.has(state);
		}

	}

	public static class Composter extends TopFaceArmInteractionPoint {

		@Override
		protected Vector3d getInteractionPositionVector() {
			return Vector3d.atLowerCornerOf(pos).add(.5f, 13 / 16f, .5f);
		}

		@Override
		protected boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return Blocks.COMPOSTER.equals(state.getBlock());
		}

		@Nullable
		@Override
		protected IItemHandler getHandler(World world) {
			return new InvWrapper(
				((ComposterBlock) Blocks.COMPOSTER).getContainer(world.getBlockState(pos), world, pos));
		}
	}

	public static class Deployer extends ArmInteractionPoint {

		@Override
		protected boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return AllBlocks.DEPLOYER.has(state);
		}

		@Override
		protected Direction getInteractionDirection() {
			return state.getValue(DeployerBlock.FACING)
				.getOpposite();
		}

		@Override
		protected Vector3d getInteractionPositionVector() {
			return super.getInteractionPositionVector()
				.add(Vector3d.atLowerCornerOf(getInteractionDirection().getNormal()).scale(.65f));
		}

	}

	public static class BlazeBurner extends ArmInteractionPoint {

		@Override
		protected boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return AllBlocks.BLAZE_BURNER.has(state);
		}

		@Override
		protected ItemStack extract(World world, int slot, int amount, boolean simulate) {
			return ItemStack.EMPTY;
		}

		@Override
		protected ItemStack insert(World world, ItemStack stack, boolean simulate) {
			ItemStack input = stack.copy();
			if (!BlazeBurnerBlock.tryInsert(state, world, pos, input, false, true)
				.getObject()
				.isEmpty()) {
				return stack;
			}
			ActionResult<ItemStack> res = BlazeBurnerBlock.tryInsert(state, world, pos, input, false, simulate);
			return res.getResult() == ActionResultType.SUCCESS
				? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - 1)
				: stack;
		}

		@Override
		protected void cycleMode() {}

	}

	public static class Crafter extends ArmInteractionPoint {

		@Override
		protected boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return AllBlocks.MECHANICAL_CRAFTER.has(state);
		}

		@Override
		protected Direction getInteractionDirection() {
			return state.getValue(MechanicalCrafterBlock.HORIZONTAL_FACING)
				.getOpposite();
		}

		@Override
		protected ItemStack extract(World world, int slot, int amount, boolean simulate) {
			TileEntity te = world.getBlockEntity(pos);
			if (!(te instanceof MechanicalCrafterTileEntity))
				return ItemStack.EMPTY;
			MechanicalCrafterTileEntity crafter = (MechanicalCrafterTileEntity) te;
			SmartInventory inventory = crafter.getInventory();
			inventory.allowExtraction();
			ItemStack extract = super.extract(world, slot, amount, simulate);
			inventory.forbidExtraction();
			return extract;
		}

		@Override
		protected Vector3d getInteractionPositionVector() {
			return super.getInteractionPositionVector()
				.add(Vector3d.atLowerCornerOf(getInteractionDirection().getNormal()).scale(.5f));
		}

	}

	public static class Basin extends ArmInteractionPoint {

		@Override
		protected boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return AllBlocks.BASIN.has(state);
		}

	}

	public static class Jukebox extends TopFaceArmInteractionPoint {

		@Override
		protected boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return state.getBlock() instanceof JukeboxBlock;
		}

		@Override
		protected int getSlotCount(World world) {
			return 1;
		}

		@Override
		protected ItemStack insert(World world, ItemStack stack, boolean simulate) {
			TileEntity tileEntity = world.getBlockEntity(pos);
			if (!(tileEntity instanceof JukeboxTileEntity))
				return stack;
			if (!(state.getBlock() instanceof JukeboxBlock))
				return stack;
			JukeboxBlock jukeboxBlock = (JukeboxBlock) state.getBlock();
			JukeboxTileEntity jukeboxTE = (JukeboxTileEntity) tileEntity;
			if (!jukeboxTE.getRecord()
				.isEmpty())
				return stack;
			if (!(stack.getItem() instanceof MusicDiscItem))
				return stack;
			ItemStack remainder = stack.copy();
			ItemStack toInsert = remainder.split(1);
			if (!simulate && !world.isClientSide) {
				jukeboxBlock.setRecord(world, pos, state, toInsert);
				world.levelEvent(null, 1010, pos, Item.getId(toInsert.getItem()));
				AllTriggers.triggerForNearbyPlayers(AllTriggers.MUSICAL_ARM, world, pos, 10);
			}
			return remainder;
		}

		@Override
		protected ItemStack extract(World world, int slot, int amount, boolean simulate) {
			TileEntity tileEntity = world.getBlockEntity(pos);
			if (!(tileEntity instanceof JukeboxTileEntity))
				return ItemStack.EMPTY;
			if (!(state.getBlock() instanceof JukeboxBlock))
				return ItemStack.EMPTY;
			JukeboxTileEntity jukeboxTE = (JukeboxTileEntity) tileEntity;
			ItemStack itemstack = jukeboxTE.getRecord();
			if (itemstack.isEmpty())
				return ItemStack.EMPTY;
			if (!simulate && !world.isClientSide) {
				world.levelEvent(1010, pos, 0);
				jukeboxTE.clearContent();
				world.setBlock(pos, state.setValue(JukeboxBlock.HAS_RECORD, false), 2);
			}
			return itemstack;
		}

	}

	public static class Belt extends Depot {

		@Override
		protected boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return AllBlocks.BELT.has(state) && !(reader.getBlockState(pos.above())
				.getBlock() instanceof BeltTunnelBlock);
		}

		@Override
		protected void keepAlive(IWorld world) {
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
		protected boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return AbstractChuteBlock.isChute(state);
		}
	}

	public static class Funnel extends ArmInteractionPoint {

		@Override
		protected Vector3d getInteractionPositionVector() {
			return VecHelper.getCenterOf(pos)
				.add(Vector3d.atLowerCornerOf(FunnelBlock.getFunnelFacing(state)
					.getNormal()).scale(-.15f));
		}

		@Override
		protected int getSlotCount(World world) {
			return 0;
		}

		@Override
		protected ItemStack extract(World world, int slot, int amount, boolean simulate) {
			return ItemStack.EMPTY;
		}

		@Override
		protected Direction getInteractionDirection() {
			return FunnelBlock.getFunnelFacing(state)
				.getOpposite();
		}

		@Override
		protected ItemStack insert(World world, ItemStack stack, boolean simulate) {
			FilteringBehaviour filtering = TileEntityBehaviour.get(world, pos, FilteringBehaviour.TYPE);
			InvManipulationBehaviour inserter = TileEntityBehaviour.get(world, pos, InvManipulationBehaviour.TYPE);
			BlockState state = world.getBlockState(pos);
			if (state.getOptionalValue(BlockStateProperties.POWERED).orElse(false))
				return stack;
			if (inserter == null)
				return stack;
			if (filtering != null && !filtering.test(stack))
				return stack;
			if (simulate)
				inserter.simulate();
			ItemStack insert = inserter.insert(stack);
			if (!simulate && insert.getCount() != stack.getCount()) {
				TileEntity tileEntity = world.getBlockEntity(pos);
				if (tileEntity instanceof FunnelTileEntity) {
					FunnelTileEntity funnelTileEntity = (FunnelTileEntity) tileEntity;
					funnelTileEntity.onTransfer(stack);
					if (funnelTileEntity.hasFlap())
						funnelTileEntity.flap(true);
				}
			}
			return insert;
		}

		@Override
		protected boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return state.getBlock() instanceof AbstractFunnelBlock
				&& !(state.hasProperty(FunnelBlock.EXTRACTING) && state.getValue(FunnelBlock.EXTRACTING))
				&& !(state.hasProperty(BeltFunnelBlock.SHAPE) && state.getValue(BeltFunnelBlock.SHAPE) == Shape.PUSHING);
		}

		@Override
		protected void cycleMode() {}

	}
}
