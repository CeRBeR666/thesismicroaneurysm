package com.agaoglu.tez;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Volkan on 15.03.2017.
 */

public class hastasecholder extends RecyclerView.ViewHolder {

    TextView hastaisim;

    hastasecholder(View itemview){
        super(itemview);
        hastaisim = (TextView) itemview.findViewById(R.id.item_hastaisim);
    }

}