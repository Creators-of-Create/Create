package com.simibubi.create.foundation.render.instancing;


import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.render.RenderMath;
import com.simibubi.create.foundation.render.RenderWork;
import com.simibubi.create.foundation.render.TemplateBuffer;
import net.minecraft.client.renderer.BufferBuilder;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.function.Consumer;

import static com.simibubi.create.foundation.render.instancing.VertexAttribute.*;

public abstract class InstanceBuffer<D extends InstanceData> extends TemplateBuffer {
    public static final VertexFormat FORMAT = new VertexFormat(POSITION, NORMAL, UV);

    protected int vao, ebo, invariantVBO, instanceVBO, instanceCount;

    protected int bufferSize = -1;

    protected final ArrayList<D> data = new ArrayList<>();
    protected boolean rebuffer = false;
    protected boolean shouldBuild = true;

    public InstanceBuffer(BufferBuilder buf) {
        super(buf);
        if (vertexCount > 0) setup();
    }

    private void setup() {
        int stride = FORMAT.getStride();

        int invariantSize = vertexCount * stride;

        vao = GL30.glGenVertexArrays();
        ebo = GlStateManager.genBuffers();
        invariantVBO = GlStateManager.genBuffers();
        instanceVBO = GlStateManager.genBuffers();

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, invariantVBO);

        // allocate the buffer on the gpu
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, invariantSize, GL15.GL_STATIC_DRAW);

        // mirror it in system memory so we can write to it
        ByteBuffer constant = GL15.glMapBuffer(GL15.GL_ARRAY_BUFFER, GL15.GL_WRITE_ONLY);

        for (int i = 0; i < vertexCount; i++) {
            constant.putFloat(getX(template, i));
            constant.putFloat(getY(template, i));
            constant.putFloat(getZ(template, i));

            constant.put(getNX(template, i));
            constant.put(getNY(template, i));
            constant.put(getNZ(template, i));

            constant.putFloat(getU(template, i));
            constant.putFloat(getV(template, i));
        }
        constant.rewind();
        GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);

        buildEBO(ebo);

        FORMAT.informAttributes(0);

        GlStateManager.bindBuffers(GL15.GL_ARRAY_BUFFER, 0);
        GlStateManager.bindBuffers(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        // Deselect (bind to 0) the VAO
        GL30.glBindVertexArray(0);
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

    public void delete() {
        if (vertexCount > 0) {
            RenderWork.enqueue(() -> {
                GL15.glDeleteBuffers(invariantVBO);
                GL15.glDeleteBuffers(instanceVBO);
                GL15.glDeleteBuffers(ebo);
                GL30.glDeleteVertexArrays(vao);
                vao = 0;
                ebo = 0;
                invariantVBO = 0;
                instanceVBO = 0;
                bufferSize = -1;
            });
        }
    }

    protected abstract D newInstance();

    public void setupInstance(Consumer<D> setup) {
        if (!shouldBuild) return;

        D instanceData = newInstance();
        setup.accept(instanceData);

        data.add(instanceData);
    }

    public void render() {
        if (vao == 0) return;

        GL30.glBindVertexArray(vao);
        finishBuffering();

        int numAttributes = getInstanceFormat().getNumAttributes() + FORMAT.getNumAttributes();
        for (int i = 0; i <= numAttributes; i++) {
            GL40.glEnableVertexAttribArray(i);
        }

        GlStateManager.bindBuffers(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);

        GL40.glDrawElementsInstanced(GL11.GL_QUADS, vertexCount, GL11.GL_UNSIGNED_SHORT, 0, instanceCount);

        for (int i = 0; i <= numAttributes; i++) {
            GL40.glDisableVertexAttribArray(i);
        }

        GlStateManager.bindBuffers(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    private void finishBuffering() {
        if (!rebuffer || data.isEmpty()) return;

        instanceCount = data.size();

        VertexFormat instanceFormat = getInstanceFormat();

        int instanceSize = RenderMath.nextPowerOf2(instanceCount * instanceFormat.getStride());

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, instanceVBO);

        // this changes enough that it's not worth reallocating the entire buffer every time.
        if (instanceSize > bufferSize) {
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, instanceSize, GL15.GL_STATIC_DRAW);
            bufferSize = instanceSize;
        }

        ByteBuffer buffer = GL15.glMapBuffer(GL15.GL_ARRAY_BUFFER, GL15.GL_WRITE_ONLY);

        data.forEach(instanceData -> instanceData.write(buffer));
        buffer.rewind();
        GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);

        int staticAttributes = FORMAT.getNumAttributes();
        instanceFormat.informAttributes(staticAttributes);

        for (int i = 0; i < instanceFormat.getNumAttributes(); i++) {
            GL33.glVertexAttribDivisor(i + staticAttributes, 1);
        }

        // Deselect (bind to 0) the VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        shouldBuild = false;
        rebuffer = false;
        data.clear();
    }
}
