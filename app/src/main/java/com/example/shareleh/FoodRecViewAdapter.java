package com.example.shareleh;

import static com.example.shareleh.FoodInfoActivity.FOOD_ID_KEY;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

public class FoodRecViewAdapter extends RecyclerView.Adapter<FoodRecViewAdapter.ViewHolder> {
    private static final String TAG = "foodRecViewAdapter";
    
    private ArrayList<Food> foodList = new ArrayList<>();
    private String listType;
    private Context mContext;

    public FoodRecViewAdapter(Context mContext, String listType)
    {
        this.mContext = mContext;
        this.listType = listType;
    }

    public void setFoodList(ArrayList<Food> foodList) {
        this.foodList = foodList;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: Called");
        holder.foodTitle.setText(foodList.get(holder.getAdapterPosition()).getTitle());
        holder.foodOwner.setText(foodList.get(holder.getAdapterPosition()).getOwner());
        holder.foodCollectBefore.setText(foodList.get(holder.getAdapterPosition()).getCollectBefore());
        String imgUrl = foodList.get(holder.getAdapterPosition()).getImageUrl();
        if (imgUrl == null) {
            Glide.with(mContext).asBitmap().load(R.drawable.no_image).into(holder.foodImage);
        } else {
            if (imgUrl.contains("http")) {
                Glide.with(mContext).asBitmap().load(imgUrl).into(holder.foodImage);
            } else {
                byte[] bytes = Base64.getDecoder().decode(imgUrl);
                Glide.with(mContext).asBitmap().load(bytes).into(holder.foodImage);
            }
        }
        boolean reserved = foodList.get(holder.getAdapterPosition()).getReserved();

        if(listType.equals("allListings")) {
            if (reserved) {
                holder.foodReserved.setVisibility(View.VISIBLE);
            }

            holder.parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, FoodInfoActivity.class);
                    intent.putExtra(FOOD_ID_KEY, foodList.get(holder.getAdapterPosition()).getId()); // FOOD_ID_KEY defined in FoodInfoActivity.java
                    intent.putExtra("activityContext", "reservable");
                    mContext.startActivity(intent);
                }
            });
        }

        if (listType.equals("myListings")) {
            holder.parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, FoodInfoActivity.class);
                    intent.putExtra(FOOD_ID_KEY, foodList.get(holder.getAdapterPosition()).getId()); // FOOD_ID_KEY defined in FoodInfoActivity.java
                    intent.putExtra("activityContext", "editable");
                    mContext.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private CardView parent;
        private ImageView foodImage;
        private TextView foodTitle;
        private TextView foodOwner;
        private TextView foodCollectBefore;
        private TextView foodReserved;
        public ViewHolder(@NonNull View itemView) {

            super(itemView);
            parent = itemView.findViewById(R.id.foodItem);
            foodImage = itemView.findViewById(R.id.foodImage);
            foodTitle = itemView.findViewById(R.id.foodTitle);
            foodOwner = itemView.findViewById(R.id.foodOwner);
            foodCollectBefore = itemView.findViewById(R.id.foodCollectBefore);
            foodReserved = itemView.findViewById(R.id.foodReserved);
        }
    }
}
