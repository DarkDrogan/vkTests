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
import java.util.List;

class App {

    private static List<Integer> aid;
    private static File fileAlbums;
    private static String workDirectory;

    public static void main(final String[] args) throws Exception {

        fileAlbums = getAlbums();
        aid = new ArrayList<Integer>();
        parseAlbums(fileAlbums);
        for (int i = 1; i < aid.size(); i++) {
            Integer e = aid.get(i);
            String filename = getJsonPhoto(e);
            parseJsonPhotoAlbums(filename, e);
        }
    }

    private static File getAlbums(){
        fileAlbums = null;
        try {
            URL targetURL = new URL("https://api.vk.com/method/photos.getAlbums?owner_id=6536405");
            HttpURLConnection httpURLConnection = (HttpURLConnection) targetURL.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.addRequestProperty("User-Agent", USER_AGENT);
            int responseCode = httpURLConnection.getResponseCode();

            if (responseCode == 200) {
                System.out.println("\nSending 'GET' request to URL : " + httpURLConnection);
                fileAlbums = new File(DIRECTORY + "/Downloads/vk/photos/albums.json");
                if (!fileAlbums.exists()) {
                    fileAlbums.createNewFile();
                }
            }
            readFile(targetURL, fileAlbums);
            System.out.println("JSON has been downloaded");
        }
        catch (ProtocolException e){
            System.out.println("Protocol exception");
        }
        catch (MalformedURLException e) {
            System.out.println("Error in URL");
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return fileAlbums;
    }

    private static String getJsonPhoto(Integer pID) {

        fileAlbums = null;
        String filename = "";

        try {
            URL targetURL = new URL("https://api.vk.com/method/photos.get?owner_id=6536405&album_id=" + pID);
            System.out.println(targetURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) targetURL.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.addRequestProperty("User-Agent", USER_AGENT);

            int responseCode = httpURLConnection.getResponseCode();

            if (responseCode == 200) {
                System.out.println("\nSending 'GET' request to URL : " + targetURL);
                fileAlbums = new File(DIRECTORY + "/Downloads/vk/photos/" + pID + ".json");
                if (!fileAlbums.exists()) {
                    fileAlbums.createNewFile();
                }
            }

            filename = fileAlbums.getName();
            readFile(targetURL, fileAlbums);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("File " + filename + " has been downloaded");
        return filename;
    }

    private static JSONArray getResponse(File fileName) throws IOException, ParseException{
        FileReader reader = new FileReader(fileName);
        JSONParser parser = new JSONParser();
        JSONObject object = (JSONObject) parser.parse(reader);
        reader.close();
        return (JSONArray) object.get("response");
    }

    private static void readFile(final URL targetURL, final File file) throws IOException{
        ReadableByteChannel rbc = Channels.newChannel(targetURL.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }

    private static void parseAlbums(File fileAlbums){
        try {
            JSONArray response = getResponse(fileAlbums);

            for (int i = 0; i < response.size(); i++) {
                JSONObject innerObject = (JSONObject) response.get(i);
                aid.add(Integer.valueOf(innerObject.get("aid").toString()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static void parseJsonPhotoAlbums(String pFilename, Integer pDir) {
        workDirectory = DIRECTORY + "/Downloads/vk/photos/" + pDir;
        File file = new File(workDirectory);

        checkAndCreateDirectory(file);

        try {
            JSONArray array = getResponse(new File(DIRECTORY + "/Downloads/vk/photos/" + pFilename));
            parsePhoto(array);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            System.out.println("Album id empty");
        }
    }

    private static void parsePhoto(JSONArray array) throws MalformedURLException{
        for (int j = 0; j < array.size(); j++) {
            JSONObject innerObj = (JSONObject) array.get(j);
            String name = workDirectory + "/" +  innerObj.get("pid").toString();
            URL urlPhoto = takeURLPhoto(innerObj);
            downloadPhoto(urlPhoto, name);
            System.out.println("Downloaded file number " + j);
        }
    }

    private static URL takeURLPhoto(JSONObject innerObj) throws MalformedURLException {
        URL urlPhoto = null;
        if (innerObj.get("src_xxxbig") != null) {
            urlPhoto = new URL(innerObj.get("src_xxxbig").toString());
        } else if (innerObj.get("src_xxbig") != null) {
            urlPhoto = new URL(innerObj.get("src_xxbig").toString());
        } else if (innerObj.get("src_xbig") != null) {
            urlPhoto = new URL(innerObj.get("src_xbig").toString());
        } else if (innerObj.get("src_big") != null) {
            urlPhoto = new URL(innerObj.get("src_big").toString());
        } else if (innerObj.get("src") != null) {
            urlPhoto = new URL(innerObj.get("src").toString());
        } else if (innerObj.get("src_small") != null) {
            urlPhoto = new URL(innerObj.get("src_small").toString());
        }
        return urlPhoto;
    }

    private static void checkAndCreateDirectory(File file){
        if (!file.exists()) {
            if (file.mkdir()) {
                System.out.println("Directory is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }
    }

    private static String downloadPhoto(final URL targetURL, String pName){
        File photo = new File(pName + ".jpg");
        try {
            readFile(targetURL, photo);
            Thread.sleep(500);
        }catch (InterruptedException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return photo.getName();
    }
    /**
     * Root directory for files.
     */
    private static final String DIRECTORY = System.getProperty("user.home");
    /**
     * User-Agent for request.
     */
    private static final String USER_AGENT = "Mozilla/5.0";
}