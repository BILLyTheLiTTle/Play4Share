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

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class UrlUtils {
    public static String getRawPageUrl(String facebookUrl) throws UnsupportedEncodingException {
        String start = "u=";
        String end = "&h=";
        // decode URL special characters
        facebookUrl = URLDecoder.decode(facebookUrl, "UTF-8");
        // clean the stripped url from page pointers
        if (facebookUrl.lastIndexOf("/#") != -1)
            facebookUrl = facebookUrl.replaceAll("/#", "/");

        if (!facebookUrl.contains(start)) {
            return facebookUrl;
        }
        String rawURL = facebookUrl.substring(facebookUrl.indexOf(start) + 2,
                facebookUrl.indexOf(end));
        return rawURL;
    }

    public static String getHtmlSource(String pageUrl) throws IllegalStateException, IOException {
        HttpClient httpclient = new DefaultHttpClient(); // Create HTTP Client
        HttpGet httpget = new HttpGet(pageUrl); // Set the action you want to do
        HttpResponse response = httpclient.execute(httpget); // Execute it
        HttpEntity entity = response.getEntity();
        InputStream is = entity.getContent(); // Create an InputStream with the
                                              // response
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null)
            // Read line by line
            sb.append(line + "\n");

        String resString = sb.toString(); // Result is here

        is.close();
        return resString;
    }

    public static List<String> exportVideoUrl(String pageSource) throws NullPointerException {
        final String youtube = "YouTube", dailymotion = "Dailymotion", liveleak = "LiveLeak", vimeo = "Vimeo";
        Map<String, List<String>> indices = new HashMap<String, List<String>>(3);
        final String[] start = new String[] {
                "data-videoid=\"", ".youtube.com/embed/", ".youtube.com/watch?v=",
                ".dailymotion.com/video/", "liveleak.com/ll_embed?f=", "player.vimeo.com/video/"

        };
        final int[] start_advance = new int[] {
                14, 19, 21, 23, 24, 23
        };
        final String[] end = new String[] {
                "\"", "\'"
        };
        String videoUrl = null, videoId = null, server = null;
        boolean found = false;

        for (int i = 0; i < start.length; i++) {
            for (int j = 0; j < end.length; j++) {
                if (pageSource.contains(start[i])) {
                    // find all occurrences of the specific string
                    int index = pageSource.indexOf(start[i]);
                    while (index >= 0) {
                        // sniff the possible start of the url of the
                        // video
                        videoUrl = pageSource.substring(index + start_advance[i]);
                        // sniff the possible end of the url of the
                        // video
                        videoUrl = videoUrl.substring(0, videoUrl.indexOf(end[j]));
                        videoId = retrieveVideoId(videoUrl);
                        // store every video id found
                        if (videoId != null) {
                            switch (i) {
                                case 3:
                                    server = dailymotion;
                                    break;
                                case 4:
                                    server = liveleak;
                                    break;
                                case 5:
                                    server = vimeo;
                                    break;
                                default:
                                    server = youtube;
                                    break;
                            }
                            List<String> currentValue = indices.get(server);
                            if (currentValue == null) {
                                currentValue = new ArrayList<String>();
                                indices.put(server, currentValue);
                            }
                            if (!currentValue.contains(videoId)) {
                                currentValue.add(videoId);
                            }
                        }
                        index = pageSource.indexOf(start[i], index + 1);
                    }
                }
            }
        }
        // create the lsit with all videos' urls
        List<String> videosUrls=new ArrayList<String>(3);
        // convert every video id to appropriate url
        for (Map.Entry<String, List<String>> entry : indices.entrySet()) {
            Log.e("BREAKPOINT", "HERE " + (entry == null));
            if (entry != null) {
                List<String> videosIds = entry.getValue();
                found = true;
                // create the video URL according to the server
                switch (entry.getKey()) {
                    case dailymotion:
                        for (int j = 0; j < videosIds.size(); j++) {
                            videoId = videosIds.get(j);
                            videosIds.set(j, "http://www.dailymotion.com/video/" + videoId);
                            Log.e("DAILYMOTION-" + j, videosIds.get(j));
                            // add the url to the total list
                            videosUrls.add(videosIds.get(j));
                        }
                        break;
                    case liveleak:
                        for (int j = 0; j < videosIds.size(); j++) {
                            videoId = videosIds.get(j);
                            videosIds.set(j, "http://www.liveleak.com/ll_embed?f=" + videoId);
                            Log.e("LIVELEAK-" + j, videosIds.get(j));
                            // add the url to the total list
                            videosUrls.add(videosIds.get(j));
                        }
                        break;
                    case vimeo:
                        for (int j = 0; j < videosIds.size(); j++) {
                            videoId = videosIds.get(j);
                            videosIds.set(j, "http://player.vimeo.com/video/" + videoId);
                            Log.e("VIMEO-" + j, videosIds.get(j));
                            // add the url to the total list
                            videosUrls.add(videosIds.get(j));
                        }
                        break;
                    default:
                        for (int j = 0; j < videosIds.size(); j++) {
                            videoId = videosIds.get(j);
                            videosIds.set(j, "http://www.youtube.com/watch?v=" + videoId);
                            Log.e("YOUTUBE-" + j, videosIds.get(j));
                            // add the url to the total list
                            videosUrls.add(videosIds.get(j));
                        }
                        break;
                }
            }
        }
        // throw exception if no video found
        if (!found) {
            throw new NullPointerException("No videos found");
        }
        return videosUrls;
    }
    
    public static String getPrimaryVideo(List<String> videosUrls){
        return videosUrls.get(0);
    }
    
    public static List<String> getSecondaryVideos(List<String> videosUrls){
        videosUrls.remove(0);
        return videosUrls;
    }

    private static String retrieveVideoId(String videoUrl) {
        String videoId = null;
        char[] chars = videoUrl.toCharArray();
        // check every character if it a part of video's url
        for (int k = 0; k < chars.length; k++) {
            // we need letters, numbers and some special
            // characters as a part of the video's url
            if (isApprovedCharacter(chars[k])) {
                // if the videoHash string is null do not append
                // new characters
                if (videoId != null)
                    videoId = videoId + chars[k];
                else
                    videoId = String.valueOf(chars[k]);
            }
            // finish the url construction if any non-specified
            // character found
            else {
                Log.e("HASH", videoId);
                break;
            }
        }
        return videoId;
    }

    private static boolean isApprovedCharacter(char c) {
        char[] approved = new char[] {
                '-', '_'
        };
        if (Character.isLetterOrDigit(c)) {
            return true;
        }
        else {
            for (int i = 0; i < approved.length; i++) {
                if (c == approved[i]) {
                    return true;
                }
            }
        }
        return false;
    }
}
