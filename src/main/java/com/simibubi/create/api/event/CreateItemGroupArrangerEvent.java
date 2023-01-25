package com.simibubi.create.api.event;

import com.simibubi.create.foundation.item.CreateItemGroupBase;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

/**
 * Event to change item order and add item to Create's creative mode tab.
 */
public class CreateItemGroupArrangerEvent extends Event {
    private final CreateItemGroupBase itemGroup;
    private final NonNullList<ItemStack> items;
    private final Stage stage;

    public CreateItemGroupArrangerEvent(CreateItemGroupBase itemGroup, NonNullList<ItemStack> items, Stage stage) {
        this.itemGroup = itemGroup;
        this.items = items;
        this.stage = stage;
    }

    /**
     * Get the creative mod tab, so you could determine which tab are you adding into.
     * @return the creative mod tab
     */
    public CreateItemGroupBase getItemGroup() {
        return itemGroup;
    }

    /**
     * Get all Tab items of this stage in the tab. <br>
     * For modify item order or adding item.
     * @return a modifiable list of all items in the tab
     */
    public NonNullList<ItemStack> getItems() {
        return items;
    }

    /**
     * Get which stage the event is fired.
     * @return Stage: SPECIAL_ITEMS, BLOCKS, ITEMS.
     */
    public Stage getStage() {
        return stage;
    }

    public enum Stage{
        ITEMS,
        BLOCKS,
        SPECIAL_ITEMS
    }

}
