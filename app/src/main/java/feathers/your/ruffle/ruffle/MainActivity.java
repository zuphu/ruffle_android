package feathers.your.ruffle.ruffle;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.annotations.SerializedName;

import java.io.File;;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedFile;
import static android.R.layout.simple_spinner_dropdown_item;

public class MainActivity extends ActionBarActivity {
    String picturePath;
    public final static String EXTRA_MESSAGE = "feathers.your.ruffle.MESSAGE";
    public final static String ERROR_MESSAGE = "feathers.your.ruffle.ERRORMSG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!isNetworkAvailable(this)) {
            Toast.makeText(getApplicationContext(), "Internet connection required.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Populate spinner from resource file
        Spinner spinner = (Spinner) findViewById(R.id.spinnerCountryCode);
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
                ImageView iv = (ImageView)findViewById(R.id.imageviewPicture);
                File f = new File(imgPath);
                Bitmap b = BitmapFactory.decodeFile(f.getAbsolutePath());
                //b.setWidth(200);b.setHeight(200);
                iv.setImageBitmap(b);
                Log.v("imgPath: ", imgPath);
            }
            else if (requestCode == PICK_CONTACT) {
                Uri contactData = data.getData();
                Cursor c = getContentResolver().query(contactData, null, null, null, null);
                if (c.moveToFirst()) {


                    String id =c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                    String hasPhone =c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                    if (hasPhone.equalsIgnoreCase("1")) {
                        Cursor phones = getContentResolver().query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,
                                null, null);
                        phones.moveToFirst();
                        String cNumber = phones.getString(phones.getColumnIndex("data1"));

                        Log.v("PHONENUMBER: ", cNumber);
                        EditText et = (EditText)findViewById(R.id.phonenumber);
                        et.setText(cNumber);
                    }
                    String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    Log.v("NAME: ", name);

                }
            }
        }
    }

    public void sendPost(View v) {
        //phonenumber from text field
        EditText et = (EditText)findViewById(R.id.phonenumber);
        //from country
        Spinner s = (Spinner)findViewById(R.id.spinnerCountryCode);

        //phonenumber
        String pn = et.getText().toString();
        //countrycode
        String cc = s.getSelectedItem().toString();

        if (imgPath.isEmpty()) {
            Toast.makeText(this, "Please select a picture", Toast.LENGTH_SHORT).show(); return; }
        else if (pn.isEmpty()) {
            Toast.makeText(this, "Please enter a phone number.", Toast.LENGTH_SHORT).show(); return; }

        RetrofitInterface retrofitInterface = new RestAdapter.Builder()
                .setEndpoint("http://192.168.1.63:3000").build().create(RetrofitInterface.class);

        TypedFile img = new TypedFile("image/jpg", new File(imgPath));

        Toast.makeText(this, "Please select a picture", Toast.LENGTH_LONG);

        Log.v("pn: ", pn);
        Log.v("cc: ", cc);
        Log.v("imgPath: ", imgPath);

        retrofitInterface.registerUser(img, pn, cc, new Callback<Response>() {
            @Override
            public void success(Response result, Response response)
            {
                Log.v("Success", "Success");
                Intent intent = new Intent(getApplicationContext(), DisplayRuffleResult.class);
                String message = "Success!!!";
                intent.putExtra(EXTRA_MESSAGE, message);
                intent.putExtra(ERROR_MESSAGE, "");
                startActivity(intent);
            }
            @Override
            public void failure(RetrofitError re)
            {
                String json =  new String(((TypedByteArray) re.getResponse().getBody()).getBytes());
                Log.v("failure", json.toString());

                Intent intent = new Intent(getApplicationContext(), DisplayRuffleResult.class);
                String message = "FAIL!!!";
                intent.putExtra(EXTRA_MESSAGE, message);
                intent.putExtra(ERROR_MESSAGE, json);
                startActivity(intent);
            }
        });
    }

    static final int PICK_CONTACT = 200;
    public void selectContact (View v) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }
    public interface RetrofitInterface {
        // synchronously
        @Multipart
        @POST("/upload")
        void registerUser( @Part("photo") TypedFile image, @Part("phonenumber") String phone, @Part("cell_countrycode") String countrycode, Callback<Response> cb);
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

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(conMan.getActiveNetworkInfo() != null && conMan.getActiveNetworkInfo().isConnected())
            return true;
        else
            return false;
    }
}
