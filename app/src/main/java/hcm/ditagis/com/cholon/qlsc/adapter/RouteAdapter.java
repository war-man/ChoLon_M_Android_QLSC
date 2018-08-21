package hcm.ditagis.com.cholon.qlsc.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.utities.Distance;

/**
 * Created by ThanLe on 04/10/2017.
 */

public class RouteAdapter extends ArrayAdapter<RouteAdapter.Item> {
    private Context mContext;
    private List<Item> items;

    public RouteAdapter(Context context, List<Item> items) {
        super(context, 0, items);
        this.mContext = context;
        this.items = items;
    }

    public List<Item> getItems() {
        return items;
    }

    public void clear() {
        items.clear();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint({"InflateParams", "DefaultLocale"})
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            convertView = inflater.inflate(R.layout.item_route, null);
        }
        Item item = items.get(position);

        TextView txtInstruction =  convertView.findViewById(R.id.txt_route_instruction);
        txtInstruction.setText(item.getHtml_instructions());

        TextView txtSubInstruction =  convertView.findViewById(R.id.txt_route_sub_instruction);
        if (item.getHtml_sub_instructions() != null)
            txtSubInstruction.setText(item.getHtml_sub_instructions());
        else
            txtSubInstruction.setVisibility(View.GONE);
        TextView txtDistance =  convertView.findViewById(R.id.txt_route_distance);
        txtDistance.setText(String.format("%dm", item.getDistance().value));
        return convertView;
    }


    public static class Item {
        private Distance distance;
        private String html_instructions;
        private String html_sub_instructions;

        public Item() {
        }

        String getHtml_sub_instructions() {
            return html_sub_instructions;
        }

        public void setHtml_sub_instructions(String html_sub_instructions) {
            this.html_sub_instructions = html_sub_instructions;
        }

        Distance getDistance() {
            return distance;
        }

        public void setDistance(Distance distance) {
            this.distance = distance;
        }


        String getHtml_instructions() {
            return html_instructions;
        }

        public void setHtml_instructions(String html_instructions) {
            this.html_instructions = html_instructions;
        }

    }
}
