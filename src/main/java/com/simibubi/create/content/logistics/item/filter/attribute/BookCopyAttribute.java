package com.simibubi.create.content.logistics.item.filter.attribute;

import com.simibubi.create.content.logistics.item.filter.ItemAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.CompoundNBT;

import java.util.ArrayList;
import java.util.List;

public class BookCopyAttribute implements ItemAttribute {
    int generation;

    public BookCopyAttribute(int generation) {
        this.generation = generation;
    }

    @Override
    public boolean appliesTo(ItemStack itemStack) {
        return extractGeneration(itemStack) == generation;
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack itemStack) {
        int generation = extractGeneration(itemStack);

        List<ItemAttribute> atts = new ArrayList<>();
        if(generation >= 0) {
            atts.add(new BookCopyAttribute(generation));
        }
        return atts;
    }

    @Override
    public String getTranslationKey() {
        switch(generation){
            case 0:
                return "book_copy_original";
            case 1:
                return "book_copy_first";
            case 2:
                return "book_copy_second";
            default:
                return "book_copy_tattered";
        }
    }

    @Override
    public void writeNBT(CompoundNBT nbt) {
        nbt.putInt("generation", this.generation);
    }

    @Override
    public ItemAttribute readNBT(CompoundNBT nbt) {
        return new BookCopyAttribute(nbt.getInt("generation"));
    }

    private int extractGeneration(ItemStack stack) {
        CompoundNBT nbt = stack.getTag();
        if (nbt != null && stack.getItem() instanceof WrittenBookItem) {
            return nbt.getInt("generation");
        }
        return -1;
    }
}
