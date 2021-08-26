package io.jpower.kcp.example.echo;

import io.jpower.kcp.netty.UkcpChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Handler implementation for the echo server.
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        System.out.println("channelRegistered");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        System.out.println("channelUnregistered");
        ctx.channel().close();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelActive");
        UkcpChannel kcpCh = (UkcpChannel) ctx.channel();
        kcpCh.conv(EchoServer.CONV);

    }

    private static  int n = 0;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("channelRead");
        ctx.write(msg);
        n++;
        if(n % 10 == 2){
            throw new RuntimeException("Hello Error");
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        System.out.println("channelReadComplete");
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("exceptionCaught");
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.fireExceptionCaught(cause);
        ctx.close();
    }

}
