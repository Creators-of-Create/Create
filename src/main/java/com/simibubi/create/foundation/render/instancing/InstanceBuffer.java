package com.simibubi.create.foundation.render.instancing;


import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.render.GPUBuffer;
import com.simibubi.create.foundation.render.RenderMath;
import com.simibubi.create.foundation.render.RenderWork;
import com.simibubi.create.foundation.render.TemplateBuffer;
import net.minecraft.client.renderer.BufferBuilder;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.function.Consumer;

import static com.simibubi.create.foundation.render.instancing.VertexAttribute.*;

public abstract class InstanceBuffer<D extends InstanceData> extends GPUBuffer {
    public static final VertexFormat FORMAT = new VertexFormat(POSITION, NORMAL, UV);

    protected int instanceVBO;
    protected int instanceCount;

    protected int instanceBufferSize = -1;

    protected final ArrayList<D> data = new ArrayList<>();
    protected boolean rebuffer = false;
    protected boolean shouldBuild = true;

    public InstanceBuffer(BufferBuilder buf) {
        super(buf);
    }

    @Override
    protected void setup() {
        super.setup();
        instanceVBO = GlStateManager.genBuffers();
    }

    @Override
    protected VertexFormat getModelFormat() {
        return FORMAT;
    }

    @Override
    protected void copyVertex(ByteBuffer constant, int i) {
        constant.putFloat(getX(template, i));
        constant.putFloat(getY(template, i));
        constant.putFloat(getZ(template, i));

        constant.put(getNX(template, i));
        constant.put(getNY(template, i));
        constant.put(getNZ(template, i));

        constant.putFloat(getU(template, i));
        constant.putFloat(getV(template, i));
    }

    protected abstract VertexFormat getInstanceFormat();

    public int numInstances() {
        return instanceCount + data.size();
    }

    public boolean isEmpty() {
        return numInstances() == 0;
    }

    public void clearInstanceData() {
        instanceCount = 0;
        shouldBuild = true;
    }

    public void markDirty() {
        if (shouldBuild) rebuffer = true;
    }

    protected void deleteInternal() {
        GL15.glDeleteBuffers(invariantVBO);
        GL15.glDeleteBuffers(instanceVBO);
        GL15.glDeleteBuffers(ebo);
        GL30.glDeleteVertexArrays(vao);
        vao = 0;
        ebo = 0;
        invariantVBO = 0;
        instanceVBO = 0;
        instanceBufferSize = -1;
    }

    protected abstract D newInstance();

    public void setupInstance(Consumer<D> setup) {
        if (!shouldBuild) return;

        D instanceData = newInstance();
        setup.accept(instanceData);

        data.add(instanceData);
    }

    protected int getTotalShaderAttributeCount() {
        return getInstanceFormat().getShaderAttributeCount() + FORMAT.getShaderAttributeCount();
    }

    @Override
    protected void drawCall() {
        GL40.glDrawElementsInstanced(GL11.GL_QUADS, vertexCount, GL11.GL_UNSIGNED_SHORT, 0, instanceCount);
    }

    protected void preDrawTask() {
        if (!rebuffer || data.isEmpty()) return;

        instanceCount = data.size();

        VertexFormat instanceFormat = getInstanceFormat();

        int instanceSize = RenderMath.nextPowerOf2(instanceCount * instanceFormat.getStride());

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, instanceVBO);

        // this changes enough that it's not worth reallocating the entire buffer every time.
        if (instanceSize > instanceBufferSize) {
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, instanceSize, GL15.GL_STATIC_DRAW);
            instanceBufferSize = instanceSize;
        }

        ByteBuffer buffer = GL15.glMapBuffer(GL15.GL_ARRAY_BUFFER, GL15.GL_WRITE_ONLY);

        data.forEach(instanceData -> instanceData.write(buffer));
        buffer.rewind();
        GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);

        int staticAttributes = FORMAT.getShaderAttributeCount();
        instanceFormat.informAttributes(staticAttributes);

        for (int i = 0; i < instanceFormat.getShaderAttributeCount(); i++) {
            GL33.glVertexAttribDivisor(i + staticAttributes, 1);
        }

        // Deselect (bind to 0) the VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        shouldBuild = false;
        rebuffer = false;
        data.clear();
    }
}
