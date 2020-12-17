package com.simibubi.create.content.contraptions.processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nonnull;

import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.contraptions.components.mixer.MechanicalMixerTileEntity;
import com.simibubi.create.content.contraptions.fluids.FluidFX;
import com.simibubi.create.content.contraptions.fluids.particle.FluidParticleData;
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
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.LerpedFloat;
import com.simibubi.create.foundation.utility.LerpedFloat.Chaser;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

public class BasinTileEntity extends SmartTileEntity {

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

	public static final int OUTPUT_ANIMATION_TIME = 10;
	List<IntAttached<ItemStack>> visualizedOutputItems;
	List<IntAttached<FluidStack>> visualizedOutputFluids;

	public BasinTileEntity(TileEntityType<? extends BasinTileEntity> type) {
		super(type);
		inputInventory = new BasinInventory(9, this);
		inputInventory.whenContentsChanged($ -> contentsChanged = true);
		outputInventory = new BasinInventory(9, this).forbidInsertion();
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
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new DirectBeltInputBehaviour(this));
		filtering = new FilteringBehaviour(this, new BasinValueBox()).moveText(new Vec3d(2, -8, 0))
			.withCallback(newFilter -> contentsChanged = true)
			.forRecipes();
		behaviours.add(filtering);

		inputTank = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.INPUT, this, 2, 1000, true)
			.whenFluidUpdates(() -> contentsChanged = true);
		outputTank = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.OUTPUT, this, 2, 1000, true).forbidInsertion();
		behaviours.add(inputTank);
		behaviours.add(outputTank);

		fluidCapability = LazyOptional.of(() -> {
			LazyOptional<? extends IFluidHandler> inputCap = inputTank.getCapability();
			LazyOptional<? extends IFluidHandler> outputCap = outputTank.getCapability();
			return new CombinedTankWrapper(inputCap.orElse(null), outputCap.orElse(null));
		});
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		inputInventory.deserializeNBT(compound.getCompound("InputItems"));
		outputInventory.deserializeNBT(compound.getCompound("OutputItems"));

		preferredSpoutput = null;
		if (compound.contains("PreferredSpoutput"))
			preferredSpoutput = NBTHelper.readEnum(compound, "PreferredSpoutput", Direction.class);
		disabledSpoutputs.clear();
		ListNBT disabledList = compound.getList("DisabledSpoutput", NBT.TAG_STRING);
		disabledList.forEach(d -> disabledSpoutputs.add(Direction.valueOf(((StringNBT) d).getString())));

		if (!clientPacket)
			return;

		NBTHelper.iterateCompoundList(compound.getList("VisualizedItems", NBT.TAG_COMPOUND),
			c -> visualizedOutputItems.add(IntAttached.with(OUTPUT_ANIMATION_TIME, ItemStack.read(c))));
		NBTHelper.iterateCompoundList(compound.getList("VisualizedFluids", NBT.TAG_COMPOUND),
			c -> visualizedOutputFluids
				.add(IntAttached.with(OUTPUT_ANIMATION_TIME, FluidStack.loadFluidStackFromNBT(c))));
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.put("InputItems", inputInventory.serializeNBT());
		compound.put("OutputItems", outputInventory.serializeNBT());

		if (preferredSpoutput != null)
			NBTHelper.writeEnum(compound, "PreferredSpoutput", preferredSpoutput);
		ListNBT disabledList = new ListNBT();
		disabledSpoutputs.forEach(d -> disabledList.add(StringNBT.of(d.name())));
		compound.put("DisabledSpoutput", disabledList);

		if (!clientPacket)
			return;

		compound.put("VisualizedItems", NBTHelper.writeCompoundList(visualizedOutputItems, ia -> ia.getValue()
			.serializeNBT()));
		compound.put("VisualizedFluids", NBTHelper.writeCompoundList(visualizedOutputFluids, ia -> ia.getValue()
			.writeToNBT(new CompoundNBT())));
		visualizedOutputItems.clear();
		visualizedOutputFluids.clear();
	}

	public void onEmptied() {
		getOperator().ifPresent(te -> te.basinRemoved = true);
	}

	@Override
	public void remove() {
		onEmptied();
		itemCapability.invalidate();
		fluidCapability.invalidate();
		super.remove();
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return itemCapability.cast();
		if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
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
		updateSpoutput();
		if (!world.isRemote)
			return;

		TileEntity tileEntity = world.getTileEntity(pos.up(2));
		if (!(tileEntity instanceof MechanicalMixerTileEntity)) {
			setAreFluidsMoving(false);
			return;
		}
		MechanicalMixerTileEntity mixer = (MechanicalMixerTileEntity) tileEntity;
		setAreFluidsMoving(mixer.running && mixer.runningTicks <= 20);
	}

	public void onWrenched(Direction face) {
		BlockState blockState = getBlockState();
		Direction currentFacing = blockState.get(BasinBlock.FACING);

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
		if (world.isRemote)
			return;

		BlockState blockState = getBlockState();
		Direction currentFacing = blockState.get(BasinBlock.FACING);

		if (currentFacing != Direction.DOWN)
			notifyChangeOfContents();

		Direction newFacing = Direction.DOWN;
		for (Direction test : Iterate.horizontalDirections) {
			boolean canOutputTo = BasinBlock.canOutputTo(world, pos, test);
			if (canOutputTo && !disabledSpoutputs.contains(test))
				newFacing = test;
		}

		if (preferredSpoutput != null && BasinBlock.canOutputTo(world, pos, preferredSpoutput)  && preferredSpoutput != Direction.UP)
			newFacing = preferredSpoutput;

		if (newFacing != currentFacing)
			world.setBlockState(pos, blockState.with(BasinBlock.FACING, newFacing));
	}

	@Override
	public void tick() {
		super.tick();
		if (world.isRemote) {
			createFluidParticles();
			tickVisualizedOutputs();
			ingredientRotationSpeed.tickChaser();
			ingredientRotation.setValue(ingredientRotation.getValue() + ingredientRotationSpeed.getValue());
		}
		if (!contentsChanged)
			return;
		contentsChanged = false;
		getOperator().ifPresent(te -> te.basinChecker.scheduleUpdate());

		for (Direction offset : Iterate.horizontalDirections) {
			BlockPos toUpdate = pos.up()
				.offset(offset);
			BlockState stateToUpdate = world.getBlockState(toUpdate);
			if (stateToUpdate.getBlock() instanceof BasinBlock
				&& stateToUpdate.get(BasinBlock.FACING) == offset.getOpposite()) {
				TileEntity te = world.getTileEntity(toUpdate);
				if (te instanceof BasinTileEntity)
					((BasinTileEntity) te).contentsChanged = true;
			}
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
		if (world == null)
			return Optional.empty();
		TileEntity te = world.getTileEntity(pos.up(2));
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

	@Override
	@OnlyIn(Dist.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 256;
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
		Direction direction = blockState.get(BasinBlock.FACING);

		IItemHandler targetInv = null;
		IFluidHandler targetTank = null;

		if (direction == Direction.DOWN) {
			// No output basin, gather locally
			targetInv = outputInventory;
			targetTank = outputTank.getCapability()
				.orElse(null);

		} else {
			// Output basin, try moving items to it
			TileEntity te = world.getTileEntity(pos.down()
				.offset(direction));
			if (te == null)
				return false;
			targetInv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite())
				.orElse(null);
			targetTank = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite())
				.orElse(null);
		}

		if (targetInv == null && !outputItems.isEmpty())
			return false;
		for (ItemStack itemStack : outputItems)
			if (!ItemHandlerHelper.insertItemStacked(targetInv, itemStack.copy(), simulate)
				.isEmpty())
				return false;
			else if (!simulate)
				visualizedOutputItems.add(IntAttached.withZero(itemStack));

		if (outputFluids.isEmpty())
			return true;
		if (targetTank == null)
			return false;

		for (FluidStack fluidStack : outputFluids) {
			FluidAction action = simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE;
			int fill = targetTank instanceof SmartFluidTankBehaviour.InternalFluidHandler
				? ((SmartFluidTankBehaviour.InternalFluidHandler) targetTank).forceFill(fluidStack.copy(), action)
				: targetTank.fill(fluidStack.copy(), action);
			if (fill != fluidStack.getAmount())
				return false;
			else if (!simulate)
				visualizedOutputFluids.add(IntAttached.withZero(fluidStack));
		}

		return true;
	}

	public void readOnlyItems(CompoundNBT compound) {
		inputInventory.deserializeNBT(compound.getCompound("InputItems"));
		outputInventory.deserializeNBT(compound.getCompound("OutputItems"));
	}

	public static HeatLevel getHeatLevelOf(BlockState state) {
		if (state.has(BlazeBurnerBlock.HEAT_LEVEL))
			return state.get(BlazeBurnerBlock.HEAT_LEVEL);
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
		Random r = world.rand;

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
		float fluidLevel = MathHelper.clamp(totalUnits / 2000, 0, 1);
		float rim = 2 / 16f;
		float space = 12 / 16f;
		float surface = pos.getY() + rim + space * fluidLevel + 1 / 32f;

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
				float x = pos.getX() + rim + space * r.nextFloat();
				float z = pos.getZ() + rim + space * r.nextFloat();
				world.addOptionalParticle(
					new FluidParticleData(AllParticleTypes.BASIN_FLUID.get(), tankSegment.getRenderedFluid()), x,
					surface, z, 0, 0, 0);
			}
		}
	}

	private void createOutputFluidParticles(Random r) {
		BlockState blockState = getBlockState();
		if (!(blockState.getBlock() instanceof BasinBlock))
			return;
		Direction direction = blockState.get(BasinBlock.FACING);
		if (direction == Direction.DOWN)
			return;
		Vec3d directionVec = new Vec3d(direction.getDirectionVec());
		Vec3d outVec = VecHelper.getCenterOf(pos)
			.add(directionVec.scale(.65)
				.subtract(0, 1 / 4f, 0));
		Vec3d outMotion = directionVec.scale(1 / 16f)
			.add(0, -1 / 16f, 0);

		for (int i = 0; i < 3; i++) {
			visualizedOutputFluids.forEach(ia -> {
				FluidStack fluidStack = ia.getValue();
				IParticleData fluidParticle = FluidFX.getFluidParticle(fluidStack);
				Vec3d m = VecHelper.offsetRandomly(outMotion, r, 1 / 16f);
				world.addOptionalParticle(fluidParticle, outVec.x, outVec.y, outVec.z, m.x, m.y, m.z);
			});
		}
	}

	private void createMovingFluidParticles(float surface, int segments) {
		Vec3d pointer = new Vec3d(1, 0, 0).scale(1 / 16f);
		float interval = 360f / segments;
		Vec3d centerOf = VecHelper.getCenterOf(pos);
		float intervalOffset = (AnimationTickHolder.ticks * 18) % 360;

		int currentSegment = 0;
		for (SmartFluidTankBehaviour behaviour : getTanks()) {
			if (behaviour == null)
				continue;
			for (TankSegment tankSegment : behaviour.getTanks()) {
				if (tankSegment.isEmpty(0))
					continue;
				float angle = interval * (1 + currentSegment) + intervalOffset;
				Vec3d vec = centerOf.add(VecHelper.rotate(pointer, angle, Axis.Y));
				world.addOptionalParticle(
					new FluidParticleData(AllParticleTypes.BASIN_FLUID.get(), tankSegment.getRenderedFluid()),
					vec.getX(), surface, vec.getZ(), 1, 0, 0);
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

	class BasinValueBox extends ValueBoxTransform.Sided {

		@Override
		protected Vec3d getSouthLocation() {
			return VecHelper.voxelSpace(8, 12, 15.75);
		}

		@Override
		protected boolean isSideActive(BlockState state, Direction direction) {
			return direction.getAxis()
				.isHorizontal();
		}

	}
}
