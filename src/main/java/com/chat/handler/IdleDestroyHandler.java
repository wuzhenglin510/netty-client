package com.chat.handler;

import com.chat.proto.TopLevelDataOuterClass;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

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
                    ctx.writeAndFlush(pingMessage);
                }
            }
        }
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TopLevelDataOuterClass.TopLevelData topLevelData) throws Exception {
        lossPongTimes = 0;
        if (topLevelData.getType().equals(TopLevelDataOuterClass.TopLevelData.Type.PONG)) {
            System.out.println("IDLE, 直接结束");
            return;
        } else {
            System.out.println("IDLE, 叫下一个流程继续处理");
            channelHandlerContext.fireChannelRead(topLevelData);
        }
    }
}
