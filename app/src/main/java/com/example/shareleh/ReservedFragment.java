package com.example.shareleh;

import static com.example.shareleh.MainActivity.admin_username;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ReservedFragment extends Fragment {
//    private final String TAG = "DEBUG_TAG";

    private RecyclerView reservedListingRecView;
    private TextView fragDesc;
    private FoodRecViewAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reserved, container, false);

        adapter = new FoodRecViewAdapter(view.getContext(), "reservedListings");
        reservedListingRecView = view.findViewById(R.id.reservedListingRecView);
        fragDesc = view.findViewById(R.id.fragDesc);

        reservedListingRecView.setAdapter(adapter);
        reservedListingRecView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        new GetReservedListings().execute();

//        ArrayList<Food> reservedListings = FoodDataManager.getInstance().getReservedListings();
//        for (Food f: reservedListings) {
//            Log.d(TAG, f.toString());
//        }

//        adapter.setFoodList(reservedListings);

//        if (reservedListings.size() == 0) {
//            fragDesc.setVisibility(View.VISIBLE);
//            reservedListingRecView.setVisibility(View.INVISIBLE);
//        } else {
//            reservedListingRecView.setVisibility(View.VISIBLE);
//            fragDesc.setVisibility(View.GONE);
//        }
        return view;
    }

    private class GetReservedListings extends AsyncTask<Void, Void, ArrayList<Food>> {
        private DatabaseHelper databaseHelper;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            databaseHelper = new DatabaseHelper(getActivity());
        }

        @Override
        protected ArrayList<Food> doInBackground(Void... voids) {
            try {
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                Cursor cursor = db.query("listings", null, "reserved_by=?", new String[] {admin_username}, null, null, null);

                if (cursor != null) {
                    if(cursor.moveToFirst()) {
                        ArrayList<Food> listings = new ArrayList<>();

                        int idIndex = cursor.getColumnIndex("id");
                        int titleIndex = cursor.getColumnIndex("title");
                        int descriptionIndex = cursor.getColumnIndex("description");
                        int ownerIndex = cursor.getColumnIndex("owner");
                        int imgIndex = cursor.getColumnIndex("image_url");
                        int cDateIndex = cursor.getColumnIndex("collect_before");
                        int locationIndex = cursor.getColumnIndex("location");
                        int qtnIndex = cursor.getColumnIndex("qtn");
                        int reserveUserIndex = cursor.getColumnIndex("reserved_by");
                        int reservedIndex = cursor.getColumnIndex("reserved");

                        for (int i = 0; i < cursor.getCount(); i++) {
                            Food food = new Food();
                            boolean isReserved;
                            if (cursor.getInt(reservedIndex) == 0) {
                                isReserved = false;
                            } else isReserved = true;

                            food.setId(cursor.getInt(idIndex));
                            food.setTitle(cursor.getString(titleIndex));
                            food.setDescription(cursor.getString(descriptionIndex));
                            food.setOwner(cursor.getString(ownerIndex));
                            food.setImageUrl(cursor.getString(imgIndex));
                            food.setCollectBefore(cursor.getString(cDateIndex));
                            food.setLocation(cursor.getString(locationIndex));
                            food.setQuantity(cursor.getInt(qtnIndex));
                            food.setReservedBy(cursor.getString(reserveUserIndex));
                            food.setReserved(isReserved);

                            listings.add(food);

                            cursor.moveToNext();
                        }

                        cursor.close();
                        db.close();
                        return listings;
                    } else {
                        cursor.close();
                        db.close();
                    }

                } else db.close();
            } catch(SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Food> foods) {
            super.onPostExecute(foods);

            if (foods != null) {
                adapter.setFoodList(foods);
                reservedListingRecView.setVisibility(View.VISIBLE);
                fragDesc.setVisibility(View.GONE);
            }
            else {
                fragDesc.setVisibility(View.VISIBLE);
                reservedListingRecView.setVisibility(View.INVISIBLE);
            }
        }
    }
}