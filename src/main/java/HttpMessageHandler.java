import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.apache.http.Header;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.message.BasicHeader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;

public class HttpMessageHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    URI mUri;

    WebSocketClientHandshaker mHandshaker;

    public HttpMessageHandler(URI uri, WebSocketClientHandshaker webSocketClientHandshaker) {
        super();
        mUri = uri;
        mHandshaker = webSocketClientHandshaker;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
        if (msg.status().code() == 401) {
            FullHttpRequest request = getHandshakeRequest();
            DigestScheme ds = new DigestScheme();
            String s = msg.headers().get("Www-Authenticate");
            Header h = new BasicHeader("Www-Authenticate", s);
            ds.processChallenge(h);
            Header authenticate = ds.authenticate(new UsernamePasswordCredentials("testUser", "testPassword"), new HttpGet(mUri));
            request.headers().add("Authorization", authenticate.getValue());
          //  request.headers().add("host", "httpbin.org");
            ctx.writeAndFlush(request);
            return;
        }
        msg.retain();
        ctx.fireChannelRead(msg);
    }

    private FullHttpRequest getRequest() {

        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "");
        HttpHeaders headers = request.headers();

        headers.add(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET)
                .add(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE);


        headers.add(HttpHeaderNames.SEC_WEBSOCKET_VERSION, "13");
        return request;
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
