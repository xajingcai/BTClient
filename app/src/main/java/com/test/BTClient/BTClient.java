package com.test.BTClient;


import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.AttributedCharacterIterator;
import java.util.UUID;
import java.util.jar.Attributes;

import com.test.BTClient.DeviceListActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
//import android.view.Menu;           //如使用菜单加入此三包
//import android.view.MenuInflater;
//import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class BTClient extends Activity {
	
	private final static int REQUEST_CONNECT_DEVICE = 1;    //宏定义查询设备句柄
	
	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";    //SPP服务UUID号

	private InputStream is;    //输入流，用来接收蓝牙数据
	//private TextView text0;    //提示栏解句柄
	private EditText edit0;    //发送数据输入句柄
	private TextView dis;       //接收数据显示句柄
	private ScrollView sv;      //翻页句柄
	private String smsg = "";    //显示用数据缓存
	private String fmsg = "";    //保存用数据缓存

	private Button upbtn = null;	//向前移动按钮
	private Button downbtn = null;	//向后移动按钮
	private Button rightbtn = null; //向右移动按钮
	private Button leftbtn = null;	//向左移动按钮

	private Button btn = null;		//实现其他动作的按钮
	private Button btn2 = null;
	private Button btn3 = null;
	private Button btn4 = null;
	private Button btn5 = null;
	private Button btn6 = null;

	private SeekBar sb_test;  		//滚动条按钮

	public String filename=""; //用来保存存储的文件名
	BluetoothDevice _device = null;     //蓝牙设备
	BluetoothSocket _socket = null;      //蓝牙通信socket
	//BluetoothSocket _socketUp = null;
    boolean _discoveryFinished = false;    
    boolean bRun = true;
    boolean bThread = false;
	
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();    //获取本地蓝牙适配器，即蓝牙设备



	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);   //设置画面为主画面 main.xml

		//text0 = (TextView)findViewById(R.id.Text0);  //得到提示栏句柄
		edit0 = (EditText)findViewById(R.id.Edit0);   //得到输入框句柄
		sv = (ScrollView)findViewById(R.id.ScrollView01);  //得到翻页句柄
		dis = (TextView) findViewById(R.id.in);      //得到数据显示句柄

		btn = (Button) findViewById(R.id.button);		//添加新的控件需要在此处定义并绑定xml文件中对应的控件id
		btn.setOnTouchListener(new TouchListener());
		btn2 = (Button) findViewById(R.id.button2);
		btn2.setOnTouchListener(new TouchListener2());
		btn3 = (Button) findViewById(R.id.button3);
		btn3.setOnTouchListener(new TouchListener3());
		btn4 = (Button) findViewById(R.id.button4);
		btn4.setOnTouchListener(new TouchListener4());
		btn5 = (Button) findViewById(R.id.button5);
		btn5.setOnTouchListener(new TouchListener5());
		btn6 = (Button) findViewById(R.id.button6);
		btn6.setOnTouchListener(new TouchListener6());


		upbtn = (Button) findViewById(R.id.up);
		upbtn.setOnTouchListener(new UpTouchListener());
		downbtn =(Button) findViewById(R.id.down);
		downbtn.setOnTouchListener(new DownTouchListener());
		rightbtn = (Button) findViewById(R.id.right);
		rightbtn.setOnTouchListener(new RightTouchListener());
		leftbtn = (Button) findViewById(R.id.left);
		leftbtn.setOnTouchListener(new LeftTouchListener());

		sb_test = (SeekBar) findViewById(R.id.seekBar);
		// 进度条的最大值
		sb_test.setMax(10);
		sb_test.setOnSeekBarChangeListener(new seekBarChangeListener());


		//如果打开本地蓝牙设备不成功，提示信息，结束程序
        if (_bluetooth == null){
        	Toast.makeText(this, "无法打开手机蓝牙，请确认手机是否有蓝牙功能！", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

		//如果打开本地蓝牙设备不成功，提示信息，结束程序
       new Thread(){
    	   public void run(){
    		   if(_bluetooth.isEnabled()==false){
        		_bluetooth.enable();
    		   }
    	   }   	   
       }.start();

    }

	//将指定byte数组转换成16进制的形式
	public static int getInt(byte[] bytes)
			{
				int target = (0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)) | (0xff0000 & (bytes[2] << 16)) | (0xff000000 & (bytes[3] << 24));
				return target;
			}
			//发送按键响应
			public void onSendButtonClicked(View v){
//				int i=0;
//				int n=0;
//				try{
//					OutputStream os = _socket.getOutputStream();   //蓝牙连接输出流
//					byte[] bos = edit0.getText().toString().getBytes();
//					for(i=0;i<bos.length;i++){
//						if(bos[i]==0x0a)n++;
//					}
//					byte[] bos_new = new byte[bos.length+n];
//					n=0;
//					for(i=0;i<bos.length;i++){ //手机中换行为0a,将其改为0d 0a后再发送
//						if(bos[i]==0x0a){
//							bos_new[n]=0x0d;
//							n++;
//							bos_new[n]=0x0a;
//						}else{
//							bos_new[n]=bos[i];
//						}
//    			n++;
//    		}

//					os.write(0xAA);
//					os.write(0x22);
//					os.write(0x11);
//
//
//    	}catch(IOException e){
//    	}
    }
