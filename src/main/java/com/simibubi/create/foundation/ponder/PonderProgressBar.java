package com.simibubi.create.foundation.ponder;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.widgets.AbstractSimiWidget;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.LerpedFloat;
import net.minecraft.client.audio.SoundHandler;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.antlr.v4.runtime.misc.IntegerList;

public class PonderProgressBar extends AbstractSimiWidget {

    LerpedFloat progress;
    LerpedFloat flash;

    PonderUI ponder;

    public PonderProgressBar(PonderUI ponder, int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn);

        this.ponder = ponder;
        progress = LerpedFloat.linear()
                              .startWithValue(0);
        flash = LerpedFloat.linear()
                           .startWithValue(0);
    }

    public void tick() {
        progress.chase(ponder.getActiveScene().getSceneProgress(), .5f, LerpedFloat.Chaser.EXP);
        progress.tickChaser();

        if (isHovered)
            flash();
    }

    public void flash() {
        float value = flash.getValue();
        flash.setValue(value + (1 - value) * .2f);
    }

    public void dim() {
        float value = flash.getValue();
        flash.setValue(value * .5f);
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return this.active && this.visible &&
                !ponder.getActiveScene().keyframeTimes.isEmpty() &&
                mouseX >= (double)this.x &&
                mouseX < (double)(this.x + this.width) &&
                mouseY >= (double)this.y - 3 &&
                mouseY < (double)(this.y + this.height + 3);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        PonderScene activeScene = ponder.getActiveScene();
        int clickedAtTime = (int) ((mouseX - x) / ((double) width) * activeScene.totalTime);

        int seekTime = 0;

        IntegerList keyframeTimes = activeScene.keyframeTimes;
        for (int i = 0; i < keyframeTimes.size(); i++) {
            int keyframeTime = keyframeTimes.get(i);

            if (keyframeTime > clickedAtTime)
                break;

            seekTime = keyframeTime;
        }
        ponder.seekToTime(seekTime);
    }

    public int getHoveredKeyframeIndex(double mouseX) {
        PonderScene activeScene = ponder.getActiveScene();
        int clickedAtTime = (int) ((mouseX - x) / ((double) width) * activeScene.totalTime);

        int index = -1;

        IntegerList keyframeTimes = activeScene.keyframeTimes;
        for (int i = 0; i < keyframeTimes.size(); i++) {
            int keyframeTime = keyframeTimes.get(i);

            if (keyframeTime > clickedAtTime)
                break;

            index = i;
        }

        return index;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {

        isHovered = clicked(mouseX, mouseY);

        PonderUI.renderBox(x, y, width, height, false);
        RenderSystem.pushMatrix();
        RenderSystem.translated(x - 2, y - 2, 0);

        RenderSystem.pushMatrix();
        RenderSystem.scaled((width + 4) * progress.getValue(partialTicks), 1, 1);
        GuiUtils.drawGradientRect(200, 0, 3, 1, 4, 0x60ffeedd, 0x60ffeedd);
        RenderSystem.popMatrix();

        renderKeyframes(mouseX, partialTicks);

        RenderSystem.popMatrix();
    }

    private void renderKeyframes(int mouseX, float partialTicks) {
        PonderScene activeScene = ponder.getActiveScene();

        int hoverStartColor;
        int hoverEndColor;
        int hoverIndex;
        if (isHovered) {
            hoverIndex = getHoveredKeyframeIndex(mouseX);

            float flashValue = flash.getValue(partialTicks) * 3 + (float) Math.sin((AnimationTickHolder.getTicks() + partialTicks) / 6);

            hoverEndColor = ColorHelper.applyAlpha(0x70ffffff, flashValue);
            hoverStartColor = ColorHelper.applyAlpha(0x30ffffff, flashValue);
        }
        else {
            hoverIndex = -1;
            hoverEndColor = 0;
            hoverStartColor = 0;
        }

        IntegerList keyframeTimes = activeScene.keyframeTimes;
        for (int i = 0; i < keyframeTimes.size(); i++) {
            int keyframeTime = keyframeTimes.get(i);

            int startColor = i == hoverIndex ? hoverStartColor : 0x60ffeedd;
            int endColor = i == hoverIndex ? hoverEndColor : 0x60ffeedd;

            int keyframePos = (int) (((float) keyframeTime) / ((float) activeScene.totalTime) * (width + 4));
            GuiUtils.drawGradientRect(200, keyframePos, 1, keyframePos + 1, 4, startColor, endColor);
        }
    }

    @Override
    public void playDownSound(SoundHandler handler) {

    }
}
