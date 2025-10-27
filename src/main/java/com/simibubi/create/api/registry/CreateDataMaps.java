package com.simibubi.create.api.registry;

import com.simibubi.create.Create;
import com.simibubi.create.api.data.datamaps.BlazeBurnerFuel;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

/**
 * @see DataMapType
 * @see NeoForgeDataMaps
 */
public class CreateDataMaps {
	/**
	 * The {@linkplain Item} data map for regular blaze burner fuels.
	 * <p>
	 * The location of this data map is {@code create/data_maps/item/regular_blaze_burner_fuels.json}, and the values are objects with 1 field:
	 * <ul>
	 * <li>{@code burn_time}, a positive integer - how long the item will burn, in ticks</li>
	 * </ul>
	 * <p>
	 * The use of an integer as the value is also possible, though discouraged in case more options are added in the future.
	 */
	public static final DataMapType<Item, BlazeBurnerFuel> REGULAR_BLAZE_BURNER_FUELS = DataMapType.builder(
		Create.asResource("regular_blaze_burner_fuels"), Registries.ITEM, BlazeBurnerFuel.CODEC).build();

	/**
	 * The {@linkplain Item} data map for superheated blaze burner fuels.
	 * <p>
	 * The location of this data map is {@code create/data_maps/item/superheated_blaze_burner_fuels.json}, and the values are objects with 1 field:
	 * <ul>
	 * <li>{@code burn_time}, a positive integer - how long the item will burn, in ticks</li>
	 * </ul>
	 * <p>
	 * The use of an integer as the value is also possible, though discouraged in case more options are added in the future.
	 */
	public static final DataMapType<Item, BlazeBurnerFuel> SUPERHEATED_BLAZE_BURNER_FUELS = DataMapType.builder(
		Create.asResource("superheated_blaze_burner_fuels"), Registries.ITEM, BlazeBurnerFuel.CODEC).build();

	private CreateDataMaps() {
		throw new AssertionError("This class should not be instantiated");
	}
}
