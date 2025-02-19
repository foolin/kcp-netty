package io.jpower.kcp.example.echo;

import io.jpower.kcp.netty.ChannelOptionHelper;
import io.jpower.kcp.netty.UkcpChannel;
import io.jpower.kcp.netty.UkcpChannelOption;
import io.jpower.kcp.netty.UkcpClientChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.CharsetUtil;

import java.time.LocalDateTime;

/**
 * Sends one message when a connection is open and echoes back any received
 * data to the server.
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public final class EchoClient {

    static final int CONV = Integer.parseInt(System.getProperty("conv", "10"));
    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));
    static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));

    public static void main(String[] args) throws Exception {
        // Configure the client.
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(UkcpClientChannel.class)
                    .handler(new ChannelInitializer<UkcpChannel>() {
                        @Override
                        public void initChannel(UkcpChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new EchoClientHandler());
                        }
                    });
            ChannelOptionHelper.nodelay(b, true, 20, 2, true)
                    .option(UkcpChannelOption.UKCP_MTU, 512);

            // Start the client.
            ChannelFuture f = b.connect(HOST, PORT).sync();
            run((UkcpClientChannel) f.channel());

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            // Shut down the event loop to terminate all threads.
            group.shutdownGracefully();
        }
    }

    public static void run(UkcpClientChannel ukcpClientChannel){
        new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                if(!ukcpClientChannel.isActive()){
                    System.err.println("Server has closed! " + LocalDateTime.now());
                    break;
                }
//                if(i > 6){
//                    ukcpClientChannel.close();
//                }
                ukcpClientChannel.writeAndFlush(Unpooled.copiedBuffer(("NO." + i + "\tHello " + LocalDateTime.now() + "\n").getBytes(CharsetUtil.UTF_8)));
                System.out.println("send " + i + " done:" + LocalDateTime.now());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }, "SendThread").start();
    }

}
