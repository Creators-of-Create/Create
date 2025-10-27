package com.simibubi.create.content.redstone.displayLink.target;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
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

		WrittenBookContent writtenBookContent = book.getOrDefault(DataComponents.WRITTEN_BOOK_CONTENT, WrittenBookContent.EMPTY);
		List<Filterable<Component>> pages = new ArrayList<>(writtenBookContent.pages());

		boolean changed = false;
		for (int i = 0; i - line < text.size() && i < 50; i++) {
			if (pages.size() <= i)
				pages.add(Filterable.passThrough(i < line ? Component.empty() : text.get(i - line)));

			else if (i >= line) {
				if (i - line == 0)
					reserve(i, lectern, context);
				if (i - line > 0 && isReserved(i - line, lectern, context))
					break;

				pages.set(i, Filterable.passThrough(text.get(i - line)));
			}
			changed = true;
		}

		book.set(DataComponents.WRITTEN_BOOK_CONTENT, writtenBookContent.withReplacedPages(pages));
		lectern.setBook(book);

		if (changed)
			context.level().sendBlockUpdated(context.getTargetPos(), lectern.getBlockState(), lectern.getBlockState(), 2);
	}

	@Override
	public DisplayTargetStats provideStats(DisplayLinkContext context) {
		return new DisplayTargetStats(50, 256, this);
	}

	public Component getLineOptionText(int line) {
		return CreateLang.translateDirect("display_target.page", line + 1);
	}

	private ItemStack signBook(ItemStack book) {
		ItemStack written = new ItemStack(Items.WRITTEN_BOOK);
		WritableBookContent bookContents = book.get(DataComponents.WRITABLE_BOOK_CONTENT);

		List<Filterable<Component>> list = bookContents.pages().stream().map(filterable -> filterable.<Component>map(Component::literal)).toList();
		WrittenBookContent writtenContent = new WrittenBookContent(
				Filterable.passThrough("Printed Book"),
				"Data Gatherer",
				0,
				list,
				true
		);
		written.set(DataComponents.WRITTEN_BOOK_CONTENT, writtenContent);

		return written;
	}

	@Override
	public boolean requiresComponentSanitization() {
		return true;
	}

}
