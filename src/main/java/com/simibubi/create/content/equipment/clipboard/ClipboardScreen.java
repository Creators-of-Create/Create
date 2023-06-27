package com.simibubi.create.content.equipment.clipboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides.ClipboardType;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClipboardScreen extends AbstractSimiScreen {

	public ItemStack item;
	public BlockPos targetedBlock;

	List<List<ClipboardEntry>> pages;
	List<ClipboardEntry> currentEntries;
	int editingIndex;
	int frameTick;
	PageButton forward;
	PageButton backward;
	int currentPage;
	long lastClickTime;
	int lastIndex = -1;

	int hoveredEntry;
	boolean hoveredCheck;
	boolean readonly;

	DisplayCache displayCache = DisplayCache.EMPTY;
	TextFieldHelper editContext;

	IconButton closeBtn;
	IconButton clearBtn;

	private int targetSlot;

	public ClipboardScreen(int targetSlot, ItemStack item, @Nullable BlockPos pos) {
		this.targetSlot = targetSlot;
		this.targetedBlock = pos;
		reopenWith(item);
	}

	public void reopenWith(ItemStack clipboard) {
		item = clipboard;
		pages = ClipboardEntry.readAll(item);
		if (pages.isEmpty())
			pages.add(new ArrayList<>());
		if (clearBtn == null) {
			currentPage = item.getTag() == null ? 0
				: item.getTag()
					.getInt("PreviouslyOpenedPage");
			currentPage = Mth.clamp(currentPage, 0, pages.size() - 1);
		}
		currentEntries = pages.get(currentPage);
		boolean startEmpty = currentEntries.isEmpty();
		if (startEmpty)
			currentEntries.add(new ClipboardEntry(false, Components.empty()));
		editingIndex = 0;
		editContext = new TextFieldHelper(this::getCurrentEntryText, this::setCurrentEntryText, this::getClipboard,
			this::setClipboard, this::validateTextForEntry);
		editingIndex = startEmpty ? 0 : -1;
		readonly = item.getTag() != null && item.getTag()
			.getBoolean("Readonly");
		if (readonly)
			editingIndex = -1;
		if (clearBtn != null)
			init();
	}

	@Override
	protected void init() {
		setWindowSize(256, 256);
		super.init();
		clearDisplayCache();

		int x = guiLeft;
		int y = guiTop - 8;

		clearWidgets();
		clearBtn = new IconButton(x + 234, y + 153, AllIcons.I_CLEAR_CHECKED).withCallback(() -> {
			editingIndex = -1;
			currentEntries.removeIf(ce -> ce.checked);
			if (currentEntries.isEmpty())
				currentEntries.add(new ClipboardEntry(false, Components.empty()));
			sendIfEditingBlock();
		});
		clearBtn.setToolTip(Lang.translateDirect("gui.clipboard.erase_checked"));
		closeBtn = new IconButton(x + 234, y + 175, AllIcons.I_PRIORITY_VERY_LOW)
			.withCallback(() -> minecraft.setScreen(null));
		closeBtn.setToolTip(Lang.translateDirect("station.close"));
		addRenderableWidget(closeBtn);
		addRenderableWidget(clearBtn);

		forward = new PageButton(x + 176, y + 229, true, $ -> changePage(true), true);
		backward = new PageButton(x + 53, y + 229, false, $ -> changePage(false), true);
		addRenderableWidget(forward);
		addRenderableWidget(backward);

		forward.visible = currentPage < 50 && (!readonly || currentPage + 1 < pages.size());
		backward.visible = currentPage > 0;
	}

	private int getNumPages() {
		return pages.size();
	}

	public void tick() {
		super.tick();
		frameTick++;

		if (targetedBlock != null) {
			if (!minecraft.player.blockPosition()
				.closerThan(targetedBlock, 10)) {
				removed();
				return;
			}
			if (!AllBlocks.CLIPBOARD.has(minecraft.level.getBlockState(targetedBlock))) {
				removed();
				return;
			}
		}

		int mx = (int) (this.minecraft.mouseHandler.xpos() * (double) this.minecraft.getWindow()
			.getGuiScaledWidth() / (double) this.minecraft.getWindow()
				.getScreenWidth());
		int my = (int) (this.minecraft.mouseHandler.ypos() * (double) this.minecraft.getWindow()
			.getGuiScaledHeight() / (double) this.minecraft.getWindow()
				.getScreenHeight());

		mx -= guiLeft + 35;
		my -= guiTop + 41;

		hoveredCheck = false;
		hoveredEntry = -1;

		if (mx > 0 && mx < 183 && my > 0 && my < 190) {
			hoveredCheck = mx < 20;
			int totalHeight = 0;
			for (int i = 0; i < currentEntries.size(); i++) {
				ClipboardEntry clipboardEntry = currentEntries.get(i);
				String text = clipboardEntry.text.getString();
				totalHeight +=
					Math.max(12, font.split(Components.literal(text), clipboardEntry.icon.isEmpty() ? 150 : 130)
						.size() * 9 + 3);

				if (totalHeight > my) {
					hoveredEntry = i;
					return;
				}
			}
			hoveredEntry = currentEntries.size();
		}
	}

	private String getCurrentEntryText() {
		return currentEntries.get(editingIndex).text.getString();
	}

	private void setCurrentEntryText(String text) {
		currentEntries.get(editingIndex).text = Components.literal(text);
		sendIfEditingBlock();
	}

	private void setClipboard(String p_98148_) {
		if (minecraft != null)
			TextFieldHelper.setClipboardContents(minecraft, p_98148_);
	}

	private String getClipboard() {
		return minecraft != null ? TextFieldHelper.getClipboardContents(minecraft) : "";
	}

	private boolean validateTextForEntry(String newText) {
		int totalHeight = 0;
		for (int i = 0; i < currentEntries.size(); i++) {
			ClipboardEntry clipboardEntry = currentEntries.get(i);
			String text = i == editingIndex ? newText : clipboardEntry.text.getString();
			totalHeight += Math.max(12, font.split(Components.literal(text), 150)
				.size() * 9 + 3);
		}
		return totalHeight < 185;
	}

	private int yOffsetOfEditingEntry() {
		int totalHeight = 0;
		for (int i = 0; i < currentEntries.size(); i++) {
			if (i == editingIndex)
				break;
			ClipboardEntry clipboardEntry = currentEntries.get(i);
			totalHeight += Math.max(12, font.split(clipboardEntry.text, 150)
				.size() * 9 + 3);
		}
		return totalHeight;
	}

	private void changePage(boolean next) {
		int previously = currentPage;
		currentPage = Mth.clamp(currentPage + (next ? 1 : -1), 0, 50);
		if (currentPage == previously)
			return;
		editingIndex = -1;
		if (pages.size() <= currentPage) {
			if (readonly) {
				currentPage = previously;
				return;
			}
			pages.add(new ArrayList<>());
		}
		currentEntries = pages.get(currentPage);
		if (currentEntries.isEmpty()) {
			currentEntries.add(new ClipboardEntry(false, Components.empty()));
			if (!readonly) {
				editingIndex = 0;
				editContext.setCursorToEnd();
				clearDisplayCacheAfterChange();
			}
		}

		forward.visible = currentPage < 50 && (!readonly || currentPage + 1 < pages.size());
		backward.visible = currentPage > 0;

		if (next)
			return;
		if (pages.get(currentPage + 1)
			.stream()
			.allMatch(ce -> ce.text.getString()
				.isBlank()))
			pages.remove(currentPage + 1);
	}

	@Override
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop - 8;

		AllGuiTextures.CLIPBOARD.render(ms, x, y);
		font.draw(ms, Components.translatable("book.pageIndicator", currentPage + 1, getNumPages()), x + 150, y + 9,
			0x43ffffff);

		for (int i = 0; i < currentEntries.size(); i++) {
			ClipboardEntry clipboardEntry = currentEntries.get(i);
			boolean checked = clipboardEntry.checked;
			int iconOffset = clipboardEntry.icon.isEmpty() ? 0 : 16;

			font.draw(ms, "\u25A1", x + 45, y + 51, checked ? 0x668D7F6B : 0xff8D7F6B);
			if (checked)
				font.draw(ms, "\u2714", x + 45, y + 50, 0x31B25D);

			List<FormattedCharSequence> split = font.split(clipboardEntry.text, 150 - iconOffset);
			if (split.isEmpty()) {
				y += 12;
				continue;
			}

			if (!clipboardEntry.icon.isEmpty())
				itemRenderer.renderGuiItem(clipboardEntry.icon, x + 54, y + 50);

			for (FormattedCharSequence sequence : split) {
				if (i != editingIndex)
					font.draw(ms, sequence, x + 58 + iconOffset, y + 50, checked ? 0x31B25D : 0x311A00);
				y += 9;
			}
			y += 3;
		}

		if (editingIndex == -1)
			return;

		boolean checked = currentEntries.get(editingIndex).checked;

		setFocused(null);
		DisplayCache cache = getDisplayCache();

		for (LineInfo line : cache.lines)
			font.draw(ms, line.asComponent, line.x, line.y, checked ? 0x31B25D : 0x311A00);

		renderHighlight(cache.selection);
		renderCursor(ms, cache.cursor, cache.cursorAtEnd);
	}

	@Override
	public void removed() {
		pages.forEach(list -> list.removeIf(ce -> ce.text.getString()
			.isBlank()));
		pages.removeIf(List::isEmpty);

		for (int i = 0; i < pages.size(); i++)
			if (pages.get(i) == currentEntries)
				item.getOrCreateTag()
					.putInt("PreviouslyOpenedPage", i);

		send();

		super.removed();
	}

	private void sendIfEditingBlock() {
		ClientPacketListener handler = minecraft.player.connection;
		if (handler.getOnlinePlayers()
			.size() > 1 && targetedBlock != null)
			send();
	}

	private void send() {
		ClipboardEntry.saveAll(pages, item);
		ClipboardOverrides.switchTo(ClipboardType.WRITTEN, item);
		if (pages.isEmpty())
			item.setTag(new CompoundTag());
		AllPackets.getChannel()
			.sendToServer(new ClipboardEditPacket(targetSlot, item.getOrCreateTag(), targetedBlock));
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		changePage(pDelta < 0);
		return true;
	}

	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		if (pKeyCode == 266) {
			backward.onPress();
			return true;
		}
		if (pKeyCode == 267) {
			forward.onPress();
			return true;
		}
		if (editingIndex != -1 && pKeyCode != 256) {
			keyPressedWhileEditing(pKeyCode, pScanCode, pModifiers);
			clearDisplayCache();
			return true;
		}
		if (super.keyPressed(pKeyCode, pScanCode, pModifiers))
			return true;
		return true;
	}

	@Override
	public boolean charTyped(char pCodePoint, int pModifiers) {
		if (super.charTyped(pCodePoint, pModifiers))
			return true;
		if (!SharedConstants.isAllowedChatCharacter(pCodePoint))
			return false;
		if (editingIndex == -1)
			return false;
		editContext.insertText(Character.toString(pCodePoint));
		clearDisplayCache();
		return true;
	}

	private boolean keyPressedWhileEditing(int pKeyCode, int pScanCode, int pModifiers) {
		if (Screen.isSelectAll(pKeyCode)) {
			editContext.selectAll();
			return true;
		} else if (Screen.isCopy(pKeyCode)) {
			editContext.copy();
			return true;
		} else if (Screen.isPaste(pKeyCode)) {
			editContext.paste();
			return true;
		} else if (Screen.isCut(pKeyCode)) {
			editContext.cut();
			return true;
		} else {
			switch (pKeyCode) {
			case 257:
			case 335:
				if (hasShiftDown()) {
					editContext.insertText("\n");
					return true;
				} else if (!hasControlDown()) {
					if (currentEntries.size() <= editingIndex + 1
						|| !currentEntries.get(editingIndex + 1).text.getString()
							.isEmpty())
						currentEntries.add(editingIndex + 1, new ClipboardEntry(false, Components.empty()));
					editingIndex += 1;
					editContext.setCursorToEnd();
					if (validateTextForEntry(" "))
						return true;
					currentEntries.remove(editingIndex);
					editingIndex -= 1;
					editContext.setCursorToEnd();
					return true;
				}
				editingIndex = -1;
				return true;
			case 259:
				if (currentEntries.get(editingIndex).text.getString()
					.isEmpty() && currentEntries.size() > 1) {
					currentEntries.remove(editingIndex);
					editingIndex = Math.max(0, editingIndex - 1);
					editContext.setCursorToEnd();
					return true;
				} else if (hasControlDown()) {
					int prevPos = editContext.getCursorPos();
					editContext.moveByWords(-1);
					if (prevPos != editContext.getCursorPos())
						editContext.removeCharsFromCursor(prevPos - editContext.getCursorPos());
					return true;
				}
				editContext.removeCharsFromCursor(-1);
				return true;
			case 261:
				if (hasControlDown()) {
					int prevPos = editContext.getCursorPos();
					editContext.moveByWords(1);
					if (prevPos != editContext.getCursorPos())
						editContext.removeCharsFromCursor(prevPos - editContext.getCursorPos());
					return true;
				}
				editContext.removeCharsFromCursor(1);
				return true;
			case 262:
				if (hasControlDown()) {
					editContext.moveByWords(1, Screen.hasShiftDown());
					return true;
				}
				editContext.moveByChars(1, Screen.hasShiftDown());
				return true;
			case 263:
				if (hasControlDown()) {
					editContext.moveByWords(-1, Screen.hasShiftDown());
					return true;
				}
				editContext.moveByChars(-1, Screen.hasShiftDown());
				return true;
			case 264:
				keyDown();
				return true;
			case 265:
				keyUp();
				return true;
			case 268:
				keyHome();
				return true;
			case 269:
				keyEnd();
				return true;
			default:
				return false;
			}
		}
	}

	private void keyUp() {
		changeLine(-1);
	}

	private void keyDown() {
		changeLine(1);
	}

	private void changeLine(int pYChange) {
		int i = editContext.getCursorPos();
		int j = getDisplayCache().changeLine(i, pYChange);
		editContext.setCursorPos(j, Screen.hasShiftDown());
	}

	private void keyHome() {
		int i = editContext.getCursorPos();
		int j = getDisplayCache().findLineStart(i);
		editContext.setCursorPos(j, Screen.hasShiftDown());
	}

	private void keyEnd() {
		DisplayCache cache = getDisplayCache();
		int i = editContext.getCursorPos();
		int j = cache.findLineEnd(i);
		editContext.setCursorPos(j, Screen.hasShiftDown());
	}

	private void renderCursor(PoseStack pPoseStack, Pos2i pCursorPos, boolean pIsEndOfText) {
		if (frameTick / 6 % 2 != 0)
			return;
		pCursorPos = convertLocalToScreen(pCursorPos);
		if (!pIsEndOfText) {
			GuiComponent.fill(pPoseStack, pCursorPos.x, pCursorPos.y - 1, pCursorPos.x + 1, pCursorPos.y + 9,
				-16777216);
		} else {
			font.draw(pPoseStack, "_", (float) pCursorPos.x, (float) pCursorPos.y, 0);
		}
	}

	private void renderHighlight(Rect2i[] pSelected) {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tesselator.getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionShader);
		RenderSystem.setShaderColor(0.0F, 0.0F, 255.0F, 255.0F);
		RenderSystem.disableTexture();
		RenderSystem.enableColorLogicOp();
		RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

		for (Rect2i rect2i : pSelected) {
			int i = rect2i.getX();
			int j = rect2i.getY();
			int k = i + rect2i.getWidth();
			int l = j + rect2i.getHeight();
			bufferbuilder.vertex((double) i, (double) l, 0.0D)
				.endVertex();
			bufferbuilder.vertex((double) k, (double) l, 0.0D)
				.endVertex();
			bufferbuilder.vertex((double) k, (double) j, 0.0D)
				.endVertex();
			bufferbuilder.vertex((double) i, (double) j, 0.0D)
				.endVertex();
		}

		tesselator.end();
		RenderSystem.disableColorLogicOp();
		RenderSystem.enableTexture();
	}

	private Pos2i convertScreenToLocal(Pos2i pScreenPos) {
		return new Pos2i(pScreenPos.x - (width - 192) / 2 - 36 + 10,
			pScreenPos.y - 32 - 24 - yOffsetOfEditingEntry() - guiTop + 14);
	}

	private Pos2i convertLocalToScreen(Pos2i pLocalScreenPos) {
		return new Pos2i(pLocalScreenPos.x + (width - 192) / 2 + 36 - 10,
			pLocalScreenPos.y + 32 + 24 + yOffsetOfEditingEntry() + guiTop - 14);
	}

	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		if (super.mouseClicked(pMouseX, pMouseY, pButton))
			return true;
		if (pButton != 0)
			return true;

		if (hoveredEntry != -1) {
			if (hoveredCheck) {
				editingIndex = -1;
				if (hoveredEntry < currentEntries.size())
					currentEntries.get(hoveredEntry).checked ^= true;
				sendIfEditingBlock();
				return true;
			}

			if (hoveredEntry != editingIndex && !readonly) {
				editingIndex = hoveredEntry;
				if (hoveredEntry >= currentEntries.size()) {
					currentEntries.add(new ClipboardEntry(false, Components.empty()));
					if (!validateTextForEntry(" ")) {
						currentEntries.remove(hoveredEntry);
						editingIndex = -1;
						return true;
					}
				}
				clearDisplayCacheAfterChange();
			}
		}

		if (editingIndex == -1)
			return false;

		long i = Util.getMillis();
		DisplayCache cache = getDisplayCache();
		int j = cache.getIndexAtPosition(font, convertScreenToLocal(new Pos2i((int) pMouseX, (int) pMouseY)));
		if (j >= 0) {
			if (j == lastIndex && i - lastClickTime < 250L) {
				if (!editContext.isSelecting()) {
					selectWord(j);
				} else {
					editContext.selectAll();
				}
			} else {
				editContext.setCursorPos(j, Screen.hasShiftDown());
			}

			clearDisplayCache();
		}

		lastIndex = j;
		lastClickTime = i;
		return true;
	}

	private void selectWord(int pIndex) {
		String s = getCurrentEntryText();
		editContext.setSelectionRange(StringSplitter.getWordPosition(s, -1, pIndex, false),
			StringSplitter.getWordPosition(s, 1, pIndex, false));
	}

	public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
		if (super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY))
			return true;
		if (pButton != 0)
			return true;
		if (editingIndex == -1)
			return false;

		DisplayCache cache = getDisplayCache();
		int i = cache.getIndexAtPosition(font, convertScreenToLocal(new Pos2i((int) pMouseX, (int) pMouseY)));
		editContext.setCursorPos(i, true);
		clearDisplayCache();
		return true;
	}

	private DisplayCache getDisplayCache() {
		if (displayCache == null)
			displayCache = rebuildDisplayCache();
		return displayCache;
	}

	private void clearDisplayCache() {
		displayCache = null;
	}

	private void clearDisplayCacheAfterChange() {
		editContext.setCursorToEnd();
		clearDisplayCache();
	}

	private DisplayCache rebuildDisplayCache() {
		String s = getCurrentEntryText();
		if (s.isEmpty())
			return DisplayCache.EMPTY;

		int i = editContext.getCursorPos();
		int j = editContext.getSelectionPos();
		IntList intlist = new IntArrayList();
		List<LineInfo> list = Lists.newArrayList();
		MutableInt mutableint = new MutableInt();
		MutableBoolean mutableboolean = new MutableBoolean();
		StringSplitter stringsplitter = font.getSplitter();
		stringsplitter.splitLines(s, 150, Style.EMPTY, true, (p_98132_, p_98133_, p_98134_) -> {
			int k3 = mutableint.getAndIncrement();
			String s2 = s.substring(p_98133_, p_98134_);
			mutableboolean.setValue(s2.endsWith("\n"));
			String s3 = StringUtils.stripEnd(s2, " \n");
			int l3 = k3 * 9;
			Pos2i pos1 = convertLocalToScreen(new Pos2i(0, l3));
			intlist.add(p_98133_);
			list.add(new LineInfo(p_98132_, s3, pos1.x, pos1.y));
		});

		int[] aint = intlist.toIntArray();
		boolean flag = i == s.length();
		Pos2i pos;
		if (flag && mutableboolean.isTrue()) {
			pos = new Pos2i(0, list.size() * 9);
		} else {
			int k = findLineFromPos(aint, i);
			int l = font.width(s.substring(aint[k], i));
			pos = new Pos2i(l, k * 9);
		}

		List<Rect2i> list1 = Lists.newArrayList();
		if (i != j) {
			int l2 = Math.min(i, j);
			int i1 = Math.max(i, j);
			int j1 = findLineFromPos(aint, l2);
			int k1 = findLineFromPos(aint, i1);
			if (j1 == k1) {
				int l1 = j1 * 9;
				int i2 = aint[j1];
				list1.add(createPartialLineSelection(s, stringsplitter, l2, i1, l1, i2));
			} else {
				int i3 = j1 + 1 > aint.length ? s.length() : aint[j1 + 1];
				list1.add(createPartialLineSelection(s, stringsplitter, l2, i3, j1 * 9, aint[j1]));

				for (int j3 = j1 + 1; j3 < k1; ++j3) {
					int j2 = j3 * 9;
					String s1 = s.substring(aint[j3], aint[j3 + 1]);
					int k2 = (int) stringsplitter.stringWidth(s1);
					list1.add(createSelection(new Pos2i(0, j2), new Pos2i(k2, j2 + 9)));
				}

				list1.add(createPartialLineSelection(s, stringsplitter, aint[k1], i1, k1 * 9, aint[k1]));
			}
		}

		return new DisplayCache(s, pos, flag, aint, list.toArray(new LineInfo[0]), list1.toArray(new Rect2i[0]));
	}

	static int findLineFromPos(int[] pLineStarts, int pFind) {
		int i = Arrays.binarySearch(pLineStarts, pFind);
		return i < 0 ? -(i + 2) : i;
	}

	private Rect2i createPartialLineSelection(String pInput, StringSplitter pSplitter, int p_98122_, int p_98123_,
		int p_98124_, int p_98125_) {
		String s = pInput.substring(p_98125_, p_98122_);
		String s1 = pInput.substring(p_98125_, p_98123_);
		Pos2i firstPos = new Pos2i((int) pSplitter.stringWidth(s), p_98124_);
		Pos2i secondPos = new Pos2i((int) pSplitter.stringWidth(s1), p_98124_ + 9);
		return createSelection(firstPos, secondPos);
	}

	private Rect2i createSelection(Pos2i pCorner1, Pos2i pCorner2) {
		Pos2i firstPos = convertLocalToScreen(pCorner1);
		Pos2i secondPos = convertLocalToScreen(pCorner2);
		int i = Math.min(firstPos.x, secondPos.x);
		int j = Math.max(firstPos.x, secondPos.x);
		int k = Math.min(firstPos.y, secondPos.y);
		int l = Math.max(firstPos.y, secondPos.y);
		return new Rect2i(i, k, j - i, l - k);
	}

	@OnlyIn(Dist.CLIENT)
	static class DisplayCache {
		static final DisplayCache EMPTY = new DisplayCache("", new Pos2i(0, 0), true, new int[] { 0 },
			new LineInfo[] { new LineInfo(Style.EMPTY, "", 0, 0) }, new Rect2i[0]);
		private final String fullText;
		final Pos2i cursor;
		final boolean cursorAtEnd;
		private final int[] lineStarts;
		final LineInfo[] lines;
		final Rect2i[] selection;

		public DisplayCache(String pFullText, Pos2i pCursor, boolean pCursorAtEnd, int[] pLineStarts, LineInfo[] pLines,
			Rect2i[] pSelection) {
			fullText = pFullText;
			cursor = pCursor;
			cursorAtEnd = pCursorAtEnd;
			lineStarts = pLineStarts;
			lines = pLines;
			selection = pSelection;
		}

		public int getIndexAtPosition(Font pFont, Pos2i pCursorPosition) {
			int i = pCursorPosition.y / 9;
			if (i < 0)
				return 0;
			if (i >= lines.length)
				return fullText.length();
			LineInfo line = lines[i];
			return lineStarts[i] + pFont.getSplitter()
				.plainIndexAtWidth(line.contents, pCursorPosition.x, line.style);
		}

		public int changeLine(int pXChange, int pYChange) {
			int i = findLineFromPos(lineStarts, pXChange);
			int j = i + pYChange;
			int k;
			if (0 <= j && j < lineStarts.length) {
				int l = pXChange - lineStarts[i];
				int i1 = lines[j].contents.length();
				k = lineStarts[j] + Math.min(l, i1);
			} else {
				k = pXChange;
			}

			return k;
		}

		public int findLineStart(int pLine) {
			int i = findLineFromPos(lineStarts, pLine);
			return lineStarts[i];
		}

		public int findLineEnd(int pLine) {
			int i = findLineFromPos(lineStarts, pLine);
			return lineStarts[i] + lines[i].contents.length();
		}
	}

	@OnlyIn(Dist.CLIENT)
	static class LineInfo {
		final Style style;
		final String contents;
		final Component asComponent;
		final int x;
		final int y;

		public LineInfo(Style pStyle, String pContents, int pX, int pY) {
			style = pStyle;
			contents = pContents;
			x = pX;
			y = pY;
			asComponent = Components.literal(pContents)
				.setStyle(pStyle);
		}
	}

	@OnlyIn(Dist.CLIENT)
	static class Pos2i {
		public final int x;
		public final int y;

		Pos2i(int pX, int pY) {
			x = pX;
			y = pY;
		}
	}

}
