package com.simibubi.create.content.trains.track;

import java.util.List;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.mutable.MutableObject;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.trains.graph.EdgeData;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackGraphHelper;
import com.simibubi.create.content.trains.graph.TrackGraphLocation;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.signal.TrackEdgePoint;
import com.simibubi.create.content.trains.track.TrackBlockOutline.BezierPointSelection;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;

import net.createmod.catnip.data.Couple;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class TrackTargetingBlockItem extends BlockItem {

	private EdgePointType<?> type;

	public static <T extends Block> NonNullBiFunction<? super T, Item.Properties, TrackTargetingBlockItem> ofType(
		EdgePointType<?> type) {
		return (b, p) -> new TrackTargetingBlockItem(b, p, type);
	}

	public TrackTargetingBlockItem(Block pBlock, Properties pProperties, EdgePointType<?> type) {
		super(pBlock, pProperties);
		this.type = type;
	}

	@Override
	public InteractionResult useOn(UseOnContext pContext) {
		ItemStack stack = pContext.getItemInHand();
		BlockPos pos = pContext.getClickedPos();
		Level level = pContext.getLevel();
		BlockState state = level.getBlockState(pos);
		Player player = pContext.getPlayer();

		if (player == null)
			return InteractionResult.FAIL;

		if (player.isShiftKeyDown() && stack.has(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS)) {
			if (level.isClientSide)
				return InteractionResult.SUCCESS;
			player.displayClientMessage(CreateLang.translateDirect("track_target.clear"), true);
			stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS);
			stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION);
			stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_BEZIER);
			AllSoundEvents.CONTROLLER_CLICK.play(level, null, pos, 1, .5f);
			return InteractionResult.SUCCESS;
		}

		if (state.getBlock() instanceof ITrackBlock track) {
			if (level.isClientSide)
				return InteractionResult.SUCCESS;

			Vec3 lookAngle = player.getLookAngle();
			boolean front = track.getNearestTrackAxis(level, pos, state, lookAngle)
				.getSecond() == AxisDirection.POSITIVE;
			EdgePointType<?> type = getType(stack);

			MutableObject<OverlapResult> result = new MutableObject<>(null);
			withGraphLocation(level, pos, front, null, type, (overlap, location) -> result.setValue(overlap));

			if (result.getValue().feedback != null) {
				player.displayClientMessage(CreateLang.translateDirect(result.getValue().feedback)
					.withStyle(ChatFormatting.RED), true);
				AllSoundEvents.DENY.play(level, null, pos, .5f, 1);
				return InteractionResult.FAIL;
			}

			stack.set(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS, pos);
			stack.set(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION, front);
			stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_BEZIER);
			player.displayClientMessage(CreateLang.translateDirect("track_target.set"), true);
			AllSoundEvents.CONTROLLER_CLICK.play(level, null, pos, 1, 1);
			return InteractionResult.SUCCESS;
		}

		if (!stack.has(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS)) {
			player.displayClientMessage(CreateLang.translateDirect("track_target.missing")
				.withStyle(ChatFormatting.RED), true);
			return InteractionResult.FAIL;
		}

		CompoundTag blockEntityData = new CompoundTag();
		blockEntityData.putBoolean("TargetDirection", stack.getOrDefault(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION, false));

		BlockPos selectedPos = stack.get(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS);
		BlockPos placedPos = pos.relative(pContext.getClickedFace(), state.canBeReplaced() ? 0 : 1);

		boolean bezier = stack.has(AllDataComponents.TRACK_TARGETING_ITEM_BEZIER);

		if (!selectedPos.closerThan(placedPos, bezier ? AllConfigs.server().trains.maxTrackPlacementLength.get() + 16 : 16)) {
			player.displayClientMessage(CreateLang.translateDirect("track_target.too_far")
				.withStyle(ChatFormatting.RED), true);
			return InteractionResult.FAIL;
		}

		if (bezier) {
			BezierTrackPointLocation bezierTrackPointLocation =
				stack.get(AllDataComponents.TRACK_TARGETING_ITEM_BEZIER);
			CompoundTag bezierNbt = new CompoundTag();
			bezierNbt.putInt("Segment", bezierTrackPointLocation.segment());
			bezierNbt.put("Key", NbtUtils.writeBlockPos(bezierTrackPointLocation.curveTarget()
				.subtract(placedPos)));
			blockEntityData.put("Bezier", bezierNbt);
		}

		blockEntityData.put("TargetTrack", NbtUtils.writeBlockPos(selectedPos.subtract(placedPos)));
		blockEntityData.putString("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
		BlockEntity.addEntityType(blockEntityData, ((IBE<?>) this.getBlock()).getBlockEntityType());

		stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(blockEntityData));
		stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS);
		stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION);
		stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_BEZIER);

		InteractionResult useOn = super.useOn(pContext);
		stack.remove(DataComponents.BLOCK_ENTITY_DATA);

		if (level.isClientSide || useOn == InteractionResult.FAIL)
			return useOn;

		ItemStack itemInHand = player.getItemInHand(pContext.getHand());
		if (!itemInHand.isEmpty()) {
			itemInHand.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS);
			itemInHand.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION);
			itemInHand.remove(AllDataComponents.TRACK_TARGETING_ITEM_BEZIER);
		}
		player.displayClientMessage(CreateLang.translateDirect("track_target.success")
			.withStyle(ChatFormatting.GREEN), true);

		if (type == EdgePointType.SIGNAL)
			AllAdvancements.SIGNAL.awardTo(player);

		return useOn;
	}

	public EdgePointType<?> getType(ItemStack stack) {
		return type;
	}

	@OnlyIn(Dist.CLIENT)
	public boolean useOnCurve(BezierPointSelection selection, ItemStack stack) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		TrackBlockEntity be = selection.blockEntity();
		BezierTrackPointLocation loc = selection.loc();
		boolean front = player.getLookAngle()
			.dot(selection.direction()) < 0;

		CatnipServices.NETWORK.sendToServer(new CurvedTrackSelectionPacket(be.getBlockPos(), loc.curveTarget(),
			front, loc.segment(), player.getInventory().selected));
		return true;
	}

	public static enum OverlapResult {

		VALID,
		OCCUPIED("track_target.occupied"),
		JUNCTION("track_target.no_junctions"),
		NO_TRACK("track_target.invalid");

		public String feedback;

		private OverlapResult() {}

		private OverlapResult(String feedback) {
			this.feedback = feedback;
		}

	}

	public static void withGraphLocation(Level level, BlockPos pos, boolean front,
		BezierTrackPointLocation targetBezier, EdgePointType<?> type,
		BiConsumer<OverlapResult, TrackGraphLocation> callback) {

		BlockState state = level.getBlockState(pos);

		if (!(state.getBlock() instanceof ITrackBlock track)) {
			callback.accept(OverlapResult.NO_TRACK, null);
			return;
		}

		List<Vec3> trackAxes = track.getTrackAxes(level, pos, state);
		if (targetBezier == null && trackAxes.size() > 1) {
			callback.accept(OverlapResult.JUNCTION, null);
			return;
		}

		AxisDirection targetDirection = front ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
		TrackGraphLocation location =
			targetBezier != null ? TrackGraphHelper.getBezierGraphLocationAt(level, pos, targetDirection, targetBezier)
				: TrackGraphHelper.getGraphLocationAt(level, pos, targetDirection, trackAxes.get(0));

		if (location == null) {
			callback.accept(OverlapResult.NO_TRACK, null);
			return;
		}

		Couple<TrackNode> nodes = location.edge.map(location.graph::locateNode);
		TrackEdge edge = location.graph.getConnection(nodes);
		if (edge == null)
			return;

		EdgeData edgeData = edge.getEdgeData();
		double edgePosition = location.position;

		for (TrackEdgePoint edgePoint : edgeData.getPoints()) {
			double otherEdgePosition = edgePoint.getLocationOn(edge);
			double distance = Math.abs(edgePosition - otherEdgePosition);
			if (distance > .75)
				continue;
			if (edgePoint.canCoexistWith(type, front) && distance < .25)
				continue;

			callback.accept(OverlapResult.OCCUPIED, location);
			return;
		}

		callback.accept(OverlapResult.VALID, location);
	}

}
