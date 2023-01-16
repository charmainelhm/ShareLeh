package com.example.shareleh;

import static com.example.shareleh.FoodInfoActivity.FOOD_ID_KEY;
import static com.example.shareleh.MainActivity.admin_username;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;

public class CreateListingActivity extends AppCompatActivity {
    public static final int CAMERA_PERM_CODE = 101;
    private int foodId = -1;
    private EditText editTxtCollectBefore, editTxtFoodTitle, editTxtFoodDesc, editTxtQtn, editTxtLocation;
    private TextView txtTitleWarning, txtDescWarning, txtQtnWarning, txtCollectBeforeWarning, txtLocationWarning, txtAddImage;
    private ImageView itemImage;
    private Button btnAddToList;
    private ImageButton btnAddImage, btnDeleteImage;
    private LinearLayout listingImageGroup;
    private String imageByte = null;
    ActivityResultLauncher<Intent> getImageResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_listing);

        initViews();

        editTxtCollectBefore.setInputType(InputType.TYPE_NULL);

        Intent intent = getIntent();
        if (intent != null) {
            foodId = intent.getIntExtra(FOOD_ID_KEY, -1);
            if (foodId != -1) {
                setListing(foodId);
                btnAddToList.setText("Update");
            }
        }

        getImageResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap image = (Bitmap) result.getData().getExtras().get("data");
                    bitmapToByte(image);
                    Glide.with(CreateListingActivity.this).asBitmap().load(image).into(itemImage);
                    btnAddImage.setVisibility(View.GONE);
                    txtAddImage.setVisibility(View.GONE);
                    listingImageGroup.setVisibility(View.VISIBLE);
                }
            }
        });

        btnDeleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Glide.with(CreateListingActivity.this).clear(itemImage);
                imageByte = null;
                listingImageGroup.setVisibility(View.GONE);
                btnAddImage.setVisibility(View.VISIBLE);
                txtAddImage.setVisibility(View.VISIBLE);
            }
        });

        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu imageOptions = new PopupMenu(CreateListingActivity.this, btnAddImage);
                imageOptions.getMenuInflater().inflate(R.menu.image_options_menu, imageOptions.getMenu());

                imageOptions.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch(menuItem.getItemId()) {
                            case R.id.fromCamera:
                                askCameraPermission();
                                break;
                            case R.id.fromGallery:
                                Toast.makeText(CreateListingActivity.this, "This is function is still a work in progress!", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                Toast.makeText(CreateListingActivity.this, "An error has occurred, please try again!", Toast.LENGTH_SHORT).show();
                                break;
                        }
                        return true;
                    }
                });

                imageOptions.show();
            }
        });

        editTxtCollectBefore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateTimeDialog(editTxtCollectBefore);
            }
        });

        btnAddToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = editTxtFoodTitle.getText().toString();
                String desc = editTxtFoodDesc.getText().toString();
                String qtn = editTxtQtn.getText().toString();
                String location = editTxtLocation.getText().toString();
                String collectBefore = editTxtCollectBefore.getText().toString();

                if (inputChecker(title, desc, qtn, location, collectBefore)) {
                    boolean isSuccessful;
                    if (foodId != -1) {
                        isSuccessful = updateListing(title, desc, qtn, location, collectBefore);
                    } else {
                        isSuccessful = addNewListing(title, desc, qtn, location, collectBefore);
                    }
                    if (isSuccessful) {
                        Toast.makeText(CreateListingActivity.this, "Listing successfully created/updated!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(CreateListingActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(CreateListingActivity.this, "An error has occurred, please try again!", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use the camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initViews() {
        listingImageGroup = findViewById(R.id.listingImageGroup);
        btnAddToList = findViewById(R.id.btnAddToList);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnDeleteImage = findViewById(R.id.btnDeleteImage);
        editTxtFoodTitle = findViewById(R.id.editTxtFoodTitle);
        editTxtFoodDesc = findViewById(R.id.editTxtFoodDesc);
        editTxtQtn = findViewById(R.id.editTxtQtn);
        editTxtLocation = findViewById(R.id.editTxtLocation);
        editTxtCollectBefore = findViewById(R.id.editTxtCollectBefore);
        txtAddImage = findViewById(R.id.txtAddImage);
        txtTitleWarning = findViewById(R.id.txtTitleWarning);
        txtDescWarning = findViewById(R.id.txtDescWarning);
        txtQtnWarning = findViewById(R.id.txtQtnWarning);
        txtCollectBeforeWarning = findViewById(R.id.txtCollectBeforeWarning);
        txtLocationWarning = findViewById(R.id.txtLocationWarning);
        itemImage = findViewById(R.id.itemImage);
    }

    private void bitmapToByte(@NonNull Bitmap image) {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArray);
        byte[] bytes = byteArray.toByteArray();
        imageByte = Base64.getEncoder().encodeToString(bytes);
    }

    private void askCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        getImageResult.launch(cameraIntent);
    }

    private void showDateTimeDialog(EditText editTxtCollectBefore) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);

                        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yy HH:mm aa");
                        editTxtCollectBefore.setText(simpleDateFormat.format(calendar.getTime()));
                    }
                };

                new TimePickerDialog(CreateListingActivity.this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
            }
        };

        new DatePickerDialog(CreateListingActivity.this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setListing(int foodId) {
        String title, description, location, collectBefore, imgUrl;
        int qtn;

        DatabaseHelper databaseHelper = new DatabaseHelper(CreateListingActivity.this);

        try{
            SQLiteDatabase db = databaseHelper.getReadableDatabase();

            Cursor cursor = db.rawQuery("SELECT * FROM listings WHERE id=?", new String[] {String.valueOf(foodId)});

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int titleIndex = cursor.getColumnIndex("title");
                    int descriptionIndex = cursor.getColumnIndex("description");
                    int imgIndex = cursor.getColumnIndex("image_url");
                    int cDateIndex = cursor.getColumnIndex("collect_before");
                    int locationIndex = cursor.getColumnIndex("location");
                    int qtnIndex = cursor.getColumnIndex("qtn");

                    title = cursor.getString(titleIndex);
                    description = cursor.getString(descriptionIndex);
                    qtn = cursor.getInt(qtnIndex);
                    location = cursor.getString(locationIndex);
                    collectBefore = cursor.getString(cDateIndex);
                    imgUrl = cursor.getString(imgIndex);

                    cursor.close();
                    db.close();

                    if (imgUrl != null) {
                        imageByte = imgUrl;
                        btnAddImage.setVisibility(View.GONE);
                        txtAddImage.setVisibility(View.GONE);
                        listingImageGroup.setVisibility(View.VISIBLE);

                        if (imgUrl.contains("http")) {
                            Glide.with(this).asBitmap().load(imgUrl).into(itemImage);
                        } else {
                            byte[] bytes = Base64.getDecoder().decode(imgUrl);
                            Glide.with(this).asBitmap().load(bytes).into(itemImage);
                        }
                    }

                    editTxtFoodTitle.setText(title);
                    editTxtFoodDesc.setText(description);
                    editTxtQtn.setText(String.valueOf(qtn));
                    editTxtCollectBefore.setText(collectBefore);
                    editTxtLocation.setText(location);



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
    }

    private boolean addNewListing(String title, String desc, String qtn, String location, String collectBefore) {
        DatabaseHelper databaseHelper = new DatabaseHelper(CreateListingActivity.this);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues newListing = new ContentValues();
        newListing.put("title", title);
        newListing.put("description", desc);
        newListing.put("owner", admin_username);
        newListing.put("image_url", imageByte);
        newListing.put("collect_before", collectBefore);
        newListing.put("location", location);
        newListing.put("qtn", Integer.valueOf(qtn));
        newListing.put("reserved_by", "");

        long result = db.insert("listings", null, newListing);

        db.close();

        return result != -1;
    }

    private boolean updateListing(String title, String desc, String qtn, String location, String collectBefore) {
        try {
            DatabaseHelper databaseHelper = new DatabaseHelper(CreateListingActivity.this);

            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();

            cv.put("title", title);
            cv.put("description", desc);
            cv.put("image_url", imageByte);
            cv.put("qtn", Integer.valueOf(qtn));
            cv.put("location", location);
            cv.put("collect_before", collectBefore);

            long result = db.update("listings", cv, "id=?", new String[]{String.valueOf(foodId)});

            db.close();

            return result != -1;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean inputChecker(String title, String desc, String qtn, String location, String collectBefore) {
        int invalidInput = 0;

        if (title.trim().length() == 0) {
            txtTitleWarning.setVisibility(View.VISIBLE);
            invalidInput++;
        } else {
            txtTitleWarning.setVisibility(View.GONE);
        }

        if (desc.trim().length() == 0) {
            txtDescWarning.setVisibility(View.VISIBLE);
            invalidInput++;
        } else {
            txtDescWarning.setVisibility(View.GONE);
        }

        if (qtn.trim().length() == 0) {
            txtQtnWarning.setVisibility(View.VISIBLE);
            invalidInput++;
        } else {
            txtQtnWarning.setVisibility(View.GONE);
        }

        if (location.trim().length() == 0) {
            txtLocationWarning.setVisibility(View.VISIBLE);
            invalidInput++;
        } else {
            txtLocationWarning.setVisibility(View.GONE);
        }

        if (collectBefore.trim().length() == 0) {
            txtCollectBeforeWarning.setVisibility(View.VISIBLE);
            invalidInput++;
        } else {
            txtCollectBeforeWarning.setVisibility(View.GONE);
        }

        return invalidInput == 0;
    }


}