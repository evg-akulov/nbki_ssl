package main;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import static main.Nbki.log;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author evgeniy
 */
public class util1 {

    public static URLConnection makeRequest(String method, String apiAddress, String accessToken, String mimeType, String requestBody) throws IOException {
        URL url = new URL(apiAddress);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(!method.equals("GET"));
        urlConnection.setRequestMethod(method);

        urlConnection.setRequestProperty("Authorization", "Bearer " + accessToken);

        urlConnection.setRequestProperty("Content-Type", mimeType);
        OutputStream outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "utf-8"));
        writer.write(requestBody);
        writer.flush();
        writer.close();
        outputStream.close();

        urlConnection.connect();

        return urlConnection;
    }

    /**
     * create file flag
     *
     * @param type
     */
    public static void createFlagFile(String workDir, Nbki.typeFlag type) {
        try {
            File file1 = new File(workDir, type + ".flag");
            file1.createNewFile();
        } catch (IOException e) {
            log.log(Level.WARNING, e.toString());
        }
    }

}
