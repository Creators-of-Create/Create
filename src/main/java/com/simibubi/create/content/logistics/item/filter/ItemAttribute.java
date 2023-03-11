package com.simibubi.create.content.logistics.item.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.processing.InWorldProcessing;
import com.simibubi.create.content.logistics.item.filter.attribute.BookAuthorAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.BookCopyAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ColorAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.EnchantAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.FluidContentsAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemNameAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ShulkerFillLevelAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.astralsorcery.AstralSorceryAmuletAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.astralsorcery.AstralSorceryAttunementAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.astralsorcery.AstralSorceryCrystalAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.astralsorcery.AstralSorceryPerkGemAttribute;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public interface ItemAttribute {

	static List<ItemAttribute> types = new ArrayList<>();

	static ItemAttribute standard = register(StandardTraits.DUMMY);
	static ItemAttribute inTag = register(new InTag(ItemTags.LOGS));
	static ItemAttribute inItemGroup = register(new InItemGroup(CreativeModeTab.TAB_MISC));
	static ItemAttribute addedBy = register(new InItemGroup.AddedBy("dummy"));
	static ItemAttribute hasEnchant = register(EnchantAttribute.EMPTY);
	static ItemAttribute shulkerFillLevel = register(ShulkerFillLevelAttribute.EMPTY);
	static ItemAttribute hasColor = register(ColorAttribute.EMPTY);
	static ItemAttribute hasFluid = register(FluidContentsAttribute.EMPTY);
	static ItemAttribute hasName = register(new ItemNameAttribute("dummy"));
	static ItemAttribute bookAuthor = register(new BookAuthorAttribute("dummy"));
	static ItemAttribute bookCopy = register(new BookCopyAttribute(-1));
	static ItemAttribute astralAmulet = register(new AstralSorceryAmuletAttribute("dummy", -1));
	static ItemAttribute astralAttunement = register(new AstralSorceryAttunementAttribute("dummy"));
	static ItemAttribute astralCrystal = register(new AstralSorceryCrystalAttribute("dummy"));
	static ItemAttribute astralPerkGem = register(new AstralSorceryPerkGemAttribute("dummy"));

	static ItemAttribute register(ItemAttribute attributeType) {
		types.add(attributeType);
		return attributeType;
	}

	static ItemAttribute fromNBT(CompoundTag nbt) {
		for (ItemAttribute itemAttribute : types)
			if (itemAttribute.canRead(nbt))
				return itemAttribute.readNBT(nbt.getCompound(itemAttribute.getNBTKey()));
		return null;
	}

	default boolean appliesTo(ItemStack stack, Level world) {
		return appliesTo(stack);
	}

	boolean appliesTo(ItemStack stack);

	default List<ItemAttribute> listAttributesOf(ItemStack stack, Level world) {
		return listAttributesOf(stack);
	}

	List<ItemAttribute> listAttributesOf(ItemStack stack);

	String getTranslationKey();

	void writeNBT(CompoundTag nbt);

	ItemAttribute readNBT(CompoundTag nbt);

	default void serializeNBT(CompoundTag nbt) {
		CompoundTag compound = new CompoundTag();
		writeNBT(compound);
		nbt.put(getNBTKey(), compound);
	}

	default Object[] getTranslationParameters() {
		return new String[0];
	}

	default boolean canRead(CompoundTag nbt) {
		return nbt.contains(getNBTKey());
	}

	default String getNBTKey() {
		return getTranslationKey();
	}

	@OnlyIn(value = Dist.CLIENT)
	default MutableComponent format(boolean inverted) {
		return Lang.translateDirect("item_attributes." + getTranslationKey() + (inverted ? ".inverted" : ""),
			getTranslationParameters());
	}

	public static enum StandardTraits implements ItemAttribute {

		DUMMY(s -> false),
		PLACEABLE(s -> s.getItem() instanceof BlockItem),
		CONSUMABLE(ItemStack::isEdible),
		FLUID_CONTAINER(s -> s.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
			.isPresent()),
		ENCHANTED(ItemStack::isEnchanted),
		MAX_ENCHANTED(StandardTraits::maxEnchanted),
		RENAMED(ItemStack::hasCustomHoverName),
		DAMAGED(ItemStack::isDamaged),
		BADLY_DAMAGED(s -> s.isDamaged() && (float) s.getDamageValue() / s.getMaxDamage() > 3 / 4f),
		NOT_STACKABLE(((Predicate<ItemStack>) ItemStack::isStackable).negate()),
		EQUIPABLE(s -> LivingEntity.getEquipmentSlotForItem(s)
			.getType() != EquipmentSlot.Type.HAND),
		FURNACE_FUEL(AbstractFurnaceBlockEntity::isFuel),
		WASHABLE(InWorldProcessing::isWashable),
		HAUNTABLE(InWorldProcessing::isHauntable),
		CRUSHABLE((s, w) -> testRecipe(s, w, AllRecipeTypes.CRUSHING.getType())
			|| testRecipe(s, w, AllRecipeTypes.MILLING.getType())),
		SMELTABLE((s, w) -> testRecipe(s, w, RecipeType.SMELTING)),
		SMOKABLE((s, w) -> testRecipe(s, w, RecipeType.SMOKING)),
		BLASTABLE((s, w) -> testRecipe(s, w, RecipeType.BLASTING)),
		COMPOSTABLE(s -> ComposterBlock.COMPOSTABLES.containsKey(s.getItem()));

		private static final RecipeWrapper RECIPE_WRAPPER = new RecipeWrapper(new ItemStackHandler(1));
		private Predicate<ItemStack> test;
		private BiPredicate<ItemStack, Level> testWithWorld;

		private StandardTraits(Predicate<ItemStack> test) {
			this.test = test;
		}

		private static boolean testRecipe(ItemStack s, Level w, RecipeType<? extends Recipe<Container>> type) {
			RECIPE_WRAPPER.setItem(0, s.copy());
			return w.getRecipeManager()
				.getRecipeFor(type, RECIPE_WRAPPER, w)
				.isPresent();
		}

		private static boolean maxEnchanted(ItemStack s) {
			return EnchantmentHelper.getEnchantments(s)
				.entrySet()
				.stream()
				.anyMatch(e -> e.getKey()
					.getMaxLevel() <= e.getValue());
		}

		private StandardTraits(BiPredicate<ItemStack, Level> test) {
			this.testWithWorld = test;
		}

		@Override
		public boolean appliesTo(ItemStack stack, Level world) {
			if (testWithWorld != null)
				return testWithWorld.test(stack, world);
			return appliesTo(stack);
		}

		@Override
		public boolean appliesTo(ItemStack stack) {
			return test.test(stack);
		}

		@Override
		public List<ItemAttribute> listAttributesOf(ItemStack stack, Level world) {
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
		public void writeNBT(CompoundTag nbt) {
			nbt.putBoolean(name(), true);
		}

		@Override
		public ItemAttribute readNBT(CompoundTag nbt) {
			for (StandardTraits trait : values())
				if (nbt.contains(trait.name()))
					return trait;
			return null;
		}

	}

	public static class InTag implements ItemAttribute {

		public TagKey<Item> tag;

		public InTag(TagKey<Item> tag) {
			this.tag = tag;
		}

		@Override
		public boolean appliesTo(ItemStack stack) {
			return stack.is(tag);
		}

		@Override
		public List<ItemAttribute> listAttributesOf(ItemStack stack) {
			return stack.getTags()
				.map(InTag::new)
				.collect(Collectors.toList());
		}

		@Override
		public String getTranslationKey() {
			return "in_tag";
		}

		@Override
		public Object[] getTranslationParameters() {
			return new Object[] { "#" + tag.location() };
		}

		@Override
		public void writeNBT(CompoundTag nbt) {
			nbt.putString("space", tag.location().getNamespace());
			nbt.putString("path", tag.location().getPath());
		}

		@Override
		public ItemAttribute readNBT(CompoundTag nbt) {
			return new InTag(ItemTags.create(new ResourceLocation(nbt.getString("space"), nbt.getString("path"))));
		}

	}

	public static class InItemGroup implements ItemAttribute {

		private CreativeModeTab group;

		public InItemGroup(CreativeModeTab group) {
			this.group = group;
		}

		@Override
		public boolean appliesTo(ItemStack stack) {
			Item item = stack.getItem();
			return item.getItemCategory() == group;
		}

		@Override
		public List<ItemAttribute> listAttributesOf(ItemStack stack) {
			CreativeModeTab group = stack.getItem()
				.getItemCategory();
			return group == null ? Collections.emptyList() : Arrays.asList(new InItemGroup(group));
		}

		@Override
		public String getTranslationKey() {
			return "in_item_group";
		}

		@Override
		@OnlyIn(value = Dist.CLIENT)
		public MutableComponent format(boolean inverted) {
			return Lang.translateDirect("item_attributes." + getTranslationKey() + (inverted ? ".inverted" : ""),
				group.getDisplayName());
		}

		@Override
		public void writeNBT(CompoundTag nbt) {
			nbt.putString("path", group.getRecipeFolderName());
		}

		@Override
		public ItemAttribute readNBT(CompoundTag nbt) {
			String readPath = nbt.getString("path");
			for (CreativeModeTab group : CreativeModeTab.TABS)
				if (group.getRecipeFolderName()
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
		public void writeNBT(CompoundTag nbt) {
			nbt.putString("id", modId);
		}

		@Override
		public ItemAttribute readNBT(CompoundTag nbt) {
			return new AddedBy(nbt.getString("id"));
		}

	}

}
