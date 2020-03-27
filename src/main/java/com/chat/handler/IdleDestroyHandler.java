package com.chat.handler;

import com.chat.proto.TopLevelDataOuterClass;
import com.chat.proto.business.WordMessageOuterClass;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.VoidChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class IdleDestroyHandler extends SimpleChannelInboundHandler<TopLevelDataOuterClass.TopLevelData> {

    private int lossPongTimes = 0;
    private int maxLossPongTimes = 30;

    public IdleDestroyHandler(int maxLossPongTimes) {
        this.maxLossPongTimes = maxLossPongTimes;
    }

    public IdleDestroyHandler() {}

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                lossPongTimes++;
                if (lossPongTimes > maxLossPongTimes) {
                    ctx.channel().close();
                } else {
                    TopLevelDataOuterClass.TopLevelData pingMessage = TopLevelDataOuterClass.TopLevelData.newBuilder()
                            .setType(TopLevelDataOuterClass.TopLevelData.Type.PING)
                            .setSendTime(System.nanoTime())
                            .build();
                    ctx.channel().writeAndFlush(pingMessage, new DefaultChannelPromise(ctx.channel()).addListener(new GenericFutureListener<Future<? super Void>>() {
                        public void operationComplete(Future<? super Void> future) throws Exception {
                            System.out.println("send ping");
                        }
                    }));
                }
            }
        }
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TopLevelDataOuterClass.TopLevelData topLevelData) throws Exception {
        lossPongTimes = 0;
        if (topLevelData.getType().equals(TopLevelDataOuterClass.TopLevelData.Type.PONG)) {
            System.out.println("receive pong");
        } else {
            channelHandlerContext.fireChannelRead(topLevelData);
        }
    }
}
