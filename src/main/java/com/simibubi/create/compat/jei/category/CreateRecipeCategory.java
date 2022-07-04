package com.simibubi.create.compat.jei.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllFluids;
import com.simibubi.create.Create;
import com.simibubi.create.compat.jei.DoubleItemIcon;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Lang;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fluids.FluidStack;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class CreateRecipeCategory<T extends Recipe<?>> implements IRecipeCategory<T> {

	public final List<Supplier<List<T>>> recipes = new ArrayList<>();
	public final List<Supplier<? extends ItemStack>> recipeCatalysts = new ArrayList<>();

	protected String name;
	protected RecipeType<T> type;
	private final IDrawable background;
	private final IDrawable icon;

	private static final IDrawable basicSlot = asDrawable(AllGuiTextures.JEI_SLOT);
	private static final IDrawable chanceSlot = asDrawable(AllGuiTextures.JEI_CHANCE_SLOT);

	public CreateRecipeCategory(IDrawable icon, IDrawable background) {
		this.background = background;
		this.icon = icon;
	}

	public void setCategoryId(String name) {
		this.name = name;
		this.type = RecipeType.create(Create.ID, name, getRecipeClass());
	}

	@Override
	public ResourceLocation getUid() {
		return getRecipeType().getUid();
	}

	@Override
	public abstract Class<? extends T> getRecipeClass();

	@NotNull
	@Override
	public RecipeType<T> getRecipeType() {
		return type;
	}

	@Override
	public Component getTitle() {
		return Lang.translate("recipe." + name);
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	public void registerRecipes(IRecipeRegistration registration) {
		recipes.forEach(s -> registration.addRecipes(getRecipeType(), s.get()));
	}

	public static IDrawable getRenderedSlot() {
		return basicSlot;
	}

	public static IDrawable getRenderedSlot(ProcessingOutput output) {
		return getRenderedSlot(output.getChance());
	}

	public static IDrawable getRenderedSlot(float chance) {
		if (chance == 1)
			return basicSlot;

		return chanceSlot;
	}

	public static IDrawable emptyBackground(int width, int height) {
		return new EmptyBackground(width, height);
	}

	public static IDrawable doubleItemIcon(ItemLike item1, ItemLike item2) {
		return new DoubleItemIcon(() -> new ItemStack(item1), () -> new ItemStack(item2));
	}

	public static IDrawable itemIcon(ItemLike item) {
		return new DoubleItemIcon(() -> new ItemStack(item), () -> ItemStack.EMPTY);
	}

	public static IRecipeSlotTooltipCallback addStochasticTooltip(ProcessingOutput output) {
		return (view, tooltip) -> {
			float chance = output.getChance();
			if (chance != 1)
				tooltip.add(1, Lang.translate("recipe.processing.chance", chance < 0.01 ? "<1" : (int) (chance * 100))
					.withStyle(ChatFormatting.GOLD));
		};
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

	public static IRecipeSlotTooltipCallback addFluidTooltip() {
		return addFluidTooltip(-1);
	}

	public static IRecipeSlotTooltipCallback addFluidTooltip(int mbAmount) {
		return (view, tooltip) -> {
			Optional<FluidStack> displayed = view.getDisplayedIngredient(VanillaTypes.FLUID);
			if (displayed.isEmpty())
				return;

			FluidStack fluidStack = displayed.get();

			if (fluidStack.getFluid().isSame(AllFluids.POTION.get())) {
				Component name = fluidStack.getDisplayName();
				if (tooltip.isEmpty())
					tooltip.add(0, name);
				else
					tooltip.set(0, name);

				ArrayList<Component> potionTooltip = new ArrayList<>();
				PotionFluidHandler.addPotionTooltip(fluidStack, potionTooltip, 1);
				tooltip.addAll(1, potionTooltip.stream().toList());
			}

			int amount = mbAmount == -1 ? fluidStack.getAmount() : mbAmount;
			Component text = new TextComponent(String.valueOf(amount)).append(Lang.translate("generic.unit.millibuckets")).withStyle(ChatFormatting.GOLD);
			if (tooltip.isEmpty())
				tooltip.add(0, text);
			else {
				List<Component> siblings = tooltip.get(0).getSiblings();
				siblings.add(new TextComponent(" "));
				siblings.add(text);
			}
		};
	}

	private static IDrawable asDrawable(AllGuiTextures texture) {
		return new IDrawable() {
			@Override
			public int getWidth() {
				return texture.width;
			}

			@Override
			public int getHeight() {
				return texture.height;
			}

			@Override
			public void draw(PoseStack poseStack, int xOffset, int yOffset) {
				texture.render(poseStack, xOffset, yOffset);
			}
		};
	}

}
