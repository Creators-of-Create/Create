package com.simibubi.create.content.logistics.block.mechanicalArm;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterBlock;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterTileEntity;
import com.simibubi.create.content.contraptions.components.deployer.DeployerBlock;
import com.simibubi.create.content.contraptions.components.saw.SawBlock;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelBlock;
import com.simibubi.create.content.logistics.block.chute.AbstractChuteBlock;
import com.simibubi.create.content.logistics.block.funnel.FunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.FunnelTileEntity;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class ArmInteractionPoint {

	enum Mode {
		DEPOSIT, TAKE
	}

	BlockPos pos;
	BlockState state;
	Mode mode;

	private LazyOptional<IItemHandler> cachedHandler;
	private ArmAngleTarget cachedAngles;

	private static ImmutableMap<ArmInteractionPoint, Supplier<ArmInteractionPoint>> POINTS =
			ImmutableMap.<ArmInteractionPoint, Supplier<ArmInteractionPoint>>builder()
					.put(new Saw(), Saw::new)
					.put(new Belt(), Belt::new)
					.put(new Depot(), Depot::new)
					.put(new Chute(), Chute::new)
					.put(new Basin(), Basin::new)
					.put(new Funnel(), Funnel::new)
					.put(new Jukebox(), Jukebox::new)
					.put(new Crafter(), Crafter::new)
					.put(new Deployer(), Deployer::new)
					.put(new Composter(), Composter::new)
					.put(new Millstone(), Millstone::new)
					.put(new BlazeBurner(), BlazeBurner::new)
					.put(new CrushingWheels(), CrushingWheels::new)
					.build();

	public ArmInteractionPoint() {
		cachedHandler = LazyOptional.empty();
	}

	@OnlyIn(Dist.CLIENT)
	void transformFlag(MatrixStack stack) {}

	AllBlockPartials getFlagType() {
		return mode == Mode.TAKE ? AllBlockPartials.FLAG_LONG_OUT : AllBlockPartials.FLAG_LONG_IN;
	}

	void cycleMode() {
		mode = mode == Mode.DEPOSIT ? Mode.TAKE : Mode.DEPOSIT;
	}

	Vec3d getInteractionPositionVector() {
		return VecHelper.getCenterOf(pos);
	}

	Direction getInteractionDirection() {
		return Direction.DOWN;
	}

	boolean isStillValid(IBlockReader reader) {
		return isValid(reader, pos, reader.getBlockState(pos));
	}

	abstract boolean isValid(IBlockReader reader, BlockPos pos, BlockState state);

	static boolean isInteractable(IBlockReader reader, BlockPos pos, BlockState state) {
		for (ArmInteractionPoint armInteractionPoint : POINTS.keySet())
			if (armInteractionPoint.isValid(reader, pos, state))
				return true;
		return false;
	}

	ArmAngleTarget getTargetAngles(BlockPos armPos, boolean ceiling) {
		if (cachedAngles == null)
			cachedAngles = new ArmAngleTarget(armPos, getInteractionPositionVector(), getInteractionDirection(), ceiling);

		return cachedAngles;
	}

	@Nullable
	IItemHandler getHandler(World world) {
		if (!cachedHandler.isPresent()) {
			TileEntity te = world.getTileEntity(pos);
			if (te == null)
				return null;
			cachedHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);
		}
		return cachedHandler.orElse(null);
	}

	ItemStack insert(World world, ItemStack stack, boolean simulate) {
		IItemHandler handler = getHandler(world);
		if (handler == null)
			return stack;
		return ItemHandlerHelper.insertItem(handler, stack, simulate);
	}

	ItemStack extract(World world, int slot, int amount, boolean simulate) {
		IItemHandler handler = getHandler(world);
		if (handler == null)
			return ItemStack.EMPTY;
		return handler.extractItem(slot, amount, simulate);
	}

	ItemStack extract(World world, int slot, boolean simulate) {
		return extract(world, slot, 64, simulate);
	}

	int getSlotCount(World world) {
		IItemHandler handler = getHandler(world);
		if (handler == null)
			return 0;
		return handler.getSlots();
	}

	@Nullable
	static ArmInteractionPoint createAt(IBlockReader world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		ArmInteractionPoint point = null;

		for (ArmInteractionPoint armInteractionPoint : POINTS.keySet())
			if (armInteractionPoint.isValid(world, pos, state))
				point = POINTS.get(armInteractionPoint).get();

		if (point != null) {
			point.state = state;
			point.pos = pos;
			point.mode = Mode.DEPOSIT;
		}

		return point;
	}

	CompoundNBT serialize(BlockPos anchor) {
		CompoundNBT nbt = new CompoundNBT();
		nbt.put("Pos", NBTUtil.writeBlockPos(pos.subtract(anchor)));
		NBTHelper.writeEnum(nbt, "Mode", mode);
		return nbt;
	}

	static ArmInteractionPoint deserialize(IBlockReader world, BlockPos anchor, CompoundNBT nbt) {
		BlockPos pos = NBTUtil.readBlockPos(nbt.getCompound("Pos"));
		ArmInteractionPoint interactionPoint = createAt(world, pos.add(anchor));
		if (interactionPoint == null)
			return null;
		interactionPoint.mode = NBTHelper.readEnum(nbt, "Mode", Mode.class);
		return interactionPoint;
	}

	static abstract class TopFaceArmInteractionPoint extends ArmInteractionPoint {

		@Override
		Vec3d getInteractionPositionVector() {
			return new Vec3d(pos).add(.5f, 1, .5f);
		}

	}

	static class Depot extends ArmInteractionPoint {

		@Override
		Vec3d getInteractionPositionVector() {
			return new Vec3d(pos).add(.5f, 14 / 16f, .5f);
		}

		@Override
		boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return AllBlocks.DEPOT.has(state);
		}

	}

	static class Saw extends Depot {

		@Override
		boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return AllBlocks.MECHANICAL_SAW.has(state) && state.get(SawBlock.FACING) == Direction.UP
					&& ((KineticTileEntity) reader.getTileEntity(pos)).getSpeed() != 0;
		}

	}

	static class Millstone extends ArmInteractionPoint {

		@Override
		boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return AllBlocks.MILLSTONE.has(state);
		}

	}

	static class CrushingWheels extends TopFaceArmInteractionPoint {

		@Override
		boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return AllBlocks.CRUSHING_WHEEL_CONTROLLER.has(state);
		}

	}

	static class Composter extends TopFaceArmInteractionPoint {

		@Override
		Vec3d getInteractionPositionVector() {
			return new Vec3d(pos).add(.5f, 13 / 16f, .5f);
		}

		@Override
		boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return Blocks.COMPOSTER.equals(state.getBlock());
		}

		@Nullable
		@Override
		IItemHandler getHandler(World world) {
			return new InvWrapper(((ComposterBlock) Blocks.COMPOSTER).createInventory(world.getBlockState(pos), world, pos));
		}
	}

	static class Deployer extends ArmInteractionPoint {

		@Override
		boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return AllBlocks.DEPLOYER.has(state);
		}

		@Override
		Direction getInteractionDirection() {
			return state.get(DeployerBlock.FACING).getOpposite();
		}

		@Override
		Vec3d getInteractionPositionVector() {
			return super.getInteractionPositionVector().add(new Vec3d(getInteractionDirection().getDirectionVec()).scale(.65f));
		}

	}

	static class BlazeBurner extends ArmInteractionPoint {

		@Override
		boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return AllBlocks.BLAZE_BURNER.has(state);
		}

		@Override
		ItemStack extract(World world, int slot, int amount, boolean simulate) {
			return ItemStack.EMPTY;
		}

		@Override
		ItemStack insert(World world, ItemStack stack, boolean simulate) {
			ItemStack input = stack.copy();
			if (!BlazeBurnerBlock.tryInsert(state, world, pos, input, false, true).getResult().isEmpty()) {
				return stack;
			}
			ActionResult<ItemStack> res = BlazeBurnerBlock.tryInsert(state, world, pos, input, false, simulate);
			return res.getType() == ActionResultType.SUCCESS
					? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - 1)
					: stack;
		}

		@Override
		void cycleMode() {}

	}

	static class Crafter extends ArmInteractionPoint {

		@Override
		boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return AllBlocks.MECHANICAL_CRAFTER.has(state);
		}

		@Override
		Direction getInteractionDirection() {
			return state.get(MechanicalCrafterBlock.HORIZONTAL_FACING).getOpposite();
		}

		@Override
		ItemStack extract(World world, int slot, int amount, boolean simulate) {
			TileEntity te = world.getTileEntity(pos);
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
		Vec3d getInteractionPositionVector() {
			return super.getInteractionPositionVector().add(new Vec3d(getInteractionDirection().getDirectionVec()).scale(.5f));
		}

	}

	static class Basin extends ArmInteractionPoint {

		@Override
		boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return AllBlocks.BASIN.has(state);
		}

	}

	static class Jukebox extends TopFaceArmInteractionPoint {

		@Override
		boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return state.getBlock() instanceof JukeboxBlock;
		}

		@Override
		int getSlotCount(World world) {
			return 1;
		}

		@Override
		ItemStack insert(World world, ItemStack stack, boolean simulate) {
			TileEntity tileEntity = world.getTileEntity(pos);
			if (!(tileEntity instanceof JukeboxTileEntity))
				return stack;
			if (!(state.getBlock() instanceof JukeboxBlock))
				return stack;
			JukeboxBlock jukeboxBlock = (JukeboxBlock) state.getBlock();
			JukeboxTileEntity jukeboxTE = (JukeboxTileEntity) tileEntity;
			if (!jukeboxTE.getRecord().isEmpty())
				return stack;
			if (!(stack.getItem() instanceof MusicDiscItem))
				return stack;
			ItemStack remainder = stack.copy();
			ItemStack toInsert = remainder.split(1);
			if (!simulate && !world.isRemote) {
				jukeboxBlock.insertRecord(world, pos, state, toInsert);
				world.playEvent(null, 1010, pos, Item.getIdFromItem(toInsert.getItem()));
				AllTriggers.triggerForNearbyPlayers(AllTriggers.MUSICAL_ARM, world, pos, 10);
			}
			return remainder;
		}

		@Override
		ItemStack extract(World world, int slot, int amount, boolean simulate) {
			TileEntity tileEntity = world.getTileEntity(pos);
			if (!(tileEntity instanceof JukeboxTileEntity))
				return ItemStack.EMPTY;
			if (!(state.getBlock() instanceof JukeboxBlock))
				return ItemStack.EMPTY;
			JukeboxTileEntity jukeboxTE = (JukeboxTileEntity) tileEntity;
			ItemStack itemstack = jukeboxTE.getRecord();
			if (itemstack.isEmpty())
				return ItemStack.EMPTY;
			if (!simulate && !world.isRemote) {
				world.playEvent(1010, pos, 0);
				jukeboxTE.clear();
				world.setBlockState(pos, state.with(JukeboxBlock.HAS_RECORD, false), 2);
			}
			return itemstack;
		}

	}

	static class Belt extends Depot {

		@Override
		boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return AllBlocks.BELT.has(state) && !(reader.getBlockState(pos.up()).getBlock() instanceof BeltTunnelBlock);
		}
	}

	static class Chute extends TopFaceArmInteractionPoint {

		@Override
		boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return AbstractChuteBlock.isChute(state);
		}
	}

	static class Funnel extends ArmInteractionPoint {

		@Override
		Vec3d getInteractionPositionVector() {
			return VecHelper.getCenterOf(pos).add(new Vec3d(FunnelBlock.getFunnelFacing(state).getDirectionVec()).scale(-.15f));
		}

		@Override
		int getSlotCount(World world) {
			return 0;
		}

		@Override
		ItemStack extract(World world, int slot, int amount, boolean simulate) {
			return ItemStack.EMPTY;
		}

		@Override
		Direction getInteractionDirection() {
			return FunnelBlock.getFunnelFacing(state).getOpposite();
		}

		@Override
		ItemStack insert(World world, ItemStack stack, boolean simulate) {
			FilteringBehaviour filtering = TileEntityBehaviour.get(world, pos, FilteringBehaviour.TYPE);
			InvManipulationBehaviour inserter = TileEntityBehaviour.get(world, pos, InvManipulationBehaviour.TYPE);
			BlockState state = world.getBlockState(pos);
			if (state.has(BlockStateProperties.POWERED) && state.get(BlockStateProperties.POWERED))
				return stack;
			if (inserter == null)
				return stack;
			if (filtering != null && !filtering.test(stack))
				return stack;
			if (simulate)
				inserter.simulate();
			ItemStack insert = inserter.insert(stack);
			if (!simulate && insert.getCount() != stack.getCount()) {
				TileEntity tileEntity = world.getTileEntity(pos);
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
		boolean isValid(IBlockReader reader, BlockPos pos, BlockState state) {
			return state.getBlock() instanceof FunnelBlock && !state.get(FunnelBlock.EXTRACTING);
		}

		@Override
		void cycleMode() {}

	}

}
