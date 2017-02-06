package com.hawk.test.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MyBluetoothUtil {

	private static final String TAG = MyBluetoothUtil.class.getSimpleName();
	private BluetoothAdapter btAdapter;
	private boolean mScanning = false;
	private BluetoothDevice currentDevice;
	private List<BluetoothGattService> serviceList;
	private List<BluetoothGattCharacteristic> characterList;
	private static BluetoothManager bluetoothManager;
	private static MyBluetoothUtil instance;
	private Context context;
	private BluetoothGattCharacteristic writeCharacteristic;
	private BluetoothGattCharacteristic readCharacteristic;
	private BluetoothGatt bluetoothGatt;
	private BluetoothGattService theService;
	private Timer timer;


	private boolean isConnected = false;//是否已连接蓝牙
	public boolean isSetSuccess = false;
	private int times = 0;// 用于断线连接
//	private int errors = 1;

	private static List<BluetoothDevice> devList;//搜索到的蓝牙设备
	private static boolean isCB =false;//是否正在连接蓝牙
	private static long timeOne =0;//搜索到的第一个蓝牙时间，1秒后结束蓝牙搜索
	private static int cmd =0;//当前执行的命令：1==0x81租车请求，
	// 2==0x82开锁命令, 21==开锁分包1, 22==开锁分包2,
	// 3==读取短信命令, 6==获取交易记录,
	// 99==中间过程, 0==可以重新连接蓝牙

	public static final String myUUID = "0000fff0-0000-1000-8000-00805f9b34fb";
	public static final String READ_UUID = "0000fff6-0000-1000-8000-00805f9b34fb";
	public static final String WRITE_UUID = "0000fff6-0000-1000-8000-00805f9b34fb";

	public static final int ENABLE_BLUETOOTH = 0x123;

	private MyBluetoothUtil(Context context) {
		bluetoothManager = (BluetoothManager) context
				.getSystemService(Context.BLUETOOTH_SERVICE);
		btAdapter = bluetoothManager.getAdapter();
		this.context = context;
	}

	public static MyBluetoothUtil getInstance(Context context) {
		if (instance == null) {
			timeOne = 0;
			devList = new ArrayList<BluetoothDevice>();
			instance = new MyBluetoothUtil(context);
		}
		return instance;
	}

	public void clearInstance() {
		instance = null;
	}

	/**
	 * 重置数据
	 */
	public void selectBle(int index) {
		if(devList==null || devList.size()==0)
			return;
		if(devList.size()>index){
			currentDevice = devList.get(index);
			connectBle();
		}
		else{
			Log.i("sean","_+_+_selectBle");
			initScanLEDevice(2);
		}
	}

	private long resumeTime = 0;
	/**
	 * 重启蓝牙，接收广播
	 */
	public void resumeCheck() {
		Log.e("MyBluetoothUtil", "resumeCheck");
		resumeTime = System.currentTimeMillis();
		if(btAdapter==null) {
			Log.e("MyBluetoothUtil","btAdapter is null");
			return;
		}
		if (btAdapter.isEnabled()
				&& btAdapter.getState() == BluetoothAdapter.STATE_ON) {
			Log.i("sean","_+_+_resumeCheck");
			initScanLEDevice(1);
		}
	}

	private long jgTime = 0;//防止多次执行
	private void initScanLEDevice(int type) {
		if(type == 1) {
			closeBLE();
		}
		else {
			disconnectBLE();
		}

		isConnected = false;

		timeOne =0;
		cmd =0;
		isCB = false;
		mScanning = true;

		// 这里添加搜索设备UUID
//		UUID[] uuids = new UUID[1];
//		uuids[0] = UUID.fromString(myUUID);
//		btAdapter.startLeScan(uuids,mLeScanCallback);
//		btAdapter.startLeScan(mLeScanCallback);
		searchBle();
	}

	public void closeBLE() {
		Log.i("sean2", "#$#$#$#$#$:closeBLE");
		FristConnect = false;
		if (mScanning) {
			mScanning = false;
			if(btAdapter!=null) {
				Log.i("sean", "&&&&&&&&&&& btAdapter.stopLeScan");
				btAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
			}
		}

		if (bluetoothGatt != null) {
			Log.e(TAG, "#$#$#$#$#$:bluetoothGatt1");
			bluetoothGatt.disconnect();
			bluetoothGatt.close();
			bluetoothGatt = null;
			Log.e(TAG, "#$#$#$#$#$:bluetoothGatt2");
		}

		if(timer!=null)
			timer.cancel();
		timer = null;

		if(timerBle!=null)
			timerBle.cancel();
		timerBle = null;

		if(timerBle22!=null)
			timerBle22.cancel();
		timerBle22 = null;

		if(timerTask2 != null) {
			timerTask2.cancel();
			timerTask2 = null;
		}

//		if(timerCMD!=null)
//			timerCMD.cancel();
//		timerCMD = null;
		if(currentDevice!=null)
			currentDevice = null;

		if(devList!=null)
			devList.clear();
		cmd =0;
	}

	public void disconnectBLE() {
		Log.i("sean2", "#$#$#$#$#$:disconnectBLE");
		if (mScanning) {
			mScanning = false;
			if(btAdapter!=null) {
				Log.i("sean", "&&&&&&&&&&& btAdapter.stopLeScan");
				btAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
			}
		}

		if (bluetoothGatt != null) {
			bluetoothGatt.disconnect();
		}

		if(timer!=null)
			timer.cancel();
		timer = null;

		if(timerBle!=null)
			timerBle.cancel();
		timerBle = null;

		if(timerBle22!=null)
			timerBle22.cancel();
		timerBle22 = null;

		if(timerTask2 != null) {
			timerTask2.cancel();
			timerTask2 = null;
		}

//		if(timerCMD!=null)
//			timerCMD.cancel();
//		timerCMD = null;
		if(currentDevice!=null)
			currentDevice = null;

		if(devList!=null)
			devList.clear();
		cmd =0;
	}

	/**
	 * 写入数据
	 *
	 * @param
	 * @return
	 */
	public boolean writeIn(Activity activity, int type, byte[] data) {
		cmd = type;
		isSetSuccess = false;
		Log.i("sean4", "######write:" + type);
		if (bluetoothGatt != null && writeCharacteristic != null) {
//			boolean result = bluetoothGatt.beginReliableWrite();
			boolean result2 = writeCharacteristic.setValue(data);
			Log.i("sean4", "#"+result2+"#####write2::");
			boolean result3 = bluetoothGatt.writeCharacteristic(writeCharacteristic);//isSetSuccess =
			Log.i("sean4", "######write3:" + result3);
		}
		else {
		}

		return isSetSuccess;
	}

	private Timer timer2 = new Timer();
	private ResetTimerTask timerTask2;
	public void runtime20Reset() {        //20s后自动断开
		if(timerTask2 != null) {
			timerTask2.cancel();
		}
		timerTask2 = new ResetTimerTask();
		timer2.schedule(timerTask2, 20000);
	}
	class ResetTimerTask extends TimerTask  {
		@Override
		public void run() {
			Log.e("Bluetooth", "timout");
			if(isConnected) {
				resumeCheck();
			}

			cmd = 99;
		}
	};

	private boolean FristConnect = false;
	private ScanCallback mLeScanCallback = new ScanCallback() {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			Log.e("error", "LESCAN.name = " + result.getDevice().getName() + " : " + result.getRssi());
		}

		@Override
		public void onBatchScanResults(List<ScanResult> results) {
			Log.e("error", "LESCAN.size = " + results.size());
		}

	};

	private void doBleData(BluetoothDevice device, long newTime) {
		Log.e("MyBluetoothUtil", "cmd:" + cmd);
		if (cmd==0 || cmd==99 ) {
//            if(newTime-jgTime < 6000)
//                return;

			boolean isHas = false;//此蓝牙是否已加入列表
			for(int i=0;i<devList.size();i++){
				if(devList.get(i).getAddress().equals(device.getAddress())){
					isHas = true;
					break;
				}
			}
			if(!isHas) {
				devList.add(0,device);

				if(devList.size()==1){
					connectBle();
				}
			}

			if(timeOne==0)
				timeOne = newTime;
		}
	}

	class MyBluetoothGattCallback extends BluetoothGattCallback {

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			// TODO Auto-generated method stub
			Log.d("sean", "onServicesDiscovered" + status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// 获取SERVICE列表
				serviceList = gatt.getServices();
				// 遍历它 根据UUID搜索对应的Service
				for (int i = 0; i < serviceList.size(); i++) {
					BluetoothGattService service = serviceList.get(i);
					Log.d("sean3", "ServiceName:" + service.getUuid());
					if (myUUID.toLowerCase().equals(
							service.getUuid().toString())) {
						theService = service;
						characterList = theService.getCharacteristics();
						for (int j = 0; j < characterList.size(); j++) {

							Log.d("sean4", "------uuid:" + characterList.get(j).getUuid().toString());
							if (READ_UUID.toLowerCase().equals(
									characterList.get(j).getUuid().toString())) {
								// 读
								readCharacteristic = characterList.get(j);

								// 读数据
//								initReaddata();
							}
							if (WRITE_UUID.toLowerCase().equals(
									characterList.get(j).getUuid().toString())) {
								// 写
								writeCharacteristic = characterList.get(j);

								boolean rr = bluetoothGatt.setCharacteristicNotification(writeCharacteristic, true);

								List<BluetoothGattDescriptor> llls = writeCharacteristic.getDescriptors();
								BluetoothGattDescriptor descriptor = llls.get(0);
								descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
								bluetoothGatt.writeDescriptor(descriptor);
//								descriptor = llls.get(1);
//								descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//								bluetoothGatt.writeDescriptor(descriptor);

								Log.i("sean7", "%%%%%%%%%%" + rr);
							}
						}
						break;
					}

				}

			} else {
//				Intent intent = new Intent();
//				intent.setAction(ActionConfig.CONNECTION_ACTION);
//				Bundle bundle = new Bundle();
//				bundle.putInt("type", 0);
//				intent.putExtras(bundle);
//				activity.sendBroadcast(intent);
				isCB = false;
				timeOne =0;
				cmd =0;
				mScanning = true;
			}
			super.onServicesDiscovered(gatt, status);
		}

		//蓝牙连接变动
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
											int newState) {
			// TODO Auto-generated method stub
			super.onConnectionStateChange(gatt, status, newState);
			if(gatt == null) {
				return;
			}
			Log.e(TAG, "BluetoothProfile.STATE_CONNECTED = "
					+ BluetoothProfile.STATE_CONNECTED + "oldstatus = "
					+ status + ", newstate = " + newState);
			if (BluetoothProfile.STATE_CONNECTED == newState) {
				Log.i("sean", "BLEActivity:" + gatt.getServices().toString());
				gatt.discoverServices();
				isConnected = true;
				isCB = false;
				timeOne =0;
				cmd =0;
				mScanning = true;
			} else {
				Log.e(TAG, "******error:" + newState + "FristConnect:" + FristConnect);
				if (BluetoothProfile.STATE_DISCONNECTED == newState &&
						FristConnect) {//蓝牙状态是连接断开，且第一次连接，做一次重连
					gatt.disconnect();
					gatt.close();
//					gatt.connect();
					isConnected = false;
					FristConnect = false;
					Log.i("sean3", "-------connectGatt 3");
					bluetoothGatt = currentDevice.connectGatt(
							context, false, new MyBluetoothGattCallback());
					Log.i("sean3", "-------connectGatt 4");
				} else {
					isCB = false;
					isConnected = false;
					isCB = false;
					timeOne =0;
					cmd =0;
					mScanning = true;

					gatt.disconnect();
					gatt.close();
					isConnected = false;

				}
			}
		}

		//数据变化
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
											BluetoothGattCharacteristic characteristic) {
			Log.i("sean7", "!!!!@@@@@@onCharacteristicChanged");
			doRead(characteristic);
		}

		//读数据
		@Override
		public void onCharacteristicRead(final BluetoothGatt gatt,
										 final BluetoothGattCharacteristic characteristic, int status) {
			// TODO Auto-generate     d method stub
			Log.i("sean7", "!!!!@@@@@@onCharacteristicRead");
			super.onCharacteristicRead(gatt, characteristic, status);
			doRead(characteristic);

		}

		private void doRead(BluetoothGattCharacteristic characteristic) {
			byte[] data = characteristic.getValue();
			if (data != null) {
				String msg2 = byte2HexStr(data);
				Log.e("test", "!!!!@@@@@@Read:" + msg2);
				if(cmd == 21 || cmd == 22){
					if(ttime < 20){
						ttime ++;
					}
					else{
						if(timer!=null)
							timer.cancel();
						timer = null;

						cmd = 99;
						Log.i("sean4", "@@@@@@Read22:"+msg2);
					}
				}
				else if(cmd == 31 || cmd == 32) {
					if(ttime < 20){
						ttime ++;
					}
					else{
						if(timer!=null)
							timer.cancel();
						timer = null;

						cmd = 99;
						Log.i("sean4", "@@@@@@Read22:"+msg2);
					}
				}

				if((cmd == 1 || cmd == 32) && data.length==16 && data[3]==0x41){
					if(timer!=null)
						timer.cancel();
					timer = null;

					cmd = 99;
					Log.i("sean5", "@@@@@@Read1:"+msg2);
					if(data[4]==0){//待调整0
						byte[] keySource = new byte[4];
						keySource[0] = data[8];
						keySource[1] = data[9];
						keySource[2] = data[10];
						keySource[3] = data[11];
					}
					else{
					}
				}
				else if(cmd == 22 && data.length==6 && data[3]==0x42){
					if(timer!=null)
						timer.cancel();
					timer = null;

					cmd = 99;
					Log.i("sean5", "@@@@@@Read2:" + msg2);
				}
				else if(cmd == 3 && data.length==6 && data[3]==0x43){
					if(timer!=null)
						timer.cancel();
					timer = null;

					cmd = 99;
					String msg = byte2HexStr(data);
					Log.i("sean5", "@@@@@@Read3:"+msg);
					if(data[4]==-1){//待调整0

					}
					else{

					}

				}
				else if(cmd == 6){

				}
			}

			runtime20Reset();
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
										  BluetoothGattCharacteristic characteristic, int status) {
			// TODO Auto-generated method stub
			super.onCharacteristicWrite(gatt, characteristic, status);
			Log.i("sean3", "Write=======::::::" + status);
			String msg = byte2HexStr(characteristic.getValue());
			Log.i("sean3", "Write=======" + msg);
//			String msg2 = byte2HexStr(writeData);
//			Log.i("sean3", "Write=======" + msg + "##" + msg2);

			SystemClock.sleep(20);
			isSetSuccess = true;
		}

	}

	/**
	 * bytes转换成十六进制字符串
	 *
	 * @param b byte[]数组
	 * @return String 每个Byte值之间空格分隔
	 */
	private String byte2HexStr(byte[] b) {
		String stmp = "";
		StringBuilder sb = new StringBuilder("");
		for (int n = 0; n < b.length; n++) {
			stmp = Integer.toHexString(b[n] & 0xFF);
			sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
			// sb.append(" ");
		}
		return sb.toString().toUpperCase().trim();
	}

	/**
	 * 关闭连接通道
	 */
	public void destory() {
		if (bluetoothGatt != null) {
			bluetoothGatt.close();
			btAdapter.disable();
			if (timer != null) {
				timer.cancel();
				timer = null;
			}

		}

	}

	private int ttime = 0;

	/**
	 * 读取短信命令
	 * 0x83
	 */
	public void getMessage() {
		byte[] data = new byte[5];
		data[0] = 0x67;
		data[1] = 0x74;
		data[2] = 0x00;
		data[3] = (byte)0x83;
		data[4] = (byte)0x83;
		writeIn(null,3,data);
	}

	/**
	 * 手机主动取消蓝牙
	 * 0x83
	 */
	public void outBle() {
		byte[] data = new byte[5];
		data[0] = 0x67;
		data[1] = 0x74;
		data[2] = 0x00;
		data[3] = (byte)0x85;
		data[4] = (byte)0x85;
		writeIn(null,0,data);
	}

	//间隔300毫秒执行蓝牙连接
	private Timer timerBle;
	public void connectBle() {
		if (timerBle == null) {
			timerBle = new Timer();
			timerBle.schedule(new TimerTask() {
				@Override
				public void run() {
					if (timerBle != null)
						timerBle.cancel();
					timerBle = null;

					if(!isCB && timeOne!=0){
						jgTime = System.currentTimeMillis();
						isCB = true;
						int len = devList.size();
						if(len==1){
							currentDevice = devList.get(0);
							if (currentDevice != null) {
								if (mScanning) {
									mScanning = false;
									if(btAdapter!=null)
										btAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
								}

								FristConnect = true;
								connectBle22();
							}
						}
						else{
							if (mScanning) {
								mScanning = false;
								if(btAdapter!=null)
									btAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
							}
							String[] list = new String[len];
							for(int i=0;i<len;i++){
								list[i] = devList.get(i).getName();
							}
						}
					}

				}
			}, 300, 300);//几秒后开始，每隔几秒
		}
	}

	//间隔200毫秒执行蓝牙连接
	private Timer timerBle22;
	public void connectBle22() {
		if (timerBle22 == null) {
			timerBle22 = new Timer();
			timerBle22.schedule(new TimerTask() {
				@Override
				public void run() {
					if (timerBle22 != null)
						timerBle22.cancel();
					timerBle22 = null;

					Log.i("sean3", "-------connectGatt 1");
					if(currentDevice!=null && context!=null)
						bluetoothGatt = currentDevice.connectGatt(context,
								false, new MyBluetoothGattCallback());
					Log.i("sean3", "-------connectGatt 2");

				}
			}, 200, 200);//几秒后开始，每隔几秒
		}
	}

	private int tnum = 0;
	//间隔200毫秒执行蓝牙搜索
	private Timer timerBle2;
	public void searchBle() {
		if (timerBle2 == null) {
			tnum = 0;
			timerBle2 = new Timer();
			timerBle2.schedule(new TimerTask() {
				@Override
				public void run() {
					if (timerBle2 != null)
						timerBle2.cancel();
					timerBle2 = null;

					Log.i("sean","&&&&&&&&&&& btAdapter.startLeScan");
					btAdapter.getBluetoothLeScanner().startScan(mLeScanCallback);
				}
			}, 200, 300);//几秒后开始，每隔几秒
		}
	}

}
