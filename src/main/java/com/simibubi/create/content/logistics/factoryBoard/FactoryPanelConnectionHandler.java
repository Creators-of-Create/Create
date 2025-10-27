package com.simibubi.create.content.logistics.factoryBoard;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

public class FactoryPanelConnectionHandler {

	static FactoryPanelPosition connectingFrom;
	static AABB connectingFromBox;

	static boolean relocating;
	static FactoryPanelPosition validRelocationTarget;

	public static boolean panelClicked(LevelAccessor level, Player player, FactoryPanelBehaviour panel) {
		if (connectingFrom == null)
			return false;

		FactoryPanelBehaviour at = FactoryPanelBehaviour.at(level, connectingFrom);
		if (panel.getPanelPosition()
			.equals(connectingFrom) || at == null) {
			player.displayClientMessage(Component.empty(), true);
			connectingFrom = null;
			connectingFromBox = null;
			return true;
		}

		String checkForIssues = checkForIssues(at, panel);
		if (checkForIssues != null) {
			player.displayClientMessage(CreateLang.translate(checkForIssues)
				.style(ChatFormatting.RED)
				.component(), true);
			connectingFrom = null;
			connectingFromBox = null;
			AllSoundEvents.DENY.playAt(player.level(), player.blockPosition(), 1, 1, false);
			return true;
		}

		ItemStack filterFrom = panel.getFilter();
		ItemStack filterTo = at.getFilter();

		CatnipServices.NETWORK.sendToServer(new FactoryPanelConnectionPacket(panel.getPanelPosition(), connectingFrom, false));

		player.displayClientMessage(CreateLang.translate("factory_panel.panels_connected", filterFrom.getHoverName()
			.getString(),
			filterTo.getHoverName()
				.getString())
			.style(ChatFormatting.GREEN)
			.component(), true);

		connectingFrom = null;
		connectingFromBox = null;
		player.level()
			.playLocalSound(player.blockPosition(), SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.BLOCKS, 0.5f, 0.5f,
				false);

		return true;
	}

	@Nullable
	private static String checkForIssues(FactoryPanelBehaviour from, FactoryPanelBehaviour to) {
		if (from == null)
			return "factory_panel.connection_aborted";
		if (from.targetedBy.containsKey(to.getPanelPosition()))
			return "factory_panel.already_connected";
		if (from.targetedBy.size() >= 9)
			return "factory_panel.cannot_add_more_inputs";

		BlockState state1 = to.blockEntity.getBlockState();
		BlockState state2 = from.blockEntity.getBlockState();
		BlockPos diff = to.getPos()
			.subtract(from.getPos());

		if (state1.setValue(FactoryPanelBlock.WATERLOGGED, false)
			.setValue(FactoryPanelBlock.POWERED, false) != state2.setValue(FactoryPanelBlock.WATERLOGGED, false)
				.setValue(FactoryPanelBlock.POWERED, false))
			return "factory_panel.same_orientation";

		if (FactoryPanelBlock.connectedDirection(state1)
			.getAxis()
			.choose(diff.getX(), diff.getY(), diff.getZ()) != 0)
			return "factory_panel.same_surface";

		if (!diff.closerThan(BlockPos.ZERO, 16))
			return "factory_panel.too_far_apart";

		if (to.panelBE().restocker)
			return "factory_panel.input_in_restock_mode";

		if (to.getFilter()
			.isEmpty()
			|| from.getFilter()
				.isEmpty())
			return "factory_panel.no_item";

		return null;
	}

	@Nullable
	private static String checkForIssues(FactoryPanelBehaviour from, FactoryPanelSupportBehaviour to) {
		if (from == null)
			return "factory_panel.connection_aborted";

		BlockState state1 = from.blockEntity.getBlockState();
		BlockState state2 = to.blockEntity.getBlockState();
		BlockPos diff = to.getPos()
			.subtract(from.getPos());
		Direction connectedDirection = FactoryPanelBlock.connectedDirection(state1);

		if (connectedDirection != state2.getOptionalValue(WrenchableDirectionalBlock.FACING)
			.orElse(connectedDirection))
			return "factory_panel.same_orientation";

		if (connectedDirection.getAxis()
			.choose(diff.getX(), diff.getY(), diff.getZ()) != 0)
			return "factory_panel.same_surface";

		if (!diff.closerThan(BlockPos.ZERO, 16))
			return "factory_panel.too_far_apart";

		return null;
	}

