package com.simibubi.create.compat.curios;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;

import com.simibubi.create.content.curiosities.armor.CopperBacktankBlock;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

@OnlyIn(Dist.CLIENT)
public class CopperBacktankCurioRenderer implements ICurioRenderer {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(Create.ID, "copper_backtank"), "copper_backtank");

	private final HumanoidModel<LivingEntity> model;

	public CopperBacktankCurioRenderer(ModelPart part) {
		this.model = new HumanoidModel<>(part);
	}

	@Override
	public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		// Copied from com.simibubi.create.content.curiosities.armor.CopperBacktankArmorLayer
		// with minor changes (ms -> matrixStack, entity.level -> slotContext.entity().level)

		RenderType renderType = Sheets.cutoutBlockSheet();
		BlockState renderedState = AllBlocks.COPPER_BACKTANK.getDefaultState()
				.setValue(CopperBacktankBlock.HORIZONTAL_FACING, Direction.SOUTH);
		SuperByteBuffer backtank = CachedBufferer.block(renderedState);
		SuperByteBuffer cogs = CachedBufferer.partial(AllBlockPartials.COPPER_BACKTANK_COGS, renderedState);

		matrixStack.pushPose();

		model.body.translateAndRotate(matrixStack);
		matrixStack.translate(-1 / 2f, 10 / 16f, 1f);
		matrixStack.scale(1, -1, -1);

		// If we have a backtank in the armor slot, shift it around so the "back" slot is a micro backtank
		ItemStack chestItemSlot = slotContext.entity().getItemBySlot(EquipmentSlot.CHEST);
		if (AllItems.COPPER_BACKTANK.isIn(chestItemSlot)) {
			matrixStack.translate(0.125f, 10 / 16f, 0.125f);
			matrixStack.scale(0.75f, 0.75f, 0.75f);
		}

		backtank.forEntityRender()
				.light(light)
				.renderInto(matrixStack, buffer.getBuffer(renderType));

		cogs.centre()
				.rotateY(180)
				.unCentre()
				.translate(0, 6.5f / 16, 11f / 16)
				.rotate(Direction.EAST, AngleHelper.rad(2 * AnimationTickHolder.getRenderTime(slotContext.entity().level) % 360))
				.translate(0, -6.5f / 16, -11f / 16);

		cogs.forEntityRender()
				.light(light)
				.renderInto(matrixStack, buffer.getBuffer(renderType));

		matrixStack.popPose();
	}

	public static MeshDefinition mesh() {
		CubeListBuilder builder = new CubeListBuilder();
		MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
		mesh.getRoot().addOrReplaceChild("back", builder, PartPose.ZERO);
		return mesh;
	}
}
