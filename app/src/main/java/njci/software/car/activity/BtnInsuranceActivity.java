package njci.software.car.activity;

import android.app.Activity;
import android.os.Bundle;

import com.baidu.mapapi.map.MapView;
import njci.software.car.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BtnInsuranceActivity extends Activity {

    @Bind(R.id.bmapView)
    MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btn_insurance);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

}
