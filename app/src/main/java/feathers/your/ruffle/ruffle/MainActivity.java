package feathers.your.ruffle.ruffle;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

import static android.R.layout.simple_spinner_dropdown_item;

public class MainActivity extends ActionBarActivity {
    String picturePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Spinner spinner = (Spinner) findViewById(R.id.spinnerCountrycode);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.countrycode, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    public void takePicture (View v) {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"),MY_INTENT_CLICK);
    }

    public void selectPicture (View v) {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , 1);//one can be replaced with any action code
        return;
    }

    String imgPath = "";

    private static final int MY_INTENT_CLICK=302;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK) {
            if (requestCode == MY_INTENT_CLICK) {
                if (null == data) return;

                String selectedImagePath;
                Uri selectedImageUri = data.getData();

                //MEDIA GALLERY
                selectedImagePath = ImageFilePath.getPath(getApplicationContext(), selectedImageUri);
                imgPath = selectedImagePath;
                Log.v("imgPath: ", imgPath);
            }
        }
    }

    public void sendPost(View v) {
        //phonenumber from text field
        EditText et = (EditText)findViewById(R.id.phonenumber);
        //from country
        Spinner s = (Spinner)findViewById(R.id.spinnerCountrycode);

        //phonenumber
        String pn = et.getText().toString();
        //countrycode
        String cc = s.getSelectedItem().toString();

        if (imgPath.isEmpty()) {
            Toast.makeText(this, "Please select a picture", Toast.LENGTH_SHORT).show(); return; }
        else if (pn.isEmpty()) {
            Toast.makeText(this, "Please enter a phone number.", Toast.LENGTH_SHORT).show(); return; }

        RetrofitInterface retrofitInterface = new RestAdapter.Builder()
                .setEndpoint("http://192.168.0.77:3000").build().create(RetrofitInterface.class);

        TypedFile img = new TypedFile("image/jpg", new File(imgPath));

        Toast.makeText(this, "Please select a picture", Toast.LENGTH_LONG);

        retrofitInterface.registerUser(img, pn, cc, new Callback<String>() {
            @Override
            public void success(String s, Response r)
            {
                Log.v("Success", "Success");
            }
            @Override
            public void failure(RetrofitError re)
            {
                Log.v("Fail", "Fail");
            }
        });
    }

    public interface RetrofitInterface {
        // synchronously
        @Multipart
        @POST("/upload")
        void registerUser( @Part("photo") TypedFile image, @Part("phonenumber") String phone, @Part("cell_countrycode") String countrycode, Callback<String> cb);
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

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public String getImagePath(Uri uri){
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":")+1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }
}
