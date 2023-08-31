package com.simibubi.create.content.logistics.depot;

import org.joml.Vector3f;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.CatnipClient;
import net.createmod.catnip.utility.AnimationTickHolder;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.theme.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class EjectorTargetHandler {

	static BlockPos currentSelection;
	static ItemStack currentItem;
	static long lastHoveredBlockPos = -1;
	static EntityLauncher launcher;

	@SubscribeEvent
	public static void rightClickingBlocksSelectsThem(PlayerInteractEvent.RightClickBlock event) {
		if (currentItem == null)
			return;
		BlockPos pos = event.getPos();
		Level world = event.getLevel();
		if (!world.isClientSide)
			return;
		Player player = event.getEntity();
		if (player == null || player.isSpectator() || !player.isShiftKeyDown())
			return;

		String key = "weighted_ejector.target_set";
		ChatFormatting colour = ChatFormatting.GOLD;
		player.displayClientMessage(CreateLang.translateDirect(key)
			.withStyle(colour), true);
		currentSelection = pos;
		launcher = null;
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.SUCCESS);
	}

	@SubscribeEvent
	public static void leftClickingBlocksDeselectsThem(PlayerInteractEvent.LeftClickBlock event) {
		if (currentItem == null)
			return;
		if (!event.getLevel().isClientSide)
			return;
		if (!event.getEntity()
			.isShiftKeyDown())
			return;
		BlockPos pos = event.getPos();
		if (pos.equals(currentSelection)) {
			currentSelection = null;
			launcher = null;
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.SUCCESS);
		}
	}

	public static void flushSettings(BlockPos pos) {
		int h = 0;
		int v = 0;

		LocalPlayer player = Minecraft.getInstance().player;
		String key = "weighted_ejector.target_not_valid";
		ChatFormatting colour = ChatFormatting.WHITE;

		if (currentSelection == null)
			key = "weighted_ejector.no_target";

		Direction validTargetDirection = getValidTargetDirection(pos);
		if (validTargetDirection == null) {
			player.displayClientMessage(CreateLang.translateDirect(key)
				.withStyle(colour), true);
			currentItem = null;
			currentSelection = null;
			return;
		}

		key = "weighted_ejector.targeting";
		colour = ChatFormatting.GREEN;

		player.displayClientMessage(
			CreateLang.translateDirect(key, currentSelection.getX(), currentSelection.getY(), currentSelection.getZ())
				.withStyle(colour),
			true);

		BlockPos diff = pos.subtract(currentSelection);
		h = Math.abs(diff.getX() + diff.getZ());
		v = -diff.getY();

		AllPackets.getChannel().sendToServer(new EjectorPlacementPacket(h, v, pos, validTargetDirection));
		currentSelection = null;
		currentItem = null;

	}

	public static Direction getValidTargetDirection(BlockPos pos) {
		if (currentSelection == null)
			return null;
		if (VecHelper.onSameAxis(pos, currentSelection, Axis.Y))
			return null;

		int xDiff = currentSelection.getX() - pos.getX();
		int zDiff = currentSelection.getZ() - pos.getZ();
		int max = AllConfigs.server().kinetics.maxEjectorDistance.get();

		if (Math.abs(xDiff) > max || Math.abs(zDiff) > max)
			return null;

		if (xDiff == 0)
			return Direction.get(zDiff < 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE, Axis.Z);
		if (zDiff == 0)
			return Direction.get(xDiff < 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE, Axis.X);

		return null;
	}

	public static void tick() {
		Player player = Minecraft.getInstance().player;

		if (player == null)
			return;

		ItemStack heldItemMainhand = player.getMainHandItem();
		if (!AllBlocks.WEIGHTED_EJECTOR.isIn(heldItemMainhand)) {
			currentItem = null;
		} else {
			if (heldItemMainhand != currentItem) {
				currentSelection = null;
				currentItem = heldItemMainhand;
			}
			drawOutline(currentSelection);
		}

		checkForWrench(heldItemMainhand);
		drawArc();
	}

	protected static void drawArc() {
		Minecraft mc = Minecraft.getInstance();
		boolean wrench = AllItems.WRENCH.isIn(mc.player.getMainHandItem());

		if (currentSelection == null)
			return;
		if (currentItem == null && !wrench)
			return;

		HitResult objectMouseOver = mc.hitResult;
		if (!(objectMouseOver instanceof BlockHitResult))
			return;
		BlockHitResult blockRayTraceResult = (BlockHitResult) objectMouseOver;
		if (blockRayTraceResult.getType() == Type.MISS)
			return;

		BlockPos pos = blockRayTraceResult.getBlockPos();
		if (!wrench)
			pos = pos.relative(blockRayTraceResult.getDirection());

		int xDiff = currentSelection.getX() - pos.getX();
		int yDiff = currentSelection.getY() - pos.getY();
		int zDiff = currentSelection.getZ() - pos.getZ();
		int validX = Math.abs(zDiff) > Math.abs(xDiff) ? 0 : xDiff;
		int validZ = Math.abs(zDiff) < Math.abs(xDiff) ? 0 : zDiff;

		BlockPos validPos = currentSelection.offset(validX, yDiff, validZ);
		Direction d = getValidTargetDirection(validPos);
		if (d == null)
			return;
		if (launcher == null || lastHoveredBlockPos != pos.asLong()) {
			lastHoveredBlockPos = pos.asLong();
			launcher = new EntityLauncher(Math.abs(validX + validZ), yDiff);
		}

		double totalFlyingTicks = launcher.getTotalFlyingTicks() + 3;
		int segments = (((int) totalFlyingTicks) / 3) + 1;
		double tickOffset = totalFlyingTicks / segments;
		boolean valid = xDiff == validX && zDiff == validZ;
		int intColor = valid ? 0x9ede73 : 0xff7171;
		Vector3f color = new Color(intColor).asVectorF();
		DustParticleOptions data = new DustParticleOptions(color, 1);
		ClientLevel world = mc.level;

		AABB bb = new AABB(0, 0, 0, 1, 0, 1).move(currentSelection.offset(-validX, -yDiff, -validZ));
		CatnipClient.OUTLINER.chaseAABB("valid", bb)
			.colored(intColor)
			.lineWidth(1 / 16f);

		for (int i = 0; i < segments; i++) {
			double ticks = ((AnimationTickHolder.getRenderTime() / 3) % tickOffset) + i * tickOffset;
			Vec3 vec = launcher.getGlobalPos(ticks, d, pos)
				.add(xDiff - validX, 0, zDiff - validZ);
			world.addParticle(data, vec.x, vec.y, vec.z, 0, 0, 0);
		}
	}

	private static void checkForWrench(ItemStack heldItem) {
		if (!AllItems.WRENCH.isIn(heldItem))
			return;
		HitResult objectMouseOver = Minecraft.getInstance().hitResult;
		if (!(objectMouseOver instanceof BlockHitResult))
			return;
		BlockHitResult result = (BlockHitResult) objectMouseOver;
		BlockPos pos = result.getBlockPos();

		BlockEntity be = Minecraft.getInstance().level.getBlockEntity(pos);
		if (!(be instanceof EjectorBlockEntity)) {
			lastHoveredBlockPos = -1;
			currentSelection = null;
			return;
		}

		if (lastHoveredBlockPos == -1 || lastHoveredBlockPos != pos.asLong()) {
			EjectorBlockEntity ejector = (EjectorBlockEntity) be;
			if (!ejector.getTargetPosition()
				.equals(ejector.getBlockPos()))
				currentSelection = ejector.getTargetPosition();
			lastHoveredBlockPos = pos.asLong();
			launcher = null;
		}

		if (lastHoveredBlockPos != -1)
			drawOutline(currentSelection);
	}

	public static void drawOutline(BlockPos selection) {
		Level world = Minecraft.getInstance().level;
		if (selection == null)
			return;

		BlockPos pos = selection;
		BlockState state = world.getBlockState(pos);
		VoxelShape shape = state.getShape(world, pos);
		AABB boundingBox = shape.isEmpty() ? new AABB(BlockPos.ZERO) : shape.bounds();
		CatnipClient.OUTLINER.showAABB("target", boundingBox.move(pos))
			.colored(0xffcb74)
			.lineWidth(1 / 16f);
	}

}