//发送Up指令
//	public void onSendUpClicked(View v){
//		try {
//			OutputStream osUp = _socket.getOutputStream();
//			osUp.write(0xAA);
//			osUp.write(0x55);
//			osUp.write(0x05);
//			osUp.write(0xA0);
//			osUp.write(0x80);
//			osUp.write(0x00);
//			osUp.write(0x01);
//			osUp.write(0x01);
//			osUp.write(0x0E);
//			osUp.write(0x00);
//			osUp.write(0x64);
//			osUp.write(0x02);
//			osUp.write(0x10);
//		}catch(IOException e){
//		}
//	}
	//发送Down指令
	/*public void onSendDownClicked(View v){
		try {
			OutputStream osUp = _socket.getOutputStream();
			osUp.write(0xAA);
			osUp.write(0x55);
			osUp.write(0x03);
			osUp.write(0xA0);
			osUp.write(0x80);
			osUp.write(0x00);
			osUp.write(0x03);
			osUp.write(0x01);
			osUp.write(0x0E);
			osUp.write(0x00);
			osUp.write(0x64);
			osUp.write(0x02);
			osUp.write(0x10);
		}catch(IOException e){
		}
	}
	*/
	//发送Right指令
	public void onSendRightClicked(View v){
		try {
			OutputStream osUp = _socket.getOutputStream();
			osUp.write(0xAA);
			osUp.write(0x55);
			osUp.write(0x02);
			osUp.write(0xA0);
			osUp.write(0x80);
			osUp.write(0x00);
			osUp.write(0x04);
			osUp.write(0x01);
			osUp.write(0x0E);
			osUp.write(0x00);
			osUp.write(0x64);
			osUp.write(0x02);
			osUp.write(0x10);
		}catch(IOException e){
		}
	}

	//发送Left指令
	public void onSendLeftClicked(View v){
		try {
			OutputStream osUp = _socket.getOutputStream();
			osUp.write(0xAA);
			osUp.write(0x55);
			osUp.write(0x04);
			osUp.write(0xA0);
			osUp.write(0x80);
			osUp.write(0x00);
			osUp.write(0x02);
			osUp.write(0x01);
			osUp.write(0x0E);
			osUp.write(0x00);
			osUp.write(0x64);
			osUp.write(0x02);
			osUp.write(0x10);
		}catch(IOException e){
		}
	}

	//发送停止指令
