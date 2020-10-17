package com.simibubi.create.content.logistics.item.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.simibubi.create.content.logistics.item.filter.attribute.EnchantAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.FluidContentsAttribute;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Predicates;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.logistics.InWorldProcessing;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public interface ItemAttribute {

	static List<ItemAttribute> types = new ArrayList<>();

	static ItemAttribute standard = register(StandardTraits.DUMMY);
	static ItemAttribute inTag = register(new InTag(new ResourceLocation("dummy")));
	static ItemAttribute inItemGroup = register(new InItemGroup(ItemGroup.MISC));
	static ItemAttribute hasEnchant = register(new EnchantAttribute("dummy"));
	static ItemAttribute hasFluid = register(new FluidContentsAttribute("dummy"));
	static ItemAttribute addedBy = register(new AddedBy("dummy"));

	static ItemAttribute register(ItemAttribute attributeType) {
		types.add(attributeType);
		return attributeType;
	}

	default boolean appliesTo(ItemStack stack, World world) {
		return appliesTo(stack);
	}

	boolean appliesTo(ItemStack stack);

	default List<ItemAttribute> listAttributesOf(ItemStack stack, World world) {
		return listAttributesOf(stack);
	}

	public List<ItemAttribute> listAttributesOf(ItemStack stack);

	public String getTranslationKey();

	void writeNBT(CompoundNBT nbt);

	ItemAttribute readNBT(CompoundNBT nbt);

	public default void serializeNBT(CompoundNBT nbt) {
		CompoundNBT compound = new CompoundNBT();
		writeNBT(compound);
		nbt.put(getNBTKey(), compound);
	}

	public static ItemAttribute fromNBT(CompoundNBT nbt) {
		for (ItemAttribute itemAttribute : types) {
			if (!itemAttribute.canRead(nbt))
				continue;
			return itemAttribute.readNBT(nbt.getCompound(itemAttribute.getNBTKey()));
		}
		return null;
	}

	default Object[] getTranslationParameters() {
		return new String[0];
	}

	default boolean canRead(CompoundNBT nbt) {
		return nbt.contains(getNBTKey());
	}

	default String getNBTKey() {
		return getTranslationKey();
	}

	@OnlyIn(value = Dist.CLIENT)
	default String format(boolean inverted) {
		return Lang.translate("item_attributes." + getTranslationKey() + (inverted ? ".inverted" : ""),
			getTranslationParameters());
	}

	public static enum StandardTraits implements ItemAttribute {

		DUMMY(s -> false),
		PLACEABLE(s -> s.getItem() instanceof BlockItem),
		CONSUMABLE(ItemStack::isFood),
		FLUID_CONTAINER(s -> s.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent()),
		ENCHANTED(ItemStack::isEnchanted),
		DAMAGED(ItemStack::isDamaged),
		BADLY_DAMAGED(s -> s.isDamaged() && s.getDamage() / s.getMaxDamage() > 3 / 4f),
		NOT_STACKABLE(Predicates.not(ItemStack::isStackable)),
		EQUIPABLE(s -> s.getEquipmentSlot() != null),
		FURNACE_FUEL(AbstractFurnaceTileEntity::isFuel),
		WASHABLE(InWorldProcessing::isWashable),
		CRUSHABLE((s, w) -> testRecipe(s, w, AllRecipeTypes.CRUSHING.getType())
			|| testRecipe(s, w, AllRecipeTypes.MILLING.getType())),
		SMELTABLE((s, w) -> testRecipe(s, w, IRecipeType.SMELTING)),
		SMOKABLE((s, w) -> testRecipe(s, w, IRecipeType.SMOKING)),
		BLASTABLE((s, w) -> testRecipe(s, w, IRecipeType.BLASTING));

		private static final RecipeWrapper RECIPE_WRAPPER = new RecipeWrapper(new ItemStackHandler(1));
		private Predicate<ItemStack> test;
		private BiPredicate<ItemStack, World> testWithWorld;

		private StandardTraits(Predicate<ItemStack> test) {
			this.test = test;
		}

		private static boolean testRecipe(ItemStack s, World w, IRecipeType<? extends IRecipe<IInventory>> type) {
			RECIPE_WRAPPER.setInventorySlotContents(0, s.copy());
			return w.getRecipeManager()
				.getRecipe(type, RECIPE_WRAPPER, w)
				.isPresent();
		}

		private StandardTraits(BiPredicate<ItemStack, World> test) {
			this.testWithWorld = test;
		}

		@Override
		public boolean appliesTo(ItemStack stack, World world) {
			if (testWithWorld != null)
				return testWithWorld.test(stack, world);
			return appliesTo(stack);
		}

		@Override
		public boolean appliesTo(ItemStack stack) {
			return test.test(stack);
		}

		@Override
		public List<ItemAttribute> listAttributesOf(ItemStack stack, World world) {
			List<ItemAttribute> attributes = new ArrayList<>();
			for (StandardTraits trait : values())
				if (trait.appliesTo(stack, world))
					attributes.add(trait);
			return attributes;
		}

		@Override
		public List<ItemAttribute> listAttributesOf(ItemStack stack) {
			return null;
		}

		@Override
		public String getTranslationKey() {
			return Lang.asId(name());
		}

		@Override
		public String getNBTKey() {
			return "standard_trait";
		}

		@Override
		public void writeNBT(CompoundNBT nbt) {
			nbt.putBoolean(name(), true);
		}

		@Override
		public ItemAttribute readNBT(CompoundNBT nbt) {
			for (StandardTraits trait : values())
				if (nbt.contains(trait.name()))
					return trait;
			return null;
		}

	}

	public static class InTag implements ItemAttribute {

		ResourceLocation tagName;

		public InTag(ResourceLocation tagName) {
			this.tagName = tagName;
		}

		@Override
		public boolean appliesTo(ItemStack stack) {
			return stack.getItem()
				.getTags()
				.contains(tagName);
		}

		@Override
		public List<ItemAttribute> listAttributesOf(ItemStack stack) {
			return stack.getItem()
				.getTags()
				.stream()
				.map(InTag::new)
				.collect(Collectors.toList());
		}

		@Override
		public String getTranslationKey() {
			return "in_tag";
		}

		@Override
		public Object[] getTranslationParameters() {
			return new Object[] { "#" + tagName.toString() };
		}

		@Override
		public void writeNBT(CompoundNBT nbt) {
			nbt.putString("space", tagName.getNamespace());
			nbt.putString("path", tagName.getPath());
		}

		@Override
		public ItemAttribute readNBT(CompoundNBT nbt) {
			return new InTag(new ResourceLocation(nbt.getString("space"), nbt.getString("path")));
		}

	}

	public static class InItemGroup implements ItemAttribute {

		private ItemGroup group;

		public InItemGroup(ItemGroup group) {
			this.group = group;
		}

		@Override
		public boolean appliesTo(ItemStack stack) {
			Item item = stack.getItem();
			return item.getGroup() == group;
		}

		@Override
		public List<ItemAttribute> listAttributesOf(ItemStack stack) {
			ItemGroup group = stack.getItem()
				.getGroup();
			return group == null ? Collections.emptyList() : Arrays.asList(new InItemGroup(group));
		}

		@Override
		public String getTranslationKey() {
			return "in_item_group";
		}

		@Override
		@OnlyIn(value = Dist.CLIENT)
		public String format(boolean inverted) {
			return Lang.translate("item_attributes." + getTranslationKey() + (inverted ? ".inverted" : ""),
				I18n.format(group.getTranslationKey()));
		}

		@Override
		public void writeNBT(CompoundNBT nbt) {
			nbt.putString("path", group.getPath());
		}

		@Override
		public ItemAttribute readNBT(CompoundNBT nbt) {
			String readPath = nbt.getString("path");
			for (ItemGroup group : ItemGroup.GROUPS)
				if (group.getPath()
					.equals(readPath))
					return new InItemGroup(group);
			return null;
		}

	}

	public static class AddedBy implements ItemAttribute {

		private String modId;

		public AddedBy(String modId) {
			this.modId = modId;
		}

		@Override
		public boolean appliesTo(ItemStack stack) {
			return modId.equals(stack.getItem()
				.getCreatorModId(stack));
		}

		@Override
		public List<ItemAttribute> listAttributesOf(ItemStack stack) {
			String id = stack.getItem()
				.getCreatorModId(stack);
			return id == null ? Collections.emptyList() : Arrays.asList(new AddedBy(id));
		}

		@Override
		public String getTranslationKey() {
			return "added_by";
		}

		@Override
		public Object[] getTranslationParameters() {
			Optional<? extends ModContainer> modContainerById = ModList.get()
				.getModContainerById(modId);
			String name = modContainerById.map(ModContainer::getModInfo)
				.map(IModInfo::getDisplayName)
				.orElse(StringUtils.capitalize(modId));
			return new Object[] { name };
		}

		@Override
		public void writeNBT(CompoundNBT nbt) {
			nbt.putString("id", modId);
		}

		@Override
		public ItemAttribute readNBT(CompoundNBT nbt) {
			return new AddedBy(nbt.getString("id"));
		}

	}

}
