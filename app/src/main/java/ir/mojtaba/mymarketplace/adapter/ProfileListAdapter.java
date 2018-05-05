package ir.mojtaba.mymarketplace.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import java.util.List;

import github.ankushsachdeva.emojicon.EmojiconTextView;
import ir.mojtaba.mymarketplace.CategoryActivity;
import ir.mojtaba.mymarketplace.R;
import ir.mojtaba.mymarketplace.app.App;
import ir.mojtaba.mymarketplace.constants.Constants;
import ir.mojtaba.mymarketplace.model.Item;

public class ProfileListAdapter extends BaseAdapter implements Constants {

    private Activity activity;
    private LayoutInflater inflater;
    private List<Item> itemsList;

    ImageLoader imageLoader = App.getInstance().getImageLoader();

    public ProfileListAdapter(Activity activity, List<Item> itemsList) {

        this.activity = activity;
        this.itemsList = itemsList;
    }

    @Override
    public int getCount() {

        return itemsList.size();
    }

    @Override
    public Object getItem(int location) {

        return itemsList.get(location);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    static class ViewHolder {

        public ImageView itemImg;
        public EmojiconTextView itemTitle;
        public TextView itemDate;
        public TextView itemCategory;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;

        if (inflater == null) {

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        if (convertView == null) {

            convertView = inflater.inflate(R.layout.stream_list_row, null);

            viewHolder = new ViewHolder();

            viewHolder.itemImg = (ImageView) convertView.findViewById(R.id.itemImg);
            viewHolder.itemCategory = (TextView) convertView.findViewById(R.id.itemCategory);
            viewHolder.itemTitle = (EmojiconTextView) convertView.findViewById(R.id.itemTitle);
            viewHolder.itemDate = (TextView) convertView.findViewById(R.id.itemDate);
			Typeface tf = Typeface.createFromAsset(activity.getAssets(), "fonts/irsans.ttf");
			viewHolder.itemCategory.setTypeface(tf);
			viewHolder.itemDate.setTypeface(tf);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (imageLoader == null) {

            imageLoader = App.getInstance().getImageLoader();
        }

        viewHolder.itemImg.setTag(position);
        viewHolder.itemCategory.setTag(position);
        viewHolder.itemTitle.setTag(position);
        viewHolder.itemDate.setTag(position);

        final Item item = itemsList.get(position);

        viewHolder.itemCategory.setText(item.getCategoryTitle());
        viewHolder.itemDate.setText(Integer.toString(item.getPrice()) + " " + activity.getString(R.string.label_currency));

        viewHolder.itemCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(activity, CategoryActivity.class);
                intent.putExtra("categoryId", item.getCategoryId());
                intent.putExtra("title", item.getCategoryTitle());
                activity.startActivity(intent);
            }
        });


        if (item.getTitle().length() > 0) {

            viewHolder.itemTitle.setText(item.getTitle());

            viewHolder.itemTitle.setVisibility(View.VISIBLE);

        } else {

            viewHolder.itemTitle.setVisibility(View.GONE);
        }

        if (item.getImgUrl().length() > 0) {

            imageLoader.get(item.getImgUrl(), ImageLoader.getImageListener(viewHolder.itemImg, R.drawable.img_loading, R.drawable.img_loading));
            viewHolder.itemImg.setVisibility(View.VISIBLE);

        } else {

            viewHolder.itemImg.setVisibility(View.GONE);
        }

        return convertView;
    }
}