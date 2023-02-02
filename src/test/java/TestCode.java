import git.hashibutogarasu.mojangapiutil.MojangAPIUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestCode {

    @Test
    void getskin() {
        try {
            new MojangAPIUtil("Kara_su").getSkinfromName("./skin_cache", "skin", "png");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
