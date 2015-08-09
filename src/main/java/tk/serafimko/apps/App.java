package tk.serafimko.apps;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;


public class App {
    private static final String DIRECTORY = System.getProperty("user.home");
    private static final String APP_ID = "5025862";
    private static final String PERMISSIONS = "photos";
    private static final String REDIRECT_URI = "https://oauth.vk.com/blank.html";
    private static final String DISPLAY = "page";
    private static final String API_VERSION = "5.35";
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String TOKEN = "6ab63644dc9873ac464a082d72669bbfb278b0a59e727bdb18860bdbe3ee1dac59cb9567180a224d204a0";

    public static void main(String[] args) throws Exception {
        URL website = new URL("https://oauth.vk.com/authorize?" +
                "client_id=" + APP_ID +
                "&scope=" + PERMISSIONS +
                "&redirect_uri=" + REDIRECT_URI +
                "&display=" + DISPLAY +
                "&v=" + API_VERSION +
                "&response_type=token");
        System.out.println(website.toURI().toString());

//        URL website = new URL("https://api.vk.com/method/audio.get?count=1203&access_token=" + TOKEN);

        FileReader reader = new FileReader(getPhotos());
        try {

            JSONParser parser = new JSONParser();
            JSONObject object = (JSONObject) parser.parse(reader);
            JSONArray array = (JSONArray) object.get("response");
            for (int i = 0; i < array.size(); i++) {
                URL downloadPhoto;
                JSONObject innerObj = (JSONObject) array.get(i);
                String name = innerObj.get("post_id").toString();
                if (innerObj.get("src_xxxbig") != null){
                    downloadPhoto = new URL(innerObj.get("src_xxxbig").toString());
                    downloadPhoto(downloadPhoto, name);

                }
                else if (innerObj.get("src_xxbig") != null) {
                    downloadPhoto = new URL(innerObj.get("src_xxbig").toString());
                    downloadPhoto(downloadPhoto, name);
                }
                else if (innerObj.get("src_xbig") != null){
                    downloadPhoto = new URL(innerObj.get("src_xbig").toString());
                    downloadPhoto(downloadPhoto, name);
                }
                else if(innerObj.get("src_big") != null){
                    downloadPhoto = new URL(innerObj.get("src_big").toString());
                    downloadPhoto(downloadPhoto, name);
                }
                else if (innerObj.get("src") != null){
                    downloadPhoto = new URL(innerObj.get("src").toString());
                    downloadPhoto(downloadPhoto, name);
                }
                else if(innerObj.get("src_small") != null){
                    downloadPhoto = new URL(innerObj.get("src_small").toString());
                    downloadPhoto(downloadPhoto, name);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static File getPhotos() {
        File result = null;
        try {
            URL getPhoto = new URL("https://api.vk.com/method/photos.getAll?owner_id=1943145&access_token=" + TOKEN);
            HttpURLConnection con = (HttpURLConnection) getPhoto.openConnection();

            con.setRequestMethod("GET");
            con.addRequestProperty("User-Agent", USER_AGENT);

            System.out.println(getPhoto);

            int responseCode = con.getResponseCode();

            if (responseCode == 200) {
                System.out.println("\nSending 'GET' request to URL : " + getPhoto);
                result = new File("/home/mavdeev/vk/photos.json");
                if (!result.exists()) {
                    result.createNewFile();
                }

            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static void downloadPhoto(URL down, String pName){
        try {
            File photo = new File(DIRECTORY + "/vk/photos/" + pName);
            ReadableByteChannel rbc = Channels.newChannel(down.openStream());
            FileOutputStream fos = new FileOutputStream(photo);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        //                    move to log
        System.out.println("done");
    }
}