//	public void onSendStopClicked(View v){
//		try {
//			OutputStream osUp = _socket.getOutputStream();
//			osUp.write(0xAA);
//			osUp.write(0x00);
//			osUp.write(0x00);
//		}catch(IOException e){
//		}
//	}
	//接收活动结果，响应startActivityForResult()
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch(requestCode){
    	case REQUEST_CONNECT_DEVICE:     //连接结果，由DeviceListActivity设置返回
			// 响应返回结果
            if (resultCode == Activity.RESULT_OK) {   //连接成功，由DeviceListActivity设置返回
				// MAC地址，由DeviceListActivity设置返回
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// 得到蓝牙设备句柄
                _device = _bluetooth.getRemoteDevice(address);

				// 用服务号得到socket
                try{
                	_socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                }catch(IOException e){
                	Toast.makeText(this, "接收数据失败！", Toast.LENGTH_SHORT).show();
                }
				//连接socket
            	Button btn = (Button) findViewById(R.id.Button03);
                try{
                	_socket.connect();
                	Toast.makeText(this, "连接"+_device.getName()+"成功！", Toast.LENGTH_SHORT).show();
                	btn.setText("断开");
                }catch(IOException e){
                	try{
                		Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
                		_socket.close();
                		_socket = null;
                	}catch(IOException ee){
                		Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
                	}
                	
                	return;
                }

				//打开接收线程
                try{
            		is = _socket.getInputStream();   //得到蓝牙数据输入流
            		}catch(IOException e){
            			Toast.makeText(this, "接收数据失败！", Toast.LENGTH_SHORT).show();
            			return;
            		}
            		if(bThread==false){
            			ReadThread.start();
            			bThread=true;
            		}else{
            			bRun = true;
            		}
            }
    		break;
    	default:break;
    	}
    }

	//接收数据线程
    Thread ReadThread=new Thread(){
    	
    	public void run(){
    		int num = 0;
    		byte[] buffer = new byte[1024];
    		byte[] buffer_new = new byte[1024];
    		int i = 0;
    		int n = 0;
    		bRun = true;
			//接收线程
    		while(true){
    			try{
    				while(is.available()==0){
    					while(bRun == false){}
    				}
    				while(true){
    					num = is.read(buffer);            //读入数据
    					n=0;
    					
    					String s0 = new String(buffer,0,num);
    					fmsg+=s0;    //保存收到数据
    					for(i=0;i<num;i++){
    						if((buffer[i] == 0x0d)&&(buffer[i+1]==0x0a)){
    							buffer_new[n] = 0x0a;
    							i++;
    						}else{
    							buffer_new[n] = buffer[i];
    						}
    						n++;
    					}
    					String s = new String(buffer_new,0,n);
    					smsg+=s;   //写入接收缓存
    					if(is.available()==0)break;  //短时间没有数据才跳出进行显示
    				}
					//发送显示消息，进行显示刷新
    					handler.sendMessage(handler.obtainMessage());       	    		
    	    		}catch(IOException e){
    	    		}
    		}
    	}
    };

	//消息处理队列
    Handler handler= new Handler(){
    	public void handleMessage(Message msg){
    		super.handleMessage(msg);
    		dis.setText(smsg);   //显示数据
    		sv.scrollTo(0,dis.getMeasuredHeight()); //跳至数据最后一页
    	}
    };

	//关闭程序掉用处理部分
    public void onDestroy(){
    	super.onDestroy();
    	if(_socket!=null)  //关闭连接socket
    	try{
    		_socket.close();
    	}catch(IOException e){}
    //	_bluetooth.disable();  //关闭蓝牙服务
    }

	//菜单处理部分
 /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {//建立菜单
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }*/

  /*  @Override
    public boolean onOptionsItemSelected(MenuItem item) { //菜单响应函数
        switch (item.getItemId()) {
        case R.id.scan:
        	if(_bluetooth.isEnabled()==false){
        		Toast.makeText(this, "Open BT......", Toast.LENGTH_LONG).show();
        		return true;
        	}
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.quit:
            finish();
            return true;
        case R.id.clear:
        	smsg="";
        	ls.setText(smsg);
        	return true;
        case R.id.save:
        	Save();
        	return true;
        }
        return false;
    }*/

	//连接按键响应函数
    public void onConnectButtonClicked(View v){ 
    	if(_bluetooth.isEnabled()==false){  //如果蓝牙服务不可用则提示
    		Toast.makeText(this, " 打开蓝牙中...", Toast.LENGTH_LONG).show();
    		return;
    	}


		//如未连接设备则打开DeviceListActivity进行设备搜索
    	Button btn = (Button) findViewById(R.id.Button03);
    	if(_socket==null){
    		Intent serverIntent = new Intent(this, DeviceListActivity.class);//跳转程序设置
    		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);  //设置返回宏定义
    	}
    	else{
			//关闭连接socket
    	    try{
    	    	
    	    	is.close();
    	    	_socket.close();
    	    	_socket = null;
    	    	bRun = false;
    	    	btn.setText("连接");
    	    }catch(IOException e){}   
    	}
    	return;
    }

	//保存按键响应函数
    public void onSaveButtonClicked(View v){
    	Save();
    }

	//清除按键响应函数
    public void onClearButtonClicked(View v){
    	smsg="";
    	fmsg="";
    	dis.setText(smsg);
    	return;
    }

	//退出按键响应函数
    public void onQuitButtonClicked(View v){
    	finish();
    }

	//保存功能实现
	private void Save() {
		//显示对话框输入文件名
		LayoutInflater factory = LayoutInflater.from(BTClient.this);  //图层模板生成器句柄
		final View DialogView =  factory.inflate(R.layout.sname, null);  //用sname.xml模板生成视图模板
		new AlertDialog.Builder(BTClient.this)
								.setTitle("文件名")
								.setView(DialogView)   //设置视图模板
								.setPositiveButton("ȷ确定",
										new DialogInterface.OnClickListener() //确定按键响应函数
										{
											public void onClick(DialogInterface dialog, int whichButton) {
												EditText text1 = (EditText) DialogView.findViewById(R.id.sname);  //得到文件名输入框句柄
												filename = text1.getText().toString();  //得到文件名

												try {
													if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {  //如果SD卡已准备好

														filename = filename + ".txt";   //在文件名末尾加上.txt
														File sdCardDir = Environment.getExternalStorageDirectory();  //得到SD卡根目录
														File BuildDir = new File(sdCardDir, "/data");   //打开data目录，如不存在则生成
														if (BuildDir.exists() == false)
															BuildDir.mkdirs();
														File saveFile = new File(BuildDir, filename);  //新建文件句柄，如已存在仍新建文档
														FileOutputStream stream = new FileOutputStream(saveFile);  //打开文件输入流
														stream.write(fmsg.getBytes());
														stream.close();
														Toast.makeText(BTClient.this, "存储成功", Toast.LENGTH_SHORT).show();
													} else {
														Toast.makeText(BTClient.this, "没有存储卡！", Toast.LENGTH_LONG).show();
													}

												} catch (IOException e) {
													return;
												}


											}
										})
				.setNegativeButton("取消",   //取消按键响应函数,直接退出对话框不做任何处理
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) { 
									}
								}).show();  //显示对话框
	}
