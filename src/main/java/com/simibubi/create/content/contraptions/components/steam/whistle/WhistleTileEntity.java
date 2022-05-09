package com.simibubi.create.content.contraptions.components.steam.whistle;

import java.lang.ref.WeakReference;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.components.steam.SteamJetParticleData;
import com.simibubi.create.content.contraptions.components.steam.whistle.WhistleBlock.WhistleSize;
import com.simibubi.create.content.contraptions.components.steam.whistle.WhistleExtenderBlock.WhistleExtenderShape;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankConnectivityHandler;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankTileEntity;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class WhistleTileEntity extends SmartTileEntity implements IHaveGoggleInformation {

	public WeakReference<FluidTankTileEntity> source;
	public LerpedFloat animation;
	protected int pitch;

	public WhistleTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		source = new WeakReference<>(null);
		animation = LerpedFloat.linear();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	public void updatePitch() {
		BlockPos currentPos = worldPosition.above();
		int prevPitch = pitch;
		for (pitch = 0; pitch <= 24; pitch += 2) {
			BlockState blockState = level.getBlockState(currentPos);
			if (!AllBlocks.STEAM_WHISTLE_EXTENSION.has(blockState))
				break;
			if (blockState.getValue(WhistleExtenderBlock.SHAPE) == WhistleExtenderShape.SINGLE) {
				pitch++;
				break;
			}
			currentPos = currentPos.above();
		}
		if (prevPitch == pitch)
			return;
		notifyUpdate();
	}

	@Override
	public void tick() {
		super.tick();
		if (!level.isClientSide())
			return;

		FluidTankTileEntity tank = getTank();
		boolean powered = isPowered() && tank != null && tank.boiler.isActive()
			&& (tank.boiler.passiveHeat || tank.boiler.activeHeat > 0);
		animation.chase(powered ? 1 : 0, powered ? .5f : .4f, powered ? Chaser.EXP : Chaser.LINEAR);
		animation.tickChaser();
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> this.tickAudio(getOctave(), powered));
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		tag.putInt("Pitch", pitch);
		super.write(tag, clientPacket);
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		pitch = tag.getInt("Pitch");
		super.read(tag, clientPacket);
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		String[] pitches = "F#;F;E;D#;D;C#;C;B;A#;A;G#;G".split(";");
		tooltip.add(new TextComponent(spacing + "Pitch: " + pitches[pitch % pitches.length]));
		return true;
	}

	protected boolean isPowered() {
		return getBlockState().getOptionalValue(WhistleBlock.POWERED)
			.orElse(false);
	}

	protected WhistleSize getOctave() {
		return getBlockState().getOptionalValue(WhistleBlock.SIZE)
			.orElse(WhistleSize.MEDIUM);
	}

	@OnlyIn(Dist.CLIENT)
	protected WhistleSoundInstance soundInstance;

	@OnlyIn(Dist.CLIENT)
	protected void tickAudio(WhistleSize size, boolean powered) {
		if (!powered) {
			if (soundInstance != null) {
				soundInstance.fadeOut();
				soundInstance = null;
			}
			return;
		}

		float f = (float) Math.pow(2, -pitch / 12.0);
		boolean particle = level.getGameTime() % 8 == 0;
		Vec3 eyePosition = Minecraft.getInstance().cameraEntity.getEyePosition();
		float maxVolume = (float) Mth.clamp((64 - eyePosition.distanceTo(Vec3.atCenterOf(worldPosition))) / 64, 0, 1);

		if (soundInstance == null || soundInstance.isStopped() || soundInstance.getOctave() != size) {
			Minecraft.getInstance()
				.getSoundManager()
				.play(soundInstance = new WhistleSoundInstance(size, worldPosition));
			AllSoundEvents.WHISTLE_CHIFF.playAt(level, worldPosition, maxVolume * .175f,
				size == WhistleSize.SMALL ? f + .75f : f, false);
			particle = true;
		}

		soundInstance.keepAlive();
		soundInstance.setPitch(f);

		if (!particle)
			return;

		Direction facing = getBlockState().getOptionalValue(WhistleBlock.FACING)
			.orElse(Direction.SOUTH);
		float angle = 180 + AngleHelper.horizontalAngle(facing);
		Vec3 sizeOffset = VecHelper.rotate(new Vec3(0, -0.4f, 1 / 16f * size.ordinal()), angle, Axis.Y);
		Vec3 offset = VecHelper.rotate(new Vec3(0, 1, 0.75f), angle, Axis.Y);
		Vec3 v = offset.scale(.45f)
			.add(sizeOffset)
			.add(Vec3.atCenterOf(worldPosition));
		Vec3 m = offset.subtract(Vec3.atLowerCornerOf(facing.getNormal())
			.scale(.75f));
		level.addParticle(new SteamJetParticleData(1), v.x, v.y, v.z, m.x, m.y, m.z);
	}

	public FluidTankTileEntity getTank() {
		FluidTankTileEntity tank = source.get();
		if (tank == null || tank.isRemoved()) {
			if (tank != null)
				source = new WeakReference<>(null);
			Direction facing = WhistleBlock.getAttachedDirection(getBlockState());
			FluidTankTileEntity anyTankAt =
				FluidTankConnectivityHandler.anyTankAt(level, worldPosition.relative(facing));
			if (anyTankAt != null)
				source = new WeakReference<>(tank = anyTankAt);
		}
		if (tank == null)
			return null;
		return tank.getControllerTE();
	}

}