	public static void clientTick() {
		if (connectingFrom == null || connectingFromBox == null)
			return;

		Minecraft mc = Minecraft.getInstance();
		FactoryPanelBehaviour at = FactoryPanelBehaviour.at(mc.level, connectingFrom);

		if (!connectingFrom.pos()
			.closerThan(mc.player.blockPosition(), 16) || at == null) {
			connectingFrom = null;
			connectingFromBox = null;
			mc.player.displayClientMessage(Component.empty(), true);
			return;
		}

		Outliner.getInstance()
			.showAABB(connectingFrom, connectingFromBox)
			.colored(AnimationTickHolder.getTicks() % 16 > 8 ? 0x38b764 : 0xa7f070)
			.lineWidth(1 / 16f);

		mc.player.displayClientMessage(
			CreateLang.translate(relocating ? "factory_panel.click_to_relocate" : "factory_panel.click_second_panel")
				.component(),
			true);

		if (!relocating)
			return;

		validRelocationTarget = null;

		if (!(mc.hitResult instanceof BlockHitResult bhr) || bhr.getType() == Type.MISS)
			return;

		Vec3 offsetPos = bhr.getLocation()
			.add(Vec3.atLowerCornerOf(bhr.getDirection()
				.getNormal())
				.scale(1 / 32f));
		BlockPos pos = BlockPos.containing(offsetPos);
		BlockState blockState = at.blockEntity.getBlockState();
		PanelSlot slot = FactoryPanelBlock.getTargetedSlot(pos, blockState, offsetPos);
		BlockPos diff = pos.subtract(connectingFrom.pos());
		Direction facing = FactoryPanelBlock.connectedDirection(blockState);

		if (facing.getAxis()
			.choose(diff.getX(), diff.getY(), diff.getZ()) != 0)
			return;
		if (!AllBlocks.FACTORY_GAUGE.get()
			.canSurvive(blockState, mc.level, pos))
			return;
		if (AllBlocks.PACKAGER.has(mc.level.getBlockState(pos.relative(facing.getOpposite()))))
			return;

		validRelocationTarget = new FactoryPanelPosition(pos, slot);

		Outliner.getInstance()
			.showAABB("target", getBB(blockState, validRelocationTarget))
			.colored(0xeeeeee)
			.disableLineNormals()
			.lineWidth(1 / 16f);
	}

	public static boolean onRightClick() {
		if (connectingFrom == null || connectingFromBox == null)
			return false;
		Minecraft mc = Minecraft.getInstance();
		boolean missed = false;

		if (relocating) {
			if (mc.player.isShiftKeyDown())
				validRelocationTarget = null;
			if (validRelocationTarget != null)
				CatnipServices.NETWORK.sendToServer(new FactoryPanelConnectionPacket(validRelocationTarget, connectingFrom, true));

			connectingFrom = null;
			connectingFromBox = null;

			if (validRelocationTarget == null)
				mc.player.displayClientMessage(CreateLang.translate("factory_panel.relocation_aborted")
					.component(), true);

			relocating = false;
			validRelocationTarget = null;
			return true;
		}

		if (mc.hitResult instanceof BlockHitResult bhr && bhr.getType() != Type.MISS) {
			BlockEntity blockEntity = mc.level.getBlockEntity(bhr.getBlockPos());
			FactoryPanelSupportBehaviour behaviour =
				BlockEntityBehaviour.get(mc.level, bhr.getBlockPos(), FactoryPanelSupportBehaviour.TYPE);

			// Connecting redstone or display links
			if (behaviour != null) {
				FactoryPanelBehaviour at = FactoryPanelBehaviour.at(mc.level, connectingFrom);
				String checkForIssues = checkForIssues(at, behaviour);
				if (checkForIssues != null) {
					mc.player.displayClientMessage(CreateLang.translate(checkForIssues)
						.style(ChatFormatting.RED)
						.component(), true);
					connectingFrom = null;
					connectingFromBox = null;
					AllSoundEvents.DENY.playAt(mc.level, mc.player.blockPosition(), 1, 1, false);
					return true;
				}

				FactoryPanelPosition bestPosition = null;
				double bestDistance = Double.POSITIVE_INFINITY;

				for (PanelSlot slot : PanelSlot.values()) {
					FactoryPanelPosition panelPosition = new FactoryPanelPosition(blockEntity.getBlockPos(), slot);
					FactoryPanelConnection connection = new FactoryPanelConnection(panelPosition, 1);
					Vec3 diff =
						connection.calculatePathDiff(mc.level.getBlockState(connectingFrom.pos()), connectingFrom);
					if (bestDistance < diff.lengthSqr())
						continue;
					bestDistance = diff.lengthSqr();
					bestPosition = panelPosition;
				}

				CatnipServices.NETWORK.sendToServer(new FactoryPanelConnectionPacket(bestPosition, connectingFrom, false));

				mc.player.displayClientMessage(CreateLang
					.translate("factory_panel.link_connected", blockEntity.getBlockState()
						.getBlock()
						.getName())
					.style(ChatFormatting.GREEN)
					.component(), true);

				connectingFrom = null;
				connectingFromBox = null;
				mc.player.level()
					.playLocalSound(mc.player.blockPosition(), SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.BLOCKS,
						0.5f, 0.5f, false);
				return true;
			}

			if (!(blockEntity instanceof FactoryPanelBlockEntity))
				missed = true;
		}

		if (!mc.player.isShiftKeyDown() && !missed)
			return false;
		connectingFrom = null;
		connectingFromBox = null;
		mc.player.displayClientMessage(CreateLang.translate("factory_panel.connection_aborted")
			.component(), true);
		return true;
	}

	public static void startRelocating(FactoryPanelBehaviour behaviour) {
		startConnection(behaviour);
		relocating = true;
	}

	public static void startConnection(FactoryPanelBehaviour behaviour) {
		relocating = false;
		connectingFrom = behaviour.getPanelPosition();
		connectingFromBox = getBB(behaviour.blockEntity.getBlockState(), connectingFrom);
	}

	public static AABB getBB(BlockState blockState, FactoryPanelPosition factoryPanelPosition) {
		Vec3 location = FactoryPanelSlotPositioning.getCenterOfSlot(blockState, factoryPanelPosition.slot())
			.add(Vec3.atLowerCornerOf(factoryPanelPosition.pos()));
		Vec3 plane = VecHelper.axisAlingedPlaneOf(FactoryPanelBlock.connectedDirection(blockState));
		return new AABB(location, location).inflate(plane.x * 3 / 16f, plane.y * 3 / 16f, plane.z * 3 / 16f);
	}

}
