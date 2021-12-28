package com.simibubi.create.compat.rei.category;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.rei.category.animations.AnimatedKinetics;
import com.simibubi.create.compat.rei.display.AbstractCreateDisplay;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Lang;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public abstract class ProcessingViaFanCategory<T extends Recipe<?>, D extends AbstractCreateDisplay<T>> extends CreateRecipeCategory<T, D> {

	public ProcessingViaFanCategory(Renderer icon) {
		this(177, icon);
	}

	protected ProcessingViaFanCategory(int width, Renderer icon) {
		super(icon, emptyBackground(width, 76));
	}

	public static Supplier<ItemStack> getFan(String name) {
		return () -> AllBlocks.ENCASED_FAN.asStack()
			.setHoverName(Lang.translate("recipe." + name + ".fan").withStyle(style -> style.withItalic(false)));
	}

	@Override
	public void addWidgets(D display, List<Widget> ingredients, Point origin) {
		ingredients.add(basicSlot(new Point(origin.x + 21, origin.y + 48))
				.markInput()
				.entries(display.getInputEntries().get(0)));

		ingredients.add(basicSlot(new Point(origin.x + 140, origin.y + 48))
				.markOutput()
				.entries(display.getOutputEntries().get(0)));
	}

	protected void renderWidgets(PoseStack matrixStack, T recipe, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SLOT.render(matrixStack, 20, 47);
		AllGuiTextures.JEI_SLOT.render(matrixStack, 139, 47);
		AllGuiTextures.JEI_SHADOW.render(matrixStack, 47, 29);
		AllGuiTextures.JEI_LIGHT.render(matrixStack, 66, 39);
		AllGuiTextures.JEI_LONG_ARROW.render(matrixStack, 53, 51);
	}

	@Override
	public void draw(@Nullable T recipe, @Nullable PoseStack matrixStack, double mouseX, double mouseY) {
		if (matrixStack == null)
			return;
		renderWidgets(matrixStack, recipe, mouseX, mouseY);

		matrixStack.pushPose();
		translateFan(matrixStack);
		matrixStack.mulPose(Vector3f.XP.rotationDegrees(-12.5f));
		matrixStack.mulPose(Vector3f.YP.rotationDegrees(22.5f));
		int scale = 24;

		AnimatedKinetics.defaultBlockElement(AllBlockPartials.ENCASED_FAN_INNER)
			.rotateBlock(180, 0, AnimatedKinetics.getCurrentAngle() * 16)
			.scale(scale)
			.render(matrixStack);

		AnimatedKinetics.defaultBlockElement(AllBlocks.ENCASED_FAN.getDefaultState())
			.rotateBlock(0, 180, 0)
			.atLocal(0, 0, 0)
			.scale(scale)
			.render(matrixStack);

		renderAttachedBlock(matrixStack);
		matrixStack.popPose();
	}

	protected void translateFan(PoseStack matrixStack) {
		matrixStack.translate(56, 33, 0);
	}

	public abstract void renderAttachedBlock(PoseStack matrixStack);

}
