package njci.software.car.utils;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.navisdk.adapter.BaiduNaviManager;

/**
 * Created by 34856 on 2016/5/8.
 */
public class ConstantValues {


    public static final int SUCCESS = 1;
    public static final int ERROR = 2;
    public static final String pleaseWait = "请稍后";
    public static final String SET_START_NODE = "设置起点";
    public static final String SET_END_NODE = "设置终点";
    public static LatLng myLoc = null;
    public static String MYLOCNAME = "我的位置";
    public static String myLocName = null;
    public static LatLng startLoc = null;
    public static String startName = null;
    public static LatLng endLoc = null;
    public static String endName = null;
    //	public static String[] divingPolicys = { "驾车路线不含路况", "驾车路线含路况" };

    // ROUTE_PATH
    // 驾车路线不含路况
    // ROUTE_PATH_AND_TRAFFIC
    // 驾车路线含路况
    public static DrivingRoutePlanOption.DrivingTrafficPolicy drivingPolicy = DrivingRoutePlanOption.DrivingTrafficPolicy.ROUTE_PATH_AND_TRAFFIC;

    public static String[] policys = {"躲避拥堵", "最短距离", "较少费用", "时间优先"};

    // ECAR_AVOID_JAM ： 躲避拥堵
    // ECAR_DIS_FIRST ：最短距离
    // ECAR_FEE_FIRST ：较少费用
    // ECAR_TIME_FIRST ：时间优先

    public static DrivingRoutePlanOption.DrivingPolicy policy = DrivingRoutePlanOption.DrivingPolicy.ECAR_DIS_FIRST;
    public static int planPreference = BaiduNaviManager.RoutePlanPreference.ROUTE_PLAN_MOD_RECOMMEND;
    public static LatLng searchNearByCenter;
    public static boolean[] isChecked = {false, false, false, false};

    public static final String page = "page";
    public static final String home_gas = "加油站";
    public static final String home_park = "停车场";
    public static final String home_4s = "4S";
    public static final String home_routePlan = "路径规划";
    public static final String home_trafficCondition = "路况";
    public static final String home_route_start = "设置起点";
    public static final String home_route_end = "设置终点";


    public static DrivingRouteResult drivingRouteResult;

    public static String cityName = "南京";


    public static PoiResult poiResult = null;

    public static final int NAVIGATION_REQUEST_CODE = 0xf;
    public static final int PREFERENCE_SET_REQUEST_CODE = 0x01f;
    public static final int ROUTE_NODE_SET_START_REQUEST_CODE = 0x10f;
    public static final int ROUTE_NODE_SET_END_REQUEST_CODE = 0x100f;
}
