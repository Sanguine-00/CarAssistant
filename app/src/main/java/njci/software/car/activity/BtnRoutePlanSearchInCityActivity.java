package njci.software.car.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import njci.software.car.R;

import njci.software.car.ui.UIHelper;
import njci.software.car.utils.ConstantValues;
import njci.software.car.utils.MyAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BtnRoutePlanSearchInCityActivity extends BaseFragmentActivity implements
        OnGetPoiSearchResultListener, OnItemClickListener, OnQueryTextListener {
    @Bind(R.id.btnBack)
    Button btnBack;
    @Bind(R.id.textHeadTitle)
    TextView textHeadTitle;
    private List<Map<String, Object>> listViewlist;
    private ListView listview;
    private SearchView searchView;
    private PoiSearch poSearch;
    private PoiCitySearchOption citySearchOption;
    private Intent intent;
    private BaseAdapter adapter;
    private String hint;
    private static boolean isSubmit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btn_route_plan_search_node);
        ButterKnife.bind(this);
        listview = (ListView) findViewById(R.id.activity_routeplansearchincity_listView);
        searchView = (SearchView) findViewById(R.id.activity_routeplansearchincity_edit_text);
        searchView.requestFocus();
        searchView.setIconifiedByDefault(false);
        intent = getIntent();
        String hint = (String) intent.getExtras().get("hint");
        this.hint = hint;
        btnBack.setVisibility(View.VISIBLE);
        textHeadTitle.setText(hint);
        searchView.setQueryHint(hint);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            setResult(ConstantValues.ERROR, intent);
            finish();
        }
        return super.onKeyDown(keyCode, event);

    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

    }

    @Override
    public void onGetPoiResult(PoiResult poiResult) {
        progressBarHelper.goneLoading();
        ConstantValues.poiResult = poiResult;
        setResult(RESULT_OK, intent);
        listViewlist = new ArrayList<Map<String, Object>>();
        Map<String, Object> map;
        List<PoiInfo> list = poiResult.getAllPoi();
        for (PoiInfo poiInfo : list) {
            map = new HashMap<String, Object>();

            map.put("address", poiInfo.address);
            map.put("name", poiInfo.name);
            map.put("location", poiInfo.location);

            listViewlist.add(map);
        }
        adapter = new MyAdapter(this, listViewlist);
        listview.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        listview.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        if (hint.equals(ConstantValues.SET_START_NODE)) {
            ConstantValues.startLoc = (LatLng) listViewlist.get(
                    position).get("location");
            ConstantValues.startName = (String) listViewlist.get(
                    position).get("name");
        } else {
            ConstantValues.endLoc = (LatLng) listViewlist
                    .get(position).get("location");
            ConstantValues.endName = (String) listViewlist.get(
                    position).get("name");
        }
        finish();

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        isSubmit = true;
        searchView.clearFocus();
        if (query.toString().trim().equals("")) {
            UIHelper.ToastMessage(BtnRoutePlanSearchInCityActivity.this, "未输入关键字");
            return false;
        }
        poSearch = PoiSearch.newInstance();
        poSearch.setOnGetPoiSearchResultListener(BtnRoutePlanSearchInCityActivity.this);
        citySearchOption = new PoiCitySearchOption()
                .city(ConstantValues.cityName).keyword(query.toString().trim())
                .pageCapacity(100);
        poSearch.searchInCity(citySearchOption);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        isSubmit = false;

        String query = new StringBuffer()
                .append(searchView.getQuery().toString().trim())
                .append(newText).toString();

        if (query.toString().trim().equals("")) {
            return false;
        }
        poSearch = PoiSearch.newInstance();
        poSearch.setOnGetPoiSearchResultListener(BtnRoutePlanSearchInCityActivity.this);
        citySearchOption = new PoiCitySearchOption()
                .city(ConstantValues.cityName).keyword(query.toString().trim())
                .pageCapacity(100);
        poSearch.searchInCity(citySearchOption);
        return false;
    }


    @OnClick(R.id.btnBack)
    public void onClick() {
        finish();
    }
}
