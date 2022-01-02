package com.simibubi.create.content.contraptions.base;

import static net.minecraft.ChatFormatting.GOLD;
import static net.minecraft.ChatFormatting.GRAY;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.api.FlywheelRendered;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.simibubi.create.content.contraptions.base.IRotate.SpeedLevel;
import com.simibubi.create.content.contraptions.base.IRotate.StressImpact;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.goggles.IHaveHoveringInformation;
import com.simibubi.create.content.contraptions.relays.elementary.ICogWheel;
import com.simibubi.create.content.contraptions.relays.gearbox.GearboxBlock;
import com.simibubi.create.content.contraptions.solver.AllConnections;
import com.simibubi.create.content.contraptions.solver.IKineticController;
import com.simibubi.create.content.contraptions.solver.KineticConnections;
import com.simibubi.create.content.contraptions.solver.KineticSolver;
import com.simibubi.create.foundation.block.BlockStressValues;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.sound.SoundScapes;
import com.simibubi.create.foundation.sound.SoundScapes.AmbienceGroup;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class KineticTileEntity extends SmartTileEntity
	implements IHaveGoggleInformation, IHaveHoveringInformation, FlywheelRendered, IKineticController {

	public @Nullable Long network = null;
	public @Nullable BlockPos source = null;

	protected KineticEffectHandler effects;
	protected float theoreticalSpeed;
	protected float capacity;
	protected float stress;
	protected boolean overstressed;
	protected boolean wasMoved;

	private int flickerTally;
	private int networkSize;
	private int validationCountdown;
	protected float lastStressApplied;
	protected float lastCapacityProvided;

	private final KineticConnections connections;

	public KineticTileEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);

		effects = new KineticEffectHandler(this);

		if (state.getBlock() instanceof IRotate rotate) {
			connections = rotate.getInitialConnections(state);
		} else {
			connections = AllConnections.EMPTY;
		}
	}

	@Override public KineticConnections getConnections() { return connections; }

	@Override public float getStressImpact() { return getDefaultStressImpact(); }

	@Override public float getStressCapacity() { return getDefaultStressCapacity(); }

	public float getDefaultStressImpact() {
		return (float) BlockStressValues.getImpact(getStressConfigKey());
	}

	public float getDefaultStressCapacity() {
		return (float) BlockStressValues.getCapacity(getStressConfigKey());
	}

	public Optional<Float> isConnected(BlockPos to) {
		return KineticSolver.getSolver(level).isConnected(this.getBlockPos(), to);
	}

	public boolean isStressOnlyConnected(BlockPos to) {
		return KineticSolver.getSolver(level).isStressOnlyConnected(this.getBlockPos(), to);
	}

	@Override
	public void initialize() {
		if (!level.isClientSide) {
			KineticSolver.getSolver(level).addNode(this);
		}

		super.initialize();
	}

	@Override
	public void tick() {
		super.tick();
		effects.tick();

//		if (!level.isClientSide) {
//			KineticSolver.getSolver(level).updateNode(this);
//		}

		if (level.isClientSide) {
			cachedBoundingBox = null; // cache the bounding box for every frame between ticks
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> this.tickAudio());
			return;
		}

		if (validationCountdown-- <= 0) {
			validationCountdown = AllConfigs.SERVER.kinetics.kineticValidationFrequency.get();
			validateKinetics();
		}

		if (getFlickerScore() > 0)
			flickerTally = getFlickerScore() - 1;
	}

	@Override
	protected void setRemovedNotDueToChunkUnload() {
		if (!level.isClientSide) {
			KineticSolver.getSolver(level).removeNode(this);
		}
		super.setRemovedNotDueToChunkUnload();
	}

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		if (!level.isClientSide) {
			preKineticsUnloaded();
			KineticSolver.getSolver(level).unloadNode(this);
		}
	}

	public void preKineticsUnloaded() {}



	private void validateKinetics() {
//		if (hasSource()) {
//			if (!hasNetwork()) {
//				removeSource();
//				return;
//			}
//
//			if (!level.isLoaded(source))
//				return;
//
//			BlockEntity tileEntity = level.getBlockEntity(source);
//			KineticTileEntity sourceTe =
//				tileEntity instanceof KineticTileEntity ? (KineticTileEntity) tileEntity : null;
//			if (sourceTe == null || sourceTe.speed == 0) {
//				removeSource();
//				detachKinetics();
//				return;
//			}
//
//			return;
//		}
//
//		if (speed != 0) {
//			if (getGeneratedSpeed() == 0)
//				speed = 0;
//		}
	}

	public void updateFromNetwork(float maxStress, float currentStress, int networkSize) {
//		networkDirty = false;
//		this.capacity = maxStress;
//		this.stress = currentStress;
//		this.networkSize = networkSize;
//		boolean overStressed = maxStress < currentStress && StressImpact.isEnabled();
//
//		if (overStressed != this.overStressed) {
//			float prevSpeed = getSpeed();
//			this.overStressed = overStressed;
//			onSpeedChanged(prevSpeed);
//			sendData();
//		}
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
	}

	public void onOverstressedChanged(boolean previousOverstressed) {
		if (isOverstressed())
			effects.triggerOverStressedEffect();
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		if (clientPacket) {
			compound.putFloat("Speed", theoreticalSpeed);
			compound.putBoolean("Overstressed", overstressed);
		}

		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		if (clientPacket)
			updateFromSolver(compound.getFloat("Speed"), compound.getBoolean("Overstressed"));

		super.read(compound, clientPacket);

		if (clientPacket)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> InstancedRenderDispatcher.enqueueUpdate(this));
	}

	public boolean isSource() {
		//return getGeneratedSpeed() != 0;
		return false;
	}

	public float getSpeed() {
		if (overstressed) return 0;
		return getTheoreticalSpeed();
	}

	public float getTheoreticalSpeed() {
		return theoreticalSpeed;
	}

	public void updateFromSolver(float theoreticalSpeed, boolean overstressed) {
		float prevSpeed = getSpeed();
		boolean send = false;

		if (this.theoreticalSpeed != theoreticalSpeed) {
			this.theoreticalSpeed = theoreticalSpeed;
			send = true;
		}

		if (this.overstressed != overstressed) {
			this.overstressed = overstressed;
			onOverstressedChanged(!overstressed);
			send = true;
		}

		if (getSpeed() != prevSpeed)
			onSpeedChanged(prevSpeed);

		if (send)
			sendData();
	}

	public boolean hasSource() {
		return source != null;
	}

	public void setSource(BlockPos source) {
//		this.source = source;
//		if (level == null || level.isClientSide)
//			return;
//
//		BlockEntity tileEntity = level.getBlockEntity(source);
//		if (!(tileEntity instanceof KineticTileEntity)) {
//			removeSource();
//			return;
//		}
//
//		KineticTileEntity sourceTe = (KineticTileEntity) tileEntity;
//		setNetwork(sourceTe.network);
	}

	public void removeSource() {
//		float prevSpeed = getSpeed();
//
//		speed = 0;
//		source = null;
//		setNetwork(null);
//
//		onSpeedChanged(prevSpeed);
	}

	public void setNetwork(@Nullable Long networkIn) {
//		if (network == networkIn)
//			return;
//		if (network != null)
//			getOrCreateNetwork().remove(this);
//
//		network = networkIn;
//
//		if (networkIn == null)
//			return;
//
//		network = networkIn;
//		KineticNetwork network = getOrCreateNetwork();
//		network.initialized = true;
//		network.add(this);
	}

	public boolean hasNetwork() {
		return network != null;
	}

	public void attachKinetics() {
		//updateSpeed = false;
		//RotationPropagator.handleAdded(level, worldPosition, this);
	}

	public void detachKinetics() {
		//RotationPropagator.handleRemoved(level, worldPosition, this);
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

		world.setBlock(pos, state, 3);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	@Override
	public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		boolean notFastEnough = !isSpeedRequirementFulfilled() && getSpeed() != 0;

		if (overstressed && AllConfigs.CLIENT.enableOverstressedTooltip.get()) {
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
		theoreticalSpeed = 0;
		overstressed = false;
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

	public boolean isOverstressed() {
		return overstressed;
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

	@Override
	public void requestModelDataUpdate() {
		super.requestModelDataUpdate();
		if (!this.remove)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> InstancedRenderDispatcher.enqueueUpdate(this));
	}

	protected AABB cachedBoundingBox;

	@OnlyIn(Dist.CLIENT)
	public AABB getRenderBoundingBox() {
		if (cachedBoundingBox == null) {
			cachedBoundingBox = makeRenderBoundingBox();
		}
		return cachedBoundingBox;
	}

	protected AABB makeRenderBoundingBox() {
		return super.getRenderBoundingBox();
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

}
