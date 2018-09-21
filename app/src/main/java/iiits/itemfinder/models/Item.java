package iiits.itemfinder.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Item extends RealmObject {

    @PrimaryKey
    private  long id;
    private  String itemName;
    private  String place;
    private  String ImagePath;
    private  String date;
    private  String lat;
    private  String lon;

    public Item(long id, String itemName, String place, String imagePath, String date, String lat, String lon) {

        this.id = id;
        this.itemName = itemName;
        this.place = place;
        this.ImagePath = imagePath;
        this.date = date;
        this.lat = lat;
        this.lon = lon;
    }

    public Item()
    {

    }

    public long getId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }

    public String getPlace() {
        return place;
    }

    public String getImagePath() {
        return ImagePath;
    }

    public String getDate() {
        return date;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public void setImagePath(String imagePath) {
        ImagePath = imagePath;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }
}
