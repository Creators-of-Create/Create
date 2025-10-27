package com.simibubi.create.content.kinetics.chainConveyor;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import net.createmod.catnip.platform.CatnipServices;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(Dist.CLIENT)
public class ChainConveyorConnectionHandler {

	private static BlockPos firstPos;
	private static ResourceKey<Level> firstDim;

	public static boolean onRightClick() {
		Minecraft mc = Minecraft.getInstance();
		if (!isChain(mc.player.getMainHandItem()))
			return false;
		if (firstPos == null)
			return false;
		boolean missed = false;
		if (mc.hitResult instanceof BlockHitResult bhr && bhr.getType() != Type.MISS)
			if (!(mc.level.getBlockEntity(bhr.getBlockPos()) instanceof ChainConveyorBlockEntity))
				missed = true;
		if (!mc.player.isShiftKeyDown() && !missed)
			return false;
		firstPos = null;
		CreateLang.translate("chain_conveyor.selection_cleared")
			.sendStatus(mc.player);
		return true;
	}

	@SubscribeEvent
	public static void onItemUsedOnBlock(PlayerInteractEvent.RightClickBlock event) {
		ItemStack itemStack = event.getItemStack();
		BlockPos pos = event.getPos();
		Level level = event.getLevel();
		Player player = event.getEntity();
		BlockState blockState = level.getBlockState(pos);

		if (!AllBlocks.CHAIN_CONVEYOR.has(blockState))
			return;
		if (!isChain(itemStack))
			return;
		if (!player.mayBuild() || player instanceof FakePlayer)
			return;

		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.CONSUME);

		if (!level.isClientSide())
			return;
		if (level.getBlockEntity(pos) instanceof ChainConveyorBlockEntity ccbe
			&& ccbe.connections.size() >= AllConfigs.server().kinetics.maxChainConveyorConnections.get()) {
			CreateLang.translate("chain_conveyor.cannot_add_more_connections")
				.style(ChatFormatting.RED)
				.sendStatus(player);
			return;
		}

		if (firstPos == null || firstDim != level.dimension()) {
			firstPos = pos;
			firstDim = level.dimension();
			player.swing(event.getHand());
			return;
		}

		boolean success = validateAndConnect(level, pos, player, itemStack, false);
		firstPos = null;

		if (!success) {
			AllSoundEvents.DENY.play(level, player, pos);
			return;
		}

		SoundType soundtype = Blocks.CHAIN.defaultBlockState()
			.getSoundType();
		if (soundtype != null)
			level.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS,
				(soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
	}

	private static boolean isChain(ItemStack itemStack) {
		return itemStack.is(Items.CHAIN); // Replace with tag? generic renderer?
	}

	public static void clientTick() {
		if (firstPos == null)
			return;

		LocalPlayer player = Minecraft.getInstance().player;
		BlockEntity sourceLift = player.level()
			.getBlockEntity(firstPos);

		if (firstDim != player.level()
			.dimension() || !(sourceLift instanceof ChainConveyorBlockEntity)) {
			firstPos = null;
			CreateLang.translate("chain_conveyor.selection_cleared")
				.sendStatus(player);
			return;
		}

		ItemStack stack = player.getMainHandItem();
		HitResult hitResult = Minecraft.getInstance().hitResult;

		if (!isChain(stack)) {
			stack = player.getOffhandItem();
			if (!isChain(stack))
				return;
		}

		if (hitResult == null || hitResult.getType() != Type.BLOCK) {
			highlightConveyor(firstPos, 0xFFFFFF, "chain_connect");
			return;
		}

		Level level = player.level();
		BlockHitResult bhr = (BlockHitResult) hitResult;
		BlockPos pos = bhr.getBlockPos();
		BlockState hitState = level.getBlockState(pos);

		if (pos.equals(firstPos)) {
			highlightConveyor(firstPos, 0xFFFFFF, "chain_connect");
			CreateLang.translate("chain_conveyor.select_second")
				.sendStatus(player);
			return;
		}

		if (!(hitState.getBlock() instanceof ChainConveyorBlock)) {
			highlightConveyor(firstPos, 0xFFFFFF, "chain_connect");
			return;
		}

		boolean success = validateAndConnect(level, pos, player, stack, true);

		if (success)
			CreateLang.translate("chain_conveyor.valid_connection")
				.style(ChatFormatting.GREEN)
				.sendStatus(player);

		int color = success ? 0x95CD41 : 0xEA5C2B;

		highlightConveyor(firstPos, color, "chain_connect");
		highlightConveyor(pos, color, "chain_connect_to");

		Vec3 from = Vec3.atCenterOf(pos);
		Vec3 to = Vec3.atCenterOf(firstPos);
		Vec3 diff = from.subtract(to);

		if (diff.length() < 1)
			return;

		from = from.subtract(diff.normalize()
			.scale(.5));
		to = to.add(diff.normalize()
			.scale(.5));

		Vec3 normal = diff.cross(new Vec3(0, 1, 0))
			.normalize()
			.scale(.875);

		Outliner.getInstance().showLine("chain_connect_line", from.add(normal), to.add(normal))
			.lineWidth(1 / 16f)
			.colored(color);
		Outliner.getInstance().showLine("chain_connect_line_1", from.subtract(normal), to.subtract(normal))
			.lineWidth(1 / 16f)
			.colored(color);

	}

