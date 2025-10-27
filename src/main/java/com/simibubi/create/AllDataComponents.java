package com.simibubi.create;

import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

import com.mojang.serialization.Codec;
import com.simibubi.create.content.equipment.clipboard.ClipboardContent;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides.ClipboardType;
import com.simibubi.create.content.equipment.sandPaper.SandPaperItemComponent;
import com.simibubi.create.content.equipment.symmetryWand.mirror.SymmetryMirror;
import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;
import com.simibubi.create.content.equipment.zapper.PlacementPatterns;
import com.simibubi.create.content.equipment.zapper.terrainzapper.PlacementOptions;
import com.simibubi.create.content.equipment.zapper.terrainzapper.TerrainBrushes;
import com.simibubi.create.content.equipment.zapper.terrainzapper.TerrainTools;
import com.simibubi.create.content.fluids.potion.PotionFluid.BottleType;
import com.simibubi.create.content.logistics.box.PackageItem.PackageOrderData;
import com.simibubi.create.content.logistics.filter.AttributeFilterWhitelistMode;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute.ItemAttributeEntry;
import com.simibubi.create.content.logistics.redstoneRequester.AutoRequestData;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.content.logistics.tableCloth.ShoppingListItem.ShoppingList;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe.SequencedAssembly;
import com.simibubi.create.content.redstone.displayLink.ClickToLinkBlockItem.ClickToLinkData;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity.SchematicannonOptions;
import com.simibubi.create.content.trains.track.BezierTrackPointLocation;
import com.simibubi.create.content.trains.track.TrackPlacement.ConnectingFrom;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponentType.Builder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AllDataComponents {
	private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Create.ID);

	public static final DataComponentType<Integer> BACKTANK_AIR = register(
			"banktank_air",
			builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT)
	);

	public static final DataComponentType<BlockPos> BELT_FIRST_SHAFT = register(
			"belt_first_shaft",
			builder -> builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC)
	);

	public static final DataComponentType<Boolean> INFERRED_FROM_RECIPE = register(
			"inferred_from_recipe",
			builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
	);

	public static final DataComponentType<PlacementPatterns> PLACEMENT_PATTERN = register(
			"placement_pattern",
			builder -> builder.persistent(PlacementPatterns.CODEC).networkSynchronized(PlacementPatterns.STREAM_CODEC)
	);

	public static final DataComponentType<TerrainBrushes> SHAPER_BRUSH = register(
			"shaper_brush",
			builder -> builder.persistent(TerrainBrushes.CODEC).networkSynchronized(TerrainBrushes.STREAM_CODEC)
	);

	public static final DataComponentType<BlockPos> SHAPER_BRUSH_PARAMS = register(
			"shaper_brush_params",
			builder -> builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC)
	);

	public static final DataComponentType<PlacementOptions> SHAPER_PLACEMENT_OPTIONS = register(
			"shaper_placement_options",
			builder -> builder.persistent(PlacementOptions.CODEC).networkSynchronized(PlacementOptions.STREAM_CODEC)
	);

	public static final DataComponentType<TerrainTools> SHAPER_TOOL = register(
			"shaper_tool",
			builder -> builder.persistent(TerrainTools.CODEC).networkSynchronized(TerrainTools.STREAM_CODEC)
	);

	public static final DataComponentType<BlockState> SHAPER_BLOCK_USED = register(
			"shaper_block_used",
			builder -> builder.persistent(BlockState.CODEC).networkSynchronized(ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY))
	);

	public static final DataComponentType<Boolean> SHAPER_SWAP = register(
			"shaper_swap",
			builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
	);

	public static final DataComponentType<CompoundTag> SHAPER_BLOCK_DATA = register(
			"shaper_block_data",
			builder -> builder.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
	);

	public static final DataComponentType<ItemContainerContents> FILTER_ITEMS = register(
			"filter_items",
			builder -> builder.persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC)
	);

	// These 2 are placed on items inside filters and not the filter itself
	public static final DataComponentType<Boolean> FILTER_ITEMS_RESPECT_NBT = register(
			"filter_items_respect_nbt",
			builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
	);

	public static final DataComponentType<Boolean> FILTER_ITEMS_BLACKLIST = register(
			"filter_items_blacklist",
			builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
	);

	public static final DataComponentType<AttributeFilterWhitelistMode> ATTRIBUTE_FILTER_WHITELIST_MODE = register(
			"attribute_filter_whitelist_mode",
			builder -> builder.persistent(AttributeFilterWhitelistMode.CODEC).networkSynchronized(AttributeFilterWhitelistMode.STREAM_CODEC)
	);

	public static final DataComponentType<List<ItemAttributeEntry>> ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES = register(
			"attribute_filter_matched_attributes",
		builder -> builder.persistent(ItemAttributeEntry.CODEC.listOf()).networkSynchronized(CatnipStreamCodecBuilders.list(ItemAttributeEntry.STREAM_CODEC))
	);

	public static final DataComponentType<ClipboardContent> CLIPBOARD_CONTENT = register(
		"clipboard_content",
		builder -> builder.persistent(ClipboardContent.CODEC).networkSynchronized(ClipboardContent.STREAM_CODEC)
	);

	public static final DataComponentType<ConnectingFrom> TRACK_CONNECTING_FROM = register(
			"track_connecting_from",
			builder -> builder.persistent(ConnectingFrom.CODEC).networkSynchronized(ConnectingFrom.STREAM_CODEC)
	);

	public static final DataComponentType<Boolean> TRACK_EXTENDED_CURVE = register(
			"track_extend_curve",
			builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
	);

	public static final DataComponentType<BlockPos> TRACK_TARGETING_ITEM_SELECTED_POS = register(
			"track_targeting_item_selected_pos",
			builder -> builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC)
	);

	public static final DataComponentType<Boolean> TRACK_TARGETING_ITEM_SELECTED_DIRECTION = register(
			"track_targeting_item_selected_direction",
			builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
	);

	public static final DataComponentType<BezierTrackPointLocation> TRACK_TARGETING_ITEM_BEZIER = register(
			"track_targeting_item_bezier",
			builder -> builder.persistent(BezierTrackPointLocation.CODEC).networkSynchronized(BezierTrackPointLocation.STREAM_CODEC)
	);

	public static final DataComponentType<Boolean> SCHEMATIC_DEPLOYED = register(
			"schematic_deployed",
			builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
	);

	public static final DataComponentType<String> SCHEMATIC_OWNER = register(
			"schematic_owner",
			builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
	);

	public static final DataComponentType<String> SCHEMATIC_FILE = register(
			"schematic_file",
			builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
	);

	public static final DataComponentType<BlockPos> SCHEMATIC_ANCHOR = register(
			"schematic_anchor",
			builder -> builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC)
	);

	public static final DataComponentType<Rotation> SCHEMATIC_ROTATION = register(
			"schematic_rotation",
			builder -> builder.persistent(Rotation.CODEC).networkSynchronized(CatnipStreamCodecs.ROTATION)
	);

	public static final DataComponentType<Mirror> SCHEMATIC_MIRROR = register(
			"schematic_mirror",
			builder -> builder.persistent(Mirror.CODEC).networkSynchronized(CatnipStreamCodecs.MIRROR)
	);

	public static final DataComponentType<Vec3i> SCHEMATIC_BOUNDS = register(
			"schematic_bounds",
			builder -> builder.persistent(Vec3i.CODEC).networkSynchronized(CatnipStreamCodecs.VEC3I)
	);

	public static final DataComponentType<Integer> SCHEMATIC_HASH = register(
			"schematic_hash",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
	);

	public static final DataComponentType<Integer> CHROMATIC_COMPOUND_COLLECTING_LIGHT = register(
			"chromatic_compound_collecting_light",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
	);

	public static final DataComponentType<SandPaperItemComponent> SAND_PAPER_POLISHING = register(
			"sand_paper_polishing",
			builder -> builder.persistent(SandPaperItemComponent.CODEC).networkSynchronized(SandPaperItemComponent.STREAM_CODEC)
	);

	public static final DataComponentType<Unit> SAND_PAPER_JEI = register(
			"sand_paper_jei",
			builder -> builder.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
	);

	// Holds contraption data when a minecraft contraption is picked up
	public static final DataComponentType<CompoundTag> MINECRAFT_CONTRAPTION_DATA = register(
			"minecart_contraption_data",
			builder -> builder.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
	);

	public static final DataComponentType<ItemContainerContents> LINKED_CONTROLLER_ITEMS = register(
			"linked_controller_items",
			builder -> builder.persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC)
	);

	public static final DataComponentType<ToolboxInventory> TOOLBOX_INVENTORY = register(
			"toolbox_inventory",
		builder -> builder.persistent(ToolboxInventory.BACKWARDS_COMPAT_CODEC).networkSynchronized(ToolboxInventory.STREAM_CODEC)
	);

	public static final DataComponentType<UUID> TOOLBOX_UUID = register(
			"toolbox_uuid",
			builder -> builder.persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC)
	);

	public static final DataComponentType<SequencedAssembly> SEQUENCED_ASSEMBLY = register(
			"sequenced_assembly",
			builder -> builder.persistent(SequencedAssembly.CODEC).networkSynchronized(SequencedAssembly.STREAM_CODEC)
	);

	public static final DataComponentType<CompoundTag> TRAIN_SCHEDULE = register(
			"train_schedule",
			builder -> builder.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
	);

	public static final DataComponentType<SymmetryMirror> SYMMETRY_WAND = register(
			"symmetry_wand",
			builder -> builder.persistent(SymmetryMirror.CODEC).networkSynchronized(SymmetryMirror.STREAM_CODEC)
	);

	public static final DataComponentType<Boolean> SYMMETRY_WAND_ENABLE = register(
			"symmetry_wand_enable",
			builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
	);

	public static final DataComponentType<Boolean> SYMMETRY_WAND_SIMULATE = register(
			"symmetry_wand_simulate",
			builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
	);

	public static final DataComponentType<BottleType> POTION_FLUID_BOTTLE_TYPE = register(
			"potion_fluid_bottle_type",
			builder -> builder.persistent(BottleType.CODEC).networkSynchronized(BottleType.STREAM_CODEC)
	);

	public static final DataComponentType<SchematicannonOptions> SCHEMATICANNON_OPTIONS = register(
			"schematicannon_options",
			builder -> builder.persistent(SchematicannonOptions.CODEC).networkSynchronized(SchematicannonOptions.STREAM_CODEC)
	);

	public static final DataComponentType<AutoRequestData> AUTO_REQUEST_DATA = register(
		"auto_request_data",
		builder -> builder.persistent(AutoRequestData.CODEC).networkSynchronized(AutoRequestData.STREAM_CODEC)
	);

	public static final DataComponentType<ShoppingList> SHOPPING_LIST = register(
		"shopping_list",
		builder -> builder.persistent(ShoppingList.CODEC).networkSynchronized(ShoppingList.STREAM_CODEC)
	);

	public static final DataComponentType<String> SHOPPING_LIST_ADDRESS = register(
		"shopping_list_address",
		builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
	);

	public static final DataComponentType<String> PACKAGE_ADDRESS = register(
		"package_address",
		builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
	);

	public static final DataComponentType<ItemContainerContents> PACKAGE_CONTENTS = register(
		"package_contents",
		builder -> builder.persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC)
	);

	public static final DataComponentType<PackageOrderData> PACKAGE_ORDER_DATA = register(
		"package_order_data",
		builder -> builder.persistent(PackageOrderData.CODEC).networkSynchronized(PackageOrderData.STREAM_CODEC)
	);

	public static final DataComponentType<PackageOrderWithCrafts> PACKAGE_ORDER_CONTEXT = register(
		"package_order_context",
		builder -> builder.persistent(PackageOrderWithCrafts.CODEC).networkSynchronized(PackageOrderWithCrafts.STREAM_CODEC)
	);

	public static final DataComponentType<ClickToLinkData> CLICK_TO_LINK_DATA = register(
		"click_to_link_data",
		builder -> builder.persistent(ClickToLinkData.CODEC).networkSynchronized(ClickToLinkData.STREAM_CODEC)
	);

	/**
	 * @deprecated Use {@link AllDataComponents#CLIPBOARD_CONTENT} instead.
	 */
	@ScheduledForRemoval(inVersion = "1.21.1+ Port")
	@Deprecated(since = "6.0.7", forRemoval = true)
	public static final DataComponentType<ClipboardType> CLIPBOARD_TYPE = register(
		"clipboard_type",
		builder -> builder.persistent(ClipboardType.CODEC).networkSynchronized(ClipboardType.STREAM_CODEC)
	);

	// Deprecated components
	/**
	 * @deprecated Use {@link AllDataComponents#CLIPBOARD_CONTENT} instead.
	 */
	@ScheduledForRemoval(inVersion = "1.21.1+ Port")
	@Deprecated(since = "6.0.7", forRemoval = true)
	public static final DataComponentType<List<List<ClipboardEntry>>> CLIPBOARD_PAGES = register(
		"clipboard_pages",
		builder -> builder.persistent(ClipboardEntry.CODEC.listOf().listOf()).networkSynchronized(CatnipStreamCodecBuilders.list(CatnipStreamCodecBuilders.list(ClipboardEntry.STREAM_CODEC)))
	);

	/**
	 * @deprecated Use {@link AllDataComponents#CLIPBOARD_CONTENT} instead.
	 */
	@ScheduledForRemoval(inVersion = "1.21.1+ Port")
	@Deprecated(since = "6.0.7", forRemoval = true)
	public static final DataComponentType<Unit> CLIPBOARD_READ_ONLY = register(
		"clipboard_read_only",
		builder -> builder.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
	);

	/**
	 * @deprecated Use {@link AllDataComponents#CLIPBOARD_CONTENT} instead.
	 */
	@ScheduledForRemoval(inVersion = "1.21.1+ Port")
	@Deprecated(since = "6.0.7", forRemoval = true)
	public static final DataComponentType<CompoundTag> CLIPBOARD_COPIED_VALUES = register(
		"clipboard_copied_values",
		builder -> builder.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
	);

	/**
	 * @deprecated Use {@link AllDataComponents#CLIPBOARD_CONTENT} instead.
	 */
	@ScheduledForRemoval(inVersion = "1.21.1+ Port")
	@Deprecated(since = "6.0.7", forRemoval = true)
	public static final DataComponentType<Integer> CLIPBOARD_PREVIOUSLY_OPENED_PAGE = register(
		"clipboard_previously_opened_page",
		builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
	);

	private static <T> DataComponentType<T> register(String name, UnaryOperator<Builder<T>> builder) {
		DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
		DATA_COMPONENTS.register(name, () -> type);
		return type;
	}

	@Internal
	public static void register(IEventBus modEventBus) {
		DATA_COMPONENTS.register(modEventBus);
	}
}
