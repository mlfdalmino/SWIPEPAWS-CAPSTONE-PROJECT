package com.capstone.project.swipepaws.Main;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.capstone.project.swipepaws.R;


import java.util.List;


public class PhotoAdapter extends ArrayAdapter<Cards> {
    private Context mContext;


    public PhotoAdapter(Context context, int resource, List<Cards> objects) {
        super(context, resource, objects);
        this.mContext = context;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Cards card_item = getItem(position);


        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }


        TextView name = convertView.findViewById(R.id.name);
        ImageView image = convertView.findViewById(R.id.image);
        ImageButton btnInfo = convertView.findViewById(R.id.checkInfoBeforeMatched);


        name.setText(card_item.getDogName() + ", " + card_item.getBreed());


        // Load images with Glide based on the profile image URL
        String profileImageUrl1 = card_item.getProfileImageUrl1(); // Get the profile image URL
        String profileImageUrl2 = card_item.getProfileImageUrl2(); // Get the profile image URL
        String profileImageUrl3 = card_item.getProfileImageUrl3(); // Get the profile image URL
        String profileImageUrl4 = card_item.getProfileImageUrl4(); // Get the profile image URL
        String profileImageUrl5 = card_item.getProfileImageUrl5(); // Get the profile image URL
        String profileImageUrl6 = card_item.getProfileImageUrl6(); // Get the profile image URL


        // Create RequestOptions with a placeholder image and error image
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.default_man) // Placeholder image resource
                .error(R.drawable.default_woman); // Error image resource


        // Use RequestOptions when loading the image with Glide
        Glide.with(mContext)
                .load(profileImageUrl1)
                .apply(requestOptions)
                .into(image);


        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ProfileCheckinMain.class);


                // Pass the relevant data from the Cards object to the intent

                intent.putExtra("userId", card_item.getUserId());
                intent.putExtra("name", card_item.getDogName());
                intent.putExtra("breed", card_item.getBreed());
                intent.putExtra("gender", card_item.getDogGender());

                intent.putExtra("city", card_item.getCity());
                intent.putExtra("distance", card_item.getDistance());
                intent.putExtra("bio", card_item.getBio());

                intent.putExtra("photo1", card_item.getProfileImageUrl1());
                intent.putExtra("photo2", card_item.getProfileImageUrl2());
                intent.putExtra("photo3", card_item.getProfileImageUrl3());
                intent.putExtra("photo4", card_item.getProfileImageUrl4());
                intent.putExtra("photo5", card_item.getProfileImageUrl5());
                intent.putExtra("photo6", card_item.getProfileImageUrl6());

                mContext.startActivity(intent);
            }
        });


        return convertView;
    }
}
