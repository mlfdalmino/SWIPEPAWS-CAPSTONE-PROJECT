package com.capstone.project.swipepaws.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.viewpager.widget.PagerAdapter;
import com.bumptech.glide.Glide;
import com.capstone.project.swipepaws.R;

import java.util.List;

public class ProfileImagePagerAdapter extends PagerAdapter {
    private Context mContext;
    private List<String> profileImageUrls;

    public ProfileImagePagerAdapter(Context context, List<String> imageUrls) {
        mContext = context;
        profileImageUrls = imageUrls;
    }

    @Override
    public int getCount() {
        return profileImageUrls.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.profile_image_item, container, false);
        ImageView imageView = itemView.findViewById(R.id.profileImage);
        String imageUrl = profileImageUrls.get(position);
        Glide.with(mContext).load(imageUrl).into(imageView);
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
