package com.simibubi.create.content.contraptions.components.crafter;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.TessellatorHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class ConnectedInputRenderer {

	@SubscribeEvent
	public static void renderHighlight(DrawHighlightEvent event) {
		RayTraceResult target = event.getTarget();
		if (!(target instanceof BlockRayTraceResult))
			return;

		BlockRayTraceResult result = (BlockRayTraceResult) target;
		ClientWorld world = Minecraft.getInstance().world;
		BlockPos pos = result.getPos();
		BlockState blockState = world.getBlockState(pos);
		PlayerEntity player = Minecraft.getInstance().player;
		ItemStack heldItem = player.getHeldItem(Hand.MAIN_HAND);
		Direction face = result.getFace();

		if (player.isSneaking())
			return;
		if (!AllItems.WRENCH.typeOf(heldItem))
			return;
		if (!AllBlocks.MECHANICAL_CRAFTER.has(blockState))
			return;
		if (target.getType() != Type.BLOCK)
			return;
		if (face == blockState.get(MechanicalCrafterBlock.HORIZONTAL_FACING))
			return;

		TessellatorHelper.prepareForDrawing();
		RenderSystem.translated(pos.getX(), pos.getY(), pos.getZ());
		Direction activatedDirection = ConnectedInputHandler.getActivatedDirection(world, pos, face,
				result.getHitVec());

		for (Pair<Direction, Vec3d> pair : ConnectedInputHandler.getConnectiveSides(world, pos, face)) {

			int zRot = face == Direction.UP ? 90 : face == Direction.DOWN ? 270 : 0;
			float yRot = AngleHelper.horizontalAngle(face.getOpposite());
			Vec3d rotation = new Vec3d(0, yRot, zRot);
//
//			GlHelper.renderTransformed(pair.getValue(), rotation, .5f, () -> {
//
//				String label = "Connect / Disconnect";// Lang.translate("crafter.connect");
//				AxisAlignedBB bb = new AxisAlignedBB(Vec3d.ZERO, Vec3d.ZERO).grow(1/3f);
//				ValueBox box = new ValueBox(label, bb, pos);
//				box.withColors(0x018383, 0x42e6a4).offsetLabel(new Vec3d(10, 0, 0));
//				ValueBoxRenderer.renderBox(box, activatedDirection == pair.getKey());

//			});
		}

		TessellatorHelper.cleanUpAfterDrawing();
	}
}
