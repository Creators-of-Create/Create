package com.simibubi.create.content.kinetics.base;

import static net.minecraft.ChatFormatting.GOLD;
import static net.minecraft.ChatFormatting.GRAY;

import java.util.List;

import javax.annotation.Nullable;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.equipment.goggles.IHaveHoveringInformation;
import com.simibubi.create.content.kinetics.BlockStressValues;
import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.IRotate.SpeedLevel;
import com.simibubi.create.content.kinetics.base.IRotate.StressImpact;
import com.simibubi.create.content.kinetics.gearbox.GearboxBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlockEntity.SequenceContext;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.item.TooltipHelper.Palette;
import com.simibubi.create.foundation.sound.SoundScapes;
import com.simibubi.create.foundation.sound.SoundScapes.AmbienceGroup;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class KineticBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, IHaveHoveringInformation {

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

	public SequenceContext sequenceContext;

	public KineticBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
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

		preventSpeedUpdate = 0;

		if (level.isClientSide) {
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> this.tickAudio());
			return;
		}

		if (validationCountdown-- <= 0) {
			validationCountdown = AllConfigs.server().kinetics.kineticValidationFrequency.get();
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

			BlockEntity blockEntity = level.getBlockEntity(source);
			KineticBlockEntity sourceBE =
				blockEntity instanceof KineticBlockEntity ? (KineticBlockEntity) blockEntity : null;
			if (sourceBE == null || sourceBE.speed == 0) {
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
	public void remove() {
		if (!level.isClientSide) {
			if (hasNetwork())
				getOrCreateNetwork().remove(this);
			detachKinetics();
		}
		super.remove();
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		compound.putFloat("Speed", speed);
		if (sequenceContext != null && (!clientPacket || syncSequenceContext()))
			compound.put("Sequence", sequenceContext.serializeNBT());

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
		sequenceContext = SequenceContext.fromNBT(compound.getCompound("Sequence"));

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
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> VisualizationManager.queueUpdate(this));
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

		BlockEntity blockEntity = level.getBlockEntity(source);
		if (!(blockEntity instanceof KineticBlockEntity sourceBE)) {
			removeSource();
			return;
		}

		setNetwork(sourceBE.network);
		copySequenceContextFrom(sourceBE);
	}

	protected void copySequenceContextFrom(KineticBlockEntity sourceBE) {
		sequenceContext = sourceBE.sequenceContext;
	}

	public void removeSource() {
		float prevSpeed = getSpeed();

		speed = 0;
		source = null;
		setNetwork(null);
		sequenceContext = null;

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
		return Math.abs(getSpeed()) >= minimumRequiredSpeedLevel.getSpeedValue();
	}

	public static void switchToBlockState(Level world, BlockPos pos, BlockState state) {
		if (world.isClientSide)
			return;

		BlockEntity blockEntity = world.getBlockEntity(pos);
		BlockState currentState = world.getBlockState(pos);
		boolean isKinetic = blockEntity instanceof KineticBlockEntity;

		if (currentState == state)
			return;
		if (blockEntity == null || !isKinetic) {
			world.setBlock(pos, state, 3);
			return;
		}

		KineticBlockEntity kineticBlockEntity = (KineticBlockEntity) blockEntity;
		if (state.getBlock() instanceof KineticBlock
			&& !((KineticBlock) state.getBlock()).areStatesKineticallyEquivalent(currentState, state)) {
			if (kineticBlockEntity.hasNetwork())
				kineticBlockEntity.getOrCreateNetwork()
					.remove(kineticBlockEntity);
			kineticBlockEntity.detachKinetics();
			kineticBlockEntity.removeSource();
		}

		world.setBlock(pos, state, 3);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	@Override
	public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		boolean notFastEnough = !isSpeedRequirementFulfilled() && getSpeed() != 0;

		if (overStressed && AllConfigs.client().enableOverstressedTooltip.get()) {
			Lang.translate("gui.stressometer.overstressed")
				.style(GOLD)
				.forGoggles(tooltip);
			Component hint = Lang.translateDirect("gui.contraptions.network_overstressed");
			List<Component> cutString = TooltipHelper.cutTextComponent(hint, Palette.GRAY_AND_WHITE);
			for (int i = 0; i < cutString.size(); i++)
				Lang.builder()
					.add(cutString.get(i)
						.copy())
					.forGoggles(tooltip);
			return true;
		}

		if (notFastEnough) {
			Lang.translate("tooltip.speedRequirement")
				.style(GOLD)
				.forGoggles(tooltip);
			MutableComponent hint =
				Lang.translateDirect("gui.contraptions.not_fast_enough", I18n.get(getBlockState().getBlock()
					.getDescriptionId()));
			List<Component> cutString = TooltipHelper.cutTextComponent(hint, Palette.GRAY_AND_WHITE);
			for (int i = 0; i < cutString.size(); i++)
				Lang.builder()
					.add(cutString.get(i)
						.copy())
					.forGoggles(tooltip);
			return true;
		}

		return false;
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		boolean added = false;

		if (!StressImpact.isEnabled())
			return added;
		float stressAtBase = calculateStressApplied();
		if (Mth.equal(stressAtBase, 0))
			return added;

		Lang.translate("gui.goggles.kinetic_stats")
			.forGoggles(tooltip);

		addStressImpactStats(tooltip, stressAtBase);

		return true;

	}

	protected void addStressImpactStats(List<Component> tooltip, float stressAtBase) {
		Lang.translate("tooltip.stressImpact")
			.style(GRAY)
			.forGoggles(tooltip);

		float stressTotal = stressAtBase * Math.abs(getTheoreticalSpeed());

		Lang.number(stressTotal)
			.translate("generic.unit.stress")
			.style(ChatFormatting.AQUA)
			.space()
			.add(Lang.translate("gui.goggles.at_current_speed")
				.style(ChatFormatting.DARK_GRAY))
			.forGoggles(tooltip, 1);
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
	 * @param target           other Kinetic BE to transfer to
	 * @param stateFrom        this BE's blockstate
	 * @param stateTo          other BE's blockstate
	 * @param diff             difference in position (to.pos - from.pos)
	 * @param connectedViaAxes whether these kinetic blocks are connected via mutual
	 *                         IRotate.hasShaftTowards()
	 * @param connectedViaCogs whether these kinetic blocks are connected via mutual
	 *                         IRotate.hasIntegratedCogwheel()
	 * @return factor of rotation speed from this BE to other. 0 if no rotation is
	 *         transferred, or the standard rules apply (integrated shafts/cogs)
	 */
	public float propagateRotationTo(KineticBlockEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff,
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
				if (offset.distSqr(BlockPos.ZERO) != 2)
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
	public boolean isCustomConnection(KineticBlockEntity other, BlockState state, BlockState otherState) {
		return false;
	}

	protected boolean canPropagateDiagonally(IRotate block, BlockState state) {
		return ICogWheel.isSmallCog(state);
	}

	@Override
	public void requestModelDataUpdate() {
		super.requestModelDataUpdate();
		if (!this.remove)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> VisualizationManager.queueUpdate(this));
	}

	@OnlyIn(Dist.CLIENT)
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

	public int getRotationAngleOffset(Axis axis) {
		return 0;
	}

	protected boolean syncSequenceContext() {
		return false;
	}

}
