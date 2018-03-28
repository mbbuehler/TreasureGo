package ch.mbuehler.eth.mgis.treasurego;

import android.content.Context;
import android.support.annotation.NonNull;
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

/**
 * Adapter class to display Treasure objects.
 * Inspired by
 * https://www.journaldev.com/10416/android-listview-with-custom-adapter-example-tutorial
  */
public class TreasureAdapter extends ArrayAdapter<Treasure> {
    /**
     * List with Treasures that will be displayed
     */
    private ArrayList<Treasure> treasures;
    /**
     * List with uuids of Treasures that have been found by the user.
     */
    private ArrayList<String> uuidFoundTreasures;
    /**
     * ApplicationContext
     */
    private Context mContext;

    /**
     * View lookup cache
      */
    private static class ViewHolder {
        TextView nameText;  // Name of the Treasure
        TextView rewardText;  // Maximum reward of the Treasure
        ImageView image;  // Image of the Treasure
    }

    /**
     *
     * @param data The treasures to be displayed
     * @param uuidFoundTreasures List with Treasure uuids that have been found already
     * @param context ApplicationContext
     */
    TreasureAdapter(ArrayList<Treasure> data, ArrayList<String> uuidFoundTreasures, Context context) {
        super(context, R.layout.treasure_row, data);
        this.treasures = data;
        this.uuidFoundTreasures = uuidFoundTreasures;
        this.mContext=context;
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        Treasure treasure = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        // Only create a new View if necessary
        if (convertView == null) {
            // we need to create a new View
            viewHolder = new ViewHolder();
            // Create the view with our RelativeLayout template
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.treasure_row, parent, false);
            // Set the required Text- and ImageViews
            viewHolder.nameText = convertView.findViewById(R.id.name);
            viewHolder.rewardText = convertView.findViewById(R.id.MaxRewardValue);
            viewHolder.image = convertView.findViewById(R.id.item_info);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Set the text values
        viewHolder.nameText.setText(treasure.getName());
        viewHolder.rewardText.setText(Integer.valueOf(treasure.getReward()).toString());

        // Set the image
        // Choose Treasure picture depending on whether the Treasure has been found or not
        if(uuidFoundTreasures.indexOf(treasure.getUuid()) >= 0){
            // The user has already found the Treasure
            viewHolder.image.setImageResource(R.drawable.treasure_open);
        } else{
            // The Treasure has yet to be found
            viewHolder.image.setImageResource(R.drawable.treasure_closed);
        }
        // Return the completed view
        return convertView;
    }
}
