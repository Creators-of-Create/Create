package com.simibubi.create.foundation.tileEntity.behaviour.linked;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

public class LinkRenderer {

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		RayTraceResult target = mc.objectMouseOver;
		if (target == null || !(target instanceof BlockRayTraceResult))
			return;

		BlockRayTraceResult result = (BlockRayTraceResult) target;
		ClientWorld world = mc.world;
		BlockPos pos = result.getPos();

		LinkBehaviour behaviour = TileEntityBehaviour.get(world, pos, LinkBehaviour.TYPE);
		if (behaviour == null)
			return;

		String freq1 = Lang.translate("logistics.firstFrequency");
		String freq2 = Lang.translate("logistics.secondFrequency");

		for (boolean first : Iterate.trueAndFalse) {
			AxisAlignedBB bb = new AxisAlignedBB(Vector3d.ZERO, Vector3d.ZERO).grow(.25f);
			String label = first ? freq2 : freq1;
			boolean hit = behaviour.testHit(first, target.getHitVec());
			ValueBoxTransform transform = first ? behaviour.firstSlot : behaviour.secondSlot;

			ValueBox box = new ValueBox(label, bb, pos).withColors(0x601F18, 0xB73C2D)
				.offsetLabel(behaviour.textShift)
				.passive(!hit);
			CreateClient.outliner.showValueBox(Pair.of(Boolean.valueOf(first), pos), box.transform(transform))
				.lineWidth(1 / 64f)
				.withFaceTexture(hit ? AllSpecialTextures.THIN_CHECKERED : null)
				.highlightFace(result.getFace());
		}
	}

	public static void renderOnTileEntity(SmartTileEntity te, float partialTicks, MatrixStack ms,
		IRenderTypeBuffer buffer, int light, int overlay) {

		if (te == null || te.isRemoved())
			return;
		LinkBehaviour behaviour = te.getBehaviour(LinkBehaviour.TYPE);
		if (behaviour == null)
			return;

		for (boolean first : Iterate.trueAndFalse) {
			ValueBoxTransform transform = first ? behaviour.firstSlot : behaviour.secondSlot;
			ItemStack stack = first ? behaviour.frequencyFirst.getStack() : behaviour.frequencyLast.getStack();

			ms.push();
			transform.transform(te.getBlockState(), ms);
			ValueBoxRenderer.renderItemIntoValueBox(stack, ms, buffer, light, overlay);
			ms.pop();
		}

	}

}
