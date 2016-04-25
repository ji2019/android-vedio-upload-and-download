# android-vedio-upload-and-download
安卓视频上传与下载
核心代码：

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
					
					
					
					if(cachepercent==100.0||cachepercent==100.00){
					mVideoView.setVideoPath(localUrl);
					mVideoView.start();
					String s1 = String.format("已缓存: [%.2f%%]", cachepercent);
					tvcache.setText(s1);
					return;
