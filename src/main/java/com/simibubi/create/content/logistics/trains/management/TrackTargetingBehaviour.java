package com.simibubi.create.content.logistics.trains.management;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.ITrackBlock;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.TrackNode;
import com.simibubi.create.content.logistics.trains.TrackNodeLocation.DiscoveredLocation;
import com.simibubi.create.content.logistics.trains.TrackPropagator;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Pair;

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
import net.minecraft.world.phys.Vec3;
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
		BlockState trackBlockState = getTrackBlockState();
		ITrackBlock track = getTrack();
		if (track == null)
			return null;

		Vec3 axis = track.getTrackAxis(level, pos, trackBlockState)
			.scale(getTargetDirection().getStep());
		double length = axis.length();

		List<Pair<BlockPos, DiscoveredLocation>> ends =
			TrackPropagator.getEnds(level, pos, trackBlockState, null, true);

		TrackGraph graph = null;
		TrackNode frontNode = null;
		TrackNode backNode = null;
		double position = 0;

		for (Pair<BlockPos, DiscoveredLocation> pair : ends) {
			DiscoveredLocation current = pair.getSecond();
			BlockPos currentPos = pair.getFirst();
			Vec3 offset = Vec3.atLowerCornerOf(currentPos.subtract(pos));
			boolean forward = offset.distanceToSqr(axis.scale(-1)) < 1 / 4096f;
			boolean backwards = offset.distanceToSqr(axis) < 1 / 4096f;

			if (!forward && !backwards)
				continue;

			for (int i = 0; i < 32; i++) {
				DiscoveredLocation loc = current;
				List<Pair<BlockPos, DiscoveredLocation>> list =
					TrackPropagator.getEnds(level, currentPos, level.getBlockState(currentPos), current, true);
				if (!list.isEmpty()) {
					currentPos = list.get(0)
						.getFirst();
					current = list.get(0)
						.getSecond();
				}

				if (graph == null)
					graph = Create.RAILWAYS.getGraph(level, loc);
				if (graph == null)
					continue;
				TrackNode node = graph.locateNode(loc);
				if (node == null)
					continue;
				if (forward)
					frontNode = node;
				if (backwards) {
					backNode = node;
					position = (i + .5) * length;
				}
				break;
			}
		}

		if (frontNode == null || backNode == null)
			return null;

		GraphLocation graphLocation = new GraphLocation();
		graphLocation.edge = Couple.create(backNode.getLocation(), frontNode.getLocation());
		graphLocation.position = position;
		graphLocation.graph = graph;
		return graphLocation;
	}

	@OnlyIn(Dist.CLIENT)
	public static void render(LevelAccessor level, BlockPos pos, AxisDirection direction, int tintColor, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {
		BlockState trackState = level.getBlockState(pos);
		Block block = trackState.getBlock();
		if (!(block instanceof ITrackBlock))
			return;

		ms.pushPose();
		ms.translate(pos.getX(), pos.getY(), pos.getZ());

		ITrackBlock track = (ITrackBlock) block;
		SuperByteBuffer sbb =
			CachedBufferer.partial(track.prepareStationOverlay(level, pos, trackState, direction, ms), trackState);
		sbb.color(tintColor);
		sbb.light(LevelRenderer.getLightColor(level, pos));
		sbb.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));

		ms.popPose();
	}

}
