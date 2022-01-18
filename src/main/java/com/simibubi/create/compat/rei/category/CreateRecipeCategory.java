package com.simibubi.create.compat.rei.category;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.compat.rei.DoubleItemIcon;
import com.simibubi.create.compat.rei.EmptyBackground;
import com.simibubi.create.compat.rei.FluidStackEntryRenderer;
import com.simibubi.create.compat.rei.display.CreateDisplay;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.lib.transfer.fluid.FluidStack;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;

import me.shedaniel.rei.api.common.util.EntryStacks;

import net.minecraft.ChatFormatting;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

public abstract class CreateRecipeCategory<R extends Recipe<?>, D extends CreateDisplay<R>> implements DisplayCategory<D> {

	public final List<Supplier<List<? extends Recipe<?>>>> recipes = new ArrayList<>();
	public final List<Supplier<ItemStack>> recipeCatalysts = new ArrayList<>();

	protected CategoryIdentifier uid;
	protected String name;
	private Renderer icon;
	private int width, height;

	public CreateRecipeCategory(Renderer icon, EmptyBackground background) {
		this.icon = icon;
		this.width = background.getWidth();
		this.height = background.getHeight();
	}

	public void setCategoryId(String name) {
		this.uid = CategoryIdentifier.of(Create.asResource(name));
		this.name = name;
	}

	@Override
	public CategoryIdentifier getCategoryIdentifier() {
		return uid;
	}

	@Override
	public Component getTitle() {
		return Lang.translate("recipe." + name);
	}

	@Override
	public int getDisplayHeight() {
		return height;
	}

	@Override
	public int getDisplayWidth(D display) {
		return width;
	}

	@Override
	public Renderer getIcon() {
		return icon;
	}

	public static AllGuiTextures getRenderedSlot(Recipe<?> recipe, int index) {
		AllGuiTextures jeiSlot = AllGuiTextures.JEI_SLOT;
		if (!(recipe instanceof ProcessingRecipe))
			return jeiSlot;
		ProcessingRecipe<?> processingRecipe = (ProcessingRecipe<?>) recipe;
		List<ProcessingOutput> rollableResults = processingRecipe.getRollableResults();
		if (rollableResults.size() <= index)
			return jeiSlot;
		if (processingRecipe.getRollableResults()
			.get(index)
			.getChance() == 1)
			return jeiSlot;
		return AllGuiTextures.JEI_CHANCE_SLOT;
	}

	public static EmptyBackground emptyBackground(int width, int height) {
		return new EmptyBackground(width, height);
	}

	public static Renderer doubleItemIcon(Supplier<? extends ItemLike> item1, Supplier<? extends ItemLike> item2) {
		return new DoubleItemIcon(() -> new ItemStack(item1.get()), () -> new ItemStack(item2.get()));
	}

	public static Renderer itemIcon(ItemLike item) {
		return new DoubleItemIcon(() -> new ItemStack(item), () -> ItemStack.EMPTY);
	}

	public static void addStochasticTooltip(List<Widget> itemStacks, List<ProcessingOutput> results) {
		addStochasticTooltip(itemStacks, results, 1);
	}

	public static void addStochasticTooltip(List<Widget> itemStacks, List<ProcessingOutput> results,
											int startIndex) {
		itemStacks.stream().filter(widget -> widget instanceof Slot).forEach(widget -> {
			Slot slot = (Slot) widget;

			ClientEntryStacks.setTooltipProcessor(slot.getCurrentEntry(), (entryStack, tooltip) -> {
//				if (slotIndex < startIndex)
//					return;
				ProcessingOutput output = results.get(/*slotIndex - startIndex*/0);
				float chance = output.getChance();
				if (chance != 1)
					tooltip.add(Lang.translate("recipe.processing.chance", chance < 0.01 ? "<1" : (int) (chance * 100))
							.withStyle(ChatFormatting.GOLD));
				return tooltip;
			});
		});
	}

	@Deprecated // in favor of basicSlot(int, int)
	public static Slot basicSlot(Point point) {
		return Widgets.createSlot(point).disableBackground();
	}

