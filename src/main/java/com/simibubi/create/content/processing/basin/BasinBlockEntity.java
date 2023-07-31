package com.simibubi.create.content.processing.basin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.FluidFX;
import com.simibubi.create.content.fluids.particle.FluidParticleData;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.LangBuilder;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

public class BasinBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

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
	int recipeBackupCheck;

	public static final int OUTPUT_ANIMATION_TIME = 10;
	List<IntAttached<ItemStack>> visualizedOutputItems;
	List<IntAttached<FluidStack>> visualizedOutputFluids;

	public BasinBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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
		recipeBackupCheck = 20;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(new DirectBeltInputBehaviour(this));
		filtering = new FilteringBehaviour(this, new BasinValueBox()).withCallback(newFilter -> contentsChanged = true)
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
			return new CombinedTankWrapper(outputCap.orElse(null), inputCap.orElse(null));
		});
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		inputInventory.deserializeNBT(compound.getCompound("InputItems"));
		outputInventory.deserializeNBT(compound.getCompound("OutputItems"));

		preferredSpoutput = null;
		if (compound.contains("PreferredSpoutput"))
			preferredSpoutput = NBTHelper.readEnum(compound, "PreferredSpoutput", Direction.class);
		disabledSpoutputs.clear();
		ListTag disabledList = compound.getList("DisabledSpoutput", Tag.TAG_STRING);
		disabledList.forEach(d -> disabledSpoutputs.add(Direction.valueOf(((StringTag) d).getAsString())));
		spoutputBuffer = NBTHelper.readItemList(compound.getList("Overflow", Tag.TAG_COMPOUND));
		spoutputFluidBuffer = NBTHelper.readCompoundList(compound.getList("FluidOverflow", Tag.TAG_COMPOUND),
			FluidStack::loadFluidStackFromNBT);

		if (!clientPacket)
			return;

		NBTHelper.iterateCompoundList(compound.getList("VisualizedItems", Tag.TAG_COMPOUND),
			c -> visualizedOutputItems.add(IntAttached.with(OUTPUT_ANIMATION_TIME, ItemStack.of(c))));
		NBTHelper.iterateCompoundList(compound.getList("VisualizedFluids", Tag.TAG_COMPOUND),
			c -> visualizedOutputFluids
				.add(IntAttached.with(OUTPUT_ANIMATION_TIME, FluidStack.loadFluidStackFromNBT(c))));
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.put("InputItems", inputInventory.serializeNBT());
		compound.put("OutputItems", outputInventory.serializeNBT());

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

		compound.put("VisualizedItems", NBTHelper.writeCompoundList(visualizedOutputItems, ia -> ia.getValue()
			.serializeNBT()));
		compound.put("VisualizedFluids", NBTHelper.writeCompoundList(visualizedOutputFluids, ia -> ia.getValue()
			.writeToNBT(new CompoundTag())));
		visualizedOutputItems.clear();
		visualizedOutputFluids.clear();
	}

	@Override
	public void destroy() {
		super.destroy();
		ItemHelper.dropContents(level, worldPosition, inputInventory);
		ItemHelper.dropContents(level, worldPosition, outputInventory);
		spoutputBuffer.forEach(is -> Block.popResource(level, worldPosition, is));
	}

	@Override
	public void remove() {
		super.remove();
		onEmptied();
	}

	public void onEmptied() {
		getOperator().ifPresent(be -> be.basinRemoved = true);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		itemCapability.invalidate();
		fluidCapability.invalidate();
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
		if (cap == ForgeCapabilities.ITEM_HANDLER)
			return itemCapability.cast();
		if (cap == ForgeCapabilities.FLUID_HANDLER)
			return fluidCapability.cast();
		return super.getCapability(cap, side);
	}

	@Override
	public void notifyUpdate() {
		super.notifyUpdate();
	}

	@Override
	public void lazyTick() {
		super.lazyTick();

		if (!level.isClientSide) {
			updateSpoutput();
			if (recipeBackupCheck-- > 0)
				return;
			recipeBackupCheck = 20;
			if (isEmpty())
				return;
			notifyChangeOfContents();
			return;
		}

		BlockEntity blockEntity = level.getBlockEntity(worldPosition.above(2));
		if (!(blockEntity instanceof MechanicalMixerBlockEntity)) {
			setAreFluidsMoving(false);
			return;
		}

		MechanicalMixerBlockEntity mixer = (MechanicalMixerBlockEntity) blockEntity;
		setAreFluidsMoving(mixer.running && mixer.runningTicks <= 20);
	}

	public boolean isEmpty() {
		return inputInventory.isEmpty() && outputInventory.isEmpty() && inputTank.isEmpty() && outputTank.isEmpty();
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
		BlockState blockState = getBlockState();
		Direction currentFacing = blockState.getValue(BasinBlock.FACING);
		Direction newFacing = Direction.DOWN;
		for (Direction test : Iterate.horizontalDirections) {
			boolean canOutputTo = BasinBlock.canOutputTo(level, worldPosition, test);
			if (canOutputTo && !disabledSpoutputs.contains(test))
				newFacing = test;
		}

		if (preferredSpoutput != null && BasinBlock.canOutputTo(level, worldPosition, preferredSpoutput)
			&& preferredSpoutput != Direction.UP)
			newFacing = preferredSpoutput;

		if (newFacing == currentFacing)
			return;

		level.setBlockAndUpdate(worldPosition, blockState.setValue(BasinBlock.FACING, newFacing));

		if (newFacing.getAxis()
			.isVertical())
			return;

		for (int slot = 0; slot < outputInventory.getSlots(); slot++) {
			ItemStack extractItem = outputInventory.extractItem(slot, 64, true);
			if (extractItem.isEmpty())
				continue;
			if (acceptOutputs(ImmutableList.of(extractItem), Collections.emptyList(), true))
				acceptOutputs(ImmutableList.of(outputInventory.extractItem(slot, 64, false)), Collections.emptyList(),
					false);
		}

		IFluidHandler handler = outputTank.getCapability()
			.orElse(null);
		for (int slot = 0; slot < handler.getTanks(); slot++) {
			FluidStack fs = handler.getFluidInTank(slot)
				.copy();
			if (fs.isEmpty())
				continue;
			if (acceptOutputs(Collections.emptyList(), ImmutableList.of(fs), true)) {
				handler.drain(fs, FluidAction.EXECUTE);
				acceptOutputs(Collections.emptyList(), ImmutableList.of(fs), false);
			}
		}

		notifyChangeOfContents();
		notifyUpdate();
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
		getOperator().ifPresent(be -> be.basinChecker.scheduleUpdate());

		for (Direction offset : Iterate.horizontalDirections) {
			BlockPos toUpdate = worldPosition.above()
				.relative(offset);
			BlockState stateToUpdate = level.getBlockState(toUpdate);
			if (stateToUpdate.getBlock() instanceof BasinBlock
				&& stateToUpdate.getValue(BasinBlock.FACING) == offset.getOpposite()) {
				BlockEntity be = level.getBlockEntity(toUpdate);
				if (be instanceof BasinBlockEntity)
					((BasinBlockEntity) be).contentsChanged = true;
			}
		}
	}

	private void tryClearingSpoutputOverflow() {
		BlockState blockState = getBlockState();
		if (!(blockState.getBlock() instanceof BasinBlock))
			return;
		Direction direction = blockState.getValue(BasinBlock.FACING);
		BlockEntity be = level.getBlockEntity(worldPosition.below()
			.relative(direction));

		FilteringBehaviour filter = null;
		InvManipulationBehaviour inserter = null;
		if (be != null) {
			filter = BlockEntityBehaviour.get(level, be.getBlockPos(), FilteringBehaviour.TYPE);
			inserter = BlockEntityBehaviour.get(level, be.getBlockPos(), InvManipulationBehaviour.TYPE);
		}

		IItemHandler targetInv = be == null ? null
			: be.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite())
				.orElse(inserter == null ? null : inserter.getInventory());

		IFluidHandler targetTank = be == null ? null
			: be.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite())
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
				FluidAction action = simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE;
				int fill = targetTank instanceof SmartFluidTankBehaviour.InternalFluidHandler
					? ((SmartFluidTankBehaviour.InternalFluidHandler) targetTank).forceFill(fluidStack.copy(), action)
					: targetTank.fill(fluidStack.copy(), action);
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

	private Optional<BasinOperatingBlockEntity> getOperator() {
		if (level == null)
			return Optional.empty();
		BlockEntity be = level.getBlockEntity(worldPosition.above(2));
		if (be instanceof BasinOperatingBlockEntity)
			return Optional.of((BasinOperatingBlockEntity) be);
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

			BlockEntity be = level.getBlockEntity(worldPosition.below()
				.relative(direction));

			InvManipulationBehaviour inserter =
				be == null ? null : BlockEntityBehaviour.get(level, be.getBlockPos(), InvManipulationBehaviour.TYPE);
			IItemHandler targetInv = be == null ? null
				: be.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite())
					.orElse(inserter == null ? null : inserter.getInventory());
			IFluidHandler targetTank = be == null ? null
				: be.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite())
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
			FluidAction action = simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE;
			int fill = targetTank instanceof SmartFluidTankBehaviour.InternalFluidHandler
				? ((SmartFluidTankBehaviour.InternalFluidHandler) targetTank).forceFill(fluidStack.copy(), action)
				: targetTank.fill(fluidStack.copy(), action);
			if (fill != fluidStack.getAmount())
				return false;
		}
		return true;
	}

	private boolean acceptItemOutputsIntoBasin(List<ItemStack> outputItems, boolean simulate, IItemHandler targetInv) {
		for (ItemStack itemStack : outputItems) {
			if (!ItemHandlerHelper.insertItemStacked(targetInv, itemStack.copy(), simulate)
				.isEmpty())
				return false;
		}
		return true;
	}

	public void readOnlyItems(CompoundTag compound) {
		inputInventory.deserializeNBT(compound.getCompound("InputItems"));
		outputInventory.deserializeNBT(compound.getCompound("OutputItems"));
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
		RandomSource r = level.random;

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

	private void createOutputFluidParticles(RandomSource r) {
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
					new FluidParticleData(AllParticleTypes.BASIN_FLUID.get(), tankSegment.getRenderedFluid()), vec.x(),
					surface, vec.z(), 1, 0, 0);
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
		Lang.translate("gui.goggles.basin_contents")
			.forGoggles(tooltip);

		IItemHandlerModifiable items = itemCapability.orElse(new ItemStackHandler());
		IFluidHandler fluids = fluidCapability.orElse(new FluidTank(0));
		boolean isEmpty = true;

		for (int i = 0; i < items.getSlots(); i++) {
			ItemStack stackInSlot = items.getStackInSlot(i);
			if (stackInSlot.isEmpty())
				continue;
			Lang.text("")
				.add(Components.translatable(stackInSlot.getDescriptionId())
					.withStyle(ChatFormatting.GRAY))
				.add(Lang.text(" x" + stackInSlot.getCount())
					.style(ChatFormatting.GREEN))
				.forGoggles(tooltip, 1);
			isEmpty = false;
		}

		LangBuilder mb = Lang.translate("generic.unit.millibuckets");
		for (int i = 0; i < fluids.getTanks(); i++) {
			FluidStack fluidStack = fluids.getFluidInTank(i);
			if (fluidStack.isEmpty())
				continue;
			Lang.text("")
				.add(Lang.fluidName(fluidStack)
					.add(Lang.text(" "))
					.style(ChatFormatting.GRAY)
					.add(Lang.number(fluidStack.getAmount())
						.add(mb)
						.style(ChatFormatting.BLUE)))
				.forGoggles(tooltip, 1);
			isEmpty = false;
		}

		if (isEmpty)
			tooltip.remove(0);

		return true;
	}

	class BasinValueBox extends ValueBoxTransform.Sided {

		@Override
		protected Vec3 getSouthLocation() {
			return VecHelper.voxelSpace(8, 12, 16.05);
		}

		@Override
		protected boolean isSideActive(BlockState state, Direction direction) {
			return direction.getAxis()
				.isHorizontal();
		}

	}
}
