package com.simibubi.create.content.contraptions.fluids.tank;

import static java.lang.Math.abs;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.fluids.tank.FluidTankBlock.Shape;
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
	protected BlockPos lastKnownPos;
	protected boolean updateConnectivity;
	protected boolean window;
	protected int luminosity;
	protected int width;
	protected int height;

	private static final int SYNC_RATE = 8;
	protected int syncCooldown;
	protected boolean queuedSync;

	// For rendering purposes only
	InterpolatedChasingValue fluidLevel;

	public FluidTankTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		tankInventory = createInventory();
		fluidCapability = LazyOptional.of(() -> tankInventory);
		forceFluidLevelUpdate = true;
		updateConnectivity = false;
		window = true;
		height = 1;
		width = 1;
		refreshCapability();
	}

	protected SmartFluidTank createInventory() {
		return new SmartFluidTank(getCapacityMultiplier(), this::onFluidStackChanged);
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
		if (syncCooldown > 0) {
			syncCooldown--;
			if (syncCooldown == 0 && queuedSync)
				sendData();
		}
		
		if (lastKnownPos == null)
			lastKnownPos = getPos();
		else if (!lastKnownPos.equals(pos) && pos != null) {
			onPositionChanged();
			return;
		}
		
		if (updateConnectivity)
			updateConnectivity();
		if (fluidLevel != null)
			fluidLevel.tick();
	}

	public boolean isController() {
		return controller == null ||
				pos.getX() == controller.getX() &&
				pos.getY() == controller.getY() &&
				pos.getZ() == controller.getZ();
	}

	@Override
	public void initialize() {
		super.initialize();
		sendData();
	}

	private void onPositionChanged() {
		removeController(true);
		lastKnownPos = pos;
	}

	protected void onFluidStackChanged(FluidStack newFluidStack) {
		if (!hasWorld())
			return;

		FluidAttributes attributes = newFluidStack.getFluid()
			.getAttributes();
		int luminosity = (int) (attributes.getLuminosity(newFluidStack) / 1.2f);
		boolean reversed = attributes.isLighterThanAir();
		int maxY = (int) ((getFillState() * height) + 1);

		for (int yOffset = 0; yOffset < height; yOffset++) {
			boolean isBright = reversed ? (height - yOffset <= maxY) : (yOffset < maxY);
			int actualLuminosity = isBright ? luminosity : luminosity > 0 ? 1 : 0;

			for (int xOffset = 0; xOffset < width; xOffset++) {
				for (int zOffset = 0; zOffset < width; zOffset++) {
					BlockPos pos = this.pos.add(xOffset, yOffset, zOffset);
					FluidTankTileEntity tankAt = FluidTankConnectivityHandler.anyTankAt(world, pos);
					if (tankAt == null)
						continue;
					if (tankAt.luminosity == actualLuminosity)
						continue;
					tankAt.setLuminosity(actualLuminosity);
				}
			}
		}

		if (!world.isRemote) {
			markDirty();
			sendData();
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

	public void removeController(boolean keepFluids) {
		if (world.isRemote)
			return;
		updateConnectivity = true;
		if (!keepFluids)
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

		refreshCapability();
		markDirty();
		sendData();
	}

	public void toggleWindows() {
		FluidTankTileEntity te = getControllerTE();
		if (te == null)
			return;
		te.setWindows(!te.window);
	}

	public void sendDataImmediately() {
		syncCooldown = 0;
		queuedSync = false;
		sendData();
	}

	@Override
	public void sendData() {
		if (syncCooldown > 0) {
			queuedSync = true;
			return;
		}
		super.sendData();
		queuedSync = false;
		syncCooldown = SYNC_RATE;
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
		refreshCapability();
		markDirty();
		sendData();
	}

	private void refreshCapability() {
		LazyOptional<IFluidHandler> oldCap = fluidCapability;
		fluidCapability = LazyOptional.of(() -> isController() ? tankInventory
			: getControllerTE() != null ? getControllerTE().tankInventory : new FluidTank(0));
		oldCap.invalidate();
	}

	public BlockPos getController() {
		return isController() ? pos : controller;
	}

	private AxisAlignedBB cachedBoundingBox;
	@Override
	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		if (cachedBoundingBox == null) {
			if (isController())
				cachedBoundingBox = super.getRenderBoundingBox().expand(width - 1, height - 1, width - 1);
			else
				cachedBoundingBox = super.getRenderBoundingBox();
		}
		return cachedBoundingBox;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public double getMaxRenderDistanceSquared() {
		int dist = 64 + getMaxHeight() * 2;
		return dist * dist;
	}

	@Nullable
	public FluidTankTileEntity getOtherFluidTankTileEntity(Direction direction) {
		TileEntity otherTE = world.getTileEntity(pos.offset(direction));
		if (otherTE instanceof FluidTankTileEntity)
			return (FluidTankTileEntity) otherTE;
		return null;
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		super.read(compound, clientPacket);

		BlockPos controllerBefore = controller;
		int prevSize = width;
		int prevHeight = height;
		int prevLum = luminosity;

		updateConnectivity = compound.contains("Uninitialized");
		luminosity = compound.getInt("Luminosity");
		controller = null;
		lastKnownPos = null;

		if (compound.contains("LastKnownPos"))
			lastKnownPos = NBTUtil.readBlockPos(compound.getCompound("LastKnownPos"));
		if (compound.contains("Controller"))
			controller = NBTUtil.readBlockPos(compound.getCompound("Controller"));

		if (isController()) {
			window = compound.getBoolean("Window");
			width = compound.getInt("Size");
			height = compound.getInt("Height");
			tankInventory.setCapacity(getTotalTankSize() * getCapacityMultiplier());
			tankInventory.readFromNBT(compound.getCompound("TankContent"));
			if (tankInventory.getSpace() < 0)
				tankInventory.drain(-tankInventory.getSpace(), FluidAction.EXECUTE);
		}

		if (compound.contains("ForceFluidLevel") || fluidLevel == null)
			fluidLevel = new InterpolatedChasingValue().start(getFillState())
				.withSpeed(1 / 2f);

		if (!clientPacket)
			return;

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
			if (compound.contains("ForceFluidLevel") || fluidLevel == null)
				fluidLevel = new InterpolatedChasingValue().start(fillState);
			fluidLevel.target(fillState);
		}
		if (luminosity != prevLum && hasWorld())
			world.getChunkProvider()
				.getLightManager()
				.checkBlock(pos);

		if (compound.contains("LazySync"))
			fluidLevel.withSpeed(compound.contains("LazySync") ? 1 / 8f : 1 / 2f);
	}

	public float getFillState() {
		return (float) tankInventory.getFluidAmount() / tankInventory.getCapacity();
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		if (updateConnectivity)
			compound.putBoolean("Uninitialized", true);
		if (lastKnownPos != null)
			compound.put("LastKnownPos", NBTUtil.writeBlockPos(lastKnownPos));
		if (!isController())
			compound.put("Controller", NBTUtil.writeBlockPos(controller));
		if (isController()) {
			compound.putBoolean("Window", window);
			compound.put("TankContent", tankInventory.writeToNBT(new CompoundNBT()));
			compound.putInt("Size", width);
			compound.putInt("Height", height);
		}
		compound.putInt("Luminosity", luminosity);
		super.write(compound, clientPacket);

		if (!clientPacket)
			return;
		if (forceFluidLevelUpdate)
			compound.putBoolean("ForceFluidLevel", true);
		if (queuedSync)
			compound.putBoolean("LazySync", true);
		forceFluidLevelUpdate = false;
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		if (!fluidCapability.isPresent())
			refreshCapability();
		if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			return fluidCapability.cast();
		return super.getCapability(cap, side);
	}

	@Override
	public void remove() {
		super.remove();
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

	public static int getCapacityMultiplier() {
		return AllConfigs.SERVER.fluids.fluidTankCapacity.get() * 1000;
	}

	public static int getMaxHeight() {
		return AllConfigs.SERVER.fluids.fluidTankMaxHeight.get();
	}
	
	public InterpolatedChasingValue getFluidLevel() {
		return fluidLevel;
	}
	
	public void setFluidLevel(InterpolatedChasingValue fluidLevel) {
		this.fluidLevel = fluidLevel;
	}

}
