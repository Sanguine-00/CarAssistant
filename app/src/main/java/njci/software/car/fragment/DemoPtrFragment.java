package njci.software.car.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.squareup.okhttp.Request;
import com.squareup.picasso.Picasso;
import njci.software.car.R;
import njci.software.car.activity.MainActivity;
import njci.software.car.http.HttpClient;
import njci.software.car.http.HttpResponseHandler;
import njci.software.car.model.SearchParam;
import njci.software.car.model.SearchShop;
import njci.software.car.ui.UIHelper;
import njci.software.car.ui.loadmore.LoadMoreListView;
import njci.software.car.ui.quickadapter.BaseAdapterHelper;
import njci.software.car.ui.quickadapter.QuickAdapter;
import njci.software.car.utils.DeviceUtil;

import java.io.IOException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.StoreHouseHeader;

/**
 * Created by tiansj on 15/9/4.
 */
public class DemoPtrFragment extends Fragment {
    @Bind(R.id.btn_home_parking)
    RadioButton btnHomeParking;
    @Bind(R.id.btn_home_gas_station)
    RadioButton btnHomeGasStation;
    @Bind(R.id.btn_home_traffic_violation)
    RadioButton btnHomeTrafficViolation;
    @Bind(R.id.btn_home_tmc)
    RadioButton btnHomeTmc;
    @Bind(R.id.group)
    RadioGroup group;
    @Bind(R.id.btn_home_4s)
    RadioButton btnHome4s;
    @Bind(R.id.btn_home_insurance)
    RadioButton btnHomeInsurance;
    @Bind(R.id.btn_home_emergency)
    RadioButton btnHomeEmergency;
    @Bind(R.id.btn_home_route)
    RadioButton btnHomeRoute;
    @Bind(R.id.groupBottom)
    RadioGroup groupBottom;
    @Bind(R.id.viewSplit)
    View viewSplit;
    private MainActivity context;

    private SearchParam param;
    private int pno = 1;
    private boolean isLoadAll;

    @Bind(R.id.rotate_header_list_view_frame)
    PtrClassicFrameLayout mPtrFrame;
    @Bind(R.id.listView)
    LoadMoreListView listView;
    QuickAdapter<SearchShop> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_demo_ptr, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = (MainActivity) getActivity();
        initData();
        initView();
        loadData();
        initButton();
    }

    void initView() {
        adapter = new QuickAdapter<SearchShop>(context, R.layout.recommend_shop_list_item) {
            @Override
            protected void convert(BaseAdapterHelper helper, SearchShop shop) {
                helper.setText(R.id.name, shop.getName())
                        .setText(R.id.address, shop.getAddr())
                        .setImageUrl(R.id.logo, shop.getLogo()); // 自动异步加载图片
            }
        };
        listView.setDrawingCacheEnabled(true);
        listView.setAdapter(adapter);

        // header custom begin
        final StoreHouseHeader header = new StoreHouseHeader(context);
        header.setPadding(0, DeviceUtil.dp2px(context, 15), 0, 0);
        header.initWithString("Fine");
        header.setTextColor(getResources().getColor(R.color.gray));
        mPtrFrame.setHeaderView(header);
        mPtrFrame.addPtrUIHandler(header);
        // header custom end

        // 下拉刷新
        mPtrFrame.setLastUpdateTimeRelateObject(this);
        mPtrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                initData();
                loadData();
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }
        });

        // 加载更多
        listView.setOnLoadMoreListener(new LoadMoreListView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadData();
            }
        });

        // 点击事件
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                UIHelper.showHouseDetailActivity(context);
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    Picasso.with(context).pauseTag(context);
                } else {
                    Picasso.with(context).resumeTag(context);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }
        });

    }

    private void initData() {
        param = new SearchParam();
        pno = 1;
        isLoadAll = false;
    }

    private void loadData() {
        if (isLoadAll) {
            return;
        }
        param.setPno(pno);
        HttpClient.getRecommendShops(param, new HttpResponseHandler() {
            @Override
            public void onSuccess(String body) {
                mPtrFrame.refreshComplete();
                JSONObject object = JSON.parseObject(body);
                List<SearchShop> list = JSONArray.parseArray(object.getString("body"), SearchShop.class);
                listView.updateLoadMoreViewText(list);
                isLoadAll = list.size() < HttpClient.PAGE_SIZE;
                if (pno == 1) {
                    adapter.clear();
                }
                adapter.addAll(list);
                pno++;
            }

            @Override
            public void onFailure(Request request, IOException e) {
                mPtrFrame.refreshComplete();
                listView.setLoadMoreViewTextError();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Picasso.with(context).resumeTag(context);
    }

    @Override
    public void onPause() {
        super.onPause();
        Picasso.with(context).pauseTag(context);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Picasso.with(context).cancelTag(context);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private void initButton() {
        btnHomeParking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UIHelper.showBtnParkingActivity(context);
            }
        });

        btnHomeGasStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UIHelper.showBtnGasActivity(context);
            }
        });

        btnHomeInsurance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UIHelper.showBtnInsurance(context);
            }
        });

        btnHome4s.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                UIHelper.showBtn4SActivity(context);
            }
        });

        btnHomeTmc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIHelper.showBtnHomeTmcActivity(context);
            }
        });

        btnHomeRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIHelper.showRoutePlanActivity(context);
            }
        });
    }
}
