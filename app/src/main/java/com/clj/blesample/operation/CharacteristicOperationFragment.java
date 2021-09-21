package com.clj.blesample.operation;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.clj.blesample.R;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleIndicateCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleNotifyCallbackG;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CharacteristicOperationFragment extends Fragment {

    public static final int PROPERTY_READ = 1;
    public static final int PROPERTY_WRITE = 2;
    public static final int PROPERTY_WRITE_NO_RESPONSE = 3;
    public static final int PROPERTY_NOTIFY = 4;
    public static final int PROPERTY_INDICATE = 5;

    private LinearLayout layout_container;
    private List<String> childList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_characteric_operation, null);
        initView(v);
        return v;
    }

    private void initView(View v) {
        layout_container = (LinearLayout) v.findViewById(R.id.layout_container);
    }

    byte[] sendcg = new byte[]{0x01};
    public static final String UUID_LOST_WRITE = "2ea78970-7d44-44bb-b097-26183f402409";   //getCharacteristic 默认初始化设备发送指令
    public static final String UUID_LOST_ENABLE1 = "2ea78970-7d44-44bb-b097-26183f402401";  //getCharacteristic 加速度通知
    public static final String UUID_LOST_ENABLE2 = "2ea78970-7d44-44bb-b097-26183f402402";  //getCharacteristic 陀螺仪通知
    private final float RATE_A = 2048;
    private final float RATE_G = 16.4f;
    public List<Float> AX = new ArrayList<Float>();
    public List<Float> AY = new ArrayList<Float>();
    public List<Float> AZ = new ArrayList<Float>();
    public List<Float> GX = new ArrayList<Float>();
    public List<Float> GY = new ArrayList<Float>();
    public List<Float> GZ = new ArrayList<Float>();



    public void showData() {
//        Log.w("Operation","showData");
        final BleDevice bleDevice = ((OperationActivity) getActivity()).getBleDevice();
        final BluetoothGattService service = ((OperationActivity) getActivity()).getBluetoothGattService();
        final BluetoothGattCharacteristic characteristic = ((OperationActivity) getActivity()).getCharacteristic();
        final int charaProp = ((OperationActivity) getActivity()).getCharaProp();
        String child = characteristic.getUuid().toString() + String.valueOf(charaProp);

        for (int i = 0; i < layout_container.getChildCount(); i++) {
            layout_container.getChildAt(i).setVisibility(View.GONE);
        }
        if (childList.contains(child)) {
            layout_container.findViewWithTag(bleDevice.getKey() + characteristic.getUuid().toString() + charaProp).setVisibility(View.VISIBLE);
        } else {
            childList.add(child);

            View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation, null);
            view.setTag(bleDevice.getKey() + characteristic.getUuid().toString() + charaProp);
            LinearLayout layout_add = (LinearLayout) view.findViewById(R.id.layout_add);
            final TextView txt_title = (TextView) view.findViewById(R.id.txt_title);
            //标题为uuid的数据变化
            txt_title.setText(String.valueOf(characteristic.getUuid().toString() + getActivity().getString(R.string.data_changed)));
            final TextView txt = (TextView) view.findViewById(R.id.txt);
            final TextView txt2 = (TextView) view.findViewById(R.id.txt2);
            txt.setMovementMethod(ScrollingMovementMethod.getInstance());
            txt2.setMovementMethod(ScrollingMovementMethod.getInstance());

            switch (charaProp) {
                case PROPERTY_READ: {
                    View view_add = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_button, null);
                    Button btn = (Button) view_add.findViewById(R.id.btn);
                    btn.setText(getActivity().getString(R.string.read));
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            BleManager.getInstance().read(
                                    bleDevice,
                                    characteristic.getService().getUuid().toString(),
                                    characteristic.getUuid().toString(),
                                    new BleReadCallback() {

                                        @Override
                                        public void onReadSuccess(final byte[] data) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    addText(txt, HexUtil.formatHexString(data, true));
                                                }
                                            });
                                        }

                                        @Override
                                        public void onReadFailure(final BleException exception) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    addText(txt, exception.toString());
                                                }
                                            });
                                        }
                                    });
                        }
                    });
                    layout_add.addView(view_add);
                }
                break;

                case PROPERTY_WRITE: {
                    View view_add = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_et, null);
                    final EditText et = (EditText) view_add.findViewById(R.id.et);
                    Button btn = (Button) view_add.findViewById(R.id.btn);
                    btn.setText(getActivity().getString(R.string.write));
                    BleManager.getInstance().write(bleDevice,
                            characteristic.getService().getUuid().toString(),
                            characteristic.getUuid().toString(),sendcg,
//                                    HexUtil.hexStringToBytes(hex),
                            new BleWriteCallback() {
                                @Override
                                public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            addText(txt, "write success, current: " + current
                                                    + " total: " + total
                                                    + " justWrite: " + HexUtil.formatHexString(justWrite, true));
                                        }
                                    });
                                }
                                @Override
                                public void onWriteFailure(final BleException exception) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            addText(txt, exception.toString());
                                        }
                                    });
                                }
                            });

                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String hex = et.getText().toString();
                            if (TextUtils.isEmpty(hex)) {
                                return;
                            }
                            addText(txt, "write success, justWrite: " + hex);

                        }
                    });
                    layout_add.addView(view_add);
                }
                break;

                case PROPERTY_WRITE_NO_RESPONSE: {
                    View view_add = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_et, null);
                    final EditText et = (EditText) view_add.findViewById(R.id.et);
                    Button btn = (Button) view_add.findViewById(R.id.btn);
                    btn.setText(getActivity().getString(R.string.write));
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String hex = et.getText().toString();
                            if (TextUtils.isEmpty(hex)) {
                                return;
                            }
                            BleManager.getInstance().write(
                                    bleDevice,
                                    characteristic.getService().getUuid().toString(),
                                    characteristic.getUuid().toString(),
                                    HexUtil.hexStringToBytes(hex),
                                    new BleWriteCallback() {

                                        @Override
                                        public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    addText(txt, "write success, current: " + current
                                                            + " total: " + total
                                                            + " justWrite: " + HexUtil.formatHexString(justWrite, true));
                                                }
                                            });
                                        }

                                        @Override
                                        public void onWriteFailure(final BleException exception) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    addText(txt, exception.toString());
                                                }
                                            });
                                        }
                                    });
                        }
                    });
                    layout_add.addView(view_add);
                }
                break;

                case PROPERTY_NOTIFY: {
//                    Log.e("notify",bleDevice.toString());
                    View view_add = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_button, null);
                    final Button btn = (Button) view_add.findViewById(R.id.btn);
                    btn.setText(getActivity().getString(R.string.open_notification));
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (btn.getText().toString().equals(getActivity().getString(R.string.open_notification))) {
                                btn.setText(getActivity().getString(R.string.close_notification));

                                switch (characteristic.getUuid().toString()){
                                    case UUID_LOST_ENABLE1:
                                        BleManager.getInstance().notify(
                                                bleDevice,
                                                characteristic.getService().getUuid().toString(),
                                                UUID_LOST_ENABLE1,
                                                new BleNotifyCallback() {
                                                    @Override
                                                    public void onNotifySuccess() {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                addText(txt, "notify success");
                                                            }
                                                        });
                                                    }
                                                    @Override
                                                    public void onNotifyFailure(final BleException exception) {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                addText(txt, exception.toString());
                                                            }
                                                        });
                                                    }
                                                    @Override
                                                    public void onCharacteristicChanged(byte[] data) {
                                                        runOnUiThread(new Runnable() {
                                                            @RequiresApi(api = Build.VERSION_CODES.O)
                                                            @Override
                                                            public void run() {
//                                                                System.out.println(HexUtil.formatHexString(characteristic.getValue()));
                                                                float Acc[] = dealtBleA(characteristic.getValue());
                                                                addText(txt, FArrayToString(Acc));
                                                                AX.add(Acc[0]);
                                                                AY.add(Acc[1]);
                                                                AZ.add(Acc[2]);
//                                                                if (AX.size()>1){
//                                                                    if(Acc[0]==AX.get(AX.size()-2)){
//                                                                        AX.remove(AX.size()-1);
//                                                                        AY.remove(AX.size()-1);
//                                                                        AZ.remove(AX.size()-1);
//                                                                    }
//                                                                }

                                                                Calendar cal = Calendar.getInstance();
                                                                Date date = cal.getTime();
                                                                String time= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(date);
//                                                                System.out.println("time:"+time+"   data:"+Acc[1]);
//                                                                System.out.println("time:"+time+"   data_size:"+AY.size());
                                                                if (AX.size()%100==0){
                                                                    Judgement();
//                                                                    System.out.println("time:"+time+"   data_size:"+AY.size());
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                        break;
                                    case UUID_LOST_ENABLE2:
                                        BleManager.getInstance().notify(
                                                bleDevice,
                                                characteristic.getService().getUuid().toString(),
                                                UUID_LOST_ENABLE2,
                                                new BleNotifyCallback() {

                                                    @Override
                                                    public void onNotifySuccess() {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                addText(txt, "notify success");
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onNotifyFailure(final BleException exception) {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                addText(txt, exception.toString());
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onCharacteristicChanged(byte[] data) {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
//                                                                addText(txt, HexUtil.formatHexString(characteristic.getValue(), true));
                                                                float Gro[] = dealtBleG(characteristic.getValue());
                                                                addText(txt, FArrayToString(Gro));
                                                                GX.add(Gro[0]);
                                                                GY.add(Gro[1]);
                                                                GZ.add(Gro[2]);

                                                                Calendar cal = Calendar.getInstance();
                                                                Date date = cal.getTime();
                                                                String time= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(date);
//                                                                System.out.println("time:"+time+"   data:"+Gro[1]);
                                                            }
                                                        });
                                                    }
                                                });
                                        break;
                                }

                            } else {
                                btn.setText(getActivity().getString(R.string.open_notification));
                                BleManager.getInstance().stopNotify(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString());
                            }
                        }
                    });
                    layout_add.addView(view_add);
                }
                break;

                case PROPERTY_INDICATE: {
                    View view_add = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_button, null);
                    final Button btn = (Button) view_add.findViewById(R.id.btn);
                    btn.setText(getActivity().getString(R.string.open_notification));
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (btn.getText().toString().equals(getActivity().getString(R.string.open_notification))) {
                                btn.setText(getActivity().getString(R.string.close_notification));
                                BleManager.getInstance().indicate(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString(),
                                        new BleIndicateCallback() {

                                            @Override
                                            public void onIndicateSuccess() {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addText(txt, "indicate success");
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onIndicateFailure(final BleException exception) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addText(txt, exception.toString());
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCharacteristicChanged(byte[] data) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addText(txt, HexUtil.formatHexString(characteristic.getValue(), true));
                                                    }
                                                });
                                            }
                                        });
                            } else {
                                btn.setText(getActivity().getString(R.string.open_notification));
                                BleManager.getInstance().stopIndicate(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString());
                            }
                        }
                    });
                    layout_add.addView(view_add);
                }
                break;
            }

            layout_container.addView(view);
        }
    }

    private void runOnUiThread(Runnable runnable) {
        if (isAdded() && getActivity() != null)
            getActivity().runOnUiThread(runnable);
    }
