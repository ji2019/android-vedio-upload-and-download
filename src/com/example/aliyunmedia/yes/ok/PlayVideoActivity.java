package com.example.aliyunmedia.yes.ok;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;

import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.URLUtil;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

@SuppressLint("DefaultLocale") @TargetApi(Build.VERSION_CODES.HONEYCOMB) public class PlayVideoActivity extends ActionBarActivity {
	private VideoView mVideoView;
	private TextView tvcache;
	private String localUrl,objectname;
	private ProgressDialog progressDialog = null;
//	private String remoteUrl = "http://f02.v1.cn/transcode/14283194FLVSDT14-3.flv";
//	private static final int READY_BUFF = 600 * 1024*1000;//当视频缓存到达这个大小时开始播放
//	private static final int CACHE_BUFF = 500 * 1024;//当网络不好时建立一个动态缓存区，避免一卡一卡的播放
//	private boolean isready = false;
	private boolean iserror = false;
	private int errorCnt = 0;
	private int curPosition = 0;
	private long mediaLength = 0;
	private long readSize = 0;
	private InputStream inputStream;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playvideo);
		findbyid();
		init();
		downloadview();
	}
	
	
	
	private void init() {
		// TODO Auto-generated method stub
		Intent intent = getIntent();
		objectname= intent.getStringExtra("objectname");
		this.localUrl = intent.getStringExtra("cache");
		mVideoView.setMediaController(new MediaController(this));
//		if (!URLUtil.isNetworkUrl(remoteUrl)) {
//			mVideoView.setVideoPath(remoteUrl);
//			mVideoView.start();
//		}
		mVideoView.setOnPreparedListener(new OnPreparedListener() {

			public void onPrepared(MediaPlayer mediaplayer) {
				dismissProgressDialog();
				mVideoView.seekTo(curPosition);
				mediaplayer.start();
			}
		});
		
		
		mVideoView.setOnCompletionListener(new OnCompletionListener() {

			public void onCompletion(MediaPlayer mediaplayer) {
				curPosition = 0;
				mVideoView.pause();
			}
		});
		
		
		mVideoView.setOnErrorListener(new OnErrorListener() {

			public boolean onError(MediaPlayer mediaplayer, int i, int j) {
				iserror = true;
//				errorCnt++;
				mVideoView.pause();
				showProgressDialog();
				return true;
			}
		});
	
		
		
	}
	

	private void showProgressDialog() {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				if (progressDialog == null) {
					progressDialog = ProgressDialog.show(PlayVideoActivity.this,
							"视频缓存", "正在努力加载中 ...", true, false);
				}
			}
		});
	}
	
	private void dismissProgressDialog() {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				if (progressDialog != null) {
					progressDialog.dismiss();
					progressDialog = null;
				} 
			}
		});
	}
	
	
	
	

	private void downloadview() {
		// TODO Auto-generated method stub
		String endpoint = "http://oss-cn-shanghai.aliyuncs.com";
		// 明文设置secret的方式建议只在测试时使用，更多鉴权模式请参考官网
		OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider("5wpkJus0wsbkUefR", "Hf2xHZe0bX71h55FRAcVpIRVBQYWlO ");
		OSS oss = new OSSClient(getApplicationContext(), endpoint, credentialProvider);
		// 构造下载文件请求
		GetObjectRequest get = new GetObjectRequest("qhtmedia", objectname);
		@SuppressWarnings("rawtypes")
		OSSAsyncTask task = oss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
		    @Override
		    public void onSuccess(GetObjectRequest request, GetObjectResult result) {
		    	// 请求成功回调
		        Log.d("Content-Length", "" + result.getContentLength());
		        //拿到输入流和文件长度
		         inputStream = result.getObjectContent();
		        mediaLength=result.getContentLength();
		        showProgressDialog();
		        byte[] buffer = new byte[2*2048];
		        int len;
		        FileOutputStream out = null;
//				long lastReadSize = 0;
		        //建立本地缓存路径，视频缓存到这个目录
				if (localUrl == null) {
					localUrl = Environment.getExternalStorageDirectory()
							.getAbsolutePath()
							+ "/VideoCache/"
							+ System.currentTimeMillis() + ".mp4";
				}
				Log.d("localUrl: " , localUrl);
				File cacheFile = new File(localUrl);
				if (!cacheFile.exists()) {
					cacheFile.getParentFile().mkdirs();
					try {
						cacheFile.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				readSize = cacheFile.length();
				try {
					out = new FileOutputStream(cacheFile, true);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (mediaLength == -1) {
					return;
				}
				mHandler.sendEmptyMessage(VIDEO_STATE_UPDATE);
		        try {
		            while ((len = inputStream.read(buffer)) != -1) {
		                // 处理下载的数据
		            	try{
							out.write(buffer, 0, len);
							readSize += len;
		            } catch (Exception e) {
						e.printStackTrace();
					}
//		            	if (!isready) {
//							if ((readSize - lastReadSize) > READY_BUFF) {
//								lastReadSize = readSize;
//								mHandler.sendEmptyMessage(CACHE_VIDEO_READY);
//							}
//						} else {
//							if ((readSize - lastReadSize) > CACHE_BUFF
//									* (errorCnt + 1)) {
//								lastReadSize = readSize;
//								mHandler.sendEmptyMessage(CACHE_VIDEO_UPDATE);
//							}
//						}	
		            }
		            mHandler.sendEmptyMessage(CACHE_VIDEO_END);
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		        finally {
					if (out != null) {
						try {
							out.close();
						} catch (IOException e) {
						}
					}
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (IOException e) {
						}
					}
				}
		    }
		    @Override
		    public void onFailure(GetObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
		        // 请求异常
		        if (clientExcepion != null) {
		            // 本地异常如网络异常等
		            clientExcepion.printStackTrace();
		        }
		        if (serviceException != null) {
		            // 服务异常
		            Log.e("ErrorCode", serviceException.getErrorCode());
		            Log.e("RequestId", serviceException.getRequestId());
		            Log.e("HostId", serviceException.getHostId());
		            Log.e("RawMessage", serviceException.getRawMessage());
		        }
		    }

		});
		// task.cancel(); // 可以取消任务

//		 task.waitUntilFinished(); // 如果需要等待任务完成
	}
	private final static int VIDEO_STATE_UPDATE = 0;
//	private final static int CACHE_VIDEO_READY = 1;
//	private final static int CACHE_VIDEO_UPDATE = 2;
	private final static int CACHE_VIDEO_END = 3;

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case VIDEO_STATE_UPDATE:
				double cachepercent = readSize * 100.00 / mediaLength * 1.0;
				String s = String.format("已缓存: [%.2f%%]", cachepercent);
//				}
				//缓存到达100%时开始播放
				if(cachepercent==100.0||cachepercent==100.00){
					mVideoView.setVideoPath(localUrl);
					mVideoView.start();
					String s1 = String.format("已缓存: [%.2f%%]", cachepercent);
					tvcache.setText(s1);
					return;
				}
				tvcache.setText(s);
				mHandler.sendEmptyMessageDelayed(VIDEO_STATE_UPDATE, 1000);
				break;
				
//			case CACHE_VIDEO_READY:
//				isready = true;
//				mVideoView.setVideoPath(localUrl);
//				mVideoView.start();
//				break;
//
//			case CACHE_VIDEO_UPDATE:
//				if (iserror) {
//					mVideoView.setVideoPath(localUrl);
//					mVideoView.start();
//					iserror = false;
//				}
//				break;

			case CACHE_VIDEO_END:
				if (iserror) {
					mVideoView.setVideoPath(localUrl);
					mVideoView.start();
					iserror = false;
				}
				break;
			}
			super.handleMessage(msg);
		}
	};
	private void findbyid() {
		// TODO Auto-generated method stub
		mVideoView = (VideoView) findViewById(R.id.bbvideoview);
		tvcache = (TextView) findViewById(R.id.tvcache);
	}
}
