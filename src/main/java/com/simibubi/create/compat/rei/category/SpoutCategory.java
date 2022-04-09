package com.simibubi.create.compat.rei.category;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

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

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
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
	public static void consumeRecipes(Consumer<FillingRecipe> consumer) {
		List<EntryStack<dev.architectury.fluid.FluidStack>> fluidStacks = EntryRegistry.getInstance().getEntryStacks()
				.filter(stack -> Objects.equals(stack.getType(), VanillaEntryTypes.FLUID))
				.<EntryStack<dev.architectury.fluid.FluidStack>>map(EntryStack::cast)
				.toList();
		EntryRegistry.getInstance().getEntryStacks()
				.filter(stack -> Objects.equals(stack.getType(), VanillaEntryTypes.ITEM))
				.<EntryStack<ItemStack>>map(EntryStack::cast)
				.toList().forEach(entryStack -> {
			ItemStack stack = entryStack.getValue();
			if (stack.getItem() instanceof PotionItem) {
				FluidStack fluidFromPotionItem = PotionFluidHandler.getFluidFromPotionItem(stack);
				Ingredient bottle = Ingredient.of(Items.GLASS_BOTTLE);
				consumer.accept(new ProcessingRecipeBuilder<>(FillingRecipe::new, Create.asResource("potions"))
					.withItemIngredients(bottle)
					.withFluidIngredients(FluidIngredient.fromFluidStack(fluidFromPotionItem))
					.withSingleItemOutput(stack)
					.build());
				return;
			}

			for (EntryStack<dev.architectury.fluid.FluidStack> fluidEntry: fluidStacks) {
				FluidStack fluidStack = new FluidStack(fluidEntry.getValue().getFluid(), fluidEntry.getValue().getAmount(), fluidEntry.getValue().getTag());
				ItemStack copy = stack.copy();
				ContainerItemContext ctx = ContainerItemContext.withInitial(copy);
				Storage<FluidVariant> fhi = ctx.find(FluidStorage.ITEM);
				if(fhi != null) {
					if (!GenericItemFilling.isFluidHandlerValid(copy, fhi))
						return;
					FluidStack fluidCopy = fluidStack.copy();
					fluidCopy.setAmount(FluidConstants.BUCKET);
					try(Transaction t = TransferUtil.getTransaction()) {
						fhi.insert(fluidCopy.getType(), fluidCopy.getAmount(), t);
						t.commit();
					}
						ItemStack container = ctx.getItemVariant().toStack((int) ctx.getAmount());
						if (container.sameItem(copy))
							return;
						if (container.isEmpty())
							return;

						Ingredient bucket = Ingredient.of(stack);
						ResourceLocation itemName = Registry.ITEM
								.getKey(stack.getItem());
						ResourceLocation fluidName = Registry.FLUID
								.getKey(fluidCopy.getFluid());
						consumer.accept(new ProcessingRecipeBuilder<>(FillingRecipe::new,
								Create.asResource("fill_" + itemName.getNamespace() + "_" + itemName.getPath()
										+ "_with_" + fluidName.getNamespace() + "_" + fluidName.getPath()))
								.withItemIngredients(bucket)
								.withFluidIngredients(FluidIngredient.fromFluidStack(fluidCopy))
								.withSingleItemOutput(container)
								.build());
					}
			}
		});
	}

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
}
