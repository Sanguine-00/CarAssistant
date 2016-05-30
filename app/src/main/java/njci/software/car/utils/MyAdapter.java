package njci.software.car.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import njci.software.car.R;

import java.util.List;
import java.util.Map;


public class MyAdapter extends BaseAdapter {

	private List<Map<String, Object>> list;
	private Context context;

	public MyAdapter(Context context, List<Map<String, Object>> list) {
		this.context = context;
		this.list = list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	private ImageView listViewLocPic;
	private TextView listViewName, listViewAddress;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.activity_btn_route_plan_search_node_listview_item, null);
		}

		listViewLocPic = (ImageView) convertView
				.findViewById(R.id.listViewLocPic);
		listViewName = (TextView) convertView.findViewById(R.id.listViewName);
		listViewAddress = (TextView) convertView
				.findViewById(R.id.listViewAddress);
		Map<String, Object> map = list.get(position);

		listViewLocPic.setImageResource(R.drawable.marker);
		listViewName.setText(map.get("name").toString());
		listViewAddress.setText(map.get("address").toString());

		return convertView;
	}

}
