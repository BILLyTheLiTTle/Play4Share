/**This file is part of Play4Share.
 * 
 * Play4Share is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * Play4Share is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Play4Share.  If not, see <http://www.gnu.org/licenses/>.
 */

package tsapalos.bill.play4share;

import java.util.List;

import tsapalos.bill.play4share.R;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class YoutubeExtractorActivity extends Activity {

    private TextView incomingURLTextView, primaryVideoUrlTextView, primaryVideoUrlTitleTextView,
            secondaryVideoUrlTitleTextView;
    private TextView[] secondaryVideoUrlTextViews;
    private Button playPrimaryVideo;
    private Button[] playSecondaryVideos;

    private String htmlSource, link, exceptionLog, primaryVideoUrl;

    private List<String> videosUrls, secondaryVideosUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_extractor);

        incomingURLTextView = (TextView) findViewById(R.id.incoming_url_content_textview);
        primaryVideoUrlTextView = (TextView) findViewById(R.id.video_url_content_textview);
        primaryVideoUrlTitleTextView = (TextView) findViewById(R.id.video_url_title_textview);
        playPrimaryVideo = (Button) findViewById(R.id.primary_play_button);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        // respond to share intent
        if (Intent.ACTION_SEND.equals(action) && type.equals("text/plain")) {
            link = intent.getStringExtra(Intent.EXTRA_TEXT);
        }
        else {
            link = intent.getDataString();
        }

        if (link != null) {
            incomingURLTextView.setText(link);
            exceptionLog = "The URL (" + link
                    + ") does not throw an exception but the video cannot be played.";
        }

        // get the html source and export the video url
        stripUrl();
    }

    private void stripUrl() {
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 3) {
                    String txt = getString(R.string.play) + " (3)";
                    playPrimaryVideo.setText(txt);
                }
                else if (msg.what == 2) {
                    primaryVideoUrlTextView.setText(R.string.searching);
                    String txt = getString(R.string.play) + " (2)";
                    playPrimaryVideo.setText(txt);
                }
                else if (msg.what == 1) {
                    String txt = getString(R.string.play) + " (1)";
                    playPrimaryVideo.setText(txt);
                }
                else if (msg.what == 0) {
                    primaryVideoUrlTextView.setText(primaryVideoUrl);
                    String txt = getString(R.string.play);
                    playPrimaryVideo.setText(txt);
                    playPrimaryVideo.setEnabled(true);
                    // update the UI if the the secondary video list is
                    // available
                    if (secondaryVideosUrls != null) {
                        primaryVideoUrlTitleTextView.setText(R.string.primary_video_url);
                        int secondaryVideosSum = secondaryVideosUrls.size();
                        LinearLayout parent = (LinearLayout) findViewById(R.id.internal_layout);
                        // configure different layout parameters according to
                        // the view
                        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        textViewParams.setMargins(0,
                                (int) getResources().getDimension(R.dimen.medium_margin), 0, 0);
                        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        buttonParams.setMargins(0,
                                (int) getResources().getDimension(R.dimen.small_margin), 0, 0);
                        secondaryVideoUrlTitleTextView = new TextView(YoutubeExtractorActivity.this);
                        secondaryVideoUrlTitleTextView
                                .setTextAppearance(YoutubeExtractorActivity.this,
                                        android.R.style.TextAppearance_Large);
                        
                        //get the text color specified from theme
                        TypedValue typedValue = new TypedValue();
                        Theme theme=YoutubeExtractorActivity.this.getTheme();
                        theme.resolveAttribute(android.R.attr.textColor, typedValue, true);
                        
                        secondaryVideoUrlTitleTextView.setTextColor(typedValue.data);
                        if (secondaryVideosSum == 1) {
                            secondaryVideoUrlTitleTextView.setText(R.string.secondary_video_url);
                        }
                        else {
                            secondaryVideoUrlTitleTextView.setText(R.string.secondary_videos_urls);
                        }
                        parent.addView(secondaryVideoUrlTitleTextView);
                        secondaryVideoUrlTextViews = new TextView[secondaryVideosSum];
                        playSecondaryVideos = new Button[secondaryVideosSum];
                        for (int i = 0; i < secondaryVideosSum; i++) {
                            secondaryVideoUrlTextViews[i] = new TextView(
                                    YoutubeExtractorActivity.this);
                            playSecondaryVideos[i] = new Button(YoutubeExtractorActivity.this);
                            secondaryVideoUrlTextViews[i].setText(secondaryVideosUrls.get(i));
                            playSecondaryVideos[i].setId(i);
                            playSecondaryVideos[i].setText(R.string.play);
                            playSecondaryVideos[i].setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    play(v);

                                }
                            });
                            parent.addView(secondaryVideoUrlTextViews[i], textViewParams);
                            parent.addView(playSecondaryVideos[i], buttonParams);
                        }
                    }

                    String toast = String.format(getString(R.string.ok_toast),
                            getString(R.string.play));
                    Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_LONG).show();
                }
                else if (msg.what == -1) {
                    primaryVideoUrlTextView.setText(R.string.not_found);
                    String txt = getString(R.string.play);
                    playPrimaryVideo.setText(txt);
                    String toast = String.format(getString(R.string.error_toast),
                            getString(R.string.bug_report));
                    Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_LONG).show();
                }
            }
        };
        new Thread() {
            public void run() {
                try {
                    if (link != null) {
                        handler.sendMessage(handler.obtainMessage(3));
                        String raw = UrlUtils.getRawPageUrl(link);
                        Log.e("RAW", raw);
                        handler.sendMessage(handler.obtainMessage(2));
                        htmlSource = UrlUtils.getHtmlSource(raw);
                        // Log.e("PAGE", htmlSource);
                        handler.sendMessage(handler.obtainMessage(1));
                        videosUrls = UrlUtils.exportVideoUrl(htmlSource);
                        primaryVideoUrl = UrlUtils.getPrimaryVideo(videosUrls);
                        // retrieve secondary video list if available
                        if (videosUrls.size() > 1) {
                            secondaryVideosUrls = UrlUtils.getSecondaryVideos(videosUrls);
                        }
                        // Log.e("VIDEO", videoUrl);
                        handler.sendMessage(handler.obtainMessage(0));
                    }
                } catch (Exception e) {
                    exceptionLog = "The URL (" + link
                            + ") throws " + e.getClass().toString()
                            + ".\nThe exception log is:\n==========\n";
                    exceptionLog = exceptionLog + e.getMessage() + "\n==========";
                    handler.sendMessage(handler.obtainMessage(-1));
                }
            };
        }.start();
    }

    public void play(View view) {
        // if the button is enabled and able to be click means that at least one
        // video exists. So, go on, and set it as default playable video.
        String url = primaryVideoUrl;

        // check if secondary videos list is not empty
        if (secondaryVideosUrls != null) {
            // check the id of the button to match with the url video
            if (view.getId() < secondaryVideosUrls.size()) {
                // go on, and update the playable video.
                url = secondaryVideosUrls.get(view.getId());
            }
        }

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void exit(View view) {
        finish();
    }

    public void reportBug(View view) {
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        // Fill it with Data
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {
                "littleprog@gmail.com"
        });
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                "[BUG] Play4Share video sniffing");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, exceptionLog);
        // Send it off to the Activity-Chooser
        startActivity(Intent.createChooser(emailIntent, getString(R.string.email_chooser_title)));
    }

    /*
     * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
     * menu; this adds items to the action bar if it is present.
     * getMenuInflater().inflate(R.menu.youtube_extractor, menu); return true; }
     * @Override public boolean onOptionsItemSelected(MenuItem item) { // Handle
     * action bar item clicks here. The action bar will // automatically handle
     * clicks on the Home/Up button, so long // as you specify a parent activity
     * in AndroidManifest.xml. int id = item.getItemId(); if (id ==
     * R.id.action_settings) { return true; } return
     * super.onOptionsItemSelected(item); }
     */
}
