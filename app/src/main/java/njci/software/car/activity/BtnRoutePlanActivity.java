package njci.software.car.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;
import njci.software.car.R;

import njci.software.car.ui.UIHelper;
import njci.software.car.utils.ConstantValues;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BtnRoutePlanActivity extends BaseFragmentActivity implements OnClickListener,
        OnGetRoutePlanResultListener, BDLocationListener {
    private static String hint;

    @Bind(R.id.tvRoutePlanCity)
    TextView tvRoutePlanCity;

    @Bind(R.id.tvRoutePlanStartNode)
    TextView tvRoutePlanStartNode;

    @Bind(R.id.tvRoutePlanEndNode)
    TextView tvRoutePlanEndNode;


    @Bind(R.id.btnBack)
    Button btnBack;

    @Bind(R.id.textHeadTitle)
    TextView textHeadTitle;

    private RoutePlanSearch routeSearch;
    private DrivingRoutePlanOption drivingOption;
    private Intent intent;


    private LocationClient mLocClient;
    private LocationClientOption mLocCliOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btn_routeplan);
        ButterKnife.bind(this);
        textHeadTitle.setText(R.string.home_route);
        btnBack.setVisibility(View.VISIBLE);
        intent = getIntent();
        routeSearch = RoutePlanSearch.newInstance();
        routeSearch.setOnGetRoutePlanResultListener(this);
        init();
    }

    public void init() {
        mLocClient = new LocationClient(getApplicationContext());
        mLocCliOption = new LocationClientOption();
        mLocCliOption.setOpenGps(true);
        mLocCliOption
                .setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        mLocCliOption.setCoorType("bd09ll");
        mLocCliOption.setIsNeedAddress(true);
        mLocCliOption.setIsNeedLocationDescribe(true);
        mLocCliOption.setIsNeedLocationPoiList(true);
        mLocClient.setLocOption(mLocCliOption);
        mLocClient.registerLocationListener(this);
        mLocClient.start();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        tvRoutePlanCity.setText(ConstantValues.cityName);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mLocClient.stop();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mLocClient.stop();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
        }
        return super.onKeyDown(keyCode, event);

    }


    @Override
    public void onGetBikingRouteResult(BikingRouteResult result) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
        ConstantValues.drivingRouteResult = result;
        progressBarHelper.goneLoading();
        UIHelper.showRoutePlanResultActivity(BtnRoutePlanActivity.this);
    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult result) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
        // TODO Auto-generated method stub

    }

    @OnClick({R.id.tvRoutePlanCity, R.id.tvRoutePlanBegin, R.id.tvRoutePlanStartNode, R.id.tvRoutePlanEndNode, R.id.imgSetMyLocationAsStartNode, R.id.imgChangeStartNodeAndEndNode})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvRoutePlanCity:

                break;
            case R.id.tvRoutePlanBegin:
                startNavigation();
                break;
            case R.id.tvRoutePlanStartNode:
                try {

                    hint = ConstantValues.home_route_start;
                    Intent intent1 = new Intent(BtnRoutePlanActivity.this,
                            BtnRoutePlanSearchInCityActivity.class);
                    intent1.putExtra("hint", hint);
                    startActivityForResult(intent1, ConstantValues.ROUTE_NODE_SET_START_REQUEST_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.tvRoutePlanEndNode:
                try {
                    hint = "设置终点";
                    Intent intent2 = new Intent(BtnRoutePlanActivity.this,
                            BtnRoutePlanSearchInCityActivity.class);
                    intent2.putExtra("hint", hint);
                    startActivityForResult(intent2, ConstantValues.ROUTE_NODE_SET_END_REQUEST_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.imgSetMyLocationAsStartNode:
                try {

                    ConstantValues.startLoc = ConstantValues.myLoc;
                    ConstantValues.startName = ConstantValues.myLocName;
                    tvRoutePlanStartNode.setText(ConstantValues.MYLOCNAME);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.imgChangeStartNodeAndEndNode:
                changeNode();
                break;
        }
    }

    @OnClick(R.id.btnBack)
    public void onClick() {
        finish();
    }

    public void startNavigation() {
        try {
            if (tvRoutePlanStartNode == null
                    || tvRoutePlanStartNode.getText().toString().trim().equals("")
                    || tvRoutePlanEndNode == null
                    || tvRoutePlanEndNode.getText().toString().trim().equals("")) {
                Toast.makeText(BtnRoutePlanActivity.this, "起点或者终点设置有误", Toast.LENGTH_SHORT).show();
                return;
            } else if (DistanceUtil.getDistance(ConstantValues.startLoc,
                    ConstantValues.endLoc) < 500) {
                UIHelper.ToastMessage(BtnRoutePlanActivity.this, "起始点太接近，无法引起路径规划！");
                return;
            }
            drivingOption = new DrivingRoutePlanOption()
                    .from(PlanNode
                            .withLocation(ConstantValues.startLoc))
                    .to(PlanNode.withLocation(ConstantValues.endLoc))
                    .trafficPolicy(ConstantValues.drivingPolicy)
                    .policy(ConstantValues.policy);
            routeSearch.drivingSearch(drivingOption);
            progressBarHelper.showLoading();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        try {
            super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode) {
                case ConstantValues.ROUTE_NODE_SET_START_REQUEST_CODE:
                    if (resultCode != ConstantValues.ERROR)
                        tvRoutePlanStartNode.setText(ConstantValues.startName);
                    break;

                case ConstantValues.ROUTE_NODE_SET_END_REQUEST_CODE:
                    if (resultCode != ConstantValues.ERROR)
                        tvRoutePlanEndNode.setText(ConstantValues.endName);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        try {
            if (bdLocation != null) {
                ConstantValues.myLoc = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                ConstantValues.myLocName = bdLocation.getAddrStr();
                UIHelper.ToastMessage(BtnRoutePlanActivity.this, "定位成功！");
            } else {
                UIHelper.ToastMessage(BtnRoutePlanActivity.this, "定位出错！");
            }
            progressBarHelper.goneLoading();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    LatLng tempLoc = null;
    String tempName = null;

    public void changeNode() {
        try {

            if (ConstantValues.startLoc == null || ConstantValues.endLoc == null) {
                UIHelper.ToastMessage(BtnRoutePlanActivity.this, "起终点未设置完全，不能交换！");
                return;
            }
            tempLoc = ConstantValues.startLoc;
            tempName = ConstantValues.startName;
            ConstantValues.startLoc = ConstantValues.endLoc;
            ConstantValues.startName = ConstantValues.endName;
            ConstantValues.endLoc = tempLoc;
            ConstantValues.endName = tempName;

            tempName = tvRoutePlanStartNode.getText().toString();
            tvRoutePlanStartNode.setText(tvRoutePlanEndNode.getText().toString().trim());
            tvRoutePlanEndNode.setText(tempName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


