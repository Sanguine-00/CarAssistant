package njci.software.car.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.search.route.DrivingRoutePlanOption.DrivingPolicy;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import njci.software.car.R;
import njci.software.car.utils.ConstantValues;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BtnMapPreferenceSettingActivity extends Activity implements
        OnClickListener, OnCheckedChangeListener {

    private static CheckBox activity_setting_prefence_cb_avoid_jam,
            activity_setting_prefence_cb_avoid_charge,
            activity_setting_prefence_cb_avoid_high,
            activity_setting_prefence_cb_high_first;
    @Bind(R.id.textHeadTitle)
    TextView textHeadTitle;
    private Intent intent;
    private static boolean isChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btn_map_setting_prefence);
        ButterKnife.bind(this);
        textHeadTitle.setText(R.string.preference);
        intent = getIntent();
        activity_setting_prefence_cb_avoid_jam = (CheckBox) findViewById(R.id.activity_setting_prefence_cb_avoid_jam);
        activity_setting_prefence_cb_avoid_charge = (CheckBox) findViewById(R.id.activity_setting_prefence_cb_avoid_charge);
        activity_setting_prefence_cb_avoid_high = (CheckBox) findViewById(R.id.activity_setting_prefence_cb_avoid_high);
        activity_setting_prefence_cb_high_first = (CheckBox) findViewById(R.id.activity_setting_prefence_cb_high_first);

        activity_setting_prefence_cb_avoid_jam.setOnCheckedChangeListener(this);
        activity_setting_prefence_cb_avoid_charge
                .setOnCheckedChangeListener(this);
        activity_setting_prefence_cb_avoid_high
                .setOnCheckedChangeListener(this);
        activity_setting_prefence_cb_high_first
                .setOnCheckedChangeListener(this);

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (ConstantValues.isChecked[0])
            activity_setting_prefence_cb_avoid_jam.setChecked(true);
        if (ConstantValues.isChecked[1])
            activity_setting_prefence_cb_avoid_charge.setChecked(true);
        if (ConstantValues.isChecked[2])
            activity_setting_prefence_cb_avoid_high.setChecked(true);
        if (ConstantValues.isChecked[3]) {
            activity_setting_prefence_cb_high_first.setChecked(true);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        isChanged = true;
        switch (buttonView.getId()) {
            case R.id.activity_setting_prefence_cb_avoid_jam:
                if (isChecked) {
                    ConstantValues.isChecked[0] = true;
                } else {
                    ConstantValues.isChecked[0] = false;
                }
                ConstantValues.planPreference = BaiduNaviManager.RoutePlanPreference.ROUTE_PLAN_MOD_AVOID_TAFFICJAM;
                ConstantValues.policy = DrivingPolicy.ECAR_AVOID_JAM;
                break;
            case R.id.activity_setting_prefence_cb_avoid_charge:
                if (isChecked) {
                    activity_setting_prefence_cb_high_first.setChecked(false);
                    ConstantValues.isChecked[1] = true;
                    ConstantValues.isChecked[3] = false;
                } else {
                    ConstantValues.isChecked[1] = false;
                }
                ConstantValues.planPreference = BaiduNaviManager.RoutePlanPreference.ROUTE_PLAN_MOD_MIN_TOLL;
                ConstantValues.policy = DrivingPolicy.ECAR_FEE_FIRST;
                break;
            case R.id.activity_setting_prefence_cb_avoid_high:
                if (isChecked) {
                    activity_setting_prefence_cb_high_first.setChecked(false);
                    ConstantValues.isChecked[3] = false;
                    ConstantValues.isChecked[2] = true;
                } else {
                    ConstantValues.isChecked[2] = false;
                }
                ConstantValues.planPreference = BaiduNaviManager.RoutePlanPreference.ROUTE_PLAN_MOD_MIN_DIST;
                ConstantValues.policy = DrivingPolicy.ECAR_DIS_FIRST;
                break;
            case R.id.activity_setting_prefence_cb_high_first:
                if (isChecked) {
                    activity_setting_prefence_cb_avoid_high.setChecked(false);
                    activity_setting_prefence_cb_avoid_charge.setChecked(false);
                    ConstantValues.isChecked[3] = true;
                    ConstantValues.isChecked[2] = false;
                    ConstantValues.isChecked[1] = false;
                } else {
                    ConstantValues.isChecked[3] = false;
                }

                ConstantValues.planPreference = BaiduNaviManager.RoutePlanPreference.ROUTE_PLAN_MOD_MIN_TIME;
                ConstantValues.policy = DrivingPolicy.ECAR_TIME_FIRST;
                break;

            default:
                break;
        }

    }

    @Override
    public void onBackPressed() {
        if (isChanged) {
            setResult(R.id.imgPreference, intent);
            Toast.makeText(BtnMapPreferenceSettingActivity.this, "重新规划路径",
                    Toast.LENGTH_SHORT).show();
        } else {
            setResult(RESULT_CANCELED, intent);
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                    finish();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }).start();
    }


    @OnClick(R.id.btnBack)
    public void onClick(View view) {
        if (isChanged) {
            setResult(R.id.imgPreference, intent);
            Toast.makeText(BtnMapPreferenceSettingActivity.this, "重新规划路径",
                    Toast.LENGTH_SHORT).show();
        } else {
            setResult(RESULT_CANCELED, intent);
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                    finish();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }).start();
    }
}