package com.simibubi.create.content.contraptions.base;

import static net.minecraft.ChatFormatting.GOLD;
import static net.minecraft.ChatFormatting.GRAY;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
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
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
	protected KineticConnections clientConnections;
	protected @Nullable BlockPos speedSource;
	protected boolean overstressed;
	protected float networkImpact;
	protected float networkCapacity;

	protected boolean wasMoved;
	private int flickerTally;
	private int validationCountdown;

	private final KineticConnections connections;

	public KineticTileEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);

		effects = new KineticEffectHandler(this);

		if (state.getBlock() instanceof IRotate rotate) {
			connections = rotate.getInitialConnections(state);
		} else {
			connections = AllConnections.EMPTY;
		}
		clientConnections = connections;
	}

	@Override public KineticConnections getConnections() { return connections; }

	@Override public float getStressImpact() { return getDefaultStressImpact(); }

	@Override public float getStressCapacity() { return getDefaultStressCapacity(); }


	protected Block getStressConfigKey() {
		return getBlockState().getBlock();
	}

	public float getDefaultStressImpact() {
		return (float) BlockStressValues.getImpact(getStressConfigKey());
	}

	public float getDefaultStressCapacity() {
		return (float) BlockStressValues.getCapacity(getStressConfigKey());
	}


	public float getRotationSpeedModifier(Vec3i offset) {
		return getClientConnections().checkConnection(offset).orElse(0f);
	}

	public float getRotationSpeedModifier(Direction face) {
		return getRotationSpeedModifier(face.getNormal());
	}


	@Override
	public void initialize() {
		if (!level.isClientSide)
			KineticSolver.getSolver(level).addNode(this);

		super.initialize();
	}

	@Override
	public void tick() {
		super.tick();
		effects.tick();

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
		// TODO: validation for new kinetics system
	}

	public void onSpeedChanged(float previousSpeed) {
		boolean fromOrToZero = (previousSpeed == 0) != (getSpeed() == 0);
		boolean directionSwap = !fromOrToZero && Math.signum(previousSpeed) != Math.signum(getSpeed());
		if (fromOrToZero || directionSwap)
			flickerTally = getFlickerScore() + 5;
	}

	public void onStressChanged(float prevNetworkImpact, float prevNetworkCapacity, boolean prevOverstressed) {
		if (!prevOverstressed && isOverstressed())
			effects.triggerOverStressedEffect();
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		if (clientPacket) {
			compound.putFloat("Speed", theoreticalSpeed);
			compound.put("Connections", clientConnections.save(new CompoundTag()));
			compound.putBoolean("Overstressed", overstressed);
			compound.putFloat("Impact", networkImpact);
			compound.putFloat("Capacity", networkCapacity);
			if (speedSource != null)
				compound.put("Source", NbtUtils.writeBlockPos(speedSource));
		}

		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		if (clientPacket) {
			updateFromSolver(tag.getFloat("Speed"),
					tag.contains("Source") ? NbtUtils.readBlockPos(tag.getCompound("Source")) : null,
					KineticConnections.load(tag.getCompound("Connections")),
					tag.getBoolean("Overstressed"),
					tag.getFloat("Impact"),
					tag.getFloat("Capacity"));
		}

		super.read(tag, clientPacket);

		if (clientPacket)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> InstancedRenderDispatcher.enqueueUpdate(this));
	}

	public float getSpeed() {
		if (overstressed) return 0;
		return getTheoreticalSpeed();
	}

	public float getTheoreticalSpeed() {
		return theoreticalSpeed;
	}

	public Optional<BlockPos> getSpeedSource() {
		return Optional.ofNullable(speedSource);
	}

	public KineticConnections getClientConnections() {
		return clientConnections;
	}

	public boolean isOverstressed() {
		return overstressed;
	}

	public float getNetworkImpact() {
		return networkImpact;
	}

	public float getNetworkCapacity() {
		return networkCapacity;
	}

	public void updateFromSolver(float theoreticalSpeed, @Nullable BlockPos speedSource, KineticConnections connections,
								 boolean overstressed, float networkImpact, float networkCapacity) {
		float prevSpeed = getSpeed();
		BlockPos prevSpeedSource = getSpeedSource().orElse(null);
		KineticConnections prevConnections = getClientConnections();
		boolean prevOverstressed = isOverstressed();
		float prevNetworkImpact = getNetworkImpact();
		float prevNetworkCapacity = getNetworkCapacity();

		this.theoreticalSpeed = theoreticalSpeed;
		this.speedSource = speedSource;
		this.clientConnections = connections;
		this.overstressed = overstressed;
		this.networkImpact = networkImpact;
		this.networkCapacity = networkCapacity;

		boolean send = false;

		if (prevOverstressed != overstressed || prevNetworkImpact != networkImpact || prevNetworkCapacity != networkCapacity) {
			onStressChanged(prevNetworkImpact, prevNetworkCapacity, prevOverstressed);
			send = true;
		}

		if (getSpeed() != prevSpeed) {
			onSpeedChanged(prevSpeed);
			send = true;
		}

		if (level.isClientSide) return;

		if (!connections.equals(prevConnections))
			send = true;
		if (!Objects.equal(speedSource, prevSpeedSource))
			send = true;
		if (send)
			sendData();
	}

	public boolean hasSource() {
		return source != null;
	}

	public boolean hasNetwork() {
		return network != null;
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
		if (!world.isClientSide)
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
		float stressAtBase = getStressImpact();

		if (stressAtBase != 0 && StressImpact.isEnabled()) {
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
