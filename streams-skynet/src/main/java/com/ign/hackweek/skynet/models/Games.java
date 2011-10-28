package skynet.Constants;

/**
 * Created by IntelliJ IDEA.
 * User: limiller
 * Date: Oct 13, 2011
 * Time: 11:01:29 AM
 */
public final class constants {

  // Tags
  //----------------------------------------------------------
  public static final String tagsFilePath = "tags.txt";

  // Vocabulizer
  //----------------------------------------------------------
  // ex: http://content-api.ign.com/v1/games?startIndex=1&dedupe=true&max=50
  public static final String GAMES_BASE_URL = "http://content-api.ign.com/v1/games?";
  public static final String CHARACTER_BASE_URL = "http://content-api.ign.com/v1/games/";
  public static final String CHARACTER_BASE_END_URL = "/characters.json";
  public static final String CHARSET = "utf-8";
  // 7791

  private constants()   {
    //this prevents even the native class from
    //calling this ctor as well :
    throw new AssertionError();
  }

}
