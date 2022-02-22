package com.simibubi.create.content.contraptions.base;

import static net.minecraft.ChatFormatting.GOLD;
import static net.minecraft.ChatFormatting.GRAY;

import java.util.List;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.KineticNetwork;
import com.simibubi.create.content.contraptions.RotationPropagator;
import com.simibubi.create.content.contraptions.base.IRotate.SpeedLevel;
import com.simibubi.create.content.contraptions.base.IRotate.StressImpact;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.goggles.IHaveHoveringInformation;
import com.simibubi.create.content.contraptions.relays.elementary.ICogWheel;
import com.simibubi.create.content.contraptions.relays.gearbox.GearboxBlock;
import com.simibubi.create.foundation.block.BlockStressValues;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.sound.SoundScapes;
import com.simibubi.create.foundation.sound.SoundScapes.AmbienceGroup;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.lib.block.CustomRenderBoundingBoxBlockEntity;
import com.tterrag.registrate.fabric.EnvExecutor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class KineticTileEntity extends SmartTileEntity
	implements IHaveGoggleInformation, IHaveHoveringInformation, CustomRenderBoundingBoxBlockEntity {

	public @Nullable Long network;
	public @Nullable BlockPos source;
	public boolean networkDirty;
	public boolean updateSpeed;
	public int preventSpeedUpdate;

	protected KineticEffectHandler effects;
	protected float speed;
	protected float capacity;
	protected float stress;
	protected boolean overStressed;
	protected boolean wasMoved;

	private int flickerTally;
	private int networkSize;
	private int validationCountdown;
	protected float lastStressApplied;
	protected float lastCapacityProvided;

	public KineticTileEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
		effects = new KineticEffectHandler(this);
		updateSpeed = true;
	}

	@Override
	public void initialize() {
		if (hasNetwork() && !level.isClientSide) {
			KineticNetwork network = getOrCreateNetwork();
			if (!network.initialized)
				network.initFromTE(capacity, stress, networkSize);
			network.addSilently(this, lastCapacityProvided, lastStressApplied);
		}

		super.initialize();
	}

	@Override
	public void tick() {
		if (!level.isClientSide && needsSpeedUpdate())
			attachKinetics();

		super.tick();
		effects.tick();

		if (level.isClientSide) {
			EnvExecutor.runWhenOn(EnvType.CLIENT, () -> () -> this.tickAudio());
			return;
		}

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

			if (!level.isLoaded(source))
				return;

			BlockEntity tileEntity = level.getBlockEntity(source);
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
		setChanged();

		if (overStressed != this.overStressed) {
			float prevSpeed = getSpeed();
			this.overStressed = overStressed;
			onSpeedChanged(prevSpeed);
			sendData();
		}
	}

	protected Block getStressConfigKey() {
		return getBlockState().getBlock();
	}

	public float calculateStressApplied() {
		float impact = (float) BlockStressValues.getImpact(getStressConfigKey());
		this.lastStressApplied = impact;
		return impact;
	}

	public float calculateAddedStressCapacity() {
		float capacity = (float) BlockStressValues.getCapacity(getStressConfigKey());
		this.lastCapacityProvided = capacity;
		return capacity;
	}

	public void onSpeedChanged(float previousSpeed) {
		boolean fromOrToZero = (previousSpeed == 0) != (getSpeed() == 0);
		boolean directionSwap = !fromOrToZero && Math.signum(previousSpeed) != Math.signum(getSpeed());
		if (fromOrToZero || directionSwap)
			flickerTally = getFlickerScore() + 5;
		setChanged();
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
	}

	@Override
	protected void setRemovedNotDueToChunkUnload() {
		if (!level.isClientSide) {
			if (hasNetwork())
				getOrCreateNetwork().remove(this);
			detachKinetics();
		}
		super.setRemovedNotDueToChunkUnload();
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		compound.putFloat("Speed", speed);

		if (needsSpeedUpdate())
			compound.putBoolean("NeedsSpeedUpdate", true);

		if (hasSource())
			compound.put("Source", NbtUtils.writeBlockPos(source));

		if (hasNetwork()) {
			CompoundTag networkTag = new CompoundTag();
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

		super.write(compound, clientPacket);
	}

	public boolean needsSpeedUpdate() {
		return updateSpeed;
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		boolean overStressedBefore = overStressed;
		clearKineticInformation();

		// DO NOT READ kinetic information when placed after movement
		if (wasMoved) {
			super.read(compound, clientPacket);
			return;
		}

		speed = compound.getFloat("Speed");

		if (compound.contains("Source"))
			source = NbtUtils.readBlockPos(compound.getCompound("Source"));

		if (compound.contains("Network")) {
			CompoundTag networkTag = compound.getCompound("Network");
			network = networkTag.getLong("Id");
			stress = networkTag.getFloat("Stress");
			capacity = networkTag.getFloat("Capacity");
			networkSize = networkTag.getInt("Size");
			lastStressApplied = networkTag.getFloat("AddedStress");
			lastCapacityProvided = networkTag.getFloat("AddedCapacity");
			overStressed = capacity < stress && StressImpact.isEnabled();
		}

		super.read(compound, clientPacket);

		if (clientPacket && overStressedBefore != overStressed && speed != 0)
			effects.triggerOverStressedEffect();

		if (clientPacket)
			EnvExecutor.runWhenOn(EnvType.CLIENT, () -> () -> InstancedRenderDispatcher.enqueueUpdate(this));
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
		if (level == null || level.isClientSide)
			return;

		BlockEntity tileEntity = level.getBlockEntity(source);
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
		setChanged();

		if (networkIn == null)
			return;

		network = networkIn;
		KineticNetwork network = getOrCreateNetwork();
		network.initialized = true;
		network.add(this);
	}

	public KineticNetwork getOrCreateNetwork() {
		return Create.TORQUE_PROPAGATOR.getOrCreateNetworkFor(this);
	}

	public boolean hasNetwork() {
		return network != null;
	}

	public void attachKinetics() {
		updateSpeed = false;
		RotationPropagator.handleAdded(level, worldPosition, this);
	}

	public void detachKinetics() {
		RotationPropagator.handleRemoved(level, worldPosition, this);
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

	public static void switchToBlockState(Level world, BlockPos pos, BlockState state) {
		if (world.isClientSide)
			return;

		BlockEntity tileEntityIn = world.getBlockEntity(pos);
		BlockState currentState = world.getBlockState(pos);
		boolean isKinetic = tileEntityIn instanceof KineticTileEntity;

		if (currentState == state)
			return;
		if (tileEntityIn == null || !isKinetic) {
			world.setBlock(pos, state, 3);
			return;
		}

		KineticTileEntity tileEntity = (KineticTileEntity) tileEntityIn;
		if (state.getBlock() instanceof KineticBlock
			&& !((KineticBlock) state.getBlock()).areStatesKineticallyEquivalent(currentState, state)) {
			if (tileEntity.hasNetwork())
				tileEntity.getOrCreateNetwork()
					.remove(tileEntity);
			tileEntity.detachKinetics();
			tileEntity.removeSource();
		}

		world.setBlock(pos, state, 3);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	@Override
	public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		boolean notFastEnough = !isSpeedRequirementFulfilled() && getSpeed() != 0;

		if (overStressed && AllConfigs.CLIENT.enableOverstressedTooltip.get()) {
			tooltip.add(componentSpacing.plainCopy()
				.append(Lang.translate("gui.stressometer.overstressed")
					.withStyle(GOLD)));
			Component hint = Lang.translate("gui.contraptions.network_overstressed");
			List<Component> cutString = TooltipHelper.cutTextComponent(hint, GRAY, ChatFormatting.WHITE);
			for (int i = 0; i < cutString.size(); i++)
				tooltip.add(componentSpacing.plainCopy()
					.append(cutString.get(i)));
			return true;
		}

		if (notFastEnough) {
			tooltip.add(componentSpacing.plainCopy()
				.append(Lang.translate("tooltip.speedRequirement")
					.withStyle(GOLD)));
			Component hint = Lang.translate("gui.contraptions.not_fast_enough", I18n.get(getBlockState().getBlock()
				.getDescriptionId()));
			List<Component> cutString = TooltipHelper.cutTextComponent(hint, GRAY, ChatFormatting.WHITE);
			for (int i = 0; i < cutString.size(); i++)
				tooltip.add(componentSpacing.plainCopy()
					.append(cutString.get(i)));
			return true;
		}

		return false;
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		boolean added = false;
		float stressAtBase = calculateStressApplied();

		if (calculateStressApplied() != 0 && StressImpact.isEnabled()) {
			tooltip.add(componentSpacing.plainCopy()
				.append(Lang.translate("gui.goggles.kinetic_stats")));
			tooltip.add(componentSpacing.plainCopy()
				.append(Lang.translate("tooltip.stressImpact")
					.withStyle(ChatFormatting.GRAY)));

			float stressTotal = stressAtBase * Math.abs(getTheoreticalSpeed());

			tooltip.add(componentSpacing.plainCopy()
				.append(new TextComponent(" " + IHaveGoggleInformation.format(stressTotal))
					.append(Lang.translate("generic.unit.stress"))
					.append(" ")
					.withStyle(ChatFormatting.AQUA))
				.append(Lang.translate("gui.goggles.at_current_speed")
					.withStyle(ChatFormatting.DARK_GRAY)));

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

	public static float convertToDirection(float axisSpeed, Direction d) {
		return d.getAxisDirection() == AxisDirection.POSITIVE ? axisSpeed : -axisSpeed;
	}

	public static float convertToLinear(float speed) {
		return speed / 512f;
	}

	public static float convertToAngular(float speed) {
		return speed * 3 / 10f;
	}

	public boolean isOverStressed() {
		return overStressed;
	}

	// Custom Propagation

	/**
	 * Specify ratio of transferred rotation from this kinetic component to a
	 * specific other.
	 *
	 * @param target           other Kinetic TE to transfer to
	 * @param stateFrom        this TE's blockstate
	 * @param stateTo          other TE's blockstate
	 * @param diff             difference in position (to.pos - from.pos)
	 * @param connectedViaAxes whether these kinetic blocks are connected via mutual
	 *                         IRotate.hasShaftTowards()
	 * @param connectedViaCogs whether these kinetic blocks are connected via mutual
	 *                         IRotate.hasIntegratedCogwheel()
	 * @return factor of rotation speed from this TE to other. 0 if no rotation is
	 *         transferred, or the standard rules apply (integrated shafts/cogs)
	 */
	public float propagateRotationTo(KineticTileEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff,
		boolean connectedViaAxes, boolean connectedViaCogs) {
		return 0;
	}

	/**
	 * Specify additional locations the rotation propagator should look for
	 * potentially connected components. Neighbour list contains offset positions in
	 * all 6 directions by default.
	 *
	 * @param block
	 * @param state
	 * @param neighbours
	 * @return
	 */
	public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours) {
		if (!canPropagateDiagonally(block, state))
			return neighbours;

		Axis axis = block.getRotationAxis(state);
		BlockPos.betweenClosedStream(new BlockPos(-1, -1, -1), new BlockPos(1, 1, 1))
			.forEach(offset -> {
				if (axis.choose(offset.getX(), offset.getY(), offset.getZ()) != 0)
					return;
				if (offset.distSqr(0, 0, 0, false) != BlockPos.ZERO.distSqr(1, 1, 0, false))
					return;
				neighbours.add(worldPosition.offset(offset));
			});
		return neighbours;
	}

	/**
	 * Specify whether this component can propagate speed to the other in any
	 * circumstance. Shaft and cogwheel connections are already handled by internal
	 * logic. Does not have to be specified on both ends, it is assumed that this
	 * relation is symmetrical.
	 *
	 * @param other
	 * @param state
	 * @param otherState
	 * @return true if this and the other component should check their propagation
	 *         factor and are not already connected via integrated cogs or shafts
	 */
	public boolean isCustomConnection(KineticTileEntity other, BlockState state, BlockState otherState) {
		return false;
	}

	protected boolean canPropagateDiagonally(IRotate block, BlockState state) {
		return ICogWheel.isSmallCog(state);
	}

//	@Override
	public void requestModelDataUpdate() {
//		super.requestModelDataUpdate();
		if (!this.remove)
			EnvExecutor.runWhenOn(EnvType.CLIENT, () -> () -> InstancedRenderDispatcher.enqueueUpdate(this));
	}

	@Environment(EnvType.CLIENT)
	public void tickAudio() {
		float componentSpeed = Math.abs(getSpeed());
		if (componentSpeed == 0)
			return;
		float pitch = Mth.clamp((componentSpeed / 256f) + .45f, .85f, 1f);

		if (isNoisy())
			SoundScapes.play(AmbienceGroup.KINETIC, worldPosition, pitch);

		Block block = getBlockState().getBlock();
		if (ICogWheel.isSmallCog(block) || ICogWheel.isLargeCog(block) || block instanceof GearboxBlock)
			SoundScapes.play(AmbienceGroup.COG, worldPosition, pitch);
	}

	protected boolean isNoisy() {
		return true;
	}

}
