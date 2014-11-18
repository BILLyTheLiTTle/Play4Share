
package tsapalos.bill.youforce;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class YoutubeExtractorActivity extends Activity {

    private TextView incomingURLTextView, youtubeVideoURLTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_extractor);

        incomingURLTextView = (TextView) findViewById(R.id.incoming_url_content_textview);

        Intent intent = getIntent();
        String link = intent.getDataString();
        if (link != null)
            incomingURLTextView.setText(link);
        
        //read the html source and export the video url
        String htmlSource=null;
        try {
            htmlSource = readHtmlSource(link);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //String videoURL = exportVideoURL(htmlSource);
        
        // show me the video url
        youtubeVideoURLTextView = (TextView) findViewById(R.id.youtube_video_url_content_textview);
        if (htmlSource != null)
            youtubeVideoURLTextView.setText(htmlSource);
    }
    
    private String readHtmlSource(String pageUrl) throws IllegalStateException, IOException{
        HttpClient httpclient = new DefaultHttpClient(); // Create HTTP Client
        HttpGet httpget = new HttpGet(pageUrl); // Set the action you want to do
        HttpResponse response = httpclient.execute(httpget); // Execute it
        HttpEntity entity = response.getEntity(); 
        InputStream is = entity.getContent(); // Create an InputStream with the response
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) // Read line by line
            sb.append(line + "\n");

        String resString = sb.toString(); // Result is here

        is.close();
        
        return resString;
    }
    private String exportVideoURL(String pageSource){
        String start = "data-videoid=\"";
        String end = "\" data-appid=\"";
        String videoURL = pageSource.substring(pageSource.indexOf(start), pageSource.indexOf(end));
        return videoURL;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.youtube_extractor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
