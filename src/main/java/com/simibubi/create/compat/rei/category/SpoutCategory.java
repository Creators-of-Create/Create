package com.simibubi.create.compat.rei.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.compat.rei.category.animations.AnimatedSpout;
import com.simibubi.create.compat.rei.display.CreateDisplay;
import com.simibubi.create.content.contraptions.fluids.actors.FillingRecipe;
import com.simibubi.create.content.contraptions.fluids.actors.GenericItemFilling;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import com.simibubi.create.lib.transfer.TransferUtil;
import com.simibubi.create.lib.transfer.fluid.FluidStack;

import com.simibubi.create.lib.transfer.fluid.IFluidHandlerItem;

import com.simibubi.create.lib.util.LazyOptional;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.Ingredient;

public class SpoutCategory extends CreateRecipeCategory<FillingRecipe> {

	public SpoutCategory() {
		super(doubleItemIcon(AllBlocks.SPOUT, () -> Items.WATER_BUCKET), emptyBackground(177, 80));
		setCategoryId("spout_filling");
	}

	@SuppressWarnings("UnstableApiUsage")
	public static List<FillingRecipe> getRecipes() {
		List<FillingRecipe> recipes = new ArrayList<>();

		EntryRegistry.getInstance().getEntryStacks()
				.filter(stack -> Objects.equals(stack.getType(), VanillaEntryTypes.ITEM))
				.<EntryStack<ItemStack>>map(EntryStack::cast)
				.toList()
				.forEach(entryStack -> {
					ItemStack stack = entryStack.getValue();
					if (stack.getItem() instanceof PotionItem) {
						FluidStack fluidFromPotionItem = PotionFluidHandler.getFluidFromPotionItem(stack);
						Ingredient bottle = Ingredient.of(Items.GLASS_BOTTLE);
						recipes.add(new ProcessingRecipeBuilder<>(FillingRecipe::new, Create.asResource("potions"))
								.withItemIngredients(bottle)
								.withFluidIngredients(FluidIngredient.fromFluidStack(fluidFromPotionItem))
								.withSingleItemOutput(stack)
								.build());
						return;
					}

					LazyOptional<IFluidHandlerItem> capability =
							TransferUtil.getFluidHandlerItem(stack);
					if (!capability.isPresent())
						return;

					EntryRegistry.getInstance().getEntryStacks()
							.filter(stack1 -> Objects.equals(stack1.getType(), VanillaEntryTypes.FLUID))
							.<EntryStack<dev.architectury.fluid.FluidStack>>map(EntryStack::cast)
							.toList()
							.forEach(entryStack1 -> {
								dev.architectury.fluid.FluidStack archStack = entryStack1.getValue();
								FluidStack fluidStack = new FluidStack(archStack.getFluid(), archStack.getAmount(), archStack.getTag());
								ItemStack copy = stack.copy();
								TransferUtil.getFluidHandlerItem(copy)
										.ifPresent(fhi -> {
											if (!GenericItemFilling.isFluidHandlerValid(copy, fhi))
												return;
											FluidStack fluidCopy = fluidStack.copy();
											fluidCopy.setAmount(FluidConstants.BUCKET);
											fhi.fill(fluidCopy, false);
											ItemStack container = fhi.getContainer();
											if (container.sameItem(copy))
												return;
											if (container.isEmpty())
												return;

											Ingredient bucket = Ingredient.of(stack);
											ResourceLocation itemName = Registry.ITEM.getKey(stack.getItem());
											ResourceLocation fluidName = Registry.FLUID.getKey(fluidCopy.getFluid());
											recipes.add(new ProcessingRecipeBuilder<>(FillingRecipe::new,
													Create.asResource("fill_" + itemName.getNamespace() + "_" + itemName.getPath()
															+ "_with_" + fluidName.getNamespace() + "_" + fluidName.getPath()))
													.withItemIngredients(bucket)
													.withFluidIngredients(FluidIngredient.fromFluidStack(fluidCopy))
													.withSingleItemOutput(container)
													.build());
										});
							});
				});

		return recipes;
	}

//	@Override
//	public Class<? extends FillingRecipe> getRecipeClass() {
//		return FillingRecipe.class;
//	}

//	@Override
//	public void setIngredients(FillingRecipe recipe, IIngredients ingredients) {
//		ingredients.setInputIngredients(recipe.getIngredients());
//		ingredients.setInputLists(VanillaTypes.FLUID, recipe.getFluidIngredients()
//			.stream()
//			.map(FluidIngredient::getMatchingFluidStacks)
//			.collect(Collectors.toList()));
//
//		if (!recipe.getRollableResults()
//			.isEmpty())
//			ingredients.setOutput(VanillaTypes.ITEM, recipe.getResultItem());
//		if (!recipe.getFluidResults()
//			.isEmpty())
//			ingredients.setOutputs(VanillaTypes.FLUID, recipe.getFluidResults());
//	}

//	@Override
//	public void setRecipe(IRecipeLayout recipeLayout, FillingRecipe recipe, IIngredients ingredients) {
//		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
//		IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
//		FluidIngredient fluidIngredient = recipe.getRequiredFluid();
//		List<ItemStack> matchingIngredients = Arrays.asList(recipe.getIngredients()
//			.get(0)
//			.getItems());
//
//		fluidStacks.init(0, true, 27, 32);
//		fluidStacks.set(0, withImprovedVisibility(fluidIngredient.getMatchingFluidStacks()));
//		itemStacks.init(0, true, 26, 50);
//		itemStacks.set(0, matchingIngredients);
//		itemStacks.init(1, false, 131, 50);
//		itemStacks.set(1, recipe.getResultItem());
//
//		addFluidTooltip(fluidStacks, ImmutableList.of(fluidIngredient), Collections.emptyList());
//	}

