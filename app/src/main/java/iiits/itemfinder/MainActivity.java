package iiits.itemfinder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import iiits.itemfinder.Adapters.ItemAdapter;
import iiits.itemfinder.Adapters.RecyclerTouchListener;
import iiits.itemfinder.models.Item;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 10000;
    private static final int RESULT_LOAD_IMG = 22222;
    private com.github.clans.fab.FloatingActionButton cameraBttn;
    private com.github.clans.fab.FloatingActionButton galleryBttn;
    private FloatingActionMenu fabmenu;
    private Uri photoURI;
    private int REQUEST_TAKE_PHOTO = 1;
    private File image;
    private String mCurrentPhotoPath;
    private String mPhotoPathString;
    private Realm realm;
    private RealmResults<Item> items;
    private ImageView snippetOutlineImg;
    private RecyclerView recyclerview1;
    private ItemAdapter mAdapter;
    private int Position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Realm.init(getApplicationContext());
        realm = Realm.getDefaultInstance();
        setupViews();

        cameraBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {


                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA))
                    {
                        Toast.makeText(getApplicationContext(),"Grant Permission to access camera", Toast.LENGTH_SHORT).show();
                        callForPermissions();

                    }
                    else {
                        callForPermissions();
                    }
                }
                else {
                    dispatchTakePictureIntent();
                    fabmenu.close(true);
                }

            }
        });
        galleryBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent PhotoPickIntent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(PhotoPickIntent,RESULT_LOAD_IMG);
                fabmenu.close(true);
            }
        });

        recyclerview1 = (RecyclerView)findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerview1.setLayoutManager(layoutManager);

        recyclerview1.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerview1, new RecyclerTouchListener.Clicklistner() {
            @Override
            public void onclick(View view, int position) {
                Item item = items.get(position);

                    Intent intent = new Intent(MainActivity.this,MapsActivity.class);
                    intent.putExtra("lat",item.getLat().toString());
                    intent.putExtra("lon",item.getLon().toString());
                    startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {
                Position = position;
                deleteItem();
            }

            @Override
            public void onDoubleTap(View view, int position) {

            }
        }));


    }

    private void deleteItem() {
        try {
                Item item = items.get(Position);
                mAdapter.deleteItem(realm,Position);
            int noOfSnippets = items.size();
            if(noOfSnippets==0)
            {
                snippetOutlineImg.setVisibility(View.VISIBLE);
            }
        }
        catch (Exception e)
        {

        }
    }


    @Override
    protected void onStart() {

        super.onStart();

        items = realm.where(Item.class).findAll();

        int noOfitems = items.size();
        if(noOfitems>0)
        {
            snippetOutlineImg.setVisibility(View.GONE);
        }

        mAdapter = new ItemAdapter(items);
        recyclerview1.setAdapter(mAdapter);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case MY_PERMISSIONS_REQUEST_CAMERA:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(getApplicationContext(),"Permission Granted",Toast.LENGTH_SHORT).show();
                    dispatchTakePictureIntent();
                }
                else {
                    Toast.makeText(getApplicationContext(),"Requires camera permission for functionality",Toast.LENGTH_SHORT).show();
                }

            }
        }
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();

            } catch (IOException ex) {
                Toast.makeText(getApplicationContext(),"Error Creating file",Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "iiits.itemfinder.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir("Pictures/itemfinder/");
        image = File.createTempFile(
                imageFileName, ".jpg", storageDir
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        mPhotoPathString = mCurrentPhotoPath.toString();
//        Log.d("TAG", "createImageFile: "+mCurrentPhotoPath);
        return image;
    }



    private void callForPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.CAMERA},
                MY_PERMISSIONS_REQUEST_CAMERA);

    }

    private void setupViews() {
        cameraBttn = (com.github.clans.fab.FloatingActionButton)findViewById(R.id.camera);
        galleryBttn = (com.github.clans.fab.FloatingActionButton)findViewById(R.id.browse);
        fabmenu = (FloatingActionMenu)findViewById(R.id.fabmenu);
        snippetOutlineImg = (ImageView)findViewById(R.id.bookOutline);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_TAKE_PHOTO)
        {

            if(resultCode == RESULT_CANCELED)
            {
                try
                {
                    image.delete();
                }
                catch (Exception e){}

            }
            if(resultCode == RESULT_OK)
            {

                Intent intent = new Intent(MainActivity.this, CropActivity.class);
                intent.putExtra("PhotoUri", photoURI);
                intent.putExtra("photoPath",mPhotoPathString);
               //Toast.makeText(getApplicationContext(),"Took Picture",Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }

        }

        else if(requestCode == RESULT_LOAD_IMG)
        {


            try {
                final Uri imageUri = data.getData();
                Intent intent = new Intent(MainActivity.this, CropActivity.class);
                intent.putExtra("PhotoUri", imageUri);
                String imagePath = getPhotoPathFromGallery(imageUri).toString();
                File imageFile = new File(imagePath);
                if(imageFile.exists())
                {
                    intent.putExtra("photoPath",imagePath);
                    startActivity(intent);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"File Doesnt Exist",Toast.LENGTH_SHORT).show();
                }


            } catch (Exception e) {
                e.printStackTrace();

            }

        }
    }

    public String getPhotoPathFromGallery(Uri uri) {
        if (uri == null) {
            // TODO perform some logging or show user feedback
            return null;
        }

        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null); //Since manageQuery is deprecated
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }

        return uri.getPath();
    }

}
