package shape;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.example.skq.demo.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import utils.ShaderHelper;
import utils.TextResourceReader;

public class Triangle {
    private Context context;
    private int program;

    private static final String A_POSITION = "a_Position";
    private static final String U_COLOR = "u_Color";

    public int uPosHandle;
    public int aTexHandle;
    public int mMVPMatrixHandle;

    private FloatBuffer mPosBuffer;
    private FloatBuffer mTexBuffer;
    private float[] mPosCoordinate = {-1, -1, -1, 1, 1, -1, 1, 1};
    private float[] mTexCoordinateBackRight = {1, 1, 0, 1, 1, 0, 0, 0};//顺时针转90并沿Y轴翻转  后摄像头正确，前摄像头上下颠倒
    private float[] mTexCoordinateForntRight = {0, 1, 1, 1, 0, 0, 1, 0};//顺时针旋转90  后摄像头上下颠倒了，前摄像头正确


    private float[] mProjectMatrix = new float[16];
    private float[] mCameraMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private float[] mTempMatrix = new float[16];

    public Triangle(Context context){
        this.context = context;

        Matrix.setIdentityM(mProjectMatrix, 0);
        Matrix.setIdentityM(mCameraMatrix, 0);
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.setIdentityM(mTempMatrix, 0);

        getProgram();

        uPosHandle = GLES20.glGetAttribLocation(program, A_POSITION);
        aTexHandle = GLES20.glGetAttribLocation(program, "inputTextureCoordinate");
        mMVPMatrixHandle = GLES20.glGetUniformLocation(program, "textureTransform");

        mPosBuffer = convertToFloatBuffer(mPosCoordinate);
        mTexBuffer = convertToFloatBuffer(mTexCoordinateBackRight);

        GLES20.glVertexAttribPointer(uPosHandle, 2, GLES20.GL_FLOAT, false, 0, mPosBuffer);
        GLES20.glVertexAttribPointer(aTexHandle, 2, GLES20.GL_FLOAT, false, 0, mTexBuffer);

        GLES20.glEnableVertexAttribArray(uPosHandle);
        GLES20.glEnableVertexAttribArray(aTexHandle);

    }

    private void getProgram(){
        String vertexShaderSource = TextResourceReader.readTextFileFromResource(context, R.raw.simple_vertex_shader);
        String fragmentShaderSource = TextResourceReader.readTextFileFromResource(context,R.raw.simple_fragment_shader);
        program = ShaderHelper.buildProgram(vertexShaderSource, fragmentShaderSource);
        GLES20.glUseProgram(program);
    }
    public void draw(){
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mPosCoordinate.length / 2);
    }

    public void Change(int width,int height){
        GLES20.glViewport(0, 0, width, height);
        Matrix.scaleM(mMVPMatrix,0,1,-1,1);
        float ratio = (float) width / height;
        Matrix.orthoM(mProjectMatrix, 0, -1, 1, -ratio, ratio, 1, 7);// 3和7代表远近视点与眼睛的距离，非坐标点
        Matrix.setLookAtM(mCameraMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);// 3代表眼睛的坐标点
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mCameraMatrix, 0);
    }
    private FloatBuffer convertToFloatBuffer(float[] buffer) {
        FloatBuffer fb = ByteBuffer.allocateDirect(buffer.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        fb.put(buffer);
        fb.position(0);
        return fb;
    }

}
