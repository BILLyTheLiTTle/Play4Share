
package tsapalos.bill.play4share;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String exportVideoUrl(String pageSource) throws NullPointerException {
        final String youtube = "YouTube", dailymotion = "Dailymotion", liveleak = "LiveLeak", vimeo = "Vimeo";
        Map<String, List<Integer>> indices = new HashMap<String, List<Integer>>(5);
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
        String videosUrls = null, videosIds = null, server=null;
        int i = 0;
        for (i = 0; i < start.length; i++) {
            for (int j = 0; j < end.length; j++) {
                // if videoHash is not empty (null) it means that the video has
                // been found
                // if (videoHash == null) {
                // make sure that the html code has one of the starting
                // strings
                if (pageSource.contains(start[i])) {
                    // find all occurrences of the specific string
                    int index = pageSource.indexOf(start[i]);
                    while (index >= 0) {
                        if (i == 0 || i == 1 || i == 2) {
                            server=youtube;
                        }
                        else if (i == 3) {
                            server=dailymotion;
                        }
                        else if (i == 4) {
                            server=liveleak;
                        }
                        else if (i == 5) {
                            server=vimeo;
                        }
                        List<Integer> currentValue = indices.get(server);
                        if (currentValue == null) {
                            currentValue = new ArrayList<Integer>();
                            indices.put(server, currentValue);
                        }
                        currentValue.add(Integer.valueOf(index));
                        index = pageSource.indexOf(start[i], index + 1);
                    }
                    // TODO for every occurrence sniff the possible url of the
                    // video

                    // TODO for every possible url video sniff the video id

                    // TODO store every video id in a list

                    // remove unnecessary string before the starting string
                    videosUrls = pageSource.substring(pageSource.indexOf(start[i])
                            + start_advance[i]);
                    // find a possible end of the video url string
                    videosUrls = videosUrls.substring(0, videosUrls.indexOf(end[j]));
                    char[] chars = videosUrls.toCharArray();
                    // check every character if it a part of video's url
                    for (int k = 0; k < chars.length; k++) {
                        // we need letters, numbers and some special
                        // characters as a part of the video's url
                        if (isApprovedCharacter(chars[k])) {
                            // if the videoHash string is null do not append
                            // new characters
                            if (videosIds != null)
                                videosIds = videosIds + chars[k];
                            else
                                videosIds = String.valueOf(chars[k]);
                        }
                        // finish the url construction if any non-specified
                        // character found
                        else {
                            Log.e("HASH", videosIds);
                            break;
                        }
                    }
                }
                // }
                // exit "end" loop if the video hash is found
                // else
                // break;
            }
            // exit the "start" loop if the video hash is found
            if (videosIds != null)
                break;
        }
        // throw exception if the video is not found
        if (videosUrls == null) {
            throw new NullPointerException("The video URL is null!");
        }
        // create the video URL according to the server
        switch (i) {
            case 3:
                videosUrls = "http://www.dailymotion.com/video/" + videosIds;
                break;
            case 4:
                videosUrls = "http://www.liveleak.com/ll_embed?f=" + videosIds;
                break;
            case 5:
                videosUrls = "http://player.vimeo.com/video/" + videosIds;
                break;
            default:
                videosUrls = "http://www.youtube.com/watch?v=" + videosIds;
                break;
        }
        return videosUrls;
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
