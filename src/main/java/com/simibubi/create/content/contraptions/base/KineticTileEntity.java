package com.simibubi.create.content.contraptions.base;

import static net.minecraft.util.text.TextFormatting.GOLD;
import static net.minecraft.util.text.TextFormatting.GRAY;

import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.KineticNetwork;
import com.simibubi.create.content.contraptions.RotationPropagator;
import com.simibubi.create.content.contraptions.base.IRotate.SpeedLevel;
import com.simibubi.create.content.contraptions.base.IRotate.StressImpact;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.goggles.IHaveHoveringInformation;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import com.simibubi.create.foundation.render.backend.instancing.IInstanceRendered;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public abstract class KineticTileEntity extends SmartTileEntity
	implements ITickableTileEntity, IHaveGoggleInformation, IHaveHoveringInformation, IInstanceRendered {

	public @Nullable Long network;
	public @Nullable BlockPos source;
	public boolean networkDirty;
	public boolean updateSpeed;
	public boolean preventSpeedUpdate;

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
		if (hasNetwork() && !world.isRemote) {
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
			float prevSpeed = getSpeed();
			this.overStressed = overStressed;
			onSpeedChanged(prevSpeed);
			sendData();
		}
	}

	public float calculateAddedStressCapacity() {
		float capacity = (float) AllConfigs.SERVER.kinetics.stressValues.getCapacityOf(getStressConfigKey());
		this.lastCapacityProvided = capacity;
		return capacity;
	}

	protected Block getStressConfigKey() {
		return getBlockState().getBlock();
	}

	public float calculateStressApplied() {
		float impact = (float) AllConfigs.SERVER.kinetics.stressValues.getImpactOf(getBlockState().getBlock());
		this.lastStressApplied = impact;
		return impact;
	}

	public void onSpeedChanged(float previousSpeed) {
		boolean fromOrToZero = (previousSpeed == 0) != (getSpeed() == 0);
		boolean directionSwap = !fromOrToZero && Math.signum(previousSpeed) != Math.signum(getSpeed());
		if (fromOrToZero || directionSwap)
			flickerTally = getFlickerScore() + 5;
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
	protected void write(CompoundNBT compound, boolean clientPacket) {
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

		super.write(compound, clientPacket);
	}

	public boolean needsSpeedUpdate() {
		return updateSpeed;
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		boolean overStressedBefore = overStressed;
		clearKineticInformation();

		// DO NOT READ kinetic information when placed after movement
		if (wasMoved) {
			super.read(compound, clientPacket);
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

		super.read(compound, clientPacket);

		if (clientPacket && overStressedBefore != overStressed && speed != 0)
			effects.triggerOverStressedEffect();

		if (clientPacket)
			FastRenderDispatcher.enqueueUpdate(this);
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
		BlockState currentState = world.getBlockState(pos);
		boolean isKinetic = tileEntityIn instanceof KineticTileEntity;

		if (currentState == state)
			return;
		if (tileEntityIn == null || !isKinetic) {
			world.setBlockState(pos, state, 3);
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

		world.setBlockState(pos, state, 3);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	@Override
	public boolean hasFastRenderer() {
		return true;
	}

	@Override
	public boolean addToTooltip(List<String> tooltip, boolean isPlayerSneaking) {
		boolean notFastEnough = !isSpeedRequirementFulfilled() && getSpeed() != 0;

		if (overStressed && AllConfigs.CLIENT.enableOverstressedTooltip.get()) {
			tooltip.add(spacing + GOLD + Lang.translate("gui.stressometer.overstressed"));
			String hint = Lang.translate("gui.contraptions.network_overstressed", I18n.format(getBlockState().getBlock()
				.getTranslationKey()));
			List<String> cutString = TooltipHelper.cutString(spacing + hint, GRAY, TextFormatting.WHITE);
			for (int i = 0; i < cutString.size(); i++)
				tooltip.add((i == 0 ? "" : spacing) + cutString.get(i));
			return true;
		}

		if (notFastEnough) {
			tooltip.add(spacing + GOLD + Lang.translate("tooltip.speedRequirement"));
			String hint = Lang.translate("gui.contraptions.not_fast_enough", I18n.format(getBlockState().getBlock()
				.getTranslationKey()));
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

			float stressTotal = stressAtBase * Math.abs(getTheoreticalSpeed());

			String stressString =
				spacing + "%s%s" + Lang.translate("generic.unit.stress") + " " + TextFormatting.DARK_GRAY + "%s";
			tooltip.add(" " + String.format(stressString, TextFormatting.AQUA,
				IHaveGoggleInformation.format(stressTotal), Lang.translate("gui.goggles.at_current_speed")));

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
		BlockPos.getAllInBox(new BlockPos(-1, -1, -1), new BlockPos(1, 1, 1))
			.forEach(offset -> {
				if (axis.getCoordinate(offset.getX(), offset.getY(), offset.getZ()) != 0)
					return;
				if (offset.distanceSq(0, 0, 0, false) != BlockPos.ZERO.distanceSq(1, 1, 0, false))
					return;
				neighbours.add(pos.add(offset));
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
		return block.hasIntegratedCogwheel(world, pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (world != null && world.isRemote)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CreateClient.kineticRenderer.add(this));
	}

	@Override
	public void onChunkUnloaded() {
		if (world != null && world.isRemote)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CreateClient.kineticRenderer.remove(this));
	}

	@Override
	public void requestModelDataUpdate() {
		super.requestModelDataUpdate();
		if (!this.removed) {
			FastRenderDispatcher.enqueueUpdate(this);
		}
	}

	@Override
	public void onChunkLightUpdate() {
		CreateClient.kineticRenderer.onLightUpdate(this);
	}

	protected AxisAlignedBB cachedBoundingBox;
	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		if (cachedBoundingBox == null) {
			cachedBoundingBox = makeRenderBoundingBox();
		}
		return cachedBoundingBox;
	}

	protected AxisAlignedBB makeRenderBoundingBox() {
		return super.getRenderBoundingBox();
	}
}
