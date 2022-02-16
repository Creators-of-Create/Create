package com.simibubi.create.content.logistics.trains.management;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.trains.ITrackBlock;
import com.simibubi.create.content.logistics.trains.TrackGraphHelper;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TrackTargetingBehaviour extends TileEntityBehaviour {

	public static final BehaviourType<TrackTargetingBehaviour> TYPE = new BehaviourType<>();

	private BlockPos targetTrack;
	private AxisDirection targetDirection;

	public TrackTargetingBehaviour(SmartTileEntity te) {
		super(te);
		targetDirection = AxisDirection.POSITIVE;
		targetTrack = BlockPos.ZERO;
	}

	@Override
	public void write(CompoundTag nbt, boolean clientPacket) {
		nbt.put("TargetTrack", NbtUtils.writeBlockPos(targetTrack));
		nbt.putBoolean("TargetDirection", targetDirection == AxisDirection.POSITIVE);
		super.write(nbt, clientPacket);
	}

	@Override
	public void read(CompoundTag nbt, boolean clientPacket) {
		targetTrack = NbtUtils.readBlockPos(nbt.getCompound("TargetTrack"));
		targetDirection = nbt.getBoolean("TargetDirection") ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
		super.read(nbt, clientPacket);
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	public boolean hasValidTrack() {
		return getTrackBlockState().getBlock() instanceof ITrackBlock;
	}

	public ITrackBlock getTrack() {
		return (ITrackBlock) getTrackBlockState().getBlock();
	}

	public BlockState getTrackBlockState() {
		return getWorld().getBlockState(getGlobalPosition());
	}

	public BlockPos getGlobalPosition() {
		return targetTrack.offset(tileEntity.getBlockPos());
	}

	public AxisDirection getTargetDirection() {
		return targetDirection;
	}

	public GraphLocation determineGraphLocation() {
		Level level = getWorld();
		BlockPos pos = getGlobalPosition();
		BlockState state = getTrackBlockState();
		return TrackGraphHelper.getGraphLocationAt(level, pos, getTargetDirection(),
			getTrack().getTrackAxes(level, pos, state)
				.get(0));
	}

	public static enum RenderedTrackOverlayType {
		STATION, SIGNAL, DUAL_SIGNAL;
	}

	@OnlyIn(Dist.CLIENT)
	public static void render(LevelAccessor level, BlockPos pos, AxisDirection direction, int tintColor, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay, RenderedTrackOverlayType type) {
		BlockState trackState = level.getBlockState(pos);
		Block block = trackState.getBlock();
		if (!(block instanceof ITrackBlock))
			return;

		ms.pushPose();
		ms.translate(pos.getX(), pos.getY(), pos.getZ());

		ITrackBlock track = (ITrackBlock) block;
		SuperByteBuffer sbb =
			CachedBufferer.partial(track.prepareTrackOverlay(level, pos, trackState, direction, ms, type), trackState);
		sbb.light(LevelRenderer.getLightColor(level, pos));
		sbb.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));

		ms.popPose();
	}

}
