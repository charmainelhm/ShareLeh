package com.example.shareleh;

import static com.example.shareleh.MainActivity.admin_username;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.AsyncTaskLoader;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

public class FoodInfoActivity extends AppCompatActivity {

    public static final String FOOD_ID_KEY = "foodId";

    private LinearLayout btnGrp;
    private ImageView foodImage;
    private TextView foodTitle, foodDesc, foodQtn, foodCollectBefore, foodCollectionAdd;
    private Button btnReserveFood;
    private String activityContext;
    private Food currentFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_info);

        initViews();

        Intent intent = getIntent();
        if (intent != null) {
            int foodId = intent.getIntExtra(FOOD_ID_KEY, -1);
            activityContext = intent.getExtras().getString("activityContext", "reservable");
            if (foodId != -1) {
                new GetFoodById().execute(foodId);
            }
        }

        if (activityContext.equals("editable")) {
            btnReserveFood.setVisibility(View.GONE);
            btnGrp.setVisibility(View.VISIBLE);
        } else if (activityContext.equals("reservable")) {
            btnReserveFood.setVisibility(View.VISIBLE);
            btnGrp.setVisibility(View.GONE);
        }
    }
    private void initViews() {
        foodImage = findViewById(R.id.foodImage);
        foodTitle = findViewById(R.id.foodTitle);
        foodDesc = findViewById(R.id.foodDesc);
        foodQtn = findViewById(R.id.foodQtn);
        foodCollectBefore = findViewById(R.id.foodCollectBefore);
        foodCollectionAdd = findViewById(R.id.foodCollectionAdd);
        btnReserveFood = findViewById(R.id.btnReserveFood);
        btnGrp = findViewById(R.id.btnGrp);
    }

    private void disableReserveButton() {
        btnReserveFood.setEnabled(false);
        btnReserveFood.setText("Reserved");
    }

    private void setData() {
        foodTitle.setText(currentFood.getTitle());
        foodDesc.setText(currentFood.getDescription());
        foodQtn.setText(String.valueOf(currentFood.getQuantity()));
        foodCollectBefore.setText(currentFood.getCollectBefore());
        foodCollectionAdd.setText(currentFood.getLocation());
        String imgUrl = currentFood.getImageUrl();
        if (imgUrl == null) {
            Glide.with(this).asBitmap().load(R.drawable.no_image).into(foodImage);
        } else {
            if (imgUrl.contains("http")) {
                Glide.with(this).asBitmap().load(imgUrl).into(foodImage);
            } else {
                byte[] bytes = Base64.getDecoder().decode(imgUrl);
                Glide.with(this).asBitmap().load(bytes).into(foodImage);
            }
        }

    }

    private void checkAvailability() {
        if (currentFood.getReserved()) {
            disableReserveButton();
        } else if (currentFood.getOwner().equals(admin_username)) {
            btnReserveFood.setVisibility(View.GONE);
        } else {
            btnReserveFood.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new UpdateFoodReservationStatus().execute(currentFood.getId());
                }
            });
        }
    }

    public void onDeleteBtnClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(FoodInfoActivity.this)
                .setTitle("Deleting")
                .setMessage("Are you sure you want to delete this item?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new DeleteFoodById().execute(currentFood.getId());
                    }
                });

        builder.create().show();
    }

    public void onEditBtnClick(View view) {
        Intent intent = new Intent(FoodInfoActivity.this, CreateListingActivity.class);
        intent.putExtra(FOOD_ID_KEY, currentFood.getId()); // FOOD_ID_KEY defined in FoodInfoActivity.java
        startActivity(intent);
    }

    private class GetFoodById extends AsyncTask<Integer, Void, Food> {
        private DatabaseHelper databaseHelper;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            databaseHelper = new DatabaseHelper(FoodInfoActivity.this);
        }

        @Override
        protected Food doInBackground(Integer... integers) {
            try {
                SQLiteDatabase db = databaseHelper.getReadableDatabase();

                Cursor cursor = db.rawQuery("SELECT * FROM listings WHERE id=?", new String[] {String.valueOf(integers[0])});

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
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

                        boolean isReserved;
                        if (cursor.getInt(reservedIndex) == 0) {
                            isReserved = false;
                        } else isReserved = true;

                        Food food = new Food();
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

                        cursor.close();
                        db.close();

                        return food;
                    } else {
                        cursor.close();
                        db.close();
                    }
                } else {
                    db.close();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Food food) {
            super.onPostExecute(food);

            if (food != null) {
                currentFood = food;
                setData();
                checkAvailability();
            } else {
                Toast.makeText(FoodInfoActivity.this, "An error has occurred. Please try again!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class UpdateFoodReservationStatus extends AsyncTask<Integer, Void, Boolean> {
        private DatabaseHelper databaseHelper;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            databaseHelper = new DatabaseHelper(FoodInfoActivity.this);
        }
        @Override
        protected Boolean doInBackground(Integer... integers) {
            try {
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                ContentValues cv = new ContentValues();

                cv.put("reserved_by", admin_username);
                cv.put("reserved", 1);

                long result = db.update("listings", cv, "id=?", new String[]{String.valueOf(integers[0])});

                db.close();

                if (result == -1) {
                    return false;
                } else return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (aBoolean) {
                Toast.makeText(FoodInfoActivity.this, "Food successfully reserved", Toast.LENGTH_SHORT).show();
//                disableReserveButton();
                Intent intent = new Intent(FoodInfoActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(FoodInfoActivity.this, "An error has occurred, please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class DeleteFoodById extends AsyncTask<Integer, Void, Boolean> {
        private DatabaseHelper databaseHelper;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            databaseHelper = new DatabaseHelper(FoodInfoActivity.this);
        }
        @Override
        protected Boolean doInBackground(Integer... integers) {
            try {
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                long result = db.delete("listings", "id=?", new String[]{String.valueOf(integers[0])});

                db.close();

                return result > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (aBoolean) {
                Toast.makeText(FoodInfoActivity.this, "Item has been deleted", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(FoodInfoActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(FoodInfoActivity.this, "An error has occurred, please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }
}