//    textview中显示数据
    private void addText(TextView textView, String content) {
        textView.append(content);
        textView.append("\n");
        int offset = textView.getLineCount() * textView.getLineHeight();
        if (offset > textView.getHeight()) {
            textView.scrollTo(0, offset - textView.getHeight());
        }
    }

    //数据变字符串
    private String FArrayToString(float[] data){
        float x = data[0];
        float y = data[1];
        float z = data[2];
        return String.valueOf(x)+"         "+String.valueOf(y)+"          "+String.valueOf(z);
    }

    //计算加速度
    private float[] dealtBleA(byte[] data) {
        float ax = byteArrayToInt(data[3],data[4])/RATE_A;
        float ay = byteArrayToInt(data[5],data[6])/RATE_A;
        float az = byteArrayToInt(data[7],data[8])/RATE_A;
        float Acc[] = {ax,ay,az};
        return Acc;
    }

    //计算重力角速度
    private float[] dealtBleG(byte[] data) {
        float gx = byteArrayToInt(data[3], data[4]) / RATE_G;
        float gy = byteArrayToInt(data[5], data[6]) / RATE_G;
        float gz = byteArrayToInt(data[7], data[8]) / RATE_G;
        float[] Gro = {gx,gy,gz};
        return Gro;
    }

    //字节数组变int
    public int byteArrayToInt(byte byte1,byte byte2) {
        return (byte2 << 8) | (byte1 & 0xFF);
    }


    public int endPoingt = 0;
