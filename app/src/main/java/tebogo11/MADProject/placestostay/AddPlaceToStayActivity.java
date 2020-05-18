package tebogo11.MADProject.placestostay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class AddPlaceToStayActivity extends AppCompatActivity implements View.OnClickListener {

    class SubmitPlaceToStayOnline extends AsyncTask<String,Void,String>
    {

        public String doInBackground(String... pTSentrys) {
            HttpURLConnection conn = null;
            try{
                URL url = new URL("https://www.hikar.org/course/ws/add.php" );
                conn = (HttpURLConnection) url.openConnection();

                String[] components = pTSentrys[0].split(",");
                String postDataT = "username=user012&name="+components[1]+"&type="+components[2]+"&price="+components[3]+"&lon="+components[5]+"&lat="+components[4]+"&year=20";

                conn.setDoOutput(true);
                conn.setFixedLengthStreamingMode(postDataT.length());

                OutputStream out = conn.getOutputStream();
                out.write(postDataT.getBytes());
                if(conn.getResponseCode() == 200) {
                    InputStream in = conn.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String all = "", line;
                    while ((line = br.readLine()) != null)
                        all += line;
                        return all;

                }
                    else
                    {
                        return "HTTP ERROR ; " + conn.getResponseCode();
                    }

            }
            catch (IOException e) {
                e.toString();
            }
            finally {
                if(conn!=null)
                {
                    conn.disconnect();
                }
            }
            return null;
        }

        public void onPostExecute(String result)
        {
            new AlertDialog.Builder(AddPlaceToStayActivity.this).setMessage("Server sent back : " + result)
                    .setPositiveButton("OK", null).show();
        }
    }

    @Override
  public void onCreate(Bundle saveInStanceState) {

      super.onCreate(saveInStanceState);
        setContentView(R.layout.activity_addplacestostay);
        Button add = (Button)findViewById(R.id.btnAddPlace);
        add.setOnClickListener(this);

  }


    @Override
    public void onClick(View v) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            double lat = Double.parseDouble(prefs.getString("com.tebogo11.latitude", null));
            double lon = Double.parseDouble(prefs.getString("com.tebogo11.longitude", null));
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            EditText name = (EditText) findViewById(R.id.pname);
            EditText type = (EditText) findViewById(R.id.ptype);
            EditText price = (EditText) findViewById(R.id.pprice);
            String ptsname = name.getText().toString();
            String ptstype = type.getText().toString();
            double ptsprice = Double.parseDouble(price.getText().toString());
            boolean autoupload = prefs.getBoolean("autoUpload", false);
            if (!autoupload) {
                bundle.putString("com.tebogo11.name", ptsname);
                bundle.putString("com.tebogo11.type", ptstype);
                bundle.putDouble("com.tebogo11.price", ptsprice);
                bundle.putDouble("com.tebogo11.addlat", lat);
                bundle.putDouble("com.tebogo11.addlong", lon);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
            } else {
                try {
                    PrintWriter pw = new PrintWriter(new FileWriter(Environment.getExternalStorageDirectory() + "/placestostay.txt", true));
                    String inputPlace = "" + ptsname + "," + ptstype + "," + ptsprice + "," + lat + "," + lon;
                    pw.println(inputPlace);
                    pw.close();
                } catch (IOException e) {
                    System.out.println("I/O Error " + e);
                }

            }
            boolean autouploadonline = prefs.getBoolean("autoUploadonline", false);
            if (!autouploadonline) {
                finish();
            } else {
                String inputPlace = "user012," + ptsname + "," + ptstype + "," + ptsprice + "," + lat + "," + lon;
                SubmitPlaceToStayOnline NewSubmittion = new SubmitPlaceToStayOnline();
                NewSubmittion.execute(inputPlace);
                finish();
            }
        }
    }



