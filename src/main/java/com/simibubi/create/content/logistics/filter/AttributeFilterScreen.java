package com.simibubi.create.content.logistics.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.logistics.filter.FilterScreenPacket.Option;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class AttributeFilterScreen extends AbstractFilterScreen<AttributeFilterMenu> {

	private static final String PREFIX = "gui.attribute_filter.";

	private Component addDESC = CreateLang.translateDirect(PREFIX + "add_attribute");
	private Component addInvertedDESC = CreateLang.translateDirect(PREFIX + "add_inverted_attribute");

	private Component allowDisN = CreateLang.translateDirect(PREFIX + "allow_list_disjunctive");
	private Component allowDisDESC = CreateLang.translateDirect(PREFIX + "allow_list_disjunctive.description");
	private Component allowConN = CreateLang.translateDirect(PREFIX + "allow_list_conjunctive");
	private Component allowConDESC = CreateLang.translateDirect(PREFIX + "allow_list_conjunctive.description");
	private Component denyN = CreateLang.translateDirect(PREFIX + "deny_list");
	private Component denyDESC = CreateLang.translateDirect(PREFIX + "deny_list.description");

	private Component referenceH = CreateLang.translateDirect(PREFIX + "add_reference_item");
	private Component noSelectedT = CreateLang.translateDirect(PREFIX + "no_selected_attributes");
	private Component selectedT = CreateLang.translateDirect(PREFIX + "selected_attributes");

	private IconButton whitelistDis, whitelistCon, blacklist;
	private IconButton add;
	private IconButton addInverted;

	private ItemStack lastItemScanned = ItemStack.EMPTY;
	private List<ItemAttribute> attributesOfItem = new ArrayList<>();
	private List<Component> selectedAttributes = new ArrayList<>();
	private SelectionScrollInput attributeSelector;
	private Label attributeSelectorLabel;

	public AttributeFilterScreen(AttributeFilterMenu menu, Inventory inv, Component title) {
		super(menu, inv, title, AllGuiTextures.ATTRIBUTE_FILTER);
	}

	@Override
	protected void init() {
		setWindowOffset(-11, 7);
		super.init();

		int x = leftPos;
		int y = topPos;

		whitelistDis = new IconButton(x + 38, y + 61, AllIcons.I_WHITELIST_OR);
		whitelistDis.withCallback(() -> {
			menu.whitelistMode = AttributeFilterWhitelistMode.WHITELIST_DISJ;
			sendOptionUpdate(Option.WHITELIST);
		});
		whitelistDis.setToolTip(allowDisN);
		whitelistCon = new IconButton(x + 56, y + 61, AllIcons.I_WHITELIST_AND);
		whitelistCon.withCallback(() -> {
			menu.whitelistMode = AttributeFilterWhitelistMode.WHITELIST_CONJ;
			sendOptionUpdate(Option.WHITELIST2);
		});
		whitelistCon.setToolTip(allowConN);
		blacklist = new IconButton(x + 74, y + 61, AllIcons.I_WHITELIST_NOT);
		blacklist.withCallback(() -> {
			menu.whitelistMode = AttributeFilterWhitelistMode.BLACKLIST;
			sendOptionUpdate(Option.BLACKLIST);
		});
		blacklist.setToolTip(denyN);

		addRenderableWidgets(blacklist, whitelistCon, whitelistDis);

		addRenderableWidget(add = new IconButton(x + 182, y + 26, AllIcons.I_ADD));
		addRenderableWidget(addInverted = new IconButton(x + 200, y + 26, AllIcons.I_ADD_INVERTED_ATTRIBUTE));
		add.withCallback(() -> {
			handleAddedAttibute(false);
		});
		add.setToolTip(addDESC);
		addInverted.withCallback(() -> {
			handleAddedAttibute(true);
		});
		addInverted.setToolTip(addInvertedDESC);

		handleIndicators();

		attributeSelectorLabel = new Label(x + 43, y + 31, CommonComponents.EMPTY).colored(0xF3EBDE)
			.withShadow();
		attributeSelector = new SelectionScrollInput(x + 39, y + 26, 137, 18);
		attributeSelector.forOptions(Arrays.asList(CommonComponents.EMPTY));
		attributeSelector.removeCallback();
		referenceItemChanged(menu.ghostInventory.getStackInSlot(0));

		addRenderableWidget(attributeSelector);
		addRenderableWidget(attributeSelectorLabel);

		selectedAttributes.clear();
		selectedAttributes.add((menu.selectedAttributes.isEmpty() ? noSelectedT : selectedT).plainCopy()
			.withStyle(ChatFormatting.YELLOW));
		menu.selectedAttributes.forEach(at -> {
			selectedAttributes.add(Component.literal("- ")
				.append(at.attribute()
					.format(at.inverted()))
				.withStyle(ChatFormatting.GRAY));
		});
	}

	private void referenceItemChanged(ItemStack stack) {
		HolderLookup.Provider registries = Minecraft.getInstance().level.registryAccess();
		lastItemScanned = stack;

		if (stack.isEmpty()) {
			attributeSelector.active = false;
			attributeSelector.visible = false;
			attributeSelectorLabel.text = referenceH.plainCopy()
				.withStyle(ChatFormatting.ITALIC);
			add.active = false;
			addInverted.active = false;
			attributeSelector.calling(s -> {
			});
			return;
		}

		add.active = true;

		addInverted.active = true;
		attributeSelector.titled(CreateLang.text(stack.getHoverName()
			.getString() + "...")
			.color(ScrollInput.HEADER_RGB.getRGB())
			.component());
		attributesOfItem.clear();
		for (ItemAttributeType type : CreateBuiltInRegistries.ITEM_ATTRIBUTE_TYPE)
			attributesOfItem.addAll(type.getAllAttributes(stack, minecraft.level));
		List<Component> options = attributesOfItem.stream()
			.map(a -> a.format(false))
			.collect(Collectors.toList());
		attributeSelector.forOptions(options);
		attributeSelector.active = true;
		attributeSelector.visible = true;
		attributeSelector.setState(0);
		attributeSelector.calling(i -> {
			attributeSelectorLabel.setTextAndTrim(options.get(i), true, 112);
			ItemAttribute selected = attributesOfItem.get(i);
			for (ItemAttribute.ItemAttributeEntry existing : menu.selectedAttributes) {
				CompoundTag testTag = ItemAttribute.saveStatic(existing.attribute(), registries);
				CompoundTag testTag2 = ItemAttribute.saveStatic(selected, registries);
				if (testTag.equals(testTag2)) {
					add.active = false;
					addInverted.active = false;
					return;
				}
			}
			add.active = true;
			addInverted.active = true;
		});
		attributeSelector.onChanged();
	}

	@Override
	public void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		ItemStack stack = menu.ghostInventory.getStackInSlot(1);
		PoseStack matrixStack = graphics.pose();
		matrixStack.pushPose();
		matrixStack.translate(0, 0, 150);
		graphics.renderItemDecorations(font, stack, leftPos + 16, topPos + 62,
			String.valueOf(selectedAttributes.size() - 1));
		matrixStack.popPose();

		super.renderForeground(graphics, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		ItemStack stackInSlot = menu.ghostInventory.getStackInSlot(0);
		if (!ItemStack.matches(stackInSlot, lastItemScanned))
			referenceItemChanged(stackInSlot);
	}

	@Override
	protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
		if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
			if (this.hoveredSlot.index == 37) {
				graphics.renderComponentTooltip(font, selectedAttributes, mouseX, mouseY);
				return;
			}
			graphics.renderTooltip(font, this.hoveredSlot.getItem(), mouseX, mouseY);
		}
		super.renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	protected List<IconButton> getTooltipButtons() {
		return Arrays.asList(blacklist, whitelistCon, whitelistDis);
	}

	@Override
	protected List<MutableComponent> getTooltipDescriptions() {
		return Arrays.asList(denyDESC.plainCopy(), allowConDESC.plainCopy(), allowDisDESC.plainCopy());
	}

	protected boolean handleAddedAttibute(boolean inverted) {
		int index = attributeSelector.getState();
		if (index >= attributesOfItem.size())
			return false;
		add.active = false;
		addInverted.active = false;
		ItemAttribute itemAttribute = attributesOfItem.get(index);
		CompoundTag tag = ItemAttribute.saveStatic(itemAttribute, Minecraft.getInstance().level.registryAccess());
		CatnipServices.NETWORK.sendToServer(new FilterScreenPacket(inverted ? Option.ADD_INVERTED_TAG : Option.ADD_TAG, tag));
		menu.appendSelectedAttribute(itemAttribute, inverted);
		if (menu.selectedAttributes.size() == 1)
			selectedAttributes.set(0, selectedT.plainCopy()
				.withStyle(ChatFormatting.YELLOW));
		selectedAttributes.add(Component.literal("- ").append(itemAttribute.format(inverted))
			.withStyle(ChatFormatting.GRAY));
		return true;
	}

	@Override
	protected void contentsCleared() {
		selectedAttributes.clear();
		selectedAttributes.add(noSelectedT.plainCopy()
			.withStyle(ChatFormatting.YELLOW));
		if (!lastItemScanned.isEmpty()) {
			add.active = true;
			addInverted.active = true;
		}
	}

	@Override
	protected boolean isButtonEnabled(IconButton button) {
		if (button == blacklist)
			return menu.whitelistMode != AttributeFilterWhitelistMode.BLACKLIST;
		if (button == whitelistCon)
			return menu.whitelistMode != AttributeFilterWhitelistMode.WHITELIST_CONJ;
		if (button == whitelistDis)
			return menu.whitelistMode != AttributeFilterWhitelistMode.WHITELIST_DISJ;
		return true;
	}

}
