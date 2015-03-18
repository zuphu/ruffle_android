package feathers.your.ruffle.ruffle;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class DisplayRuffleResult extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        String message2 = intent.getStringExtra(MainActivity.ERROR_MESSAGE);
        Log.v(message, message);
        Log.v(message2, message2);
        setContentView(R.layout.activity_display_ruffle_result);
        TextView textView = (TextView)findViewById(R.id.textView);
        TextView textView2 = (TextView)findViewById(R.id.textviewResult);
        textView.setText(message);
        textView2.setText(message2);
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
}
