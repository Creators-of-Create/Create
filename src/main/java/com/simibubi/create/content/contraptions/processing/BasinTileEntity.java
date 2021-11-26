package com.simibubi.create.content.contraptions.processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.contraptions.components.mixer.MechanicalMixerTileEntity;
import com.simibubi.create.content.contraptions.fluids.FluidFX;
import com.simibubi.create.content.contraptions.fluids.particle.FluidParticleData;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import com.simibubi.create.lib.transfer.TransferUtil;
import com.simibubi.create.lib.transfer.fluid.FluidStack;
import com.simibubi.create.lib.transfer.fluid.FluidTransferable;
import com.simibubi.create.lib.transfer.fluid.IFluidHandler;

import com.simibubi.create.lib.transfer.fluid.IFluidHandlerItem;
import com.simibubi.create.lib.transfer.item.CombinedInvWrapper;
import com.simibubi.create.lib.transfer.item.IItemHandler;
import com.simibubi.create.lib.transfer.item.IItemHandlerModifiable;

import com.simibubi.create.lib.transfer.item.ItemHandlerHelper;
import com.simibubi.create.lib.transfer.item.ItemTransferable;
import com.simibubi.create.lib.utility.NBT;
import com.simibubi.create.lib.utility.LazyOptional;
import com.simibubi.create.lib.utility.NBTSerializer;

import org.jetbrains.annotations.Nullable;

public class BasinTileEntity extends SmartTileEntity implements IHaveGoggleInformation, FluidTransferable, ItemTransferable {

	private boolean areFluidsMoving;
	LerpedFloat ingredientRotationSpeed;
	LerpedFloat ingredientRotation;

	public BasinInventory inputInventory;
	public SmartFluidTankBehaviour inputTank;
	protected SmartInventory outputInventory;
	protected SmartFluidTankBehaviour outputTank;
	private FilteringBehaviour filtering;
	private boolean contentsChanged;

	private Couple<SmartInventory> invs;
	private Couple<SmartFluidTankBehaviour> tanks;

	protected LazyOptional<IItemHandlerModifiable> itemCapability;
	protected LazyOptional<IFluidHandler> fluidCapability;

	List<Direction> disabledSpoutputs;
	Direction preferredSpoutput;
	protected List<ItemStack> spoutputBuffer;
	protected List<FluidStack> spoutputFluidBuffer;

	public static final int OUTPUT_ANIMATION_TIME = 10;
	List<IntAttached<ItemStack>> visualizedOutputItems;
	List<IntAttached<FluidStack>> visualizedOutputFluids;

	public BasinTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		inputInventory = new BasinInventory(9, this);
		inputInventory.whenContentsChanged($ -> contentsChanged = true);
		outputInventory = new BasinInventory(9, this).forbidInsertion()
			.withMaxStackSize(64);
		areFluidsMoving = false;
		itemCapability = LazyOptional.of(() -> new CombinedInvWrapper(inputInventory, outputInventory));
		contentsChanged = true;
		ingredientRotation = LerpedFloat.angular()
			.startWithValue(0);
		ingredientRotationSpeed = LerpedFloat.linear()
			.startWithValue(0);

