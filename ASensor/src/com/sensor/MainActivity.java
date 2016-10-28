package com.sensor;

import java.util.ArrayList;
import java.util.Iterator;

import com.sensor.R;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final String TAG = "SensorTest";
	private TextView mSensorInfoA;
	private TextView RecordWalkPace;
	private TextView DebugView;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private TestSensorListener mSensorListener;
    private ArrayList<Long> WalkPace = new ArrayList<Long>();
    private ArrayList<Long> referenceWalkPace = new ArrayList<Long>();
    private ArrayList<Double> TotalAcceleration = new ArrayList<Double>();
    private ArrayList<Double> referanceTotalAcceleration = new ArrayList<Double>();
	private ArrayList<Double> DebugData = new ArrayList<Double>();
	private Handler myHandler;
	private Handler myReferanceHandler;
	private boolean ifThreadcalSteps = true;
	private boolean ifThreadReferToWave = true;
	private long lastTime = System.currentTimeMillis();
	private long referanceLastTime = System.currentTimeMillis();
	private long LastreferenceWalkPace = 0;
	private ArrayList<Double> STD = new ArrayList<Double>();
	private int STD_length = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        // 初始化传感器
        mSensorListener = new TestSensorListener();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        
        myHandler = new Handler() {
        	@Override
        	public void handleMessage(Message msg) {
        		if (msg.what == Constant.msgFromCalStemps) {
        			STD.add(DebugData.get(0));
        			if (STD.size() > STD_length) {
        				STD.remove(0);
        			}
        			if (WalkPace.get(1)-lastTime >= 200) {
        				if (ifAddInto()) {
        					RecordWalkPace.setText("走路步数："+WalkPace.get(0));
        					lastTime = WalkPace.get(1);
        				}
        				else {
        					WalkPace.set(0, WalkPace.get(0)-1);
            				WalkPace.set(1, lastTime);
            				RecordWalkPace.setText("走路步数："+WalkPace.get(0));
        				}
        			}
        			else if ((WalkPace.get(1) - lastTime) > 0){
        				WalkPace.set(0, WalkPace.get(0)-1);
        				WalkPace.set(1, lastTime);
        				RecordWalkPace.setText("走路步数："+WalkPace.get(0));
        			}
        			else {
        				RecordWalkPace.setText("走路步数："+WalkPace.get(0));
        			}
        			DebugView.setText("B_STD = "+DebugData.get(0)+"\n"+"Relative_AB_max = "+DebugData.get(1));
        			DebugData.clear();
        			ifThreadcalSteps = true;
        		}
        	}
        };
        myReferanceHandler = new Handler() {
        	public void handleMessage(Message msg) {
        		if (msg.what == Constant.magFromReferToWave) {
        			ifThreadReferToWave = true;
        			if (ifAddInto()) {
        				if ((referenceWalkPace.get(1)-referanceLastTime)!=0 && (referenceWalkPace.get(0) - LastreferenceWalkPace)/(referenceWalkPace.get(1)-referanceLastTime) > 5000) {
            				referenceWalkPace.set(0, (long)LastreferenceWalkPace);
            				referenceWalkPace.set(1, referanceLastTime);
            			}
            			else {
            				synchronized (WalkPace) {
            					if (referenceWalkPace.get(0) > WalkPace.get(0) && referenceWalkPace.get(1) >= WalkPace.get(1)) {
                					WalkPace.set(0, referenceWalkPace.get(0));
                				}
                				else if (referenceWalkPace.get(0) <= WalkPace.get(0) && referenceWalkPace.get(1) >= WalkPace.get(1)) {
                					referenceWalkPace.set(0, WalkPace.get(0));
                				}
    						}
            			}
        			}
        			else {
        				referenceWalkPace.set(0, LastreferenceWalkPace);
        				referenceWalkPace.set(1, referanceLastTime);
        			}
        			referanceLastTime = referenceWalkPace.get(1);
        			LastreferenceWalkPace = referenceWalkPace.get(0);
        		}
        	}
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 注册传感器监听函数
        mSensorManager.registerListener(mSensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 注销监听函数
        mSensorManager.unregisterListener(mSensorListener);
    }

    private void initViews() {
        mSensorInfoA = (TextView) findViewById(R.id.accValue);
        RecordWalkPace = (TextView) findViewById(R.id.WalkNum);
        DebugView = (TextView) findViewById(R.id.DebugView);
        WalkPace.add((long)0);
        WalkPace.add(System.currentTimeMillis());
        lastTime = WalkPace.get(1);
        referenceWalkPace.add((long)0);
        referenceWalkPace.add(System.currentTimeMillis());
        referanceLastTime = referenceWalkPace.get(1);
    }
    
    private boolean ifAddInto() {
    	int num = 0;
    	Iterator<Double> it = STD.iterator();
    	while (it.hasNext()) {
    		if (it.next() >= 0.5) {
    			num = num + 1;
    		}
    	}
    	if (num >= STD_length/2) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }


    class TestSensorListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // 读取加速度传感器数值，values数组0,1,2分别对应x,y,z轴的加速度
            Log.i(TAG, "onSensorChanged: " + event.values[0] + ", " + event.values[1] + ", " + event.values[2]);
            TotalAcceleration.add(MathTools.calTotalAcceleration(event.values[0], event.values[1], event.values[2]));
            referanceTotalAcceleration.add(MathTools.calTotalAcceleration(event.values[0], event.values[1], event.values[2]));
            mSensorInfoA.setText("onSensorChanged: " + event.values[0] + ", " + event.values[1] + ", " + event.values[2]+","+TotalAcceleration.get(TotalAcceleration.size()-1));
            if (ifThreadcalSteps && TotalAcceleration.size() >= 24) {
            	ifThreadcalSteps = false;
            	Thread calSteps = new CalSteps(TotalAcceleration, WalkPace, DebugData, myHandler);
            	calSteps.start();
            }
            if (ifThreadReferToWave && referanceTotalAcceleration.size() >= 9) {
            	ifThreadReferToWave = false;
            	Thread refertowave = new ReferToWave(referanceTotalAcceleration, referenceWalkPace, myReferanceHandler);
            	refertowave.start();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.i(TAG, "onAccuracyChanged");
            mSensorInfoA.setText("onAccuracyChanged");
        }
        
        

    }
}
