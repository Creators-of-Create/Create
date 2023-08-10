package com.simibubi.create.content.logistics.filter.attribute;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.logistics.filter.ItemAttribute;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class BookAuthorAttribute implements ItemAttribute {
    String author;

    public BookAuthorAttribute(String author) {
        this.author = author;
    }

    @Override
    public boolean appliesTo(ItemStack itemStack) {
        return extractAuthor(itemStack).equals(author);
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack itemStack) {
        String name = extractAuthor(itemStack);

        List<ItemAttribute> atts = new ArrayList<>();
        if(name.length() > 0) {
            atts.add(new BookAuthorAttribute(name));
        }
        return atts;
    }

    @Override
    public String getTranslationKey() {
        return "book_author";
    }

    @Override
    public Object[] getTranslationParameters() {
        return new Object[] {author};
    }

    @Override
    public void writeNBT(CompoundTag nbt) {
        nbt.putString("author", this.author);
    }

    @Override
    public ItemAttribute readNBT(CompoundTag nbt) {
        return new BookAuthorAttribute(nbt.getString("author"));
    }

    private String extractAuthor(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains("author")) {
            return nbt.getString("author");
        }
        return "";
    }
}
