package git.hashibutogarasu.mojangapiutil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import git.hashibutogarasu.mojangapiutil.jsonmodel.Profile;
import git.hashibutogarasu.mojangapiutil.jsonmodel.SessionserverProfile;
import git.hashibutogarasu.mojangapiutil.jsonmodel.Texture;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class MojangAPIUtil {
    public final String player_name;
    private String id;
    public Path skin_image_path;

    public Path face_image_path;

    public MojangAPIUtil(String player_name){
        this.player_name = player_name;
    }

    public void getSkinfromName(String file_path, String output_name, String ext) throws IOException, InterruptedException {
        String id = getid();
        String value = getproperties(id);
        String json = base64Decode(value);
        String url = getUrlfromJson(json);
        saveImagefromUrl(url, Path.of(file_path), output_name, ext);
    }

    public String getid() throws IOException, InterruptedException{
        HttpRequest request = HttpRequest
                .newBuilder(URI.create("https://api.mojang.com/users/profiles/minecraft/" + player_name))
                .build();
        HttpResponse.BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        HttpResponse<String> response = HttpClient.newBuilder().build().send(request, bodyHandler);
        ObjectMapper mapper = new ObjectMapper();
        Profile profile = mapper.readValue(response.body(), Profile.class);
        this.id = profile.id;

        return profile.id;
    }

    public String getproperties(String id) throws IOException, InterruptedException{
        HttpRequest request = HttpRequest
                .newBuilder(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + id))
                .build();
        HttpResponse.BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        HttpResponse<String> response = HttpClient.newBuilder().build().send(request, bodyHandler);
        ObjectMapper mapper = new ObjectMapper();
        SessionserverProfile profile = mapper.readValue(response.body(), SessionserverProfile.class);

        return profile.properties[0].get("value");
    }

    public String base64Decode(String base64){
        byte[] decoded = Base64.getDecoder().decode(base64);
        return new String(decoded,StandardCharsets.UTF_8);
    }

    public String getUrlfromJson(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Texture texture = mapper.readValue(json, Texture.class);
        return texture.textures.get("SKIN").get("url");
    }

    public void saveImagefromUrl(String url, Path path, String player_name, String ext) throws IOException {
        Path player_path = Path.of(path + "\\");
        if(!new File(path.toString()).exists()) Files.createDirectory(path);
//        if(!new File(player_path.toString()).exists()) Files.createDirectory(player_path);
//        String image_path = player_path + "\\" + player_name + "." + ext;
        String image_path = player_path + "\\" + player_name + "." + ext;
        String face_path = player_path + "\\" + player_name + "_face." + ext;
        String face_path_resized = player_path + "\\" + player_name + "_face_resized." + ext;

        this.skin_image_path = Path.of(image_path);

        HttpURLConnection urlConnection = (HttpURLConnection)new URL(url).openConnection();
        urlConnection.setInstanceFollowRedirects(true);

        InputStream in = urlConnection.getInputStream();

        byte[] buf = new byte[4096];
        int readSize;
        int total = 0;

        FileOutputStream fos = new FileOutputStream(image_path);
        while(((readSize = in.read(buf)) != -1))
        {
            total = total + readSize;
            fos.write(buf,0,readSize);
        }
        fos.flush();
        fos.close();
        in.close();

//        getFace(image_path, face_path);
//        scaleImage(new File(face_path),256,256, new File(face_path_resized));
    }

    public void getFace(String filepath, String outputpath) throws IOException {
        this.face_image_path = Path.of(outputpath);
        File f = new File(filepath);
        BufferedImage input = ImageIO.read(f);
        BufferedImage face1 = input.getSubimage(8, 8, 8, 8);
        BufferedImage face2 = input.getSubimage(40, 8, 8, 8);
        Graphics graphics1 = face1.getGraphics();
        int x = 0;
        int y = 0;
        graphics1.drawImage(face2, x, y, null);
        graphics1.dispose();

        ImageIO.write(face1, "png", new File(outputpath));
    }

    public static BufferedImage scaleImage(File in, int destWidth, int destHeight, File output) throws IOException {
        BufferedImage src = ImageIO.read(in);

        int width = src.getWidth();    // オリジナル画像の幅
        int height = src.getHeight();  // オリジナル画像の高さ

        // 縦横の比率から、scaleを決める
        double widthScale = (double) destWidth / (double) width;
        double heightScale = (double) destHeight / (double) height;
        double scale = widthScale < heightScale ? widthScale : heightScale;

        ImageFilter filter = new AreaAveragingScaleFilter(
                (int) (src.getWidth() * scale), (int) (src.getHeight() * scale));
        ImageProducer p = new FilteredImageSource(src.getSource(), filter);
        Image dstImage = Toolkit.getDefaultToolkit().createImage(p);
        BufferedImage dst = new BufferedImage(
                dstImage.getWidth(null), dstImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        g.drawImage(dstImage, 0, 0, null);
        g.dispose();
        ImageIO.write(dst,"png", output);
        return dst;
    }
}
