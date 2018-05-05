package ir.mojtaba.mymarketplace.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.pkmmte.view.CircularImageView;

import java.util.List;

import github.ankushsachdeva.emojicon.EmojiconTextView;
import ir.mojtaba.mymarketplace.ProfileActivity;
import ir.mojtaba.mymarketplace.R;
import ir.mojtaba.mymarketplace.app.App;
import ir.mojtaba.mymarketplace.constants.Constants;
import ir.mojtaba.mymarketplace.model.Review;
import ir.mojtaba.mymarketplace.util.ReviewInterface;
import ir.mojtaba.mymarketplace.util.TagSelectingTextview;


public class ReviewListAdapter extends BaseAdapter implements Constants {

	private Activity activity;
	private LayoutInflater inflater;
	private List<Review> itemsList;

    private ReviewInterface responder;

    TagSelectingTextview mTagSelectingTextview;

    ImageLoader imageLoader = App.getInstance().getImageLoader();

	public ReviewListAdapter(Activity activity, List<Review> itemsList, ReviewInterface responder) {

		this.activity = activity;
		this.itemsList = itemsList;
        this.responder = responder;

        mTagSelectingTextview = new TagSelectingTextview();
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

        public CircularImageView itemAuthorPhoto;
        public TextView itemAuthor;
        public EmojiconTextView itemText;
        public TextView itemTimeAgo;
        public ImageView itemAction;
	        
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		ViewHolder viewHolder = null;

		if (inflater == null) {

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

		if (convertView == null) {
			
			convertView = inflater.inflate(R.layout.review_list_row, null);
			
			viewHolder = new ViewHolder();

            viewHolder.itemAuthorPhoto = (CircularImageView) convertView.findViewById(R.id.itemAuthorPhoto);
            viewHolder.itemAction = (ImageView) convertView.findViewById(R.id.itemAction);
			viewHolder.itemText = (EmojiconTextView) convertView.findViewById(R.id.itemText);
            viewHolder.itemAuthor = (TextView) convertView.findViewById(R.id.itemAuthor);
            viewHolder.itemTimeAgo = (TextView) convertView.findViewById(R.id.itemTimeAgo);
			Typeface tf = Typeface.createFromAsset(activity.getAssets(), "fonts/irsans.ttf");
			viewHolder.itemAuthor.setTypeface(tf); 
			viewHolder.itemTimeAgo.setTypeface(tf); 

//            viewHolder.questionRemove.setTag(position);
            convertView.setTag(viewHolder);

		} else {
			
			viewHolder = (ViewHolder) convertView.getTag();
		}

        if (imageLoader == null) {

            imageLoader = App.getInstance().getImageLoader();
        }

        viewHolder.itemAuthorPhoto.setTag(position);
        viewHolder.itemText.setTag(position);
        viewHolder.itemAuthor.setTag(position);
        viewHolder.itemTimeAgo.setTag(position);
        viewHolder.itemAction.setTag(position);
		
		final Review review = itemsList.get(position);

        viewHolder.itemAuthor.setVisibility(View.VISIBLE);
        viewHolder.itemAuthor.setText(review.getFromUserFullname());

        viewHolder.itemAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(activity, ProfileActivity.class);
                intent.putExtra("profileId", review.getFromUserId());
                activity.startActivity(intent);
            }
        });

        if (review.getFromUserPhotoUrl() != null && review.getFromUserPhotoUrl().length() != 0) {

            viewHolder.itemAuthorPhoto.setVisibility(View.VISIBLE);

            imageLoader.get(review.getFromUserPhotoUrl(), ImageLoader.getImageListener(viewHolder.itemAuthorPhoto, R.drawable.profile_default_photo, R.drawable.profile_default_photo));

        } else {

            viewHolder.itemAuthorPhoto.setVisibility(View.VISIBLE);
            viewHolder.itemAuthorPhoto.setImageResource(R.drawable.profile_default_photo);
        }

        viewHolder.itemAuthorPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(activity, ProfileActivity.class);
                intent.putExtra("profileId", review.getFromUserId());
                activity.startActivity(intent);
            }
        });

        if (App.getInstance().getId() != 0 && App.getInstance().getId() == review.getFromUserId()) {

            viewHolder.itemAction.setVisibility(View.VISIBLE);

        } else {

            viewHolder.itemAction.setVisibility(View.GONE);
        }

        viewHolder.itemAction.setImageResource(R.drawable.ic_action_remove);

        viewHolder.itemAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final int getPosition = (Integer) view.getTag();

                responder.reviewAction(getPosition);
            }
        });

        viewHolder.itemText.setText(review.getReview().replaceAll("<br>", "\n"));

        viewHolder.itemText.setMovementMethod(LinkMovementMethod.getInstance());

        viewHolder.itemText.setVisibility(View.VISIBLE);

        viewHolder.itemTimeAgo.setVisibility(View.VISIBLE);
        viewHolder.itemTimeAgo.setText(review.getTimeAgo());

		return convertView;
	}
}