	private static void highlightConveyor(BlockPos pos, int color, String key) {
		for (int y : Iterate.zeroAndOne) {
			Vec3 prevV = VecHelper.rotate(new Vec3(0, .125 + y * .75, 1.25), -22.5, Axis.Y)
				.add(Vec3.atBottomCenterOf(pos));
			for (int i = 0; i < 8; i++) {
				Vec3 v = VecHelper.rotate(new Vec3(0, .125 + y * .75, 1.25), 22.5 + i * 45, Axis.Y)
					.add(Vec3.atBottomCenterOf(pos));
				Outliner.getInstance().showLine(key + y + i, prevV, v)
					.lineWidth(1 / 16f)
					.colored(color);
				prevV = v;
			}
		}
	}

	public static boolean validateAndConnect(LevelAccessor level, BlockPos pos, Player player, ItemStack chain,
		boolean simulate) {
		if (!simulate && player.isShiftKeyDown()) {
			CreateLang.translate("chain_conveyor.selection_cleared")
				.sendStatus(player);
			return false;
		}

		if (pos.equals(firstPos))
			return false;
		if (!pos.closerThan(firstPos, AllConfigs.server().kinetics.maxChainConveyorLength.get()))
			return fail("chain_conveyor.too_far");
		if (pos.closerThan(firstPos, 2.5))
			return fail("chain_conveyor.too_close");

		Vec3 diff = Vec3.atLowerCornerOf(pos.subtract(firstPos));
		double horizontalDistance = diff.multiply(1, 0, 1)
			.length() - 1.5;

		if (horizontalDistance <= 0)
			return fail("chain_conveyor.cannot_connect_vertically");
		if (Math.abs(diff.y) / horizontalDistance > 1)
			return fail("chain_conveyor.too_steep");

		ChainConveyorBlock chainConveyorBlock = AllBlocks.CHAIN_CONVEYOR.get();
		ChainConveyorBlockEntity sourceLift = chainConveyorBlock.getBlockEntity(level, firstPos);
		ChainConveyorBlockEntity targetLift = chainConveyorBlock.getBlockEntity(level, pos);

		if (targetLift.connections.size() >= AllConfigs.server().kinetics.maxChainConveyorConnections.get())
			return fail("chain_conveyor.cannot_add_more_connections");
		if (targetLift.connections.contains(firstPos.subtract(pos)))
			return fail("chain_conveyor.already_connected");
		if (sourceLift == null || targetLift == null)
			return fail("chain_conveyor.blocks_invalid");

		if (!player.isCreative()) {
			int chainCost = ChainConveyorBlockEntity.getChainCost(pos.subtract(firstPos));
			boolean hasEnough = ChainConveyorBlockEntity.getChainsFromInventory(player, chain, chainCost, true);
			if (simulate)
				BlueprintOverlayRenderer.displayChainRequirements(chain.getItem(), chainCost, hasEnough);
			if (!hasEnough)
				return fail("chain_conveyor.not_enough_chains");
		}

		if (simulate)
			return true;

		CatnipServices.NETWORK.sendToServer(new ChainConveyorConnectionPacket(firstPos, pos, chain, true));

		CreateLang.text("") // Clear status message
			.sendStatus(player);
		firstPos = null;
		firstDim = null;
		return true;
	}

	private static boolean fail(String message) {
		CreateLang.translate(message)
			.style(ChatFormatting.RED)
			.sendStatus(Minecraft.getInstance().player);
		return false;
	}

}
