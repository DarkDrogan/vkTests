package tk.serafimko.apps;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

class App {

    public static void main(final String[] args) throws Exception {
//    URL website = new URL("https://oauth.vk.com/authorize?"
//                + "client_id=" + APP_ID
//                + "&scope=" + PERMISSIONS
//                + "&redirect_uri=" + REDIR_URI
//                + "&display=" + DISPLAY
//                + "&v=" + API_VERSION
//                + "&response_type=token");
//        System.out.println(website.toURI().toString());
//        System.exit(0);
        getAlbums();
        URL website = new URL("https://api.vk.com/method/audio.get?count=1203&access_token=" + TOKEN);

        ArrayList<Integer> aids = parseAlbums(getAlbums());

        for (int i = 1; i < aids.size(); i++) {
            Integer e = aids.get(i);
            String filename = getJsonPhoto(e);
            parseJsonPhoto(filename, e);

        }
//
//        for (Integer e : aids) {
//            getPhotos(e);
//            String filename = DIRECTORY + "/vk/photos/" + e.toString() + ".json";
//            String dir = e.toString();
//            parsePhotos(filename, dir);
//        }
    }


    private static String getJsonPhoto(Integer pID) {

        File result = null;
        String filename = "";

        try {
            URL urlPhoto = new URL("https://api.vk.com/method/photos.get?owner_id=1943145&album_id=" + pID);
            System.out.println(urlPhoto);
            HttpURLConnection con = (HttpURLConnection) urlPhoto.openConnection();

            con.setRequestMethod("GET");
            con.addRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();

            if (responseCode == 200) {
                System.out.println("\nSending 'GET' request to URL : " + urlPhoto);
                result = new File(DIRECTORY + "/vk/photos/" + pID + ".json");
                if (!result.exists()) {
                    result.createNewFile();
                }
            }

            filename = result.getName();
            ReadableByteChannel rbc = Channels.newChannel(urlPhoto.openStream());
            FileOutputStream fos = new FileOutputStream(result);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            Thread.sleep(5000);

        }catch (InterruptedException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("File " + filename + " has been downloaded");
        return filename;
    }

    private static void parseJsonPhoto(String pFilename, Integer pDir) {
        String workdir = DIRECTORY + "/vk/photos/" + pDir;
        File file = new File(workdir);

        if (!file.exists()) {
            if (file.mkdir()) {
                System.out.println("Directory is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }

        try {
            FileReader reader = new FileReader(new File(DIRECTORY + "/vk/photos/" + pFilename));
            JSONParser parser = new JSONParser();
            JSONObject object = (JSONObject) parser.parse(reader);
            JSONArray array = (JSONArray) object.get("response");
            for (int j = 0; j < array.size(); j++) {
                URL downloadPhoto;
                JSONObject innerObj = (JSONObject) array.get(j);

                String name = workdir + "/" + innerObj.get("pid").toString();
                if (innerObj.get("src_xxxbig") != null) {
                    downloadPhoto = new URL(innerObj.get("src_xxxbig").toString());
                    downloadPhoto(downloadPhoto, name);
                } else if (innerObj.get("src_xxbig") != null) {
                    downloadPhoto = new URL(innerObj.get("src_xxbig").toString());
                    downloadPhoto(downloadPhoto, name);
                } else if (innerObj.get("src_xbig") != null) {
                    downloadPhoto = new URL(innerObj.get("src_xbig").toString());
                    downloadPhoto(downloadPhoto, name);
                } else if (innerObj.get("src_big") != null) {
                    downloadPhoto = new URL(innerObj.get("src_big").toString());
                    downloadPhoto(downloadPhoto, name);
                } else if (innerObj.get("src") != null) {
                    downloadPhoto = new URL(innerObj.get("src").toString());
                    downloadPhoto(downloadPhoto, name);
                } else if (innerObj.get("src_small") != null) {
                    downloadPhoto = new URL(innerObj.get("src_small").toString());
                    downloadPhoto(downloadPhoto, name);
                }
                System.out.println("Downloaded file number " + j);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            System.out.println("Album id empty");
        }


    }

    private static String downloadPhoto(final URL pDown, String pName){
        File photo = new File(pName + ".jpg");
        try {

            URL down = pDown;
            ReadableByteChannel rbc = Channels.newChannel(down.openStream());
            FileOutputStream fos = new FileOutputStream(photo);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            Thread.sleep(500);
        }catch (InterruptedException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return photo.getName();
    }

    private static File getAlbums(){
        File albums = null;
        try {
            URL albumsURL = new URL("https://api.vk.com/method/photos.getAlbums?owner_id=1943145");
            HttpURLConnection getAlbums = (HttpURLConnection) albumsURL.openConnection();
            getAlbums.setRequestMethod("GET");
            getAlbums.addRequestProperty("User-Agent", USER_AGENT);

            int responseCode = getAlbums.getResponseCode();

            if (responseCode == 200) {
                System.out.println("\nSending 'GET' request to URL : " + getAlbums);
                albums = new File(DIRECTORY + "/vk/photos/albums.json");
                if (!albums.exists()) {
                    albums.createNewFile();
                }
            }
            ReadableByteChannel rbc = Channels.newChannel(albumsURL.openStream());
            FileOutputStream fos = new FileOutputStream(albums);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            System.out.println("JSON has been downloaded");

            parseAlbums(albums);
        }catch (ProtocolException e){
            System.out.println("Protocol exception");
        } catch (MalformedURLException e) {
            System.out.println("Error in URL");
        }catch (IOException e){
            e.printStackTrace();
        }

        return albums;

    }

    private static ArrayList<Integer> parseAlbums(File pFile){
        ArrayList<Integer> aids = new ArrayList<Integer>();
        try {
            FileReader reader = new FileReader(pFile);
            JSONParser parser = new JSONParser();
            JSONObject object = (JSONObject) parser.parse(reader);
            JSONArray response = (JSONArray) object.get("response");

            for (int i = 0; i < response.size(); i++) {
                JSONObject innerObject = (JSONObject) response.get(i);
                aids.add(Integer.valueOf(innerObject.get("aid").toString()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return aids;
    }

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
            "e52e64e53a0a88c081042d2599b53edf986680616bf39ae835606013a1a3c2589281fe656aa4680bc83be";
}
