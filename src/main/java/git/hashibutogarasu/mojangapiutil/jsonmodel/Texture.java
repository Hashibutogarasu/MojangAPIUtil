package git.hashibutogarasu.mojangapiutil.jsonmodel;

import java.util.Map;

@SuppressWarnings("serial")
public class Texture {
    public String timestamp;
    public String profileId;
    public String profileName;
    public Map<String,Map<String,String>> textures;
}
