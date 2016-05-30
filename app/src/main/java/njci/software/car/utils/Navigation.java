package njci.software.car.utils;

import java.io.File;
import java.util.List;

import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.baidu.navisdk.adapter.BaiduNaviManager.NaviInitListener;
import com.baidu.navisdk.adapter.BaiduNaviManager.RoutePlanListener;

import njci.software.car.activity.BtnGuideActivity;
import njci.software.car.ui.UIHelper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class Navigation implements DialogInterface.OnCancelListener {

    public static final String ROUTE_PLAN_NODE = "routePlanNode";
    private List<BNRoutePlanNode> list = null;
    private Activity mActivity;
    private ProgressDialog progressDialog;

    // private

    @SuppressWarnings("unchecked")
    public Navigation(List<BNRoutePlanNode> list, Activity activity) {
//        MainActivity.activityList.add(this);
        this.list = list;
        this.mActivity = activity;
        if (initDirs()) {
            initNavi();
        }
    }

    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(
                Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    BNOuterTTSPlayerCallback ttsCallback = null;
    String mSDCardPath;

    String APP_FOLDER_NAME = "MyNavigationDemo";

    private void initNavi() {

        System.out.println(mSDCardPath);

        // BNOuterTTSPlayerCallback ttsCallback = null;
        BaiduNaviManager.getInstance().init(mActivity, mSDCardPath,
                APP_FOLDER_NAME, new NaviInitListener() {
                    @Override
                    public void onAuthResult(int status, String msg) {
                    }

                    public void initSuccess() {
                        // Toast.makeText(MainActivity.this, "百度导航引擎初始化成功",
                        // Toast.LENGTH_SHORT).show();
                        initSetting();
                        routePlanToNavi(list);
                    }

                    public void initStart() {
                        // Toast.makeText(MainActivity.this, "百度导航引擎初始化开始",
                        // Toast.LENGTH_SHORT).show();
                    }

                    public void initFailed() {
                        // Toast.makeText(MainActivity.this, "百度导航引擎初始化失败",
                        // Toast.LENGTH_SHORT).show();
                    }

                }, null, ttsHandler, ttsPlayStateListener);

    }

    /**
     * 内部TTS播报状态回传handler
     */
    private Handler ttsHandler = new Handler() {
        public void handleMessage(Message msg) {
            int type = msg.what;

            switch (type) {
                case BaiduNaviManager.TTSPlayMsgType.PLAY_START_MSG: {
//                    showToastMsg("Handler : TTS play start");
                    break;
                }
                case BaiduNaviManager.TTSPlayMsgType.PLAY_END_MSG: {
//                    showToastMsg("Handler : TTS play end");
                    break;
                }
                default:
                    break;
            }
        }
    };

    /**
     * 内部TTS播报状态回调接口
     */
    private BaiduNaviManager.TTSPlayStateListener ttsPlayStateListener = new BaiduNaviManager.TTSPlayStateListener() {

        @Override
        public void playEnd() {
            // showToastMsg("TTSPlayStateListener : TTS play end");
        }

        @Override
        public void playStart() {
            // showToastMsg("TTSPlayStateListener : TTS play start");
        }
    };

//    public void showToastMsg(final String msg) {
//        MainActivity.this.runOnUiThread(new Runnable() {
//
//            @Override
//            public void run() {
//                // Toast.makeText(MainActivity.this, msg,
//                // Toast.LENGTH_SHORT)
//                // .show();
//            }
//        });
//    }

    private void routePlanToNavi(List<BNRoutePlanNode> list) {
        list.add(new BNRoutePlanNode(
                ConstantValues.endLoc.longitude,
                ConstantValues.endLoc.latitude,
                ConstantValues.endName, null,
                BNRoutePlanNode.CoordinateType.BD09LL));
        if (list != null) {
            BaiduNaviManager.getInstance().launchNavigator(
                    mActivity, list,
                    ConstantValues.planPreference, true,
                    new DemoRoutePlanListener(list.get(0)));
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        System.exit(0);
    }

    public class DemoRoutePlanListener implements RoutePlanListener {

        private BNRoutePlanNode mBNRoutePlanNode = null;

        public DemoRoutePlanListener(BNRoutePlanNode node) {
            this.mBNRoutePlanNode = node;
        }

        @Override
        public void onJumpToNavigator() {

            /**
             * 设置途经点已经resetEndNode会回调该接口
             */

            Intent intent = new Intent(mActivity,
                    BtnGuideActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(ROUTE_PLAN_NODE,
                    (BNRoutePlanNode) mBNRoutePlanNode);
            intent.putExtras(bundle);
            hideProgressDialog();
            UIHelper.showBtnGuideActivity(mActivity);
        }

        @Override
        public void onRoutePlanFailed() {
            hideProgressDialog();
            Toast.makeText(mActivity, "算路失败", Toast.LENGTH_SHORT)
                    .show();
        }

    }

    private void initSetting() {
        BNaviSettingManager
                .setDayNightMode(BNaviSettingManager.DayNightMode.DAY_NIGHT_MODE_DAY);
        BNaviSettingManager
                .setShowTotalRoadConditionBar(BNaviSettingManager.PreViewRoadCondition.ROAD_CONDITION_BAR_SHOW_ON);
        BNaviSettingManager
                .setVoiceMode(BNaviSettingManager.VoiceMode.Novice);
        BNaviSettingManager
                .setPowerSaveMode(BNaviSettingManager.PowerSaveMode.DISABLE_MODE);
        BNaviSettingManager
                .setRealRoadCondition(BNaviSettingManager.RealRoadCondition.NAVI_ITS_ON);
    }

    private void showProgressDialog() {
        progressDialog = ProgressDialog.show(mActivity, null, ConstantValues.pleaseWait);
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(this);
    }

    private void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }


}
