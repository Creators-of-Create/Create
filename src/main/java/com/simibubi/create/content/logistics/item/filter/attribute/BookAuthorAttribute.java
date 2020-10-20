package com.simibubi.create.content.logistics.item.filter.attribute;

import com.google.gson.JsonParseException;
import com.simibubi.create.content.logistics.item.filter.ItemAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

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
    public void writeNBT(CompoundNBT nbt) {
        nbt.putString("author", this.author);
    }

    @Override
    public ItemAttribute readNBT(CompoundNBT nbt) {
        return new BookAuthorAttribute(nbt.getString("author"));
    }

    private String extractAuthor(ItemStack stack) {
        CompoundNBT nbt = stack.getTag();
        if (nbt != null && nbt.contains("author")) {
            return nbt.getString("author");
        }
        return "";
    }
}
