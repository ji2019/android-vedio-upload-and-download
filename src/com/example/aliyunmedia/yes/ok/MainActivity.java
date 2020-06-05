package com.example.aliyunmedia.yes.ok;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import android.support.v7.app.ActionBarActivity;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
@TargetApi(Build.VERSION_CODES.HONEYCOMB) public class MainActivity extends ActionBarActivity {
	private TextView tv,detail;
	private Button camerabutton,playbutton,selectvideo;
	private ProgressBar pb;
	private String path,objectname;
	private EditText filename;
	 private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       findbyid();
    }
    private void findbyid() {
		// TODO Auto-generated method stub
    	selectvideo= (Button) findViewById(R.id.camerabutton);
    	detail= (TextView) findViewById(R.id.detail);
    	tv = (TextView) findViewById(R.id.text);
		pb = (ProgressBar) findViewById(R.id.progressBar1);
		camerabutton = (Button) findViewById(R.id.camerabutton);
		playbutton= (Button) findViewById(R.id.playbutton);
		filename=(EditText) findViewById(R.id.filename);
		
		playbutton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this, PlayVideoActivity.class);
				intent.putExtra("objectname", objectname);
				//设置缓存目录
				intent.putExtra("cache",
						Environment.getExternalStorageDirectory().getAbsolutePath()
								+ "/VideoCache/" + System.currentTimeMillis() + ".mp4");
				startActivity(intent);
			}
		});
		
		camerabutton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		    beginupload();
			}
		});
	}

    public void selectvideo(View view)
    {
    	//跳到图库
    	  Intent intent = new Intent(Intent.ACTION_PICK);
    	  //选择的格式为视频,图库中就只显示视频（如果图片上传的话可以改为image/*，图库就只显示图片）
    	           intent.setType("video/*");
    	           // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_GALLERY
    	           startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
    }
//    /*
//          * 判断sdcard是否被挂载
//          */
//         private boolean hasSdcard() {
//             if (Environment.getExternalStorageState().equals(
//                     Environment.MEDIA_MOUNTED)) {
//                 return true;
//            } else {
//                 return false;
//             }
//         }
    public void beginupload(){
    	//通过填写文件名形成objectname,通过这个名字指定上传和下载的文件
    	objectname=filename.getText().toString();
    	if(objectname==null||objectname.equals("")){
    		Toast.makeText(MainActivity.this, "文件名不能为空", 2000).show();
    		return;
    	}
    	//填写自己的OSS外网域名
		String endpoint = "http://oss-cn-shanghai.aliyuncs.com";
		//填写明文accessKeyId和accessKeySecret，加密官网有
    	OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider("****", "**** ");
		OSS oss = new OSSClient(getApplicationContext(), endpoint, credentialProvider);
		//下面3个参数依次为bucket名，Object名，上传文件路径
    	PutObjectRequest put = new PutObjectRequest("qhtmedia", objectname, path);
    	if(path==null||path.equals("")){
    		detail.setText("请选择视频!!!!");
    		return;
    	}
				tv.setText("正在上传中....");
		    	pb.setVisibility(View.VISIBLE);
    	// 异步上传，可以设置进度回调
    	put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
    		@Override
    	    public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
    	    	Log.d("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
				}
    	});
    	@SuppressWarnings("rawtypes")
		OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
    	    @Override
    	    public void onSuccess(PutObjectRequest request, PutObjectResult result) {
    	        Log.d("PutObject", "UploadSuccess");
    	        //去UI线程更新UI
    	    	runOnUiThread(new Runnable() {
   				@Override
    				public void run() {
    					// TODO Auto-generated method stub
   				  tv.setText("UploadSuccess");
      	          pb.setVisibility(ProgressBar.INVISIBLE);
   				}
    			});
    	    }
    	    @Override
    	    public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
    	        // 请求异常
    	    	runOnUiThread(new Runnable() {
       				@Override
        				public void run() {
        					// TODO Auto-generated method stub
       					pb.setVisibility(ProgressBar.INVISIBLE);
       	    	    	tv.setText("Uploadfile,localerror");
       				}
        			});
    	        if (clientExcepion != null) {
    	            // 本地异常如网络异常等
    	            clientExcepion.printStackTrace();
    	        }
    	        if (serviceException != null) {
    	            // 服务异常
    	        	tv.setText("Uploadfile,servererror");
    	            Log.e("ErrorCode", serviceException.getErrorCode());
    	            Log.e("RequestId", serviceException.getRequestId());
    	            Log.e("HostId", serviceException.getHostId());
    	            Log.e("RawMessage", serviceException.getRawMessage());
    	        }
    	    }
    	});
    	// task.cancel(); // 可以取消任务
//    	 task.waitUntilFinished(); // 可以等待直到任务完成
}
         @Override
              protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                 if (requestCode == PHOTO_REQUEST_GALLERY) {
                     // 从相册返回的数据
                     if (data != null) {
                         // 得到视频的全路径
                        Uri uri = data.getData();
                        getRealFilePath(MainActivity.this,uri);
                     }
                 } 
                 super.onActivityResult(requestCode, resultCode, data);
             }
        /* 下面是4.4后通过Uri获取路径以及文件名一种方法，比如得到的路径 /storage/emulated/0/video/20160422.3gp，
                                 通过索引最后一个/就可以在String中截取了*/
         public  void getRealFilePath( final Context context, final Uri uri ) {
             if ( null == uri ) return ;
             final String scheme = uri.getScheme();
             String data = null;
             if ( scheme == null )
                 data = uri.getPath();
             else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
                 data = uri.getPath();
             } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
                 Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
                 if ( null != cursor ) {
                     if ( cursor.moveToFirst() ) {
                         int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                         if ( index > -1 ) {
                             data = cursor.getString( index );
                         }
                     }
                     cursor.close();
                 }
             }
             path=data;
             String b = path.substring(path.lastIndexOf("/") + 1, path.length());
             detail.setText(b);
         }
}
