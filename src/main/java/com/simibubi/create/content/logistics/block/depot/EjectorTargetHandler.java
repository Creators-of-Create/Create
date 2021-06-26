package com.simibubi.create.content.logistics.block.depot;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
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
		World world = event.getWorld();
		if (!world.isRemote)
			return;
		PlayerEntity player = event.getPlayer();
		if (player == null || player.isSpectator() || !player.isSneaking())
			return;

		String key = "weighted_ejector.target_set";
		TextFormatting colour = TextFormatting.GOLD;
		player.sendStatusMessage(Lang.translate(key).formatted(colour), true);
		currentSelection = pos;
		launcher = null;
		event.setCanceled(true);
		event.setCancellationResult(ActionResultType.SUCCESS);
	}

	@SubscribeEvent
	public static void leftClickingBlocksDeselectsThem(PlayerInteractEvent.LeftClickBlock event) {
		if (currentItem == null)
			return;
		if (!event.getWorld().isRemote)
			return;
		if (!event.getPlayer()
			.isSneaking())
			return;
		BlockPos pos = event.getPos();
		if (pos.equals(currentSelection)) {
			currentSelection = null;
			launcher = null;
			event.setCanceled(true);
			event.setCancellationResult(ActionResultType.SUCCESS);
		}
	}

	public static void flushSettings(BlockPos pos) {
		if (currentItem == null)
			return;

		int h = 0;
		int v = 0;

		ClientPlayerEntity player = Minecraft.getInstance().player;
		String key = "weighted_ejector.target_not_valid";
		TextFormatting colour = TextFormatting.WHITE;

		if (currentSelection == null)
			key = "weighted_ejector.no_target";

		Direction validTargetDirection = getValidTargetDirection(pos);
		if (validTargetDirection == null) {
			player.sendStatusMessage(Lang.translate(key).formatted(colour), true);
			currentItem = null;
			currentSelection = null;
			return;
		}

		key = "weighted_ejector.targeting";
		colour = TextFormatting.GREEN;

		player.sendStatusMessage(
			Lang.translate(key, currentSelection.getX(), currentSelection.getY(), currentSelection.getZ())
				.formatted(colour),
			true);

		BlockPos diff = pos.subtract(currentSelection);
		h = Math.abs(diff.getX() + diff.getZ());
		v = -diff.getY();

		AllPackets.channel.sendToServer(new EjectorPlacementPacket(h, v, pos, validTargetDirection));
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
		int max = AllConfigs.SERVER.kinetics.maxEjectorDistance.get();

		if (Math.abs(xDiff) > max || Math.abs(zDiff) > max)
			return null;

		if (xDiff == 0)
			return Direction.getFacingFromAxis(zDiff < 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE, Axis.Z);
		if (zDiff == 0)
			return Direction.getFacingFromAxis(xDiff < 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE, Axis.X);

		return null;
	}

	public static void tick() {
		PlayerEntity player = Minecraft.getInstance().player;

		if (player == null)
			return;

		ItemStack heldItemMainhand = player.getHeldItemMainhand();
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
		boolean wrench = AllItems.WRENCH.isIn(mc.player.getHeldItemMainhand());

		if (currentSelection == null)
			return;
		if (currentItem == null && !wrench)
			return;

		RayTraceResult objectMouseOver = mc.objectMouseOver;
		if (!(objectMouseOver instanceof BlockRayTraceResult))
			return;
		BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) objectMouseOver;
		if (blockRayTraceResult.getType() == Type.MISS)
			return;

		BlockPos pos = blockRayTraceResult.getPos();
		if (!wrench)
			pos = pos.offset(blockRayTraceResult.getFace());

		int xDiff = currentSelection.getX() - pos.getX();
		int yDiff = currentSelection.getY() - pos.getY();
		int zDiff = currentSelection.getZ() - pos.getZ();
		int validX = Math.abs(zDiff) > Math.abs(xDiff) ? 0 : xDiff;
		int validZ = Math.abs(zDiff) < Math.abs(xDiff) ? 0 : zDiff;

		BlockPos validPos = currentSelection.add(validX, yDiff, validZ);
		Direction d = getValidTargetDirection(validPos);
		if (d == null)
			return;
		if (launcher == null || lastHoveredBlockPos != pos.toLong()) {
			lastHoveredBlockPos = pos.toLong();
			launcher = new EntityLauncher(Math.abs(validX + validZ), yDiff);
		}

		double totalFlyingTicks = launcher.getTotalFlyingTicks() + 3;
		int segments = (((int) totalFlyingTicks) / 3) + 1;
		double tickOffset = totalFlyingTicks / segments;
		boolean valid = xDiff == validX && zDiff == validZ;
		int intColor = valid ? 0x9ede73 : 0xff7171;
		Vector3d color = ColorHelper.getRGB(intColor);
		RedstoneParticleData data = new RedstoneParticleData((float) color.x, (float) color.y, (float) color.z, 1);
		ClientWorld world = mc.world;

		AxisAlignedBB bb = new AxisAlignedBB(0, 0, 0, 1, 0, 1).offset(currentSelection.add(-validX, -yDiff, -validZ));
		CreateClient.OUTLINER.chaseAABB("valid", bb)
				.colored(intColor)
				.lineWidth(1 / 16f);

		for (int i = 0; i < segments; i++) {
			double ticks = ((AnimationTickHolder.getRenderTime() / 3) % tickOffset) + i * tickOffset;
			Vector3d vec = launcher.getGlobalPos(ticks, d, pos)
					.add(xDiff - validX, 0, zDiff - validZ);
			world.addParticle(data, vec.x, vec.y, vec.z, 0, 0, 0);
		}
	}

	private static void checkForWrench(ItemStack heldItem) {
		if (!AllItems.WRENCH.isIn(heldItem))
			return;
		RayTraceResult objectMouseOver = Minecraft.getInstance().objectMouseOver;
		if (!(objectMouseOver instanceof BlockRayTraceResult))
			return;
		BlockRayTraceResult result = (BlockRayTraceResult) objectMouseOver;
		BlockPos pos = result.getPos();

		TileEntity te = Minecraft.getInstance().world.getTileEntity(pos);
		if (!(te instanceof EjectorTileEntity)) {
			lastHoveredBlockPos = -1;
			currentSelection = null;
			return;
		}

		if (lastHoveredBlockPos == -1 || lastHoveredBlockPos != pos.toLong()) {
			EjectorTileEntity ejector = (EjectorTileEntity) te;
			if (!ejector.getTargetPosition()
				.equals(ejector.getPos()))
				currentSelection = ejector.getTargetPosition();
			lastHoveredBlockPos = pos.toLong();
			launcher = null;
		}

		if (lastHoveredBlockPos != -1)
			drawOutline(currentSelection);
	}

	private static void drawOutline(BlockPos selection) {
		World world = Minecraft.getInstance().world;
		if (currentSelection == null)
			return;

		BlockPos pos = currentSelection;
		BlockState state = world.getBlockState(pos);
		VoxelShape shape = state.getShape(world, pos);
		AxisAlignedBB boundingBox = shape.isEmpty() ? new AxisAlignedBB(BlockPos.ZERO) : shape.getBoundingBox();
		CreateClient.OUTLINER.showAABB("target", boundingBox.offset(pos))
				.colored(0xffcb74)
				.lineWidth(1 / 16f);
	}

}