		invs = Couple.create(inputInventory, outputInventory);
		tanks = Couple.create(inputTank, outputTank);
		visualizedOutputItems = Collections.synchronizedList(new ArrayList<>());
		visualizedOutputFluids = Collections.synchronizedList(new ArrayList<>());
		disabledSpoutputs = new ArrayList<>();
		preferredSpoutput = null;
		spoutputBuffer = new ArrayList<>();
		spoutputFluidBuffer = new ArrayList<>();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new DirectBeltInputBehaviour(this));
		filtering = new FilteringBehaviour(this, new BasinValueBox()).moveText(new Vec3(2, -8, 0))
			.withCallback(newFilter -> contentsChanged = true)
			.forRecipes();
		behaviours.add(filtering);

		inputTank = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.INPUT, this, 2, 1000, true)
			.whenFluidUpdates(() -> contentsChanged = true);
		outputTank = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.OUTPUT, this, 2, 1000, true)
			.whenFluidUpdates(() -> contentsChanged = true)
			.forbidInsertion();
		behaviours.add(inputTank);
		behaviours.add(outputTank);

		fluidCapability = LazyOptional.of(() -> {
			LazyOptional<? extends IFluidHandler> inputCap = inputTank.getCapability();
			LazyOptional<? extends IFluidHandler> outputCap = outputTank.getCapability();
			return new CombinedTankWrapper(inputCap.orElse(null), outputCap.orElse(null));
		});
	}

	@Override
	protected void fromTag(CompoundTag compound, boolean clientPacket) {
		super.fromTag(compound, clientPacket);
		inputInventory.create$deserializeNBT(compound.getCompound("InputItems"));
		outputInventory.create$deserializeNBT(compound.getCompound("OutputItems"));

		preferredSpoutput = null;
		if (compound.contains("PreferredSpoutput"))
			preferredSpoutput = NBTHelper.readEnum(compound, "PreferredSpoutput", Direction.class);
		disabledSpoutputs.clear();
		ListTag disabledList = compound.getList("DisabledSpoutput", NBT.TAG_STRING);
		disabledList.forEach(d -> disabledSpoutputs.add(Direction.valueOf(((StringTag) d).getAsString())));
		spoutputBuffer = NBTHelper.readItemList(compound.getList("Overflow", NBT.TAG_COMPOUND));
		spoutputFluidBuffer = NBTHelper.readCompoundList(compound.getList("FluidOverflow", NBT.TAG_COMPOUND),
			FluidStack::loadFluidStackFromNBT);

		if (!clientPacket)
			return;

		NBTHelper.iterateCompoundList(compound.getList("VisualizedItems", NBT.TAG_COMPOUND),
			c -> visualizedOutputItems.add(IntAttached.with(OUTPUT_ANIMATION_TIME, ItemStack.of(c))));
		NBTHelper.iterateCompoundList(compound.getList("VisualizedFluids", NBT.TAG_COMPOUND),
			c -> visualizedOutputFluids
				.add(IntAttached.with(OUTPUT_ANIMATION_TIME, FluidStack.loadFluidStackFromNBT(c))));
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.put("InputItems", inputInventory.create$serializeNBT());
		compound.put("OutputItems", outputInventory.create$serializeNBT());

		if (preferredSpoutput != null)
			NBTHelper.writeEnum(compound, "PreferredSpoutput", preferredSpoutput);
		ListTag disabledList = new ListTag();
		disabledSpoutputs.forEach(d -> disabledList.add(StringTag.valueOf(d.name())));
		compound.put("DisabledSpoutput", disabledList);
		compound.put("Overflow", NBTHelper.writeItemList(spoutputBuffer));
		compound.put("FluidOverflow",
			NBTHelper.writeCompoundList(spoutputFluidBuffer, fs -> fs.writeToNBT(new CompoundTag())));

		if (!clientPacket)
			return;

		compound.put("VisualizedItems", NBTHelper.writeCompoundList(visualizedOutputItems, ia -> NBTSerializer.serializeNBT(ia.getValue())));
		compound.put("VisualizedFluids", NBTHelper.writeCompoundList(visualizedOutputFluids, ia -> ia.getValue()
			.writeToNBT(new CompoundTag())));
		visualizedOutputItems.clear();
		visualizedOutputFluids.clear();
	}

	public void onEmptied() {
		getOperator().ifPresent(te -> te.basinRemoved = true);
	}

	@Override
	public void setRemoved() {
		onEmptied();
		itemCapability.invalidate();
		fluidCapability.invalidate();
		super.setRemoved();
	}