//public class MyButton extends Button
//{
//	public MyButton(Context context, AttributeSet set)
//	{
//		super(context, set);
//	}
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event)
//	{
//			super.onKeyDown(keyCode, event);
//		try {
//			OutputStream osUp = _socket.getOutputStream();
//			osUp.write(0xAA);
//			osUp.write(0x33);
//			osUp.write(0x44);
//		}catch(IOException e){
//		}
//		return true;
//	}
//	@Override
//	public boolean onKeyUp(int keyCode, KeyEvent event)
//	{
//		super.onKeyUp(keyCode, event);
//		try {
//			OutputStream osUp = _socket.getOutputStream();
//			osUp.write(0xAA);
//			osUp.write(0x00);
//			osUp.write(0x00);
//		}catch(IOException e){
//		}
//		return true;
//	}
//}
	final class TouchListener implements OnTouchListener{
	public boolean onTouch(View v,MotionEvent event){
	if(event.getAction() == KeyEvent.ACTION_DOWN){			//当控件按下时发送指令
		try {
			OutputStream osTurnRight = _socket.getOutputStream();
			osTurnRight.write(0xAA);
			osTurnRight.write(0x55);
			osTurnRight.write(0x01);
			osTurnRight.write(0xA0);
			osTurnRight.write(0x80);
			osTurnRight.write(0x00);
			osTurnRight.write(0x05);
			osTurnRight.write(0x01);
			osTurnRight.write(0x0E);
			osTurnRight.write(0x00);
			osTurnRight.write(0x64);
			osTurnRight.write(0x02);
			osTurnRight.write(0x10);
		}catch(IOException e){
		}
	}
	if(event.getAction() == KeyEvent.ACTION_UP){				//当控件释放时发送指令
		try {
			OutputStream osStop = _socket.getOutputStream();
			osStop.write(0xAA);
			osStop.write(0x55);
			osStop.write(0x74);
			osStop.write(0xAA);
			osStop.write(0x8E);
			osStop.write(0xFF);
			osStop.write(0x00);
		}catch(IOException e){
		}
	}
	return true;
}
}
	final class TouchListener2 implements OnTouchListener{
	public boolean onTouch(View v,MotionEvent event){
		if(event.getAction() == KeyEvent.ACTION_DOWN){			//当控件按下时发送指令
			try {
				OutputStream os = _socket.getOutputStream();
				os.write(0xAA);
				os.write(0x55);
				os.write(0x00);
				os.write(0xA0);
				os.write(0x80);
				os.write(0x00);
				os.write(0x06);
				os.write(0x01);
				os.write(0x0E);
				os.write(0x00);
				os.write(0x64);
				os.write(0x02);
				os.write(0x10);
			}catch(IOException e){
			}
		}
		if(event.getAction() == KeyEvent.ACTION_UP){				//当控件释放时发送指令
			try {
				OutputStream osStop = _socket.getOutputStream();
				osStop.write(0xAA);
				osStop.write(0x55);
				osStop.write(0x74);
				osStop.write(0xAA);
				osStop.write(0x8E);
				osStop.write(0xFF);
				osStop.write(0x00);
			}catch(IOException e){
			}
		}
		return true;
	}
}
	final class TouchListener3 implements OnTouchListener{
		public boolean onTouch(View v,MotionEvent event){
			if(event.getAction() == KeyEvent.ACTION_DOWN){			//当控件按下时发送指令
				try {
					OutputStream os = _socket.getOutputStream();
					os.write(0xAA);
					os.write(0x55);
					os.write(0x01);
					os.write(0xA0);
					os.write(0x80);
					os.write(0x00);
					os.write(0x05);
					os.write(0x01);
					os.write(0x0E);
					os.write(0x00);
					os.write(0x64);
					os.write(0x02);
					os.write(0x10);
				}catch(IOException e){
				}
			}
			if(event.getAction() == KeyEvent.ACTION_UP){				//当控件释放时发送指令
				try {
					OutputStream osStop = _socket.getOutputStream();
					osStop.write(0xAA);
					osStop.write(0x55);
					osStop.write(0x74);
					osStop.write(0xAA);
					osStop.write(0x8E);
					osStop.write(0xFF);
					osStop.write(0x00);
				}catch(IOException e){
				}
			}
			return true;
		}
	}

	final class TouchListener4 implements OnTouchListener{
		public boolean onTouch(View v,MotionEvent event){
			if(event.getAction() == KeyEvent.ACTION_DOWN){			//当控件按下时发送指令
				try {
					OutputStream osTurnleft = _socket.getOutputStream();
					osTurnleft.write(0xAA);
					osTurnleft.write(0x55);
					osTurnleft.write(0x01);
					osTurnleft.write(0xA0);
					osTurnleft.write(0x80);
					osTurnleft.write(0x00);
					osTurnleft.write(0x05);
					osTurnleft.write(0x01);
					osTurnleft.write(0x0E);
					osTurnleft.write(0x00);
					osTurnleft.write(0x64);
					osTurnleft.write(0x02);
					osTurnleft.write(0x10);
				}catch(IOException e){
				}
			}
			if(event.getAction() == KeyEvent.ACTION_UP){				//当控件释放时发送指令
				try {
					OutputStream osStop = _socket.getOutputStream();
					osStop.write(0xAA);
					osStop.write(0x55);
					osStop.write(0x74);
					osStop.write(0xAA);
					osStop.write(0x8E);
					osStop.write(0xFF);
					osStop.write(0x00);
				}catch(IOException e){
				}
			}
			return true;
		}
	}

	final class TouchListener5 implements OnTouchListener{
		public boolean onTouch(View v,MotionEvent event){
			if(event.getAction() == KeyEvent.ACTION_DOWN){			//当控件按下时发送指令
				try {
					OutputStream osTurnLeft = _socket.getOutputStream();
					osTurnLeft.write(0xAA);
					osTurnLeft.write(0x55);
					osTurnLeft.write(0x02);
					osTurnLeft.write(0xA0);
					osTurnLeft.write(0x80);
					osTurnLeft.write(0x00);
					osTurnLeft.write(0x04);
					osTurnLeft.write(0x01);
					osTurnLeft.write(0x0E);
					osTurnLeft.write(0x00);
					osTurnLeft.write(0x64);
					osTurnLeft.write(0x02);
					osTurnLeft.write(0x10);
				}catch(IOException e){
				}
			}
			if(event.getAction() == KeyEvent.ACTION_UP){				//当控件释放时发送指令
				try {
					OutputStream osStop = _socket.getOutputStream();
					osStop.write(0xAA);
					osStop.write(0x55);
					osStop.write(0x74);
					osStop.write(0xAA);
					osStop.write(0x8E);
					osStop.write(0xFF);
					osStop.write(0x00);
				}catch(IOException e){
				}
			}
			return true;
		}
	}

	final class TouchListener6 implements OnTouchListener{
		public boolean onTouch(View v,MotionEvent event){
			if(event.getAction() == KeyEvent.ACTION_DOWN){			//当控件按下时发送指令
				try {
					OutputStream osTurnRight = _socket.getOutputStream();
					osTurnRight.write(0xAA);
					osTurnRight.write(0x55);
					osTurnRight.write(0x04);
					osTurnRight.write(0xA0);
					osTurnRight.write(0x80);
					osTurnRight.write(0x00);
					osTurnRight.write(0x02);
					osTurnRight.write(0x01);
					osTurnRight.write(0x0E);
					osTurnRight.write(0x00);
					osTurnRight.write(0x64);
					osTurnRight.write(0x02);
					osTurnRight.write(0x10);
				}catch(IOException e){
				}
			}
			if(event.getAction() == KeyEvent.ACTION_UP){				//当控件释放时发送指令
				try {
					OutputStream osStop = _socket.getOutputStream();
					osStop.write(0xAA);
					osStop.write(0x55);
					osStop.write(0x74);
					osStop.write(0xAA);
					osStop.write(0x8E);
					osStop.write(0xFF);
					osStop.write(0x00);
				}catch(IOException e){
				}
			}
			return true;
		}
	}

	final class UpTouchListener implements OnTouchListener{
		public boolean onTouch(View v,MotionEvent event){
			if(event.getAction() == KeyEvent.ACTION_DOWN){
				try {
					OutputStream osUp = _socket.getOutputStream();
					osUp.write(0xAA);
					osUp.write(0x55);
					osUp.write(0x05);
					osUp.write(0xA0);
					osUp.write(0x80);
					osUp.write(0x00);
					osUp.write(0x01);
					osUp.write(0x01);
					osUp.write(0x0E);
					osUp.write(0x00);
					osUp.write(0x64);
					osUp.write(0x02);
					osUp.write(0x10);
				}catch(IOException e){
				}
			}
			if(event.getAction() == KeyEvent.ACTION_UP){
				try {
					OutputStream osStop = _socket.getOutputStream();
					osStop.write(0xAA);
					osStop.write(0x55);
					osStop.write(0x74);
					osStop.write(0xAA);
					osStop.write(0x8E);
					osStop.write(0xFF);
					osStop.write(0x00);
				}catch(IOException e){
				}
			}
			return true;
		}
	}//test

	final class DownTouchListener implements OnTouchListener{
		public boolean onTouch(View v,MotionEvent event){
			if(event.getAction() == KeyEvent.ACTION_DOWN){
				try {
					OutputStream osDown = _socket.getOutputStream();
					osDown.write(0xAA);
					osDown.write(0x55);
					osDown.write(0x03);
					osDown.write(0xA0);
					osDown.write(0x80);
					osDown.write(0x00);
					osDown.write(0x03);
					osDown.write(0x01);
					osDown.write(0x0E);
					osDown.write(0x00);
					osDown.write(0x64);
					osDown.write(0x02);
					osDown.write(0x10);
				}catch(IOException e){
				}
			}
			if(event.getAction() == KeyEvent.ACTION_UP){
				try {
					OutputStream osStop = _socket.getOutputStream();
					osStop.write(0xAA);
					osStop.write(0x55);
					osStop.write(0x74);
					osStop.write(0xAA);
					osStop.write(0x8E);
					osStop.write(0xFF);
					osStop.write(0x00);
				}catch(IOException e){
				}
			}
			return true;
		}
	}

	final class RightTouchListener implements OnTouchListener{
		public boolean onTouch(View v,MotionEvent event){
			if(event.getAction() == KeyEvent.ACTION_DOWN){
				try {
					OutputStream osRight = _socket.getOutputStream();
					osRight.write(0xAA);
					osRight.write(0x55);
					osRight.write(0x02);
					osRight.write(0xA0);
					osRight.write(0x80);
					osRight.write(0x00);
					osRight.write(0x04);
					osRight.write(0x01);
					osRight.write(0x0E);
					osRight.write(0x00);
					osRight.write(0x64);
					osRight.write(0x02);
					osRight.write(0x10);
				}catch(IOException e){
				}
			}
			if(event.getAction() == KeyEvent.ACTION_UP){
				try {
					OutputStream osStop = _socket.getOutputStream();
					osStop.write(0xAA);
					osStop.write(0x55);
					osStop.write(0x74);
					osStop.write(0xAA);
					osStop.write(0x8E);
					osStop.write(0xFF);
					osStop.write(0x00);
				}catch(IOException e){
				}
			}
			return true;
		}
	}

	final class LeftTouchListener implements OnTouchListener{
		public boolean onTouch(View v,MotionEvent event){
			if(event.getAction() == KeyEvent.ACTION_DOWN){
				try {
					OutputStream osLeft = _socket.getOutputStream();
					osLeft.write(0xAA);
					osLeft.write(0x55);
					osLeft.write(0x04);
					osLeft.write(0xA0);
					osLeft.write(0x80);
					osLeft.write(0x00);
					osLeft.write(0x02);
					osLeft.write(0x01);
					osLeft.write(0x0E);
					osLeft.write(0x00);
					osLeft.write(0x64);
					osLeft.write(0x02);
					osLeft.write(0x10);
				}catch(IOException e){
				}
			}
			if(event.getAction() == KeyEvent.ACTION_UP){
				try {
					OutputStream osStop = _socket.getOutputStream();
					osStop.write(0xAA);
					osStop.write(0x55);
					osStop.write(0x74);
					osStop.write(0xAA);
					osStop.write(0x8E);
					osStop.write(0xFF);
					osStop.write(0x00);
				}catch(IOException e){
				}
			}
			return true;
		}
	}

	private static final String TAG = "SeekBar";
