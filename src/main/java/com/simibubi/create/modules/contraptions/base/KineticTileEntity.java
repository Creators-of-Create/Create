package com.simibubi.create.modules.contraptions.base;

import static net.minecraft.util.text.TextFormatting.GOLD;
import static net.minecraft.util.text.TextFormatting.GRAY;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.simibubi.create.Create;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.KineticNetwork;
import com.simibubi.create.modules.contraptions.RotationPropagator;
import com.simibubi.create.modules.contraptions.base.IRotate.SpeedLevel;
import com.simibubi.create.modules.contraptions.base.IRotate.StressImpact;
import com.simibubi.create.modules.contraptions.goggle.IHaveGoggleInformation;
import com.simibubi.create.modules.contraptions.goggle.IHaveHoveringInformation;

import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public abstract class KineticTileEntity extends SmartTileEntity
		implements ITickableTileEntity, IHaveGoggleInformation, IHaveHoveringInformation {

	public @Nullable Long network;
	public @Nullable BlockPos source;
	public boolean networkDirty;
	public boolean updateSpeed;

	protected KineticEffectHandler effects;
	protected float speed;
	protected float capacity;
	protected float stress;
	protected boolean overStressed;
	protected boolean wasMoved;

	private int flickerTally;
	private int networkSize;
	private int validationCountdown;
	private float lastStressApplied;
	private float lastCapacityProvided;

	public KineticTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
		effects = new KineticEffectHandler(this);
		updateSpeed = true;
	}

	@Override
	public void initialize() {
		if (hasNetwork()) {
			KineticNetwork network = getOrCreateNetwork();
			if (!network.initialized)
				network.initFromTE(capacity, stress, networkSize);
			network.addSilently(this, lastCapacityProvided, lastStressApplied);
		}

		super.initialize();
	}

	@Override
	public void tick() {
		if (!world.isRemote && needsSpeedUpdate())
			attachKinetics();

		super.tick();
		effects.tick();

		if (world.isRemote)
			return;

		if (validationCountdown-- <= 0) {
			validationCountdown = AllConfigs.SERVER.kinetics.kineticValidationFrequency.get();
			validateKinetics();
		}

		if (getFlickerScore() > 0)
			flickerTally = getFlickerScore() - 1;

		if (networkDirty) {
			if (hasNetwork())
				getOrCreateNetwork().updateNetwork();
			networkDirty = false;
		}
	}

	private void validateKinetics() {
		if (hasSource()) {
			if (!hasNetwork()) {
				removeSource();
				return;
			}

			if (!world.isBlockPresent(source))
				return;

			TileEntity tileEntity = world.getTileEntity(source);
			KineticTileEntity sourceTe =
				tileEntity instanceof KineticTileEntity ? (KineticTileEntity) tileEntity : null;
			if (sourceTe == null || sourceTe.speed == 0) {
				removeSource();
				detachKinetics();
				return;
			}

			return;
		}

		if (speed != 0) {
			if (getGeneratedSpeed() == 0)
				speed = 0;
		}
	}

	public void updateFromNetwork(float maxStress, float currentStress, int networkSize) {
		networkDirty = false;
		this.capacity = maxStress;
		this.stress = currentStress;
		this.networkSize = networkSize;
		boolean overStressed = maxStress < currentStress && StressImpact.isEnabled();

		if (overStressed != this.overStressed) {
			if (speed != 0 && overStressed)
				AllTriggers.triggerForNearbyPlayers(AllTriggers.OVERSTRESSED, world, pos, 8);
			float prevSpeed = getSpeed();
			this.overStressed = overStressed;
			onSpeedChanged(prevSpeed);
			sendData();
		}
	}

	public float calculateAddedStressCapacity() {
		Map<ResourceLocation, ConfigValue<Double>> capacityMap = AllConfigs.SERVER.kinetics.stressValues.capacities;
		ResourceLocation path = getBlockState().getBlock().getRegistryName();

		float capacity = capacityMap.containsKey(path) ? capacityMap.get(path).get().floatValue() : 0;
		this.lastCapacityProvided = capacity;
		return capacity;
	}

	public float calculateStressApplied() {
		Map<ResourceLocation, ConfigValue<Double>> stressEntries = AllConfigs.SERVER.kinetics.stressValues.impacts;
		ResourceLocation path = getBlockState().getBlock().getRegistryName();

		float impact = stressEntries.containsKey(path) ? stressEntries.get(path).get().floatValue() : 1;
		this.lastStressApplied = impact;
		return impact;
	}

	public void onSpeedChanged(float previousSpeed) {
		boolean fromOrToZero = (previousSpeed == 0) != (getSpeed() == 0);
		boolean directionSwap = !fromOrToZero && Math.signum(previousSpeed) != Math.signum(getSpeed());
		if (fromOrToZero || directionSwap) {
			flickerTally = getFlickerScore() + 5;
		}
	}

	@Override
	public void remove() {
		if (!world.isRemote) {
			if (hasNetwork())
				getOrCreateNetwork().remove(this);
			detachKinetics();
		}
		super.remove();
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putFloat("Speed", speed);

		if (needsSpeedUpdate())
			compound.putBoolean("NeedsSpeedUpdate", true);

		if (hasSource())
			compound.put("Source", NBTUtil.writeBlockPos(source));

		if (hasNetwork()) {
			CompoundNBT networkTag = new CompoundNBT();
			networkTag.putLong("Id", this.network);
			networkTag.putFloat("Stress", stress);
			networkTag.putFloat("Capacity", capacity);
			networkTag.putInt("Size", networkSize);

			if (lastStressApplied != 0)
				networkTag.putFloat("AddedStress", lastStressApplied);
			if (lastCapacityProvided != 0)
				networkTag.putFloat("AddedCapacity", lastCapacityProvided);

			compound.put("Network", networkTag);
		}

		return super.write(compound);
	}

	public boolean needsSpeedUpdate() {
		return updateSpeed;
	}

	@Override
	public void read(CompoundNBT compound) {
		clearKineticInformation();

		// DO NOT READ kinetic information when placed after movement
		if (wasMoved) {
			super.read(compound);
			return;
		}

		speed = compound.getFloat("Speed");

		if (compound.contains("Source"))
			source = NBTUtil.readBlockPos(compound.getCompound("Source"));

		if (compound.contains("Network")) {
			CompoundNBT networkTag = compound.getCompound("Network");
			network = networkTag.getLong("Id");
			stress = networkTag.getFloat("Stress");
			capacity = networkTag.getFloat("Capacity");
			networkSize = networkTag.getInt("Size");
			lastStressApplied = networkTag.getFloat("AddedStress");
			lastCapacityProvided = networkTag.getFloat("AddedCapacity");
			overStressed = capacity < stress && StressImpact.isEnabled();
		}

		super.read(compound);
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		boolean overStressedBefore = overStressed;
		super.readClientUpdate(tag);
		if (overStressedBefore != overStressed && speed != 0)
			effects.triggerOverStressedEffect();
	}

	public float getGeneratedSpeed() {
		return 0;
	}

	public boolean isSource() {
		return getGeneratedSpeed() != 0;
	}

	public float getSpeed() {
		if (overStressed)
			return 0;
		return getTheoreticalSpeed();
	}

	public float getTheoreticalSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public boolean hasSource() {
		return source != null;
	}

	public void setSource(BlockPos source) {
		this.source = source;
		if (world == null || world.isRemote)
			return;

		TileEntity tileEntity = world.getTileEntity(source);
		if (!(tileEntity instanceof KineticTileEntity)) {
			removeSource();
			return;
		}

		KineticTileEntity sourceTe = (KineticTileEntity) tileEntity;
		setNetwork(sourceTe.network);
	}

	public void removeSource() {
		float prevSpeed = getSpeed();

		speed = 0;
		source = null;
		setNetwork(null);

		onSpeedChanged(prevSpeed);
	}

	public void setNetwork(@Nullable Long networkIn) {
		if (network == networkIn)
			return;
		if (network != null)
			getOrCreateNetwork().remove(this);

		network = networkIn;

		if (networkIn == null)
			return;

		network = networkIn;
		KineticNetwork network = getOrCreateNetwork();
		network.initialized = true;
		network.add(this);
	}

	public KineticNetwork getOrCreateNetwork() {
		return Create.torquePropagator.getOrCreateNetworkFor(this);
	}

	public boolean hasNetwork() {
		return network != null;
	}

	public void attachKinetics() {
		updateSpeed = false;
		RotationPropagator.handleAdded(world, pos, this);
	}

	public void detachKinetics() {
		RotationPropagator.handleRemoved(world, pos, this);
	}

	public boolean isSpeedRequirementFulfilled() {
		BlockState state = getBlockState();
		if (!(getBlockState().getBlock() instanceof IRotate))
			return true;
		IRotate def = (IRotate) state.getBlock();
		SpeedLevel minimumRequiredSpeedLevel = def.getMinimumRequiredSpeedLevel();
		if (minimumRequiredSpeedLevel == null)
			return true;
		if (minimumRequiredSpeedLevel == SpeedLevel.MEDIUM)
			return Math.abs(getSpeed()) >= AllConfigs.SERVER.kinetics.mediumSpeed.get();
		if (minimumRequiredSpeedLevel == SpeedLevel.FAST)
			return Math.abs(getSpeed()) >= AllConfigs.SERVER.kinetics.fastSpeed.get();
		return true;
	}

	public static void switchToBlockState(World world, BlockPos pos, BlockState state) {
		if (world.isRemote)
			return;

		TileEntity tileEntityIn = world.getTileEntity(pos);
		if (!(tileEntityIn instanceof KineticTileEntity))
			return;

		KineticTileEntity tileEntity = (KineticTileEntity) tileEntityIn;
		if (tileEntity.hasNetwork())
			tileEntity.getOrCreateNetwork().remove(tileEntity);
		tileEntity.detachKinetics();
		tileEntity.removeSource();
		world.setBlockState(pos, state, 3);
		tileEntity.attachKinetics();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
	}

	@Override
	public boolean hasFastRenderer() {
		return true;
	}

	@Override
	public boolean addToTooltip(List<String> tooltip, boolean isPlayerSneaking) {
		boolean notFastEnough = !isSpeedRequirementFulfilled() && getSpeed() != 0;

		if (overStressed && AllConfigs.CLIENT.enableOverstressedTooltip.get()) {
			tooltip.add(spacing + GOLD + Lang.translate("gui.stress_gauge.overstressed"));
			String hint = Lang.translate("gui.contraptions.network_overstressed",
					I18n.format(getBlockState().getBlock().getTranslationKey()));
			List<String> cutString = TooltipHelper.cutString(spacing + hint, GRAY, TextFormatting.WHITE);
			for (int i = 0; i < cutString.size(); i++)
				tooltip.add((i == 0 ? "" : spacing) + cutString.get(i));
			return true;
		}

		if (notFastEnough) {
			tooltip.add(spacing + GOLD + Lang.translate("tooltip.speedRequirement"));
			String hint = Lang.translate("gui.contraptions.not_fast_enough",
					I18n.format(getBlockState().getBlock().getTranslationKey()));
			List<String> cutString = TooltipHelper.cutString(spacing + hint, GRAY, TextFormatting.WHITE);
			for (int i = 0; i < cutString.size(); i++)
				tooltip.add((i == 0 ? "" : spacing) + cutString.get(i));
			return true;
		}

		return false;
	}

	@Override
	public boolean addToGoggleTooltip(List<String> tooltip, boolean isPlayerSneaking) {
		boolean added = false;
		float stressAtBase = calculateStressApplied();

		if (calculateStressApplied() != 0 && StressImpact.isEnabled()) {
			tooltip.add(spacing + Lang.translate("gui.goggles.kinetic_stats"));
			tooltip.add(spacing + TextFormatting.GRAY + Lang.translate("tooltip.stressImpact"));

			float stressTotal = stressAtBase * Math.abs(getSpeed());

			String stressString =
				spacing + "%s%s" + Lang.translate("generic.unit.stress") + " " + TextFormatting.DARK_GRAY + "%s";

			tooltip.add(String.format(stressString, TextFormatting.AQUA, IHaveGoggleInformation.format(stressAtBase),
					Lang.translate("gui.goggles.base_value")));
			tooltip.add(String.format(stressString, TextFormatting.GRAY, IHaveGoggleInformation.format(stressTotal),
					Lang.translate("gui.goggles.at_current_speed")));

			added = true;
		}

		return added;

	}

	public void clearKineticInformation() {
		speed = 0;
		source = null;
		network = null;
		overStressed = false;
		stress = 0;
		capacity = 0;
		lastStressApplied = 0;
		lastCapacityProvided = 0;
	}

	public void warnOfMovement() {
		wasMoved = true;
	}

	public int getFlickerScore() {
		return flickerTally;
	}

}
