package com.chat.handler;

import com.chat.proto.TopLevelDataOuterClass;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class DispatcherHandler extends SimpleChannelInboundHandler<TopLevelDataOuterClass.TopLevelData> {

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TopLevelDataOuterClass.TopLevelData topLevelData) throws Exception {
        System.out.println("收到业务数据: " + topLevelData.toString());
    }

}
