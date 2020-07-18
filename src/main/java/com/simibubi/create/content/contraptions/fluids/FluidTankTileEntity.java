package com.simibubi.create.content.contraptions.fluids;

import static java.lang.Math.abs;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.fluids.FluidTankBlock.Shape;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingValue;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class FluidTankTileEntity extends SmartTileEntity {

	private static final int MAX_SIZE = 3;

	protected LazyOptional<IFluidHandler> fluidCapability;
	protected boolean forceFluidLevelUpdate;
	protected FluidTank tankInventory;
	protected BlockPos controller;
	protected boolean updateConnectivity;
	protected boolean window;
	protected int luminosity;
	protected int width;
	protected int height;

	// For rendering purposes only
	InterpolatedChasingValue fluidLevel;

	public FluidTankTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		tankInventory = new SmartFluidTank(getCapacityMultiplier(), this::onFluidStackChanged);
		fluidCapability = LazyOptional.of(() -> tankInventory);
		forceFluidLevelUpdate = true;
		updateConnectivity = false;
		window = true;
		height = 1;
		width = 1;
	}

	protected void updateConnectivity() {
		updateConnectivity = false;
		if (world.isRemote)
			return;
		if (!isController())
			return;
		FluidTankConnectivityHandler.formTanks(this);
	}

	@Override
	public void tick() {
		super.tick();
		if (updateConnectivity)
			updateConnectivity();
		if (fluidLevel != null)
			fluidLevel.tick();
	}

	public boolean isController() {
		return controller == null || controller.equals(pos);
	}

	@Override
	public void initialize() {
		super.initialize();
		sendData();
	}

	protected void onFluidStackChanged(FluidStack newFluidStack) {
		if (!hasWorld())
			return;

		FluidAttributes attributes = newFluidStack.getFluid()
			.getAttributes();
		int luminosity = attributes.getLuminosity(newFluidStack) / 2;
		boolean reversed = attributes.isLighterThanAir();
		int maxY = (int) ((getFillState() * height) + 1);

		for (int yOffset = 0; yOffset < height; yOffset++) {
			boolean isBright = reversed ? (height - yOffset <= maxY) : (yOffset < maxY);
			int actualLuminosity = isBright ? luminosity : luminosity > 0 ? 1 : 0;

			for (int xOffset = 0; xOffset < width; xOffset++) {
				for (int zOffset = 0; zOffset < width; zOffset++) {
					BlockPos pos = this.pos.add(xOffset, yOffset, zOffset);
					FluidTankTileEntity tankAt = FluidTankConnectivityHandler.tankAt(world, pos);
					if (tankAt == null)
						continue;
					if (tankAt.luminosity == actualLuminosity)
						continue;
					tankAt.setLuminosity(actualLuminosity);
				}
			}
		}
	}

	protected void setLuminosity(int luminosity) {
		if (world.isRemote)
			return;
		if (this.luminosity == luminosity)
			return;
		this.luminosity = luminosity;
		sendData();
	}

	public FluidTankTileEntity getControllerTE() {
		if (isController())
			return this;
		TileEntity tileEntity = world.getTileEntity(controller);
		if (tileEntity instanceof FluidTankTileEntity)
			return (FluidTankTileEntity) tileEntity;
		return null;
	}

	public void applyFluidTankSize(int blocks) {
		tankInventory.setCapacity(blocks * getCapacityMultiplier());
		int overflow = tankInventory.getFluidAmount() - tankInventory.getCapacity();
		if (overflow > 0)
			tankInventory.drain(overflow, FluidAction.EXECUTE);
		forceFluidLevelUpdate = true;
	}

	public void removeController() {
		if (world.isRemote)
			return;
		updateConnectivity = true;
		applyFluidTankSize(1);
		controller = null;
		width = 1;
		height = 1;
		onFluidStackChanged(tankInventory.getFluid());

		BlockState state = getBlockState();
		if (FluidTankBlock.isTank(state)) {
			state = state.with(FluidTankBlock.BOTTOM, true);
			state = state.with(FluidTankBlock.TOP, true);
			state = state.with(FluidTankBlock.SHAPE, window ? Shape.WINDOW : Shape.PLAIN);
			getWorld().setBlockState(pos, state, 22);
		}

		markDirty();
		sendData();
	}

	public void toggleWindows() {
		FluidTankTileEntity te = getControllerTE();
		if (te == null)
			return;
		te.setWindows(!te.window);
	}

	public void setWindows(boolean window) {
		this.window = window;
		for (int yOffset = 0; yOffset < height; yOffset++) {
			for (int xOffset = 0; xOffset < width; xOffset++) {
				for (int zOffset = 0; zOffset < width; zOffset++) {

					BlockPos pos = this.pos.add(xOffset, yOffset, zOffset);
					BlockState blockState = world.getBlockState(pos);
					if (!FluidTankBlock.isTank(blockState))
						continue;

					Shape shape = Shape.PLAIN;
					if (window) {
						// SIZE 1: Every tank has a window
						if (width == 1)
							shape = Shape.WINDOW;
						// SIZE 2: Every tank has a corner window
						if (width == 2)
							shape = xOffset == 0 ? zOffset == 0 ? Shape.WINDOW_NW : Shape.WINDOW_SW
								: zOffset == 0 ? Shape.WINDOW_NE : Shape.WINDOW_SE;
						// SIZE 3: Tanks in the center have a window
						if (width == 3 && abs(abs(xOffset) - abs(zOffset)) == 1)
							shape = Shape.WINDOW;
					}

					world.setBlockState(pos, blockState.with(FluidTankBlock.SHAPE, shape), 22);
					world.getChunkProvider()
						.getLightManager()
						.checkBlock(pos);
				}
			}
		}
	}

	public void setController(BlockPos controller) {
		if (world.isRemote)
			return;
		if (controller.equals(this.controller))
			return;
		this.controller = controller;
		markDirty();
		sendData();
	}

	public BlockPos getController() {
		return isController() ? pos : controller;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		return super.getRenderBoundingBox().expand(width - 1, height - 1, width - 1);
	}

	@Nullable
	public FluidTankTileEntity getOtherFluidTankTileEntity(Direction direction) {
		TileEntity otherTE = world.getTileEntity(pos.offset(direction));
		if (otherTE instanceof FluidTankTileEntity)
			return (FluidTankTileEntity) otherTE;
		return null;
	}

	@Override
	public void read(CompoundNBT tag) {
		super.read(tag);
		updateConnectivity = tag.contains("Uninitialized");
		luminosity = tag.getInt("Luminosity");
		controller = null;

		if (tag.contains("Controller"))
			controller = NBTUtil.readBlockPos(tag.getCompound("Controller"));

		if (isController()) {
			window = tag.getBoolean("Window");
			width = tag.getInt("Size");
			height = tag.getInt("Height");
			tankInventory.setCapacity(getTotalTankSize() * getCapacityMultiplier());
			tankInventory.readFromNBT(tag.getCompound("TankContent"));
			if (tankInventory.getSpace() < 0)
				tankInventory.drain(-tankInventory.getSpace(), FluidAction.EXECUTE);
		}

		if (tag.contains("ForceFluidLevel") || fluidLevel == null)
			fluidLevel = new InterpolatedChasingValue().start(getFillState())
				.withSpeed(1 / 2f);
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		BlockPos controllerBefore = controller;
		int prevSize = width;
		int prevHeight = height;
		int prevLum = luminosity;

		super.readClientUpdate(tag);

		boolean changeOfController =
			controllerBefore == null ? controller != null : !controllerBefore.equals(controller);
		if (changeOfController || prevSize != width || prevHeight != height) {
			if (hasWorld())
				world.notifyBlockUpdate(getPos(), getBlockState(), getBlockState(), 16);
			if (isController())
				tankInventory.setCapacity(getCapacityMultiplier() * getTotalTankSize());
		}
		if (isController()) {
			float fillState = getFillState();
			if (tag.contains("ForceFluidLevel") || fluidLevel == null)
				fluidLevel = new InterpolatedChasingValue().start(fillState)
					.withSpeed(1 / 2f);
			fluidLevel.target(fillState);
		}
		if (luminosity != prevLum && hasWorld())
			world.getChunkProvider()
				.getLightManager()
				.checkBlock(pos);
	}

	protected float getFillState() {
		return (float) tankInventory.getFluidAmount() / tankInventory.getCapacity();
	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		if (updateConnectivity)
			tag.putBoolean("Uninitialized", true);
		if (!isController())
			tag.put("Controller", NBTUtil.writeBlockPos(controller));
		if (isController()) {
			tag.putBoolean("Window", window);
			tag.put("TankContent", tankInventory.writeToNBT(new CompoundNBT()));
			tag.putInt("Size", width);
			tag.putInt("Height", height);
		}
		tag.putInt("Luminosity", luminosity);
		return super.write(tag);
	}

	@Override
	public CompoundNBT writeToClient(CompoundNBT compound) {
		if (forceFluidLevelUpdate)
			compound.putBoolean("ForceFluidLevel", true);
		forceFluidLevelUpdate = false;
		return super.writeToClient(compound);
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			FluidTankTileEntity controller = getControllerTE();
			if (controller != null)
				return controller.fluidCapability.cast();
		}
		return super.getCapability(cap, side);
	}

	@Override
	public void remove() {
		super.remove();
		fluidCapability.invalidate();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	public IFluidTank getTankInventory() {
		return tankInventory;
	}

	public int getTotalTankSize() {
		return width * width * height;
	}

	public static int getMaxSize() {
		return MAX_SIZE;
	}

	protected static int getCapacityMultiplier() {
		return AllConfigs.SERVER.fluids.fluidTankCapacity.get() * 1000;
	}

	public static int getMaxHeight() {
		return AllConfigs.SERVER.fluids.fluidTankMaxHeight.get();
	}

}
