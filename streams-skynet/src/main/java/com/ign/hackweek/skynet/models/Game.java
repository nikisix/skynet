package skynet.models;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: limiller
 * Date: Oct 27, 2011
 * Time: 12:32:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class Game {
  public String gobId;
  public String commonName;
  public String title;
  public HashSet<String> tags = new HashSet<String>();

  public Game(String newId, String newName, String newTitle)  {
    gobId = newId;
    commonName = newName;
    title = newTitle;
  }

  public void addTag(String tag) {
    tags.add(tag);
  }

}
