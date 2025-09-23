package com.simibubi.create.content.logistics.stockTicker;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;

import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Package ordering context containing additional information of package orders.
 *
 * @param stacks
 * @param amounts
 */
public record PackageOrderWithCrafts(PackageOrder orderedStacks, List<CraftingEntry> orderedCrafts) {
	
	public static PackageOrderWithCrafts empty() {
		return new PackageOrderWithCrafts(PackageOrder.empty(), List.of());
	}
	
	public static PackageOrderWithCrafts simple(List<BigItemStack> orderedStacks) {
		return new PackageOrderWithCrafts(new PackageOrder(orderedStacks), List.of());
	}
	
	public static PackageOrderWithCrafts singleRecipe(List<BigItemStack> pattern) {
		return new PackageOrderWithCrafts(PackageOrder.empty(), List.of(new CraftingEntry(new PackageOrder(pattern), 1)));
	}

	public record CraftingEntry(PackageOrder pattern, int count) {

		public CompoundTag write() {
			CompoundTag tag = new CompoundTag();
			tag.put("Pattern", pattern.write());
			tag.putInt("Count", count);
			return tag;
		}

		public static CraftingEntry read(CompoundTag tag) {
			return new CraftingEntry(PackageOrder.read(tag.getCompound("Pattern")), tag.getInt("Count"));
		}

		public void write(FriendlyByteBuf buffer) {
			pattern.write(buffer);
			buffer.writeVarInt(count);
		}

		public static CraftingEntry read(FriendlyByteBuf buffer) {
			return new CraftingEntry(PackageOrder.read(buffer), buffer.readVarInt());
		}

	}
	
    public static boolean hasCraftingInformation(PackageOrderWithCrafts context) {
        if (context == null)
            return false;
		// Only a valid crafting packet if it contains exactly one recipe
        return context.orderedCrafts.size() == 1;
    }
    
    public List<BigItemStack> getCraftingInformation() {
    	return orderedCrafts.get(0).pattern.stacks();
    }
    
    public List<BigItemStack> stacks() {
    	return orderedStacks.stacks();
    }

    public boolean isEmpty() {
        return orderedStacks.isEmpty();
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.put("OrderedStacks", orderedStacks.write());
        tag.put("OrderedCrafts", NBTHelper.writeCompoundList(orderedCrafts, CraftingEntry::write));
        return tag;
    }

	public static PackageOrderWithCrafts read(CompoundTag tag) {
		if (tag.contains("Entries", Tag.TAG_LIST)) // legacy format
			return new PackageOrderWithCrafts(PackageOrder.read(tag), List.of());
			
		return new PackageOrderWithCrafts(PackageOrder.read(tag.getCompound("OrderedStacks")),
			NBTHelper.readCompoundList(tag.getList("OrderedCrafts", Tag.TAG_COMPOUND), CraftingEntry::read));
	}

    public void write(FriendlyByteBuf buffer) {
        orderedStacks.write(buffer);
        buffer.writeVarInt(orderedCrafts.size());
        orderedCrafts.forEach(ce -> ce.write(buffer));
    }

    public static PackageOrderWithCrafts read(FriendlyByteBuf buffer) {
    	PackageOrder orderedStacks = PackageOrder.read(buffer);
    	List<CraftingEntry> orderedCrafts = new ArrayList<>();
        int size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
        	orderedCrafts.add(CraftingEntry.read(buffer));
        return new PackageOrderWithCrafts(orderedStacks, orderedCrafts);
    }
    
    public boolean orderedStacksMatchOrderedRecipes() {
    	if (orderedCrafts.isEmpty())
    		return false;
    	
    	InventorySummary stacks = new InventorySummary();
    	InventorySummary crafts = new InventorySummary();
    	
    	stacks().forEach(stacks::add);
    	orderedCrafts.forEach(ce -> ce.pattern.stacks().forEach(bis -> crafts.add(new BigItemStack(bis.stack, bis.count * ce.count))));

    	List<BigItemStack> stackEntries = stacks.getStacks();
		if (stackEntries.size() != crafts.getStacks().size())
    		return false;
		for (BigItemStack bis : stackEntries)
			if (crafts.getCountOf(bis.stack) != bis.count)
				return false;
    	return true;
    }

}
