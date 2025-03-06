package com.simibubi.create.content.logistics.stockTicker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.simibubi.create.content.logistics.BigItemStack;

import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Package ordering context containing additional information of package orders.
 *
 * @param stacks
 * @param amounts
 */
public record PackageOrderCraftingContext(List<List<BigItemStack>> stacks, List<Integer> amounts) {

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        ListTag outer = new ListTag();
        for (List<BigItemStack> stack : stacks) {
            outer.add(NBTHelper.writeCompoundList(stack, BigItemStack::write));
        }
        tag.put("Entries", outer);
        tag.put("Amounts", new IntArrayTag(amounts));
        return tag;
    }

    public static boolean hasCraftingInformation(PackageOrderCraftingContext context) {
        if (context == null) {
            return false;
        }

		// Only a valid crafting packet if it contains exactly one recipe
        return context.stacks.size() == 1;
    }

    public static PackageOrderCraftingContext empty() {
        return new PackageOrderCraftingContext(List.of(), List.of());
    }

    public boolean isEmpty() {
        return stacks.isEmpty();
    }

    public static PackageOrderCraftingContext read(CompoundTag tag) {
        List<List<BigItemStack>> stacks = new ArrayList<>();
        for (Tag t : tag.getList("Entries", Tag.TAG_LIST)) {
            if (t instanceof ListTag list)
                stacks.add(NBTHelper.readCompoundList(list, BigItemStack::read));
        }
        List<Integer> amounts = Arrays.stream(tag.getIntArray("Amounts")).boxed().toList();
        return new PackageOrderCraftingContext(stacks, amounts);
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(stacks.size());
        for (List<BigItemStack> list : stacks) {
            buffer.writeVarInt(list.size());
            for (BigItemStack itemStack : list)
                itemStack.send(buffer);
        }
        buffer.writeVarInt(amounts.size());
        for (Integer amount : amounts) {
            buffer.writeVarInt(amount);
        }
    }

    public static PackageOrderCraftingContext read(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<List<BigItemStack>> stacks = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            List<BigItemStack> list = new ArrayList<>();
            int innerSize = buffer.readVarInt();
            for (int j = 0; j < innerSize; j++) {
                list.add(BigItemStack.receive(buffer));
            }
            stacks.add(list);
        }
        List<Integer> amounts = new ArrayList<>();
        size = buffer.readVarInt();
        for (int i = 0; i < size; i++) {
            amounts.add(buffer.readVarInt());
        }
        return new PackageOrderCraftingContext(stacks, amounts);
    }

}
