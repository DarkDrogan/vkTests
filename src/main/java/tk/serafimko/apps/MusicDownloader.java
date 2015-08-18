package tk.serafimko.apps;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Created by serafim on 18.08.15.
 */
public class MusicDownloader {
    /**
     * Root directory for files.
     */
    private static final String DIRECTORY = System.getProperty("user.home");
    /**
     * Application id.
     */
    private static final String APP_ID = "5025862";
    /**
     * Permission for iteration.
     */
    private static final String PERMISSIONS = "photos,audio";
    /**
     * URL for redirect and auth.
     */
    private static final String REDIR_URI = "https://oauth.vk.com/blank.html";
    /**
     * Type of redirection.
     */
    private static final String DISPLAY = "page";
    /**
     * API version.
     */
    private static final String API_VERSION = "5.35";
    /**
     * User-Agent for request.
     */
    private static final String USER_AGENT = "Mozilla/5.0";
    /**
     * Token for requests.
     */
    private static final String TOKEN =
            "f75c416a77f2bcc043710b26ef03f2e7d916961932c808c66b92345b37175cd98a215e01f801f5316c0fe";

    public static void main(String[] args) throws Exception {
//            URL website = new URL("https://oauth.vk.com/authorize?"
//                + "client_id=" + APP_ID
//                + "&scope=" + PERMISSIONS
//                + "&redirect_uri=" + REDIR_URI
//                + "&display=" + DISPLAY
//                + "&v=" + API_VERSION
//                + "&response_type=token");
//        System.out.println(website.toURI().toString());

        parseAudios(getAudioJson());
    }

    private static File getAudioJson(){
        File audios = null;
        try {
            URL audioJSONURL = new URL("https://api.vk.com/method/audio.get?owner_id=1943145&access_token=" + TOKEN + "&count=100");
            HttpURLConnection AudioJson = (HttpURLConnection) audioJSONURL.openConnection();
            AudioJson.setRequestMethod("GET");
            AudioJson.addRequestProperty("User-Agent", USER_AGENT);

            int responseCode = AudioJson.getResponseCode();

            if (responseCode == 200) {
                System.out.println("\nSending 'GET' request to URL : " + AudioJson);
                audios = new File(DIRECTORY + "/vk/music/audios.json");
                if (!audios.exists()) {
                    audios.createNewFile();
                }
            }
            ReadableByteChannel rbc = Channels.newChannel(audioJSONURL.openStream());
            FileOutputStream fos = new FileOutputStream(audios);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            AudioJson.disconnect();
            System.out.println("Audio JSON has been downloaded");

        }catch (ProtocolException e){
            System.out.println("Protocol exception");
        } catch (MalformedURLException e) {
            System.out.println("Error in URL");
        }catch (IOException e){
            e.printStackTrace();
        }
        return audios;
    }

    private static void parseAudios(File pAudios){
        File audios = pAudios;

        try {
            FileReader reader = new FileReader(audios);
            JSONParser parser = new JSONParser();
            JSONObject object = (JSONObject) parser.parse(reader);
            JSONArray array = (JSONArray) object.get("response");


            for (int i = 1; i < array.size(); i++) {
                JSONObject record = (JSONObject) array.get(i);
                String artist = record.get("artist").toString();
                String title = record.get("title").toString();
                System.out.println(artist + " " + title);
                System.out.println(record.get("url"));

                URL audioFile = new URL(record.get("url").toString());

                File file = new File(DIRECTORY + "/vk/music/" + artist + "_" + title + ".mp3");
                if (!file.exists()) file.createNewFile();
                else {
                    System.out.println("File exists!");
                    continue;
                }

                ReadableByteChannel rbc = Channels.newChannel(audioFile.openStream());
                FileOutputStream fos = new FileOutputStream(file);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fos.close();
                System.out.println("Record has been downloaded");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
