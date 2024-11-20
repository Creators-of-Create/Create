package com.simibubi.create.content.logistics.item.filter.attribute;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import com.simibubi.create.content.logistics.item.filter.attribute.attributes.astralsorcery.AstralSorceryAttunementAttribute;

import com.simibubi.create.content.logistics.item.filter.attribute.attributes.astralsorcery.AstralSorceryCrystalAttribute;

import com.simibubi.create.content.logistics.item.filter.attribute.attributes.astralsorcery.AstralSorceryPerkGemAttribute;

import org.jetbrains.annotations.ApiStatus;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllRegistries;
import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.attributes.AddedByAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.attributes.BookAuthorAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.attributes.BookCopyAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.attributes.ColorAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.attributes.EnchantAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.attributes.FluidContentsAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.attributes.InItemGroupAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.attributes.InTagAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.attributes.ItemNameAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.attributes.ShulkerFillLevelAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.attributes.astralsorcery.AstralSorceryAmuletAttribute;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraftforge.registries.DeferredRegister;

public class AllItemAttributeTypes {
	private static final DeferredRegister<ItemAttributeType> REGISTER = DeferredRegister.create(AllRegistries.Keys.ITEM_ATTRIBUTE_TYPES, Create.ID);
	private static final RecipeWrapper RECIPE_WRAPPER = new RecipeWrapper(new ItemStackHandler(1));

	public static final ItemAttributeType
			PLACEABLE = singleton("placeable", s -> s.getItem() instanceof BlockItem),
			CONSUMABLE = singleton("consumable", ItemStack::isEdible),
			FLUID_CONTAINER = singleton("fluid_container", s -> s.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
					.isPresent()),
			ENCHANTED = singleton("enchanted", ItemStack::isEnchanted),
			MAX_ENCHANTED = singleton("max_enchanted", AllItemAttributeTypes::maxEnchanted),
			RENAMED = singleton("renamed", ItemStack::hasCustomHoverName),
			DAMAGED = singleton("damaged", ItemStack::isDamaged),
			BADLY_DAMAGED = singleton("badly_damaged", s -> s.isDamaged() && (float) s.getDamageValue() / s.getMaxDamage() > 3 / 4f),
			NOT_STACKABLE = singleton("not_stackable", ((Predicate<ItemStack>) ItemStack::isStackable).negate()),
			EQUIPABLE = singleton("equipable", s -> LivingEntity.getEquipmentSlotForItem(s)
			.getType() != EquipmentSlot.Type.HAND),
			FURNACE_FUEL = singleton("furnace_fuel", AbstractFurnaceBlockEntity::isFuel),
			WASHABLE = singleton("washable", AllFanProcessingTypes.SPLASHING::canProcess),
			HAUNTABLE = singleton("hauntable", AllFanProcessingTypes.HAUNTING::canProcess),
			CRUSHABLE = singleton("crushable", (s, w) -> testRecipe(s, w, AllRecipeTypes.CRUSHING.getType())
					|| testRecipe(s, w, AllRecipeTypes.MILLING.getType())),
			SMELTABLE = singleton("smeltable", (s, w) -> testRecipe(s, w, RecipeType.SMELTING)),
			SMOKABLE = singleton("smokable", (s, w) -> testRecipe(s, w, RecipeType.SMOKING)),
			BLASTABLE = singleton("blastable", (s, w) -> testRecipe(s, w, RecipeType.BLASTING)),
			COMPOSTABLE = singleton("compostable", s -> ComposterBlock.COMPOSTABLES.containsKey(s.getItem())),

			IN_TAG = register("in_tag", new InTagAttribute.Type()),
			IN_ITEM_GROUP = register("in_item_group", new InItemGroupAttribute.Type()),
			ADDED_BY = register("added_by", new AddedByAttribute.Type()),
			HAS_ENCHANT = register("has_enchant", new EnchantAttribute.Type()),
			SHULKER_FILL_LEVEL = register("shulker_fill_level", new ShulkerFillLevelAttribute.Type()),
			HAS_COLOR = register("has_color", new ColorAttribute.Type()),
			HAS_FLUID = register("has_fluid", new FluidContentsAttribute.Type()),
			HAS_NAME = register("has_name", new ItemNameAttribute.Type()),
			BOOK_AUTHOR = register("book_author", new BookAuthorAttribute.Type()),
			BOOK_COPY = register("book_copy", new BookCopyAttribute.Type()),

			ASTRAL_AMULET = register("astral_amulet", new AstralSorceryAmuletAttribute.Type()),
			ASTRAL_ATTUNMENT = register("astral_attunment", new AstralSorceryAttunementAttribute.Type()),
			ASTRAL_CRYSTAL = register("astral_crystal", new AstralSorceryCrystalAttribute.Type()),
			ASTRAL_PERK_GEM = register("astral_perk_gem", new AstralSorceryPerkGemAttribute.Type());

	private static boolean testRecipe(ItemStack s, Level w, RecipeType<? extends AbstractCookingRecipe> type) {
		RECIPE_WRAPPER.setItem(0, s.copy());
		return w.getRecipeManager()
				.getRecipeFor(type, RECIPE_WRAPPER, w)
				.isPresent();
	}

	// TODO - Move away from stream()
	private static boolean maxEnchanted(ItemStack s) {
		return EnchantmentHelper.getEnchantments(s)
				.entrySet()
				.stream()
				.anyMatch(e -> e.getKey()
						.getMaxLevel() <= e.getValue());
	}

	private static ItemAttributeType singleton(String id, Predicate<ItemStack> predicate) {
		return register(id, new SingletonItemAttribute.Type(type -> new SingletonItemAttribute(type, (stack, level) -> predicate.test(stack), id)));
	}

	private static ItemAttributeType singleton(String id, BiPredicate<ItemStack, Level> predicate) {
		return register(id, new SingletonItemAttribute.Type(type -> new SingletonItemAttribute(type, predicate, id)));
	}

	private static ItemAttributeType register(String id, ItemAttributeType type) {
		return REGISTER.register(id, () -> type).get();
	}

	@ApiStatus.Internal
	public static void register(IEventBus modEventBus) {
		REGISTER.register(modEventBus);
	}
}
