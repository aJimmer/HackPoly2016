package com.inasweaterpoorlyknit.hackpoly2016;

/**
 * Created by Connor on 2/6/2016.
 */

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
//import java.io.InputStream;
import java.util.List;
//import java.util.Properties;

public class Search {
    // variable holding the filename of the file that contains the developer's API key
    private static final String DEVELOPER_KEY_FILENAME = "DeveloperKey.java";

    // number of videos we want the search function to return
    private static final long NUMBER_OF_VIDEOS_TO_RETURN = 10;

    // global instance of a YouTube object
    private static YouTube youtube;

    public static String Search(String query){
        /*Properties properties = new Properties();
        try{
            InputStream in = Search.class.getResourceAsStream("/" + DEVELOPER_KEY_FILENAME);
            properties.load(in);
        } catch (IOException e){
            System.err.println("An error occured in reading " + DEVELOPER_KEY_FILENAME + ": "
                    + e.getCause() + " : " + e.getMessage());
            System.exit(1);
        }
        */

        try {
            youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName("youtube-cmdline-search").build();


            // Define the API request for the search results
            YouTube.Search.List search = youtube.search().list("id,snippet");

            search.setKey(DeveloperKey.BROWSER_DEVELOPER_KEY);
            search.setQ(query);
            search.setType("video");
            search.setFields("items(id)");
            search.setMaxResults(NUMBER_OF_VIDEOS_TO_RETURN);

            // call api
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();
            if(searchResultList != null){
                return searchResultList.get(0).getId().getVideoId();
            }

        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return null;
    }
}