final class seekBarChangeListener implements OnSeekBarChangeListener{
	@Override
	// 只要进度条的滑块发生变化，无论滑块是怎样变化的，都会调用此方法
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		Log.i(TAG, "on  Progress   Changed方法,当前位置: "
				+ seekBar.getProgress());
						}
		@Override
//	 当用户停止滑动滑块的时候，调用此方法
	public void onStopTrackingTouch(SeekBar seekBar) {
			Log.i(TAG, "on  Stop  Tracking  Touch方法,当前位置: "
					+ seekBar.getProgress());
			int i = seekBar.getProgress();
			switch (i) {
				case 0:
					try {
						OutputStream seekBarLocation = _socket.getOutputStream();
						seekBarLocation.write(0xAA);
						seekBarLocation.write(0x55);
						seekBarLocation.write(0x04);
						seekBarLocation.write(0xA0);

					}catch(IOException e){
					}
					break;
				case 1:
					try {
						OutputStream seekBarLocation = _socket.getOutputStream();
						seekBarLocation.write(0xAA);
						seekBarLocation.write(0x55);
						seekBarLocation.write(0x04);
						seekBarLocation.write(0xA0);

					}catch(IOException e){
					}
					break;
				case 2:
					try {
						OutputStream seekBarLocation = _socket.getOutputStream();
						seekBarLocation.write(0xAA);
						seekBarLocation.write(0x55);
						seekBarLocation.write(0x04);
						seekBarLocation.write(0xA0);

					}catch(IOException e){
					}
					break;
				case 3:
					try {
						OutputStream seekBarLocation = _socket.getOutputStream();
						seekBarLocation.write(0xAA);
						seekBarLocation.write(0x55);
						seekBarLocation.write(0x04);
						seekBarLocation.write(0xA0);

					}catch(IOException e){
					}
					break;
				case 4:
					try {
						OutputStream seekBarLocation = _socket.getOutputStream();
						seekBarLocation.write(0xAA);
						seekBarLocation.write(0x55);
						seekBarLocation.write(0x04);
						seekBarLocation.write(0xA0);

					}catch(IOException e){
					}
					break;
				case 5:
					try {
						OutputStream seekBarLocation = _socket.getOutputStream();
						seekBarLocation.write(0xAA);
						seekBarLocation.write(0x55);
						seekBarLocation.write(0x04);
						seekBarLocation.write(0xA0);

					}catch(IOException e){
					}
					break;
				case 6:
					try {
						OutputStream seekBarLocation = _socket.getOutputStream();
						seekBarLocation.write(0xAA);
						seekBarLocation.write(0x55);
						seekBarLocation.write(0x04);
						seekBarLocation.write(0xA0);

					}catch(IOException e){
					}
					break;
				case 7:
					try {
						OutputStream seekBarLocation = _socket.getOutputStream();
						seekBarLocation.write(0xAA);
						seekBarLocation.write(0x55);
						seekBarLocation.write(0x04);
						seekBarLocation.write(0xA0);

					}catch(IOException e){
					}
					break;
				case 8:
					try {
						OutputStream seekBarLocation = _socket.getOutputStream();
						seekBarLocation.write(0xAA);
						seekBarLocation.write(0x55);
						seekBarLocation.write(0x04);
						seekBarLocation.write(0xA0);

					}catch(IOException e){
					}
					break;
				case 9:
					try {
						OutputStream seekBarLocation = _socket.getOutputStream();
						seekBarLocation.write(0xAA);
						seekBarLocation.write(0x55);
						seekBarLocation.write(0x04);
						seekBarLocation.write(0xA0);

					}catch(IOException e){
					}
					break;
				default:			//发送停止指令
					try {
					OutputStream seekBarLocation = _socket.getOutputStream();
					seekBarLocation.write(0xAA);
					seekBarLocation.write(0x55);
					seekBarLocation.write(0x04);
					seekBarLocation.write(0xA0);

				}catch(IOException e){
				}
					break;




			}
			}

		@Override
		// 当用户开始滑动滑块的时候，调用此方法
	public void onStartTrackingTouch(SeekBar seekBar) {
			Log.i(TAG, "on    Start   Tracking   Touch方法,当前位置: "
					+ seekBar.getProgress());
						}

			}
}