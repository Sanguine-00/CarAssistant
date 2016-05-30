package njci.software.car.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.thinkland.sdk.android.DataCallBack;
import com.thinkland.sdk.android.JuheData;
import com.thinkland.sdk.android.Parameters;
import njci.software.car.R;
import njci.software.car.ui.UIHelper;
import njci.software.car.utils.ConstantValues;
import njci.software.car.utils.overlay.DrivingRouteOverlay;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BtnGasActivity extends BaseFragmentActivity implements BaiduMap.OnMarkerClickListener, BaiduMap.OnMapClickListener, BaiduMap.OnPolylineClickListener, BDLocationListener, OnGetPoiSearchResultListener, OnGetRoutePlanResultListener, DialogInterface.OnCancelListener, CompoundButton.OnCheckedChangeListener {


    @Bind(R.id.btnBack)
    Button btnBack;
    @Bind(R.id.textHeadTitle)
    TextView textHeadTitle;
    @Bind(R.id.mGasMapView)
    MapView mMapView;
    @Bind(R.id.includeGasNavigation)
    LinearLayout includeNavigation;
    @Bind(R.id.imgGasPreference)
    ImageView imgPreference;
    @Bind(R.id.tvGasStationName)
    TextView tvGasStationName;
    @Bind(R.id.tvGasStationDis)
    TextView tvGasStationDis;
    @Bind(R.id.tvGasStationAddress)
    TextView tvGasStationAddress;
    @Bind(R.id.tvGasStationOtherInfo)
    TextView tvGasStationOtherInfo;
    @Bind(R.id.includeGasDetail)
    LinearLayout llDetail;
    @Bind(R.id.tvGasTrafficCondition)
    CheckBox tvTrafficCondition;

    private List<Overlay> mOverlayList = new ArrayList<>();
    private boolean isFirstLoc = true;
    private LatLng mLoc;
    private MyLocationData locData;
    private String mCityName;
    private BaiduMap mBaiduMap;
    private LocationClient mLocClient;
    private LocationClientOption mLocCliOption;
    private ProgressDialog progressDialog;

    private PoiSearch poiSearch;

    private RoutePlanSearch routePlanSearch;
    private DrivingRoutePlanOption drivingRoutePlanOption;
    private List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();

    private boolean isRoutePlanning = false;
    private static final String KEY = "4c772be5ccf4dfd5b86945c5e1375e32";
    //    private static final String KEY = "7143e8681606196525ec1e01140d597b";
    private static final String URL = "http://apis.juhe.cn/oil/region?";
    private static List<Bundle> gasStationsList = new ArrayList<>();

//    private HttpResponseHandler handler = new HttpResponseHandler() {
//        @Override
//        public void handleMessage(Message msg) {
//            try {
////                if (msg.what == HttpResponseHandler.SUCCESS_MESSAGE) {
//                JSONAction(((Object[]) msg.obj)[2].toString());
////                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btn_gas);
        ButterKnife.bind(this);
        textHeadTitle.setText(R.string.home_gas);
        btnBack.setVisibility(View.VISIBLE);
        tvTrafficCondition.setOnCheckedChangeListener(this);
        init();
    }

    public void init() {
        try {
            mMapView.showZoomControls(true);
            mBaiduMap = mMapView.getMap();
            mBaiduMap.getUiSettings().setCompassEnabled(false);
            mBaiduMap.setMyLocationEnabled(true);
            mLocClient = new LocationClient(getApplicationContext());
            mLocCliOption = new LocationClientOption();
            mLocCliOption.setOpenGps(true);
            mLocCliOption
                    .setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
            mLocCliOption.setCoorType("bd09ll");
            mLocCliOption.setIsNeedAddress(true);
            mLocCliOption.setIsNeedLocationDescribe(true);
            mLocCliOption.setIsNeedLocationPoiList(true);
//        mLocCliOption.setIseedAltitude(true);
            mLocCliOption.setScanSpan(1000);
            mLocClient.setLocOption(mLocCliOption);
            mLocClient.registerLocationListener(this);
            mBaiduMap.setOnMarkerClickListener(this);
            mBaiduMap.setOnMapClickListener(this);
            mBaiduMap.setOnPolylineClickListener(this);
            mLocClient.start();
            initSearch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initSearch() {
        try {
            poiSearch = PoiSearch.newInstance();
            poiSearch.setOnGetPoiSearchResultListener(this);
            routePlanSearch = RoutePlanSearch.newInstance();
            routePlanSearch.setOnGetRoutePlanResultListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @OnClick(R.id.btnBack)
    public void onClick() {
        finish();
    }


    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        try {
            mMapView.onResume();
            mBaiduMap.clear();
            if (mOverlayList.size() > 0) {
                showMarkers();
            }
            super.onResume();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        try {
            mLocClient.stop();
            mMapView.onDestroy();
            mMapView = null;
            hideProgressDialog();
            super.onDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onPause() {
        try {
            mMapView.onPause();
            mBaiduMap.clear();
            isFirstLoc = true;
            hideProgressDialog();
            super.onPause();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.detailGoHere, R.id.detailNavigation, R.id.tvStartNavigation, R.id.tvQuitNavigation, R.id.imgGasMyLocation, R.id.imgGasPreference, R.id.tvGasTrafficCondition})
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.detailGoHere:
                    isRoutePlanning = true;
                    ConstantValues.startLoc = mLoc;
                    drivingRoutePlanOption = new DrivingRoutePlanOption()
                            .from(PlanNode
                                    .withLocation(ConstantValues.startLoc))
                            .to(PlanNode.withLocation(ConstantValues.endLoc))
                            .trafficPolicy(ConstantValues.drivingPolicy)
                            .policy(ConstantValues.policy);
                    routePlanSearch.drivingSearch(drivingRoutePlanOption);
                    showProgressDialog();
                    break;
                case R.id.detailNavigation:
                    isRoutePlanning = true;
                    ConstantValues.startLoc = mLoc;
                    list.clear();
                    list.add(new BNRoutePlanNode(
                            ConstantValues.startLoc.longitude,
                            ConstantValues.startLoc.latitude,
                            ConstantValues.startName, null,
                            BNRoutePlanNode.CoordinateType.BD09LL));
                    showProgressDialog();
                    startNavigation();
                    break;
                case R.id.tvStartNavigation:
                    showProgressDialog();
                    isRoutePlanning = true;
                    startNavigation();
                    break;
                case R.id.tvQuitNavigation:
                    mBaiduMap.clear();
                    showBottom();
                    isRoutePlanning = false;
                    break;
                case R.id.imgMyLocation:
                    MapStatus mapStatus = new MapStatus.Builder().target(mLoc)
                            .rotate(18f).zoom(17).build();
                    MapStatusUpdate statusUpdate = MapStatusUpdateFactory
                            .newMapStatus(mapStatus);
                    mBaiduMap.setMapStatus(statusUpdate);
                    showBottom();
                    break;
                case R.id.imgPreference:
                    UIHelper.showBtnMapPreferenceSettingActivity(BtnGasActivity.this);
                    break;

                case R.id.tvTrafficCondition:
                    if (mBaiduMap.isTrafficEnabled()) {
                        mBaiduMap.setTrafficEnabled(true);
                    } else {
                        mBaiduMap.setTrafficEnabled(false);
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void startNavigation() {
        new Navigation(list, BtnGasActivity.this);
    }

    private Marker tempMarker;
    private MarkerOptions tempMarkerOptions;

    @Override
    public boolean onMarkerClick(Marker marker) {

        try {
            try {

                if (isRoutePlanning) {
                    return false;
                }
                if (marker == null || marker.getExtraInfo() == null
                        || marker.getPosition() == null
                        || marker.getExtraInfo().getString("address") == null) {
                    return false;
                }
                if (tempMarker != null) {
                    tempMarker.remove();
                }
                tempMarkerOptions = new MarkerOptions().position(marker.getPosition()).icon(
                        BitmapDescriptorFactory.fromResource(R.drawable.marker_chosen));
                tempMarker = (Marker) mBaiduMap.addOverlay(tempMarkerOptions);
                tvGasStationName.setText(marker.getExtraInfo().getString("name"));
                tvGasStationDis.setText("距离您约"
                        + String.valueOf((int) (DistanceUtil.getDistance(mLoc,
                        marker.getPosition()))) + "米");

                tvGasStationAddress.setText(marker.getExtraInfo().getString("address")
                        .toString().trim());
                tvGasStationOtherInfo.setText(marker.getExtraInfo().getString("others"));
                showDetail();
                ConstantValues.endLoc = marker.getPosition();
                ConstantValues.endName = marker.getExtraInfo().getString(
                        "name");
                ConstantValues.searchNearByCenter = marker.getPosition();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {

        }
        return false;

    }

    private void showDetail() {
        try {
            llDetail.setVisibility(View.VISIBLE);
            includeNavigation.setVisibility(View.GONE);
            imgPreference.setVisibility(View.GONE
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showBottom() {
        try {
            llDetail.setVisibility(View.GONE);
            includeNavigation.setVisibility(View.GONE);
            imgPreference.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showNavigation() {
        try {
            llDetail.setVisibility(View.GONE);
            includeNavigation.setVisibility(View.VISIBLE);
            imgPreference.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showProgressDialog() {
        try {
            progressDialog = ProgressDialog.show(this, null, ConstantValues.pleaseWait);
            progressDialog.setOnCancelListener(this);
            progressDialog.setCancelable(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideProgressDialog() {
        try {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        try {
            showBottom();
            if (tempMarker != null) {
                tempMarker.remove();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
    }

    @Override
    public boolean onPolylineClick(Polyline polyline) {
        showNavigation();
        return false;
    }

    @Override
    public void onReceiveLocation(final BDLocation bdLocation) {

        try {
            if (bdLocation != null) {

                if (isFirstLoc) {
                    isFirstLoc = false;
                    mLoc = new LatLng(bdLocation.getLatitude(),
                            bdLocation.getLongitude());
                    locData = new MyLocationData.Builder()
                            .accuracy(bdLocation.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                            .direction(100).latitude(bdLocation.getLatitude())
                            .longitude(bdLocation.getLongitude()).build();
                    mBaiduMap.setMyLocationData(locData);
                    MapStatus currentMapStatus = new MapStatus.Builder()
                            .target(mLoc).rotate(18f).zoom(19).build();
                    MapStatusUpdate currentStatusUpdate = MapStatusUpdateFactory
                            .newMapStatus(currentMapStatus);
                    mBaiduMap.setMapStatus(currentStatusUpdate);
                    showProgressDialog();
                    //                nearbySearchOption = new PoiNearbySearchOption().keyword(ConstantValues.home_gas).location(mLoc).radius(2000).pageCapacity(20);
                    //                poiSearch.searchNearby(nearbySearchOption);
                    getGasStations(bdLocation.getLatitude(), bdLocation.getLongitude());
                }
                mLoc = new LatLng(bdLocation.getLatitude(),
                        bdLocation.getLongitude());
                locData = new MyLocationData.Builder()
                        .accuracy(bdLocation.getRadius())
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(100).latitude(bdLocation.getLatitude())
                        .longitude(bdLocation.getLongitude()).build();
                mBaiduMap.setMyLocationData(locData);
                mCityName = bdLocation.getCity();
                ConstantValues.startName = bdLocation.getBuildingName();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getGasStations(double la, double lo) {
        try {
            StringBuffer sb = new StringBuffer();
            final String url = sb.append(URL).append("key=").append(KEY).append("&format=2").append("&lon=").append(lo).append("&lat=").append(la).append("&r=").append(3000).toString();
            Parameters params = new Parameters();
            params.add("lon", lo);// 经纬(如:121.538123)
            params.add("lat", la);// 纬度(如：31.677132)
            params.add("r", 5000);// 搜索范围，单位M，默认3000，最大10000
            params.add("page", 1);// 页数,默认1
            params.add("format", 1);// 格式选择1或2，默认1
            params.add("key", KEY);// 应用APPKEY(应用详细页查询)

            JuheData.executeWithAPI(getApplicationContext(), 7, "http://apis.juhe.cn/oil/local", JuheData.POST, params, new DataCallBack() {

                @Override
                public void onSuccess(int code, String response) {

                    System.out.println("输出" + code);
                    System.out.println("输出结果" + response);
                    ReadJSONDate(response);


                }

                @Override
                public void onFinish() {

                }

                @Override
                public void onFailure(int arg0, String arg1, Throwable arg2) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressWarnings("unused")
    protected void ReadJSONDate(String response) {
        try {
            System.out.println("JSON" + response);
            String json = response.replace("“", "\"");
            try {
                JSONObject jsonObject = new JSONObject(json);
                JSONObject result = jsonObject.getJSONObject("result");
                JSONArray array = result.getJSONArray("data");
                //            tv.append("数组的长度："+array.length()+"\n");
                //            String []name = new String [array.length()];
                Bundle map = null;
                StringBuffer sb = null;
                for (int i = 0; i < array.length(); i++) {
                    map = new Bundle();
                    sb = new StringBuffer();
                    JSONObject jsonData = (JSONObject) array.get(i);
                    map.putString("name", jsonData.getString("name"));
                    map.putString("address", jsonData.getString("address"));
                    map.putString("lon", jsonData.getString("lon"));
                    map.putString("lat", jsonData.getString("lat"));
                    JSONObject price = jsonData.getJSONObject("gastprice");
                    try {
                        String a = price.getString("92#");
                        sb.append("92#油价：" + a + "（元/升）\n");
                    } catch (Exception e) {
                        continue;
                    }
                    try {
                        String b = price.getString("95#");
                        sb.append("95#油价：" + b + "（元/升）\n");
                    } catch (Exception e) {
                        continue;
                    }
                    try {
                        String c = price.getString("0#车柴");
                        sb.append("0#柴油价：" + c + "（元/升）\n");
                    } catch (Exception e) {
                        continue;
                    }
                    String fwlsmc = jsonData.getString("fwlsmc");
                    sb.append("其他服务：" + fwlsmc + "\n");
                    map.putString("others", sb.toString());
                    gasStationsList.add(map);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            showMarkers();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void showMarkers() {
        try {
            Bundle map = null;
            Bundle extraInfo = null;
            BitmapDescriptor bitmap = null;
            MarkerOptions marker = null;
            for (int i = 0; i < gasStationsList.size(); i++) {
                map = gasStationsList.get(i);
                extraInfo = new Bundle(map);
                bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.navi_along_search_gas_station_icon);
                marker = new MarkerOptions().position(new LatLng(Double.parseDouble(map.get("lat").toString()), Double.parseDouble(map.get("lon").toString())))
                        .extraInfo(extraInfo).icon(bitmap).draggable(true);
                mOverlayList.add(mBaiduMap.addOverlay(marker));
            }
            hideProgressDialog();
            zoomToSpan();
            showBottom();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        try {
            if (isChecked) {
                UIHelper.ToastMessage(BtnGasActivity.this, "打开路况");
                mBaiduMap.setTrafficEnabled(true);
            } else {
                UIHelper.ToastMessage(BtnGasActivity.this, "关闭路况");
                mBaiduMap.setTrafficEnabled(false);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGetPoiResult(PoiResult result) {
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

    }

    public void zoomToSpan() {
        try {
            if (mBaiduMap == null) {
                return;
            }
            if (mOverlayList.size() > 0) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Overlay overlay : mOverlayList) {
                    if (overlay instanceof Marker) {
                        builder.include(((Marker) overlay).getPosition());
                    }
                }
                mBaiduMap.setMapStatus(MapStatusUpdateFactory
                        .newLatLngBounds(builder.build()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
        try {
            mBaiduMap.clear();
            hideProgressDialog();
            showNavigation();
            if (result == null) {
                Toast.makeText(getApplicationContext(), "策划路径失败！",
                        Toast.LENGTH_LONG).show();
                return;
            }
            BNRoutePlanNode planNode = null;
            LatLng latLng = null;
            DrivingRouteLine.DrivingStep step = null;
            list.clear();
            list.add(new BNRoutePlanNode(
                    ConstantValues.startLoc.longitude,
                    ConstantValues.startLoc.latitude,
                    ConstantValues.startName, null,
                    BNRoutePlanNode.CoordinateType.BD09LL));
            List<DrivingRouteLine.DrivingStep> drivingStep = result.getRouteLines().get(0)
                    .getAllStep();
            for (int i = 0; i < (drivingStep.size() > 3 ? 3 : drivingStep.size()); i++) {
                step = drivingStep.get(i);
                latLng = step.getWayPoints().get(0);
                planNode = new BNRoutePlanNode(latLng.longitude, latLng.latitude,
                        step.toString(), step.getInstructions(),
                        BNRoutePlanNode.CoordinateType.BD09LL);
                list.add(planNode);
            }
            DrivingRouteOverlay overlay = new DrivingRouteOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == ConstantValues.PREFERENCE_SET_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    ConstantValues.startLoc = mLoc;
                    drivingRoutePlanOption = new DrivingRoutePlanOption()
                            .from(PlanNode
                                    .withLocation(ConstantValues.startLoc))
                            .to(PlanNode.withLocation(ConstantValues.endLoc))
                            .trafficPolicy(ConstantValues.drivingPolicy)
                            .policy(ConstantValues.policy);
                    routePlanSearch.drivingSearch(drivingRoutePlanOption);
                    showProgressDialog();

                }
            } else if (requestCode == ConstantValues.NAVIGATION_REQUEST_CODE) {
                finish();
            } else if (requestCode == ConstantValues.NAVIGATION_REQUEST_CODE) {
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    @Override
    public void onCancel(DialogInterface dialog) {
        System.exit(0);
    }


    public class Navigation implements DialogInterface.OnCancelListener {


        public static final String ROUTE_PLAN_NODE = "routePlanNode";
        private List<BNRoutePlanNode> list = null;
        private Activity mActivity;
        // private

        public Navigation(List<BNRoutePlanNode> list, Activity activity) {
            this.list = list;
            this.mActivity = activity;
            if (initDirs()) {
                initNavi();
            }
        }

        private String getSdcardDir() {
            try {
                if (Environment.getExternalStorageState().equalsIgnoreCase(
                        Environment.MEDIA_MOUNTED)) {
                    return Environment.getExternalStorageDirectory().toString();
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        private boolean initDirs() {
            try {
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
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        BNOuterTTSPlayerCallback ttsCallback = null;
        String mSDCardPath;
        String authinfo = null;

        String APP_FOLDER_NAME = "AndroidFine";

        private void initNavi() {

            try {
                System.out.println(mSDCardPath);

                // BNOuterTTSPlayerCallback ttsCallback = null;
                BaiduNaviManager.getInstance().init(mActivity, mSDCardPath,
                        APP_FOLDER_NAME, new BaiduNaviManager.NaviInitListener() {
                            @Override
                            public void onAuthResult(int status, String msg) {
                                System.out.println("msg-->" + msg);
                                if (0 == status) {
                                    authinfo = "key校验成功!";
                                } else {
                                    authinfo = "key校验失败, " + msg;
                                }
                                BtnGasActivity.this.runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        Toast.makeText(BtnGasActivity.this, authinfo,
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            public void initSuccess() {
                                Toast.makeText(BtnGasActivity.this, "百度导航引擎初始化成功",
                                        Toast.LENGTH_SHORT).show();
                                initSetting();
    //                            handlerGuide.sendEmptyMessage(0);
                                routePlanToNavi(list);
                            }

                            public void initStart() {
                                Toast.makeText(BtnGasActivity.this, "百度导航引擎初始化开始",
                                        Toast.LENGTH_SHORT).show();
                            }

                            public void initFailed() {
                                Toast.makeText(BtnGasActivity.this, "百度导航引擎初始化失败",
                                        Toast.LENGTH_SHORT).show();
                            }

                        }, null, ttsHandler, ttsPlayStateListener);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        /**
         * 内部TTS播报状态回传handler
         */
        private Handler ttsHandler = new Handler() {
            public void handleMessage(Message msg) {
                try {
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
                } catch (Exception e) {
                    e.printStackTrace();
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


        public void routePlanToNavi(List<BNRoutePlanNode> list) {
            try {
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
                    list.clear();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            System.exit(0);
        }

        public class DemoRoutePlanListener implements BaiduNaviManager.RoutePlanListener {

            private BNRoutePlanNode mBNRoutePlanNode = null;

            public DemoRoutePlanListener(BNRoutePlanNode node) {
                this.mBNRoutePlanNode = node;
            }

            @Override
            public void onJumpToNavigator() {

                /**
                 * 设置途经点已经resetEndNode会回调该接口
                 */

                try {
                    Intent intent = new Intent(BtnGasActivity.this,
                            BtnGuideActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ROUTE_PLAN_NODE,
                            (BNRoutePlanNode) mBNRoutePlanNode);
                    intent.putExtras(bundle);
                    hideProgressDialog();
                    UIHelper.showBtnGuideActivity(BtnGasActivity.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                startActivity(intent);
            }

            @Override
            public void onRoutePlanFailed() {
                try {
                    hideProgressDialog();
                    Toast.makeText(mActivity, "算路失败", Toast.LENGTH_SHORT)
                            .show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        private void initSetting() {
            try {
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
}

