package ch.mbuehler.eth.mgis.treasurego;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by marcello on 28/03/18.
 */

// https://www.journaldev.com/10416/android-listview-with-custom-adapter-example-tutorial
public class TreasureAdapter extends ArrayAdapter<Treasure> {
    private ArrayList<Treasure> treasures;
    private ArrayList<String> uuidFoundTreasures;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView nameText;
        TextView rewardText;
        ImageView info;
    }

    public TreasureAdapter(ArrayList<Treasure> data, ArrayList<String> uuidFoundTreasures, Context context) {
        super(context, R.layout.treasure_row, data);
        this.treasures = data;
        this.uuidFoundTreasures = uuidFoundTreasures;
        this.mContext=context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Treasure treasure = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.treasure_row, parent, false);
            viewHolder.nameText = (TextView) convertView.findViewById(R.id.name);
            viewHolder.rewardText = (TextView) convertView.findViewById(R.id.MaxRewardValue);
            viewHolder.info = (ImageView) convertView.findViewById(R.id.item_info);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

//        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
//        result.startAnimation(animation);
//        lastPosition = position;

        viewHolder.nameText.setText(treasure.getName());
        viewHolder.rewardText.setText(Integer.valueOf(treasure.getReward()).toString());

        // Choose treasure picture depending on whether the user has already found the treasure or not
        if(uuidFoundTreasures.indexOf(treasure.getUuid()) >= 0){
            // The user has already found it
            viewHolder.info.setImageResource(R.drawable.treasure_open);
        } else{
            // It has yet to be found
            viewHolder.info.setImageResource(R.drawable.treasure_closed);
        }
        // Return the completed view to render on screen
        return convertView;
    }
}