//  判断是否存在动作
    public void Judgement(){
//        System.out.println("judgement");
        List<Float> ax =  AX;List<Float> ay = AY;List<Float> az = AZ;
//        List<Float> gx = GX;List<Float> gy = GY;List<Float> gz = GZ;

//        将之前动作数据归0
        if (endPoingt!=0){
            for (int i =0;i<endPoingt;i++){
                ax.set(i, (float) 0);ay.set(i, (float) 0);az.set(i, (float) 0);
//                gx.set(i, (float) 0);gy.set(i, (float) 0);gz.set(i, (float) 0);
            }
        }

        int num = 0;//        有多少个超过阈值的点
        for (int i=endPoingt;i<ay.size();i++){
            if (ay.get(i)>0){
                num++;
            }
        }
//        一定数量超过阈值的点，则判断存在动作
        int threshold_num = 90;
        if (num>=threshold_num){
            findPeakMotion(ax,ay,az);
//            findPeakMotion(ax,ay,az,gx,gy,gz);
        }else {
//            System.out.println(0);
        }
    }

    public int numOfMotion = 0;
    public int width =85;
//    峰值检测，时间窗，动作分割
    public void findPeakMotion(List<Float> ax,List<Float> ay,List<Float> az
//                        ,List<Float> gx,List<Float> gy,List<Float> gz
    ){
//        System.out.println("findPeakMotion");
        float max = Collections.max(ay);
        int index = ay.indexOf(max);
        List<Float> motion_ax = new ArrayList<Float>();
        List<Float> motion_ay = new ArrayList<Float>();
        List<Float> motion_az = new ArrayList<Float>();
//        List<Float> motion_gx = new ArrayList<Float>();
//        List<Float> motion_gy = new ArrayList<Float>();
//        List<Float> motion_gz = new ArrayList<Float>();

//        System.out.println("endpoint"+endPoingt/100+"s");
//        System.out.println("peak"+index/100+"s");

//        峰值处于采集的数据之间，截取动作区间
        if (index-width>endPoingt & index+width<ay.size()){
            for (int j = index-width;j<index+width;j++){
                motion_ax.add(ax.get(j));
                motion_ay.add(ay.get(j));
                motion_az.add(az.get(j));
//                motion_gx.add(gx.get(j));
//                motion_gy.add(gy.get(j));
//                motion_gz.add(gz.get(j));
            }
            endPoingt = index+width;
            numOfMotion++;

            Processing(motion_ax,motion_ay,motion_az);
//            Processing(motion_ax,motion_ay,motion_az,motion_gx,motion_gy,motion_gz);
        }
    }

