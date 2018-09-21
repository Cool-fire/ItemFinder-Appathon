package iiits.itemfinder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import iiits.itemfinder.models.Item;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.realm.Realm;

public class AddItemActivity extends AppCompatActivity {

    private static final int MY_PERMISSION_CODE = 123;
    private Realm realm;
    private ImageView croppedimage;
    private FloatingActionButton doneBttn;
    private TextInputEditText ItemName;
    private TextInputEditText place;
    private EditText lat;
    private EditText lon;
    private String croppedPath;
    private ImageButton locationBttn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        Intent intent = getIntent();
        croppedPath = intent.getStringExtra("croppedPath");

        Realm.init(getApplicationContext());
        realm = Realm.getDefaultInstance();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Add Item");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setupviews();

        try
        {
            Picasso.get().load(new File(croppedPath)).resize(600,600).centerInside().into(croppedimage);

        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),"Error Occurred ",Toast.LENGTH_SHORT).show();
        }

        locationBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(ContextCompat.checkSelfPermission(AddItemActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
                {
                    getCoordinates();
                }
                else
                {
                    checkLocationPermission();
                }
            }
        });

        doneBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String item_name = ItemName.getText().toString();
                String place_name = place.getText().toString();
                String lat_val = lat.getText().toString();
                String lon_val = lon.getText().toString();
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                if(item_name.isEmpty() || place_name.isEmpty() || lat_val.isEmpty() || lon_val.isEmpty())
                {
                    if(item_name.isEmpty())
                    {
                        ItemName.setError("Give it a good name");
                    }
                    if (place_name.isEmpty())
                    {
                        place.setError("Empty");
                    }
                    if (lat_val.isEmpty())
                    {
                        lat.setError("Empty");
                    }
                    if (lon_val.isEmpty()) {
                        lon.setError("Empty");
                    }
                }
                else
                {

                    AddItem(item_name,place_name,lat_val,lon_val,date,croppedPath);

                }


            }
        });

    }
//
//    @Override
//    public void onStop() {
//        SmartLocation.with(getApplicationContext()).location().stop();
//        super.onStop();
//    }

    private void AddItem(final String item_name, final String place_name, final String lat_val, final String lon_val, final String date, final String croppedPath) {


        realm.executeTransactionAsync(new Realm.Transaction() {
            public Number presentId;

            @Override
            public void execute(Realm realm) {
                presentId = realm.where(Item.class).max("id");
                int id = getNextID(presentId);
                Item item = realm.createObject(Item.class, id);
                item.setDate(date);
                item.setImagePath(croppedPath);
                item.setItemName(item_name);
                item.setPlace(place_name);
                item.setLat(lat_val);
                item.setLon(lon_val);

                item = realm.copyToRealmOrUpdate(item);
            }
        }, new Realm.Transaction.OnSuccess() {

            private int size;

            @Override
            public void onSuccess() {
                size = realm.where(Item.class).findAll().size();
                Log.d("TAG", "onSuccess: "+size);
                finish();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Toast.makeText(getApplicationContext(),"Error Occured",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private int getNextID(Number max) {
        int nextId ;
        if(max == null)
        {
            nextId = 1;
        }
        else
        {
            nextId = max.intValue()+1;
        }

        return nextId;
    }

    private void getCoordinates() {
try
{
    SmartLocation.with(getApplicationContext()).location()
            .oneFix()
            .start(new OnLocationUpdatedListener() {
                @Override
                public void onLocationUpdated(Location location) {
                    double lati = location.getLatitude();
                    double lng = location.getLongitude();

                    lat.setText(Double.toString(lati));
                    lon.setText(Double.toString(lng));
                    Toast.makeText(getApplicationContext(),Double.toString(lati)+" "+Double.toString(lng),Toast.LENGTH_SHORT).show();
                }
            });
}catch (Exception e)
{

}

    }

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSION_CODE);

            }
            else
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSION_CODE);

            }

        }
    }

    @Override
    public boolean onSupportNavigateUp() {

        finish();
        return super.onSupportNavigateUp();
    }

    private void setupviews() {
        croppedimage = (ImageView)findViewById(R.id.croppedimageView);
        doneBttn = (FloatingActionButton)findViewById(R.id.doneBTTN);
        ItemName = (TextInputEditText)findViewById(R.id.itemName);
        place = (TextInputEditText)findViewById(R.id.place);
        lat = (EditText)findViewById(R.id.lat);
        lon = (EditText)findViewById(R.id.lon);
        locationBttn = (ImageButton)findViewById(R.id.locationButton);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case MY_PERMISSION_CODE:

            {
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {

                        getCoordinates();
                    }

                }
                else
                {
                    Toast.makeText(this,"Permission Required",Toast.LENGTH_SHORT).show();
                }
            }

        }
    }
}
