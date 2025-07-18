package cc.raynet.worldsharing.api;

import cc.raynet.worldsharing.utils.CryptUtils;
import com.google.gson.reflect.TypeToken;
import net.labymod.api.util.io.web.exception.WebRequestException;
import net.labymod.api.util.io.web.request.Request;
import net.labymod.api.util.io.web.request.Request.Method;
import net.labymod.api.util.io.web.request.Response;
import org.jetbrains.annotations.Nullable;

import java.security.NoSuchAlgorithmException;

public class API {

    private final static String endpoint = "https://worldshar.ing";
    @Nullable public static CryptUtils.ECCKeyPair keyPair = null;

    static {
        try {
            keyPair = CryptUtils.generateECCKeyPair();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Worldsharing: failed to generate keypair: " + e.getCause());
        }
    }

    public static Control getControl() throws WebRequestException {
        Response<Control> req = Request.ofGson(new TypeToken<Control>() {})
                .method(Method.GET)
                .url(endpoint+"/connect/control")
                .handleErrorStream()
                .executeSync();
        if (req.hasException()) {
            throw req.exception();
        }

        if (req.getStatusCode() != 200) {
            throw new WebRequestException(new Exception("Unexpected response code:" + req.getStatusCode()));
        }

        return req.get();
    }

    public static class Control {
        public String host;
        public int port;
        public String key;
    }
}
