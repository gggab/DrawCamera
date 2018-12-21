package com.example.skq.demo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import permission.PermissionDenied;
import permission.PermissionHelper;
import permission.PermissionPermanentDenied;
import permission.PermissionSucceed;
import shape.Triangle;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;

public class CameraActivity extends AppCompatActivity implements SurfaceTexture.OnFrameAvailableListener {
    private String TAG = "CameraActivity";
    private final int PERMISSION_CODE = 1;
    private int mOESTextureId = -1;
    private Camera mCamera = null;

    private GLSurfaceView glSurfaceView;
    private SurfaceTexture mSurfaceTexture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CameraActivity.this, EmptyActivity.class);
                startActivity(intent);
            }
        });
        requestPermission();

        glSurfaceView = findViewById(R.id.gl);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(new MyRender(this));
        /*渲染方式，RENDERMODE_WHEN_DIRTY表示被动渲染，只有在调用requestRender或者onResume等方法时才会进行渲染。RENDERMODE_CONTINUOUSLY表示持续渲染*/
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private void requestPermission(){
        PermissionHelper.with(this).requestCode(PERMISSION_CODE).requestPermissions(Manifest.permission.CAMERA).request();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.e(TAG,"requestPermission onRequestPermissionsResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.requestPermissionsResult(this, requestCode, permissions, grantResults);
    }
    @PermissionDenied(requestCode = PERMISSION_CODE)
    private void onPermissionDenied() {
        Log.e(TAG,"requestPermission onPermissionDenied");
        Toast.makeText(this, "您拒绝了开启权限,可去设置界面打开", Toast.LENGTH_SHORT).show();
    }


    @PermissionPermanentDenied(requestCode = PERMISSION_CODE)
    private void onPermissionPermanentDenied() {
        Log.e(TAG,"requestPermission onPermissionPermanentDenied");
        Toast.makeText(this, "您选择了永久拒绝,可在设置界面重新打开", Toast.LENGTH_SHORT).show();
    }

    @PermissionSucceed(requestCode = PERMISSION_CODE)
    private void onPermissionSuccess() {
        Log.e(TAG,"requestPermission onPermissionSuccess");
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        Camera.Parameters parameters=mCamera.getParameters();
        parameters.set("orientation", "portrait");
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        parameters.setPreviewSize(1280, 720);
        mCamera.setDisplayOrientation(90);

        mCamera.setParameters(parameters);

    }
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture){
        glSurfaceView.requestRender();
    }

    public class MyRender implements GLSurfaceView.Renderer{
        private Context context;

        Triangle triangle;

        public MyRender(Context context) {
            this.context=context;
        }

        @Override
        public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
            glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

            mOESTextureId = createOESTextureObject();
            mSurfaceTexture = new SurfaceTexture(mOESTextureId);

            triangle = new Triangle(context);
            try {
                mCamera.setPreviewTexture(mSurfaceTexture);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSurfaceTexture.setOnFrameAvailableListener(CameraActivity.this);
        }

        @Override
        public void onSurfaceChanged(GL10 gl10, int width, int height) {
            //todo 渲染窗口大小发生改变的处理
            Log.e(TAG, "onSurfaceChanged232323232323232 width:" + width + "  height" + height);
            triangle.Change(width, height);
        }


        @Override
        public void onDrawFrame(GL10 gl10) {
            //todo 执行渲染工作
            glClear(GL_COLOR_BUFFER_BIT);
            mSurfaceTexture.updateTexImage();
            triangle.draw();
        }
    }
    public static int createOESTextureObject() {
        int[] tex = new int[1];
        //生成一个纹理
        GLES20.glGenTextures(1, tex, 0);
        //将此纹理绑定到外部纹理上
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]);
        //设置纹理过滤参数
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        //解除纹理绑定
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return tex[0];
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }
}
