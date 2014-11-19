
package tsapalos.bill.youforce;

import java.io.BufferedInputStream;
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
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class YoutubeExtractorActivity extends Activity {

    private TextView incomingURLTextView, youtubeVideoURLTextView;

    private String htmlSource,link, videoURL = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_extractor);

        incomingURLTextView = (TextView) findViewById(R.id.incoming_url_content_textview);

        Intent intent = getIntent();
        link = intent.getDataString();
        if (link != null)
            incomingURLTextView.setText(link);

        // get the html source and export the video url
        Thread thr = new Thread() {
            public void run() {
                try {
                    String raw = getRawPageUrl(link);
                    Log.e("RAW",raw);
                    htmlSource = getHtmlSource(raw);
                    Log.e("PAGE",htmlSource);
                    videoURL = exportVideoURL(htmlSource);
                    Log.e("VIDEO",videoURL);
                    //

                    youtubeVideoURLTextView = (TextView) findViewById(R.id.youtube_video_url_content_textview);
                    //if (htmlSource != null)
                        youtubeVideoURLTextView.setText("https://www.youtube.com/watch?v="+videoURL);
                    
                    
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        thr.start();
        // String videoURL = exportVideoURL(htmlSource);

        // show me the video url
    }
    
    public void play(View view){
        String url = "https://www.youtube.com/watch?v="+videoURL;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
    
    private String getRawPageUrl(String facebookURL){
        String start = ".php?u=";
        String end = "&h=";
        if(!(facebookURL.contains(start)&&facebookURL.contains(end))){
            return facebookURL;
        }
        String rawURL = facebookURL.substring(facebookURL.indexOf(start)+7, facebookURL.indexOf(end));
        rawURL = rawURL.replaceAll("%3A", ":");
        rawURL = rawURL.replaceAll("%2F", "/");
        return rawURL;
    }

    private String getHtmlSource(String pageUrl) throws IllegalStateException, IOException {
        HttpClient httpclient = new DefaultHttpClient(); // Create HTTP Client
        HttpGet httpget = new HttpGet(pageUrl); // Set the action you want to do
        HttpResponse response = httpclient.execute(httpget); // Executeit
        HttpEntity entity = response.getEntity(); 
        InputStream is = entity.getContent(); // Create an InputStream with the response
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine())!=null) // Read line by line
            sb.append(line + "\n");

        String resString = sb.toString(); // Result is here

        is.close();
        return resString;
    }

    private String exportVideoURL(String pageSource) {
        String start = "data-videoid=\"";
        String end = "\" data-appid=\"";
        String videoURL = pageSource.substring(pageSource.indexOf(start)+14, pageSource.indexOf(end));
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
