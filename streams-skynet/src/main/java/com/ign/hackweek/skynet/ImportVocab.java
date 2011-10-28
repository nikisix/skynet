package skynet;

import java.net.URL;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JSONArray;
//import org.slf4j.LoggerFactory;
//import org.slf4j.Logger;
import java.io.*;
import java.util.*;
import java.lang.String;
import java.lang.System;
import java.net.URLConnection;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import skynet.Constants.constants;
import skynet.models.Game;
import skynet.models.Games;


/**
 * Created by IntelliJ IDEA.
 * User: limiller
 * Date: Oct 27, 2011
 * Time: 11:49:08 AM
 *
 */
public class ImportVocab {

  // Create game buckets, add relevant tags to each
  public void addGames()  {
    getGames();      // Add games from API to Games
    HashMap<String,Game> games = Games.getGames();
    Iterator it = games.keySet().iterator();

    // Add tags for each game bucket
    while (it.hasNext())  {
      Game game = (Game) games.get(it.next());
      String gameId = game.gobId;
      if(game.commonName!=null)  { Games.addTag(gameId,game.commonName);  }
      if(game.title!=null)  { Games.addTag(gameId,game.title);   }
      addTags(gameId);
    }
    System.out.println(Games.getTags());
  }

  public void addTags(String gameId)  {
    HashSet<String> characters = getCharacters(gameId);

    Iterator it = characters.iterator();
    while (it.hasNext())  {
      Games.addTag(gameId,(String)it.next());
    }
  }


  private void getGames()  {
    HashSet<Games> gameNames = new HashSet<Games>();
    int index = 1;

// ex: http://content-api.ign.com/v1/games?startIndex=1&dedupe=true&max=50
//  public status final String GAMES_BASE_URL = "http://content-api.ign.com/v1/games?"
//  public status final String characterBaseURL = "http://content-api.ign.com/v1/games/"
//  public status final String characterBaseEndURL = "/characters.xml"

    boolean hasMore = true;
    try  {
      while (hasMore)  {
        URL url = new URL(constants.GAMES_BASE_URL + "&startIndex=" + index + "&dedupe=true");
        index +=1;

        URLConnection conn = url.openConnection();
        conn.setRequestProperty("Accept-Charset", constants.CHARSET);
        InputStream response = conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(response, constants.CHARSET));
        String stringResponse = reader.readLine();

        System.out.println(stringResponse);

        JSONObject responseJSON = (JSONObject) JSONSerializer.toJSON(stringResponse);
        if (!responseJSON.has("games") ||  !responseJSON.getJSONObject("games").has("game"))  {
          hasMore = false;
        }
        JSONArray gamesJSON = responseJSON.getJSONObject("games").getJSONArray("game");
        // Unwrap the onion
        for (int i=0; i< gamesJSON.size(); i++)  {
          JSONObject game = gamesJSON.getJSONObject(i);
          String title = (String) game.get("@name");
          String commonName = (String) game.get("commonName");
          String gobId = (String) game.get("@id");
          Integer totalArticles = (Integer) game.get("totalArticles");
          DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss-mm:ss");  // 2011-10-25T15:26:00-07:00
          String published = (String) game.get("lastArticlePublishDate");
          DateTime dateTime = formatter.parseDateTime(published);
          Days d = Days.daysBetween(dateTime, new DateTime());
          int days = d.getDays() + 1;
          if (totalArticles/days > 0.5)  {  //> 50 && days<30) {
            Game newGame = new Game(gobId, commonName, title);
            Games.addGame(newGame);
          }
          else { System.out.println("not interesting enough.");  }
        }
      }
    } catch (Exception e) {
      System.out.println(e);
    }
    return;
  }

  private HashSet<String> getCharacters(String gobId) {
    HashSet<String> characters = new HashSet<String>();

    try {
      URL url = new URL(constants.CHARACTER_BASE_URL + gobId + constants.CHARACTER_BASE_END_URL);
      URLConnection conn = url.openConnection();
      conn.setRequestProperty("Accept-Charset", constants.CHARSET);
      InputStream response = conn.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(response, constants.CHARSET));
      String stringResponse = reader.readLine();

      System.out.println(stringResponse);

      JSONObject responseJSON = (JSONObject) JSONSerializer.toJSON(stringResponse);
      if (!responseJSON.has("characters") || responseJSON.get("characters").getClass()=="".getClass() ||
          !responseJSON.getJSONObject("characters").has("character"))  {
        return characters;
      }
      JSONArray charactersJSON = responseJSON.getJSONObject("characters").getJSONArray("character");
      Iterator it = charactersJSON.iterator();
      while (it.hasNext())  {
        JSONObject character = (JSONObject) it.next();
        characters.add((String) character.get("@name"));
      }
    } catch (Exception e) {
      System.out.println(e);
    }
    return characters;
  }

  public static void main(String args[])  {
    ImportVocab iv = new ImportVocab();
    iv.addGames();
  }

}

