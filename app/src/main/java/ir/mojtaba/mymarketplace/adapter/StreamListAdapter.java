package ir.mojtaba.mymarketplace.adapter;

import android.app.Instrumentation;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import ir.mojtaba.mymarketplace.R;
import ir.mojtaba.mymarketplace.model.Item;

public class StreamListAdapter extends RecyclerView.Adapter<StreamListAdapter.MyViewHolder> {

    private Context mContext;
    private List<Item> itemList;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView title, price;
        public ImageView thumbnail;

        public MyViewHolder(View view) {

            super(view);

            
			title = (TextView) view.findViewById(R.id.title);
            price = (TextView) view.findViewById(R.id.price);


            Typeface tf_regular = Typeface.createFromAsset(view.getContext().getAssets(), "fonts/irsans.ttf");
			Typeface tf_regularr = Typeface.createFromAsset(view.getContext().getAssets(), "fonts/yekan.ttf");
			this.title.setTypeface(tf_regular);
			this.price.setTypeface(tf_regularr);
			this.price.setTextSize(15);
			
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        }
		
    }
	



    public StreamListAdapter(Context mContext, List<Item> itemList) {

        this.mContext = mContext;
        this.itemList = itemList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_row, parent, false);


        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        Item item = itemList.get(position);
        holder.title.setText(item.getTitle());
        holder.price.setText(Integer.toString(item.getPrice()) + " " + mContext.getString(R.string.label_currency));

        // loading album cover using Glide library
        Glide.with(mContext).load(item.getImgUrl()).into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {

        return itemList.size();
    }
}