//    参数
    public double[] mean = {-0.69886182,  0.10999896,  0.28090332, -2.38289307,  0.78556224,
            -0.00507273,  0.25686676,  1.12628785, -1.48653809,  0.55476286,
            -0.08734604,  0.17781622,  0.99914062, -1.08108399,  0.43034831};
    public double[] std = {0.17320876, 0.08062875, 0.48420681, 0.58345651, 0.10630166,
            0.24703707, 0.10470654, 0.46845143, 0.40601352, 0.10078683,
            0.05169459, 0.03763278, 0.26292614, 0.27349734, 0.05402046};
    public double[] coef = {0.57946462,  0.6009459 ,  0.47995817,  0.32536875, -0.53527256,
            0.64140648,  0.60129801,  0.30354943,  0.11206416,  0.53492266,
            -0.42905939,  0.26390282,  0.38501974,  0.24230262,  0.29362332};
    public double intercept = -0.19362116;
    public double threshold = 0.5;
//    滤波，特征提取，标准化，分类
    public void Processing(List<Float> motion_ax,List<Float> motion_ay,List<Float> motion_az
//                           ,List<Float> motion_gx,List<Float> motion_gy,List<Float> motion_gz
    ){
//        System.out.println("Processing");
        List<Float> ax_filted = MovingAverage(motion_ax);
        List<Float> ay_filted = MovingAverage(motion_ay);
        List<Float> az_filted = MovingAverage(motion_ax);
//        List<Float> gx_filted = MovingAverage(motion_gx);
//        List<Float> gy_filted = MovingAverage(motion_gy);
//        List<Float> gz_filted = MovingAverage(motion_gz);
        double a = Collections.max(ax_filted);
        float[] feature = {Collections.max(ax_filted),Collections.min(ax_filted),Average(ax_filted), Variance(ax_filted),RMS(ax_filted),
                Collections.max(ay_filted),Collections.min(ay_filted),Average(ay_filted), Variance(ay_filted),RMS(ay_filted),
                Collections.max(az_filted),Collections.min(az_filted),Average(az_filted), Variance(az_filted),RMS(az_filted),
//                Collections.max(gx_filted),Collections.min(gx_filted),Average(gx_filted), Variance(gx_filted),RMS(gx_filted),
//                Collections.max(gy_filted),Collections.min(gy_filted),Average(ay_filted), Variance(gy_filted),RMS(gy_filted),
//                Collections.max(gz_filted),Collections.min(gz_filted),Average(gz_filted), Variance(gz_filted),RMS(gz_filted)
        };
        float[] feature_scalered = new float[feature.length];
        for (int i =0;i<feature.length;i++){
            feature_scalered[i] = (float) ((feature[i]-mean[i])/std[i]);
        }
        double probability = 0;
        for (int i =0; i<feature.length;i++){
            probability=probability+feature[i]*coef[i];
        }
        probability+=intercept;
        probability = 1/(1+Math.pow(Math.E,-1*probability));
        System.out.println("probability"+probability);

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation, null);
        final TextView txt2 = (TextView) view.findViewById(R.id.txt2);
        txt2.setMovementMethod(ScrollingMovementMethod.getInstance());
        if (probability>threshold){
            System.out.println("第"+numOfMotion+"个动作为标准仰卧起坐");
            addText(txt2,"第"+ String.valueOf(numOfMotion)+"个动作为标准仰卧起坐");
        }else {
            System.out.println("第"+numOfMotion+"个动作为不完整仰卧起坐");
            addText(txt2,"第"+ String.valueOf(numOfMotion)+"个动作为不完整仰卧起坐");
        }
    }

