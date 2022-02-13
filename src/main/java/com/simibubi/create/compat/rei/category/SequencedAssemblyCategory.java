package com.simibubi.create.compat.rei.category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.rei.EmptyBackground;
import com.simibubi.create.compat.rei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.compat.rei.display.CreateDisplay;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedAssemblyRecipe;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;

import com.simibubi.create.foundation.utility.Lang;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

import org.jetbrains.annotations.Nullable;

public class SequencedAssemblyCategory extends CreateRecipeCategory<SequencedAssemblyRecipe> {

	Map<ResourceLocation, SequencedAssemblySubCategory> subCategories = new HashMap<>();

	public SequencedAssemblyCategory() {
		super(itemIcon(AllItems.PRECISION_MECHANISM), new EmptyBackground(180, 120));
	}

	@Override
	public void addWidgets(CreateDisplay<SequencedAssemblyRecipe> display, List<Widget> ingredients, Point origin, Rectangle bounds) {
		int xOffset = display.getRecipe().getOutputChance() == 1 ? 0 : -7;

		ingredients.add(basicSlot(origin.x + 27 + xOffset, origin.y + 91)
				.markInput()
				.entries(EntryIngredients.ofItemStacks(Arrays.asList(display.getRecipe().getIngredient()
						.getItems()))));

		Slot output = basicSlot(origin.x + 132 + xOffset, origin.y + 91)
				.markOutput()
				.entries(EntryIngredients.of(display.getRecipe().getResultItem()));
		ClientEntryStacks.setTooltipProcessor(output.getCurrentEntry(), (entryStack, tooltip) -> {
			float chance = display.getRecipe().getOutputChance();
			if (chance != 1)
				tooltip.add(Lang.translate("recipe.processing.chance", chance < 0.01 ? "<1" : (int) (chance * 100))
						.withStyle(ChatFormatting.GOLD));
			return tooltip;
		});
		ingredients.add(output);

		int width = 0;
		int margin = 3;
		for (SequencedRecipe<?> sequencedRecipe : display.getRecipe().getSequence())
			width += getSubCategory(sequencedRecipe).getWidth() + margin;
		width -= margin;
		int x = width / -2 + getDisplayWidth(null) / 2;
		int index = 2;
		int fluidIndex = 0;
		for (SequencedRecipe<?> sequencedRecipe : display.getRecipe().getSequence()) {
			SequencedAssemblySubCategory subCategory = getSubCategory(sequencedRecipe);
			index += subCategory.addItemIngredients(sequencedRecipe, ingredients, x, index, origin);
			fluidIndex += subCategory.addFluidIngredients(sequencedRecipe, ingredients, x, fluidIndex, origin);
			x += subCategory.getWidth() + margin;
		}

		// In case machines should be displayed as ingredients

//		List<Widget> inputs = ingredients.stream().filter(widget -> {
//			if(widget instanceof Slot slot)
//				return slot.getCurrentEntry().getType() == VanillaEntryTypes.ITEM;
//			return false;
//		}).toList();
//		int catalystX = -2;
//		int catalystY = 14;
//		for (; index < inputs.size(); index++) {
//			Slot slot = (Slot) inputs.get(index);
//			ingredients.add(basicSlot(point(origin.x + catalystX,  origin.y + catalystY))
//					.markInput()
//					.entries(slot.getEntries()));
//			catalystY += 19;
//		}

		ingredients.add(new WidgetWithBounds() {
			@Override
			public Rectangle getBounds() {
				return bounds;
			}

			@Override
			public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
				Point mouse = new Point(mouseX, mouseY);
				if (containsMouse(mouse)) {
					for (Slot slot : Widgets.<Slot>walk(ingredients, listener -> listener instanceof Slot)) {
						if (slot.containsMouse(mouse) && slot.isHighlightEnabled()) {
							if (slot.getCurrentTooltip(mouse) != null) {
								return;
							}
						}
					}

					Tooltip tooltip = getTooltip(mouse);

					if (tooltip != null) {
						tooltip.queue();
					}
				}
			}

			@Override
			public List<? extends GuiEventListener> children() {
				return Collections.emptyList();
			}

			@Override
			@Nullable
			public Tooltip getTooltip(Point mouse) {
				List<Component> strings = getTooltipStrings(display.getRecipe(), mouse.x - origin.x, mouse.y - origin.y);
				if (strings.isEmpty()) {
					return null;
				}
				return Tooltip.create(mouse, strings);
			}
		});
	}

	private SequencedAssemblySubCategory getSubCategory(SequencedRecipe<?> sequencedRecipe) {
		return subCategories.computeIfAbsent(Registry.RECIPE_SERIALIZER.getKey(sequencedRecipe.getRecipe()
			.getSerializer()),
			rl -> sequencedRecipe.getAsAssemblyRecipe()
				.getJEISubCategory()
				.get()
				.get());

	}

	final String[] romans = { "I", "II", "III", "IV", "V", "VI", "-" };

	@Override
	public void draw(SequencedAssemblyRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		Font font = Minecraft.getInstance().font;

		matrixStack.pushPose();

		matrixStack.pushPose();
		matrixStack.translate(0, 15, 0);
		boolean singleOutput = recipe.getOutputChance() == 1;
		int xOffset = singleOutput ? 0 : -7;
		AllGuiTextures.JEI_SLOT.render(matrixStack, 26 + xOffset, 75);
		(singleOutput ? AllGuiTextures.JEI_SLOT : AllGuiTextures.JEI_CHANCE_SLOT).render(matrixStack, 131 + xOffset, 75);
		AllGuiTextures.JEI_LONG_ARROW.render(matrixStack, 52 + xOffset, 79);
		if (!singleOutput) {
			AllGuiTextures.JEI_CHANCE_SLOT.render(matrixStack, 150 + xOffset, 75);
			Component component = new TextComponent("?").withStyle(ChatFormatting.BOLD);
			font.drawShadow(matrixStack, component, font.width(component) / -2 + 8 + 150 + xOffset, 2 + 78,
				0xefefef);
		}

		if (recipe.getLoops() > 1) {
			matrixStack.pushPose();
			matrixStack.translate(15, 9, 0);
			AllIcons.I_SEQ_REPEAT.render(matrixStack, 50 + xOffset, 75);
			Component repeat = new TextComponent("x" + recipe.getLoops());
			font.draw(matrixStack, repeat, 66 + xOffset, 80, 0x888888);
			matrixStack.popPose();
		}

		matrixStack.popPose();

		int width = 0;
		int margin = 3;
		for (SequencedRecipe<?> sequencedRecipe : recipe.getSequence())
			width += getSubCategory(sequencedRecipe).getWidth() + margin;
		width -= margin;
		matrixStack.translate(width / -2 + getDisplayWidth(null) / 2, 0, 0);

		matrixStack.pushPose();
		List<SequencedRecipe<?>> sequence = recipe.getSequence();
		for (int i = 0; i < sequence.size(); i++) {
			SequencedRecipe<?> sequencedRecipe = sequence.get(i);
			SequencedAssemblySubCategory subCategory = getSubCategory(sequencedRecipe);
			int subWidth = subCategory.getWidth();
			TextComponent component = new TextComponent("" + romans[Math.min(i, 6)]);
			font.draw(matrixStack, component, font.width(component) / -2 + subWidth / 2, 2, 0x888888);
			subCategory.draw(sequencedRecipe, matrixStack, mouseX, mouseY, i);
			matrixStack.translate(subWidth + margin, 0, 0);
		}
		matrixStack.popPose();

		matrixStack.popPose();
	}

	public List<Component> getTooltipStrings(SequencedAssemblyRecipe recipe, double mouseX, double mouseY) {
		List<Component> tooltip = new ArrayList<Component>();

		TranslatableComponent junk = Lang.translate("recipe.assembly.junk");

		boolean singleOutput = recipe.getOutputChance() == 1;
		boolean willRepeat = recipe.getLoops() > 1;

		int xOffset = -7;
		int minX = 150 + xOffset;
		int maxX = minX + 18;
		int minY = 90;
		int maxY = minY + 18;
		if (!singleOutput && mouseX >= minX && mouseX < maxX && mouseY >= minY && mouseY < maxY) {
			float chance = recipe.getOutputChance();
			tooltip.add(junk);
			tooltip.add(Lang.translate("recipe.processing.chance", chance < 0.01 ? "<1" : 100 - (int) (chance * 100))
				.withStyle(ChatFormatting.GOLD));
			return tooltip;
		}

		minX = 55 + xOffset;
		maxX = minX + 65;
		minY = 92;
		maxY = minY + 24;
		if (willRepeat && mouseX >= minX && mouseX < maxX && mouseY >= minY && mouseY < maxY) {
			tooltip.add(Lang.translate("recipe.assembly.repeat", recipe.getLoops()));
			return tooltip;
		}

		if (mouseY > 5 && mouseY < 84) {
			int width = 0;
			int margin = 3;
			for (SequencedRecipe<?> sequencedRecipe : recipe.getSequence())
				width += getSubCategory(sequencedRecipe).getWidth() + margin;
			width -= margin;
			xOffset = width / 2 + getDisplayWidth(null) / -2;

			double relativeX = mouseX + xOffset;
			List<SequencedRecipe<?>> sequence = recipe.getSequence();
			for (int i = 0; i < sequence.size(); i++) {
				SequencedRecipe<?> sequencedRecipe = sequence.get(i);
				SequencedAssemblySubCategory subCategory = getSubCategory(sequencedRecipe);
				if (relativeX >= 0 && relativeX < subCategory.getWidth()) {
					tooltip.add(Lang.translate("recipe.assembly.step", i + 1));
					tooltip.add(sequencedRecipe.getAsAssemblyRecipe()
						.getDescriptionForAssembly()
						.plainCopy()
						.withStyle(ChatFormatting.DARK_GREEN));
					return tooltip;
				}
				relativeX -= subCategory.getWidth() + margin;
			}
		}

		return tooltip;
	}

	private List<FluidIngredient> getAllFluidIngredients(SequencedAssemblyRecipe recipe) {
		List<FluidIngredient> assemblyFluidIngredients = new ArrayList<>();
		recipe.addAdditionalFluidIngredients(assemblyFluidIngredients);
		return assemblyFluidIngredients;
	}

	private List<Ingredient> getAllItemIngredients(SequencedAssemblyRecipe recipe) {
		List<Ingredient> assemblyIngredients = new ArrayList<>();
		assemblyIngredients.add(recipe.getIngredient());
		assemblyIngredients.add(Ingredient.of(recipe.getTransitionalItem()));
		recipe.addAdditionalIngredientsAndMachines(assemblyIngredients);
		return assemblyIngredients;
	}

}
