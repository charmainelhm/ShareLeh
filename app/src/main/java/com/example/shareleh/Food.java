package com.example.shareleh;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Food {
    private int id;
    private String title;
    private String description;
    private String owner;
    private String imageUrl;
    private String collectBefore;
    private String location;
    private int quantity;
    private String reservedBy;
    private boolean reserved;

    public Food(int id, String title, String description, String owner, String imageUrl, String collectBefore, String location, int quantity, String reservedBy, boolean reserved) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.owner = owner;
        this.imageUrl = imageUrl;
        this.collectBefore = collectBefore;
        this.location = location;
        this.quantity = quantity;
        this.reservedBy = reservedBy;
        this.reserved =reserved;
    }

    public Food() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCollectBefore() {
        return collectBefore;
    }

    public void setCollectBefore(String collectBefore) {
        this.collectBefore = collectBefore;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getReservedBy() {
        return reservedBy;
    }

    public void setReservedBy(String reservedBy) {
        this.reservedBy = reservedBy;
    }

    public boolean getReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    @Override
    public String toString() {
        return "Food{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", owner='" + owner + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", collectBefore='" + collectBefore + '\'' +
                ", location='" + location + '\'' +
                ", quantity=" + quantity +
                ", reservedBy='" + reservedBy + '\'' +
                ", reserved=" + reserved +
                '}';
    }
}
