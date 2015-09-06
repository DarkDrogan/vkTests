package tk.serafimko.apps;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.*;
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
    private static final String APP_ID = "";
    /**
     * Permission for iteration.
     */
    private static final String PERMISSIONS = "audio";
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
    private static final String TOKEN = "";

    public static void main(String[] args) throws Exception {
        URL website = new URL("https://oauth.vk.com/authorize?"
                + "client_id=" + APP_ID
                + "&scope=" + PERMISSIONS
                + "&redirect_uri=" + REDIR_URI
                + "&display=" + DISPLAY
                + "&v=" + API_VERSION
                + "&response_type=token");
//        System.out.println(website.toURI().toString());

        parseAudios(getAudioJson(),100);
//        removeAudio(getAudioJson());
    }
    /**
     * Запрос к VKapi audio.get(owner_id,TOKEN).
     * @return audio.json
     */
    private static File getAudioJson(){
        File audios = null;
        try {
            //
            URL audioJSONURL = new URL("https://api.vk.com/method/audio.get?owner_id=1943145&access_token=" + TOKEN);
            HttpURLConnection AudioJson = (HttpURLConnection) audioJSONURL.openConnection();
            AudioJson.setRequestMethod("GET");
            AudioJson.addRequestProperty("User-Agent", USER_AGENT);

            int responseCode = AudioJson.getResponseCode();

            if (responseCode == 200) {
                System.out.println("\nSending 'GET' request to URL : " + AudioJson);
                audios = new File(DIRECTORY + "/vk/audios.json");
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
    /**
     * Парсер исполнителя и названия трека.
     * @param pAudios файл со списком аудиозаписей.
     */
    private static void parseAudios(File pAudios, int pCount){
        File audios = pAudios;


        try {
            FileReader reader = new FileReader(audios);
            JSONParser parser = new JSONParser();
            JSONObject object = (JSONObject) parser.parse(reader);
//            Все записи содержатся в разделе response. Из него и получаем массив треков.
            JSONArray array = (JSONArray) object.get("response");
//            С первого, а не нулевого, потому что первый элемент - не трек
            for (int i = 1; i < pCount; i++) {
                JSONObject record = (JSONObject) array.get(i);
                String artist = record.get("artist").toString();
                String title = record.get("title").toString();

//                Вызов метода для проверки валидности имени
                artist = correctString(artist);
                title = correctString(title);

//
//                System.out.println(artist + " " + title);
//                System.out.println(record.get("url"));

                URL audioFileURL = new URL(record.get("url").toString());
                System.out.println(DIRECTORY + "/vk/music/" + artist + "_" + title + ".mp3");
                File audioFile = new File(DIRECTORY + "/vk/music/" + artist + "_" + title + ".mp3");
                if (!audioFile.exists()) audioFile.createNewFile();
                else {
                    System.out.println("File exists!");
                    System.out.println(i + " of " + array.size());
                    continue;
                }
                downloadAudio(audioFileURL, audioFile);
                System.out.println(i + " of " + array.size());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
    /**
     * Метод для скачивания трека.
     * @param pUrl Ссылка на трек.
     * @param pFile Файл, в который качаем.
     */
    private static void downloadAudio(URL pUrl, File pFile){
        try {
            ReadableByteChannel rbc = Channels.newChannel(pUrl.openStream());
            FileOutputStream fos = new FileOutputStream(pFile);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            System.out.println("Record has been downloaded");
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Разнообразные тесты исполнителя и названия трека.
     * @param input Исполнитель или название трека
     * @return проверенную строку.
     */
    private static String correctString(String input){
        String output = input;

        if (output.contains("&gt;")) {
            output = output.replaceAll("&gt;", "_");
            if (output.contains("&lt;")) {
                output = output.replaceAll("&lt;", "_");
            }
        }

        if (output.length() > 25){
            output = output.substring(0, 25);
        }

        if (output.contains("&amp")){
            output = output.replaceAll("&amp", "and");
        }

        if (output.contains("/")){
            output = output.replaceAll("/", "-");
        }

        return output;
    }

    private static void removeAudio(File pAudios) throws URISyntaxException{
        File audios = pAudios;


        try {
            FileReader reader = new FileReader(audios);
            JSONParser parser = new JSONParser();
            JSONObject object = (JSONObject) parser.parse(reader);
            JSONArray array = (JSONArray) object.get("response");


            for (int i = 1; i < 10; i++) {
                JSONObject record = (JSONObject) array.get(i);
                Integer audio_id = Integer.valueOf((record.get("aid")).toString());
                System.out.println(audio_id);

                URL audioURL = new URL("https://api.vk.com/method/audio.delete?audio_id=" + audio_id +
                        "&owner_id=1943145&access_token=" + TOKEN);
                System.out.println(audioURL.toURI().toString());
                HttpURLConnection AudioJson = (HttpURLConnection) audioURL.openConnection();
                AudioJson.setRequestMethod("GET");
                AudioJson.addRequestProperty("User-Agent", USER_AGENT);
                int responseCode = AudioJson.getResponseCode();
                System.out.println(responseCode);

                ReadableByteChannel rbc = Channels.newChannel(audioURL.openStream());
                File out = new File("/home/serafim/vk/remove" + i + ".txt");
                if (!out.exists()) out.createNewFile();
                FileOutputStream fos = new FileOutputStream(out);
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