//    加权移动平均滤波
    public List<Float> MovingAverage(List<Float> data){
        int size = data.size();
        List<Float> data_filted = new ArrayList<Float>();
        data_filted.add(data.get(0)*1/3);
        data_filted.add(data.get(1)*1/3+data.get(0)*1/3);
        for (int i =0;i<size-2;i++){
            data_filted.add(data.get(i)*1/4+data.get(i+1)*1/2+data.get(i+2)*1/4);
        }
        return data_filted;
    }

//    均值
    public float Average(List<Float> data){
        int size = data.size();
        int sum = 0;
        for (int i =0;i<size;i++){
            sum+=data.get(i);
        }
        float ave = sum/size;
        return ave;
    }
//    方差
    public float Variance(List<Float> data){
        int size = data.size();
        float average = Average(data);
        float sum = 0;
        for (int i = 0;i<size;i++){
            sum+=(data.get(i)-average)*(data.get(i)-average);
        }
        float var = sum/(size-1);
        return var;
    }
    //均方根
    public float RMS(List<Float> data){
        int size = data.size();
        float sum = 0;
        for (int i = 0;i<size;i++){
            sum+=data.get(i)*data.get(i);
        }
        float rms = sum/size;
        rms = (float) Math.sqrt(rms);
        return rms;
    }
}



