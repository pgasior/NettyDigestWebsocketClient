import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.message.BasicHeader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

public class WebsocketClientWithAuthHandler  extends WebSocketClientHandler {
    private WebSocketClientHandshaker mHandshaker;

    public WebsocketClientWithAuthHandler(WebSocketClientHandshaker handshaker) {
        super(handshaker);
        mHandshaker = handshaker;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
//        if (msg instanceof FullHttpResponse) {
//            if (((FullHttpResponse) msg).status().code() == 401) {
//                FullHttpRequest handshakeRequest = getHandshakeRequest();
//                if (handshakeRequest == null) {
//                    ctx.close();
//                    return;
//                }
//                String auth = "user" + ":" + "password";
//                String s = Base64.encodeBase64String(auth.getBytes(Charset.forName("UTF-8")));
//                handshakeRequest.headers().add("Authorization","Basic " +  s);
//                ctx.writeAndFlush(handshakeRequest);
//                return;

//                FullHttpRequest request = getHandshakeRequest();
//                DigestScheme ds = new DigestScheme();
//                String s =((FullHttpResponse) msg).headers().get("Www-Authenticate");
//                Header h = new BasicHeader("Www-Authenticate", s);
//                ds.processChallenge(h);
//                Header authenticate = ds.authenticate(new UsernamePasswordCredentials("user", "passwd"), new HttpGet());
//                request.headers().add("Authorization", authenticate.getValue());
//                //  request.headers().add("host", "httpbin.org");
//                ctx.writeAndFlush(request);
//                return;
//            }
//        }
        super.channelRead0(ctx, msg);
    }

    private FullHttpRequest getHandshakeRequest() {
        try {
            Method[] methods = mHandshaker.getClass().getMethods();
            Method[] declaredMethods = mHandshaker.getClass().getDeclaredMethods();
            Method newHandshakeRequest = mHandshaker.getClass().getDeclaredMethod("newHandshakeRequest");
            newHandshakeRequest.setAccessible(true);
            FullHttpRequest request = (FullHttpRequest)newHandshakeRequest.invoke(mHandshaker);
            newHandshakeRequest.setAccessible(false);
            return request;

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