//	@Nonnull
//	@Override
//	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
//		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
//			return itemCapability.cast();
//		if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
//			return fluidCapability.cast();
//		return super.getCapability(cap, side);
//	}

	@Override
	public void notifyUpdate() {
		super.notifyUpdate();
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		updateSpoutput();
		if (!level.isClientSide)
			return;

		BlockEntity tileEntity = level.getBlockEntity(worldPosition.above(2));
		if (!(tileEntity instanceof MechanicalMixerTileEntity)) {
			setAreFluidsMoving(false);
			return;
		}
		MechanicalMixerTileEntity mixer = (MechanicalMixerTileEntity) tileEntity;
		setAreFluidsMoving(mixer.running && mixer.runningTicks <= 20);
	}

	public void onWrenched(Direction face) {
		BlockState blockState = getBlockState();
		Direction currentFacing = blockState.getValue(BasinBlock.FACING);

		disabledSpoutputs.remove(face);
		if (currentFacing == face) {
			if (preferredSpoutput == face)
				preferredSpoutput = null;
			disabledSpoutputs.add(face);
		} else
			preferredSpoutput = face;

		updateSpoutput();
	}

	private void updateSpoutput() {
		if (level.isClientSide)
			return;

		BlockState blockState = getBlockState();
		Direction currentFacing = blockState.getValue(BasinBlock.FACING);

		if (currentFacing != Direction.DOWN)
			notifyChangeOfContents();

		Direction newFacing = Direction.DOWN;
		for (Direction test : Iterate.horizontalDirections) {
			boolean canOutputTo = BasinBlock.canOutputTo(level, worldPosition, test);
			if (canOutputTo && !disabledSpoutputs.contains(test))
				newFacing = test;
		}

		if (preferredSpoutput != null && BasinBlock.canOutputTo(level, worldPosition, preferredSpoutput)
			&& preferredSpoutput != Direction.UP)
			newFacing = preferredSpoutput;

		if (newFacing != currentFacing)
			level.setBlockAndUpdate(worldPosition, blockState.setValue(BasinBlock.FACING, newFacing));
	}

	@Override
	public void tick() {
		super.tick();
		if (level.isClientSide) {
			createFluidParticles();
			tickVisualizedOutputs();
			ingredientRotationSpeed.tickChaser();
			ingredientRotation.setValue(ingredientRotation.getValue() + ingredientRotationSpeed.getValue());
		}

		if ((!spoutputBuffer.isEmpty() || !spoutputFluidBuffer.isEmpty()) && !level.isClientSide)
			tryClearingSpoutputOverflow();
		if (!contentsChanged)
			return;

		contentsChanged = false;
		getOperator().ifPresent(te -> te.basinChecker.scheduleUpdate());

		for (Direction offset : Iterate.horizontalDirections) {
			BlockPos toUpdate = worldPosition.above()
				.relative(offset);
			BlockState stateToUpdate = level.getBlockState(toUpdate);
			if (stateToUpdate.getBlock() instanceof BasinBlock
				&& stateToUpdate.getValue(BasinBlock.FACING) == offset.getOpposite()) {
				BlockEntity te = level.getBlockEntity(toUpdate);
				if (te instanceof BasinTileEntity)
					((BasinTileEntity) te).contentsChanged = true;
			}
		}
	}

	private void tryClearingSpoutputOverflow() {
		BlockState blockState = getBlockState();
		if (!(blockState.getBlock() instanceof BasinBlock))
			return;
		Direction direction = blockState.getValue(BasinBlock.FACING);
		BlockEntity te = level.getBlockEntity(worldPosition.below()
			.relative(direction));

		FilteringBehaviour filter = null;
		InvManipulationBehaviour inserter = null;
		if (te != null) {
			filter = TileEntityBehaviour.get(level, te.getBlockPos(), FilteringBehaviour.TYPE);
			inserter = TileEntityBehaviour.get(level, te.getBlockPos(), InvManipulationBehaviour.TYPE);
		}

		IItemHandler targetInv = te == null ? null
				: TransferUtil.getItemHandler(te, direction.getOpposite())
				.orElse(inserter == null ? null : inserter.getInventory());

		IFluidHandler targetTank = te == null ? null
			: TransferUtil.getFluidHandler(te, direction.getOpposite())
				.orElse(null);

		boolean update = false;

		for (Iterator<ItemStack> iterator = spoutputBuffer.iterator(); iterator.hasNext();) {
			ItemStack itemStack = iterator.next();

			if (direction == Direction.DOWN) {
				Block.popResource(level, worldPosition, itemStack);
				iterator.remove();
				update = true;
				continue;
			}

			if (targetInv == null)
				break;
			if (!ItemHandlerHelper.insertItemStacked(targetInv, itemStack, true)
				.isEmpty())
				continue;
			if (filter != null && !filter.test(itemStack))
				continue;

			update = true;
			ItemHandlerHelper.insertItemStacked(targetInv, itemStack.copy(), false);
			iterator.remove();
			visualizedOutputItems.add(IntAttached.withZero(itemStack));
		}

		for (Iterator<FluidStack> iterator = spoutputFluidBuffer.iterator(); iterator.hasNext();) {
			FluidStack fluidStack = iterator.next();

			if (direction == Direction.DOWN) {
				iterator.remove();
				update = true;
				continue;
			}

			if (targetTank == null)
				break;

			for (boolean simulate : Iterate.trueAndFalse) {
				long fill = targetTank instanceof SmartFluidTankBehaviour.InternalFluidHandler
						? ((SmartFluidTankBehaviour.InternalFluidHandler) targetTank).forceFill(fluidStack.copy(), simulate)
					: targetTank.fill(fluidStack.copy(), simulate);
				if (fill != fluidStack.getAmount())
					break;
				if (simulate)
					continue;

				update = true;
				iterator.remove();
				visualizedOutputFluids.add(IntAttached.withZero(fluidStack));
			}
		}

		if (update) {
			notifyChangeOfContents();
			sendData();
		}
	}

	public float getTotalFluidUnits(float partialTicks) {
		int renderedFluids = 0;
		float totalUnits = 0;

		for (SmartFluidTankBehaviour behaviour : getTanks()) {
			if (behaviour == null)
				continue;
			for (TankSegment tankSegment : behaviour.getTanks()) {
				if (tankSegment.getRenderedFluid()
					.isEmpty())
					continue;
				float units = tankSegment.getTotalUnits(partialTicks);
				if (units < 1)
					continue;
				totalUnits += units;
				renderedFluids++;
			}
		}

		if (renderedFluids == 0)
			return 0;
		if (totalUnits < 1)
			return 0;
		return totalUnits;
	}

	private Optional<BasinOperatingTileEntity> getOperator() {
		if (level == null)
			return Optional.empty();
		BlockEntity te = level.getBlockEntity(worldPosition.above(2));
		if (te instanceof BasinOperatingTileEntity)
			return Optional.of((BasinOperatingTileEntity) te);
		return Optional.empty();
	}

	public FilteringBehaviour getFilter() {
		return filtering;
	}

	public void notifyChangeOfContents() {
		contentsChanged = true;
	}

	public SmartInventory getInputInventory() {
		return inputInventory;
	}

	public SmartInventory getOutputInventory() {
		return outputInventory;
	}

	public boolean canContinueProcessing() {
		return spoutputBuffer.isEmpty() && spoutputFluidBuffer.isEmpty();
	}

	public boolean acceptOutputs(List<ItemStack> outputItems, List<FluidStack> outputFluids, boolean simulate) {
		outputInventory.allowInsertion();
		outputTank.allowInsertion();
		boolean acceptOutputsInner = acceptOutputsInner(outputItems, outputFluids, simulate);
		outputInventory.forbidInsertion();
		outputTank.forbidInsertion();
		return acceptOutputsInner;
	}

	private boolean acceptOutputsInner(List<ItemStack> outputItems, List<FluidStack> outputFluids, boolean simulate) {
		BlockState blockState = getBlockState();
		if (!(blockState.getBlock() instanceof BasinBlock))
			return false;

		Direction direction = blockState.getValue(BasinBlock.FACING);
		if (direction != Direction.DOWN) {

			BlockEntity te = level.getBlockEntity(worldPosition.below()
				.relative(direction));

			InvManipulationBehaviour inserter =
				te == null ? null : TileEntityBehaviour.get(level, te.getBlockPos(), InvManipulationBehaviour.TYPE);
			IItemHandler targetInv = te == null ? null
				: TransferUtil.getItemHandler(te, direction.getOpposite())
					.orElse(inserter == null ? null : inserter.getInventory());
			IFluidHandler targetTank = te == null ? null
				: TransferUtil.getFluidHandler(te, direction.getOpposite())
					.orElse(null);
			boolean externalTankNotPresent = targetTank == null;

			if (!outputItems.isEmpty() && targetInv == null)
				return false;
			if (!outputFluids.isEmpty() && externalTankNotPresent) {
				// Special case - fluid outputs but output only accepts items
				targetTank = outputTank.getCapability()
					.orElse(null);
				if (targetTank == null)
				return false;
if (!acceptFluidOutputsIntoBasin(outputFluids, simulate, targetTank))
					return false;
			}

			if (simulate)
				return true;
			for (ItemStack itemStack : outputItems) {
				IFluidHandlerItem handler = TransferUtil.getFluidHandlerItem(itemStack).getValueUnsafer();
				if (handler != null && handler.getContainer()
					.sameItem(itemStack))
					continue;
				spoutputBuffer.add(itemStack.copy());
			}
			if (!externalTankNotPresent)
				for (FluidStack fluidStack : outputFluids)
					spoutputFluidBuffer.add(fluidStack.copy());
			return true;
		}

		IItemHandler targetInv = outputInventory;
		IFluidHandler targetTank = outputTank.getCapability()
			.orElse(null);

		if (targetInv == null && !outputItems.isEmpty())
			return false;
		if (!acceptItemOutputsIntoBasin(outputItems, simulate, targetInv))
			return false;
		if (outputFluids.isEmpty())
			return true;
		if (targetTank == null)
			return false;
		if (!acceptFluidOutputsIntoBasin(outputFluids, simulate, targetTank))
			return false;

		return true;
	}

	private boolean acceptFluidOutputsIntoBasin(List<FluidStack> outputFluids, boolean simulate,
		IFluidHandler targetTank) {
		for (FluidStack fluidStack : outputFluids) {
			long fill = targetTank instanceof SmartFluidTankBehaviour.InternalFluidHandler
				? ((SmartFluidTankBehaviour.InternalFluidHandler) targetTank).forceFill(fluidStack.copy(), simulate)
				: targetTank.fill(fluidStack.copy(), simulate);
			if (fill != fluidStack.getAmount())
				return false;
		}
		return true;
	}

	private boolean acceptItemOutputsIntoBasin(List<ItemStack> outputItems, boolean simulate, IItemHandler targetInv) {
		for (ItemStack itemStack : outputItems) {
			// Catalyst items are never consumed
			if (itemStack.hasContainerItem() && itemStack.getContainerItem()
				.sameItem(itemStack))
				continue;
			if (!ItemHandlerHelper.insertItemStacked(targetInv, itemStack.copy(), simulate)
				.isEmpty())
				return false;
		}
		return true;
	}

	public void readOnlyItems(CompoundTag compound) {
		inputInventory.create$deserializeNBT(compound.getCompound("InputItems"));
		outputInventory.create$deserializeNBT(compound.getCompound("OutputItems"));
	}

	public static HeatLevel getHeatLevelOf(BlockState state) {
		if (state.hasProperty(BlazeBurnerBlock.HEAT_LEVEL))
			return state.getValue(BlazeBurnerBlock.HEAT_LEVEL);
		return AllTags.AllBlockTags.FAN_HEATERS.matches(state) ? HeatLevel.SMOULDERING : HeatLevel.NONE;
	}

	public Couple<SmartFluidTankBehaviour> getTanks() {
		return tanks;
	}

	public Couple<SmartInventory> getInvs() {
		return invs;
	}

	// client things

	private void tickVisualizedOutputs() {
		visualizedOutputFluids.forEach(IntAttached::decrement);
		visualizedOutputItems.forEach(IntAttached::decrement);
		visualizedOutputFluids.removeIf(IntAttached::isOrBelowZero);
		visualizedOutputItems.removeIf(IntAttached::isOrBelowZero);
	}

	private void createFluidParticles() {
		Random r = level.random;

		if (!visualizedOutputFluids.isEmpty())
			createOutputFluidParticles(r);

		if (!areFluidsMoving && r.nextFloat() > 1 / 8f)
			return;

		int segments = 0;
		for (SmartFluidTankBehaviour behaviour : getTanks()) {
			if (behaviour == null)
				continue;
			for (TankSegment tankSegment : behaviour.getTanks())
				if (!tankSegment.isEmpty(0))
					segments++;
		}
		if (segments < 2)
			return;

		float totalUnits = getTotalFluidUnits(0);
		if (totalUnits == 0)
			return;
		float fluidLevel = Mth.clamp(totalUnits / 2000, 0, 1);
		float rim = 2 / 16f;
		float space = 12 / 16f;
		float surface = worldPosition.getY() + rim + space * fluidLevel + 1 / 32f;

		if (areFluidsMoving) {
			createMovingFluidParticles(surface, segments);
			return;
		}

		for (SmartFluidTankBehaviour behaviour : getTanks()) {
			if (behaviour == null)
				continue;
			for (TankSegment tankSegment : behaviour.getTanks()) {
				if (tankSegment.isEmpty(0))
					continue;
				float x = worldPosition.getX() + rim + space * r.nextFloat();
				float z = worldPosition.getZ() + rim + space * r.nextFloat();
				level.addAlwaysVisibleParticle(
					new FluidParticleData(AllParticleTypes.BASIN_FLUID.get(), tankSegment.getRenderedFluid()), x,
					surface, z, 0, 0, 0);
			}
		}
	}

	private void createOutputFluidParticles(Random r) {
		BlockState blockState = getBlockState();
		if (!(blockState.getBlock() instanceof BasinBlock))
			return;
		Direction direction = blockState.getValue(BasinBlock.FACING);
		if (direction == Direction.DOWN)
			return;
		Vec3 directionVec = Vec3.atLowerCornerOf(direction.getNormal());
		Vec3 outVec = VecHelper.getCenterOf(worldPosition)
			.add(directionVec.scale(.65)
				.subtract(0, 1 / 4f, 0));
		Vec3 outMotion = directionVec.scale(1 / 16f)
			.add(0, -1 / 16f, 0);

		for (int i = 0; i < 2; i++) {
			visualizedOutputFluids.forEach(ia -> {
				FluidStack fluidStack = ia.getValue();
				ParticleOptions fluidParticle = FluidFX.getFluidParticle(fluidStack);
				Vec3 m = VecHelper.offsetRandomly(outMotion, r, 1 / 16f);
				level.addAlwaysVisibleParticle(fluidParticle, outVec.x, outVec.y, outVec.z, m.x, m.y, m.z);
			});
		}
	}

	private void createMovingFluidParticles(float surface, int segments) {
		Vec3 pointer = new Vec3(1, 0, 0).scale(1 / 16f);
		float interval = 360f / segments;
		Vec3 centerOf = VecHelper.getCenterOf(worldPosition);
		float intervalOffset = (AnimationTickHolder.getTicks() * 18) % 360;

		int currentSegment = 0;
		for (SmartFluidTankBehaviour behaviour : getTanks()) {
			if (behaviour == null)
				continue;
			for (TankSegment tankSegment : behaviour.getTanks()) {
				if (tankSegment.isEmpty(0))
					continue;
				float angle = interval * (1 + currentSegment) + intervalOffset;
				Vec3 vec = centerOf.add(VecHelper.rotate(pointer, angle, Axis.Y));
				level.addAlwaysVisibleParticle(
					new FluidParticleData(AllParticleTypes.BASIN_FLUID.get(), tankSegment.getRenderedFluid()),
					vec.x(), surface, vec.z(), 1, 0, 0);
				currentSegment++;
			}
		}
	}

	public boolean areFluidsMoving() {
		return areFluidsMoving;
	}

	public boolean setAreFluidsMoving(boolean areFluidsMoving) {
		this.areFluidsMoving = areFluidsMoving;
		ingredientRotationSpeed.chase(areFluidsMoving ? 20 : 0, .1f, Chaser.EXP);
		return areFluidsMoving;
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return containedFluidTooltip(tooltip, isPlayerSneaking,
				TransferUtil.getFluidHandler(this));
	}

	@Override
	@Nullable
	public IFluidHandler getFluidHandler(@Nullable Direction direction) {
		return fluidCapability.orElse(null);
	}

	@Override
	@Nullable
	public IItemHandler getItemHandler(Direction direction) {
		return itemCapability.getValueUnsafer();
	}

	class BasinValueBox extends ValueBoxTransform.Sided {

		@Override
		protected Vec3 getSouthLocation() {
			return VecHelper.voxelSpace(8, 12, 15.75);
		}

		@Override
		protected boolean isSideActive(BlockState state, Direction direction) {
			return direction.getAxis()
				.isHorizontal();
		}

	}
}