	public static Slot basicSlot(int x, int y) {
		return Widgets.createSlot(point(x, y)).disableBackground();
	}

	@SuppressWarnings("all")
	public static EntryStack<dev.architectury.fluid.FluidStack> createFluidEntryStack(FluidStack fluidStack) {
		return ClientEntryStacks.setRenderer(EntryStacks.of(dev.architectury.fluid.FluidStack
				.create(fluidStack.getFluid(), fluidStack.getAmount(), fluidStack.getOrCreateTag())), new FluidStackEntryRenderer());
	}

	public static List<FluidStack> withImprovedVisibility(List<FluidStack> stacks) {
		return stacks.stream()
			.map(CreateRecipeCategory::withImprovedVisibility)
			.collect(Collectors.toList());
	}

	public static FluidStack withImprovedVisibility(FluidStack stack) {
		FluidStack display = stack.copy();
		int displayedAmount = (int) (stack.getAmount() * .75f) + 250;
		display.setAmount(displayedAmount);
		return display;
	}

	public static dev.architectury.fluid.FluidStack convertToREIFluid(FluidStack stack) {
		return dev.architectury.fluid.FluidStack.create(stack.getFluid(), stack.getAmount(), stack.getTag());
	}

//	public static void addFluidTooltip(IGuiFluidStackGroup fluidStacks, List<FluidIngredient> inputs,
//		List<FluidStack> outputs) {
//		addFluidTooltip(fluidStacks, inputs, outputs, -1);
//	}
//
//	public static void addFluidTooltip(IGuiFluidStackGroup fluidStacks, List<FluidIngredient> inputs,
//		List<FluidStack> outputs, int index) {
//		List<Integer> amounts = new ArrayList<>();
//		inputs.forEach(f -> amounts.add(f.getRequiredAmount()));
//		outputs.forEach(f -> amounts.add(f.getAmount()));
//
//		fluidStacks.addTooltipCallback((slotIndex, input, fluid, tooltip) -> {
//			if (index != -1 && slotIndex != index)
//				return;
//
//			if (fluid.getFluid()
//				.isSame(AllFluids.POTION.get())) {
//				Component name = fluid.getDisplayName();
//				if (tooltip.isEmpty())
//					tooltip.add(0, name);
//				else
//					tooltip.set(0, name);
//
//				ArrayList<Component> potionTooltip = new ArrayList<>();
//				PotionFluidHandler.addPotionTooltip(fluid, potionTooltip, 1);
//				tooltip.addAll(1, potionTooltip.stream()
//					.collect(Collectors.toList()));
//			}
//
//			int amount = amounts.get(index != -1 ? 0 : slotIndex);
//			Component text = (Lang.translate("generic.unit.millibuckets", amount)).withStyle(ChatFormatting.GOLD);
//			if (tooltip.isEmpty())
//				tooltip.add(0, text);
//			else {
//				List<Component> siblings = tooltip.get(0)
//					.getSiblings();
//				siblings.add(new TextComponent(" "));
//				siblings.add(text);
//			}
//		});
//	}

	public static Point point(int x, int y) {
		return new Point(x, y);
	}

	public void addWidgets(D display, List<Widget> ingredients, Point origin) {

	}

	@Override
	public List<Widget> setupDisplay(D display, Rectangle bounds) {
		List<Widget> widgets = new ArrayList<>();
		widgets.add(Widgets.createRecipeBase(bounds));
		widgets.add(Widgets.createDrawableWidget((helper, poseStack, mouseX, mouseY, partialTick) -> {
			poseStack.pushPose();
			poseStack.translate(bounds.getX(), bounds.getY() + 4, 0);
			draw(display.getRecipe(), poseStack, mouseX, mouseY);
			poseStack.popPose();
		}));
		addWidgets(display, widgets, new Point(bounds.getX(), bounds.getY() + 4));
		return widgets;
	}

	public void draw(R recipe, PoseStack matrixStack, double mouseX, double mouseY) {};

}
