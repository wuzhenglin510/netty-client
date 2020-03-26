package com.chat;

import com.chat.handler.DispatcherHandler;
import com.chat.handler.IdleDestroyHandler;
import com.chat.proto.TopLevelDataOuterClass;
import com.chat.proto.business.WordMessageOuterClass;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Client {

    protected static NioSocketChannel channelInstance;

    public static void main(String args[]) throws InterruptedException {
        final NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(nioEventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            System.out.println("链路建立");
                            ch.pipeline()
                                    .addLast("idleEventTrigger", new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS))
                                    .addLast("idleDestroy", new IdleDestroyHandler(5))
                                    .addLast("frameLengthReader", new ProtobufVarint32FrameDecoder())
                                    .addLast("decoder", new ProtobufDecoder(TopLevelDataOuterClass.TopLevelData.getDefaultInstance()))
                                    .addLast("frameLengthWriter", new ProtobufVarint32LengthFieldPrepender())
                                    .addLast("encoder", new ProtobufEncoder())
                                    .addLast("dispatcher", new DispatcherHandler());
                            channelInstance = ch;
                        }
                    });
            bootstrap.connect(new InetSocketAddress("localhost", 9001)).sync();
            Scanner scanner = new Scanner(System.in);
            while (true) {
                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine();
                    System.out.println("读取到输入:" + input + ", 当前通道状态: " + channelInstance.isActive());
                    TopLevelDataOuterClass.TopLevelData message = TopLevelDataOuterClass.TopLevelData.newBuilder()
                            .setType(TopLevelDataOuterClass.TopLevelData.Type.WORD)
                            .setSendTime(System.nanoTime())
                            .setWordMessage(WordMessageOuterClass.WordMessage.newBuilder().setText(input).build())
                            .build();
                    channelInstance.writeAndFlush(message);
                }
            }
        } finally {
            nioEventLoopGroup.shutdownGracefully();
        }
    }


}
