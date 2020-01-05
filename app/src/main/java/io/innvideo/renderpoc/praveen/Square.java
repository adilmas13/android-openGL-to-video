package io.innvideo.renderpoc.praveen;

import android.opengl.GLES10;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Square {

    private FloatBuffer mVertexBuffer;

    private float[] vertices = {
            -1.0f, -1.0f, 0.0f,
            -1.0f, -1f, 0.0f,
            1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f
    };

    public Square() {
        ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        vertexByteBuffer.order(ByteOrder.nativeOrder());
        mVertexBuffer = vertexByteBuffer.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);
    }

    public void draw(GL10 gl) {
        gl.glEnableClientState(GLES10.GL_VERTEX_ARRAY);
        gl.glColor4f(0.0f, 1.0f, 0.0f, 0.5f);
        gl.glVertexPointer(3, GLES10.GL_FLAT, 0, mVertexBuffer);
        gl.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);
        gl.glDisableClientState(GLES10.GL_VERTEX_ARRAY);
    }
}