	@Override
	public List<Widget> setupDisplay(CreateDisplay<FillingRecipe> display, Rectangle bounds) {
		Point origin = new Point(bounds.getX(), bounds.getY() + 4);
		List<Widget> widgets = new ArrayList<>();
		widgets.add(Widgets.createRecipeBase(bounds));
		// Create slots

		FluidStack fluidStack = display.getRecipe().getRequiredFluid().getMatchingFluidStacks().get(0);
		widgets.add(WidgetUtil.textured(AllGuiTextures.JEI_SLOT, origin.getX() + 26, origin.getY() + 31));
		widgets.add(Widgets.createSlot(point(origin.getX() + 27, origin.getY() + 32)).disableBackground().markInput().entries(EntryIngredient.of(createFluidEntryStack(fluidStack))));

		widgets.add(WidgetUtil.textured(AllGuiTextures.JEI_SLOT, origin.getX() + 26, origin.getY() + 50));
		widgets.add(Widgets.createSlot(point(origin.getX() + 27, origin.getY() + 51)).disableBackground().markInput().entries(display.getInputEntries().get(0)));


		widgets.add(WidgetUtil.textured(getRenderedSlot(display.getRecipe(), 0), origin.getX() + 131, origin.getY() + 50));
		// Draw arrow with shadow
		widgets.add(Widgets.createSlot(new Point(origin.getX() + 132, origin.getY() + 51)).disableBackground().markOutput().entries(display.getOutputEntries().get(0)));
		widgets.add(WidgetUtil.textured(AllGuiTextures.JEI_SHADOW, origin.getX() + 62, origin.getY() + 57));
		widgets.add(WidgetUtil.textured(AllGuiTextures.JEI_DOWN_ARROW, origin.getX() + 126, origin.getY() + 29));

		AnimatedSpout spout = new AnimatedSpout();

		spout.setPos(new Point(origin.getX() + (getDisplayWidth(display) / 2 - 13), origin.getY() + 22));
		spout.withFluids(display.getRecipe().getRequiredFluid()
						.getMatchingFluidStacks());
		widgets.add(spout);
		return widgets;
	}

//	@Override
//	public void draw(FillingRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
//		AllGuiTextures.JEI_SLOT.render(matrixStack, 26, 31);
//		AllGuiTextures.JEI_SLOT.render(matrixStack, 26, 50);
//		getRenderedSlot(recipe, 0).render(matrixStack, 131, 50);
//		AllGuiTextures.JEI_SHADOW.render(matrixStack, 62, 57);
//		AllGuiTextures.JEI_DOWN_ARROW.render(matrixStack, 126, 29);
//		spout.withFluids(recipe.getRequiredFluid()
//			.getMatchingFluidStacks())
//			.draw(matrixStack, getDisplayWidth(null) / 2 - 13, 22);
//	}

}
