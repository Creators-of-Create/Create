package com.simibubi.create.foundation.tileEntity.behaviour.linked;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;

public class LinkRenderer {

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		RayTraceResult target = mc.hitResult;
		if (target == null || !(target instanceof BlockRayTraceResult))
			return;

		BlockRayTraceResult result = (BlockRayTraceResult) target;
		ClientWorld world = mc.level;
		BlockPos pos = result.getBlockPos();

		LinkBehaviour behaviour = TileEntityBehaviour.get(world, pos, LinkBehaviour.TYPE);
		if (behaviour == null)
			return;

		ITextComponent freq1 = Lang.translate("logistics.firstFrequency");
		ITextComponent freq2 = Lang.translate("logistics.secondFrequency");

		for (boolean first : Iterate.trueAndFalse) {
			AxisAlignedBB bb = new AxisAlignedBB(Vector3d.ZERO, Vector3d.ZERO).inflate(.25f);
			ITextComponent label = first ? freq1 : freq2;
			boolean hit = behaviour.testHit(first, target.getLocation());
			ValueBoxTransform transform = first ? behaviour.firstSlot : behaviour.secondSlot;

			ValueBox box = new ValueBox(label, bb, pos).withColors(0x601F18, 0xB73C2D)
					.offsetLabel(behaviour.textShift)
					.passive(!hit);
			CreateClient.OUTLINER.showValueBox(Pair.of(Boolean.valueOf(first), pos), box.transform(transform))
					.lineWidth(1 / 64f)
					.withFaceTexture(hit ? AllSpecialTextures.THIN_CHECKERED : null)
					.highlightFace(result.getDirection());
		}
	}

	public static void renderOnTileEntity(SmartTileEntity te, float partialTicks, MatrixStack ms,
		IRenderTypeBuffer buffer, int light, int overlay) {

		if (te == null || te.isRemoved())
			return;
		
		Entity cameraEntity = Minecraft.getInstance().cameraEntity;
		float max = AllConfigs.CLIENT.filterItemRenderDistance.getF();
		if (!te.isVirtual() && cameraEntity != null && cameraEntity.position()
			.distanceToSqr(VecHelper.getCenterOf(te.getBlockPos())) > (max * max))
			return;

		LinkBehaviour behaviour = te.getBehaviour(LinkBehaviour.TYPE);
		if (behaviour == null)
			return;

		for (boolean first : Iterate.trueAndFalse) {
			ValueBoxTransform transform = first ? behaviour.firstSlot : behaviour.secondSlot;
			ItemStack stack = first ? behaviour.frequencyFirst.getStack() : behaviour.frequencyLast.getStack();

			ms.pushPose();
			transform.transform(te.getBlockState(), ms);
			ValueBoxRenderer.renderItemIntoValueBox(stack, ms, buffer, light, overlay);
			ms.popPose();
		}

	}

}
