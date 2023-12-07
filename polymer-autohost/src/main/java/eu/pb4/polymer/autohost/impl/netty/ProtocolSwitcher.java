package eu.pb4.polymer.autohost.impl.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ProtocolSwitcher extends ChannelInboundHandlerAdapter {
    public static final String ID = "polymer:autohost/protocol_switcher";
    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        var buf = (ByteBuf) msg;
        var i = buf.readerIndex();

        if (buf.readableBytes() > 2 && isHttp(buf.getByte(i), buf.getByte(i + 1))) {
            var val = new StringBuilder();
            var store = false;
            var space = 0;
            int y = 0;
            while (y < buf.readableBytes()) {
                var character = (char) buf.getByte(i + y);

                if (character == ' ') {
                    if (space == 0) {
                        store = true;
                    } else {
                        break;
                    }
                    space++;
                } else if (store) {
                    val.append(character);
                }
                y++;
            }

            if (val.toString().startsWith("/eu.pb4.polymer.autohost/")) {
                var p = ctx.channel().pipeline();
                while (p.last() != null) {
                    p.removeLast();
                }

                p.addLast(new HttpServerCodec());
                p.addLast(new HttpObjectAggregator(65536));
                p.addLast(new ChunkedWriteHandler());
                p.addLast(new CustomHttpServerHandler());
                ctx.pipeline().fireChannelRead(msg);
                return;
            }
        }
        ctx.channel().pipeline().remove(ID);
        buf.readerIndex(i);
        ctx.fireChannelRead(msg);
    }

    private static boolean isHttp(int magic1, int magic2) {
        return magic1 == 'G' && magic2 == 'E' || // GET
                        magic1 == 'P' && magic2 == 'O' || // POST
                        magic1 == 'P' && magic2 == 'U' || // PUT
                        magic1 == 'H' && magic2 == 'E' || // HEAD
                        magic1 == 'O' && magic2 == 'P' || // OPTIONS
                        magic1 == 'P' && magic2 == 'A' || // PATCH
                        magic1 == 'D' && magic2 == 'E' || // DELETE
                        magic1 == 'T' && magic2 == 'R' || // TRACE
                        magic1 == 'C' && magic2 == 'O';   // CONNECT
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
