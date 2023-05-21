package com.simibubi.create.content.redstone.displayLink.target;

import java.util.List;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;

public class LecternDisplayTarget extends DisplayTarget {

	@Override
	public void acceptText(int line, List<MutableComponent> text, DisplayLinkContext context) {
		BlockEntity be = context.getTargetBlockEntity();
		if (!(be instanceof LecternBlockEntity lectern))
			return;
		ItemStack book = lectern.getBook();
		if (book.isEmpty())
			return;

		if (book.is(Items.WRITABLE_BOOK))
			lectern.setBook(book = signBook(book));
		if (!book.is(Items.WRITTEN_BOOK))
			return;

		ListTag tag = book.getTag()
			.getList("pages", Tag.TAG_STRING);

		boolean changed = false;
		for (int i = 0; i - line < text.size() && i < 50; i++) {
			if (tag.size() <= i)
				tag.add(StringTag.valueOf(i < line ? "" : Component.Serializer.toJson(text.get(i - line))));

			else if (i >= line) {
				if (i - line == 0)
					reserve(i, lectern, context);
				if (i - line > 0 && isReserved(i - line, lectern, context))
					break;

				tag.set(i, StringTag.valueOf(Component.Serializer.toJson(text.get(i - line))));
			}
			changed = true;
		}

		book.getTag()
			.put("pages", tag);
		lectern.setBook(book);

		if (changed)
			context.level().sendBlockUpdated(context.getTargetPos(), lectern.getBlockState(), lectern.getBlockState(), 2);
	}

	@Override
	public DisplayTargetStats provideStats(DisplayLinkContext context) {
		return new DisplayTargetStats(50, 256, this);
	}

	public Component getLineOptionText(int line) {
		return Lang.translateDirect("display_target.page", line + 1);
	}

	private ItemStack signBook(ItemStack book) {
		ItemStack written = new ItemStack(Items.WRITTEN_BOOK);
		CompoundTag compoundtag = book.getTag();
		if (compoundtag != null)
			written.setTag(compoundtag.copy());

		written.addTagElement("author", StringTag.valueOf("Data Gatherer"));
		written.addTagElement("filtered_title", StringTag.valueOf("Printed Book"));
		written.addTagElement("title", StringTag.valueOf("Printed Book"));

		return written;
	}

}
