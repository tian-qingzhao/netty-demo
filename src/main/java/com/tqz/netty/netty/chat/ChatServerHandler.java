package com.tqz.netty.netty.chat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.text.SimpleDateFormat;

public class ChatServerHandler extends SimpleChannelInboundHandler<String> {
    
    /**
     * GlobalEventExecutor.INSTANCE 是全局的事件执行器，是一个单例
     */
    private static final ChannelGroup CHANNEL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    // 表示 channel 处于就绪状态, 提示上线
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        //将该客户加入聊天的信息推送给其它在线的客户端
        //该方法会将 channelGroup 中所有的 channel 遍历，并发送消息
        CHANNEL_GROUP.writeAndFlush(
                "[ 客户端 ]" + channel.remoteAddress() + " 上线了 " + SDF.format(new java.util.Date()) + "\n");
        //将当前 channel 加入到 channelGroup
        CHANNEL_GROUP.add(channel);
        System.out.println(ctx.channel().remoteAddress() + " 上线了" + "\n");
    }
    
    // 表示 channel 处于不活动状态, 提示离线了
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        // 将客户离开信息推送给当前在线的客户
        CHANNEL_GROUP.writeAndFlush("[ 客户端 ]" + channel.remoteAddress() + " 下线了" + "\n");
        System.out.println(ctx.channel().remoteAddress() + " 下线了" + "\n");
        System.out.println("channelGroup size=" + CHANNEL_GROUP.size());
    }
    
    // 读取数据
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        // 获取到当前 channel
        Channel channel = ctx.channel();
        // 这时我们遍历 channelGroup, 根据不同的情况， 回送不同的消息
        CHANNEL_GROUP.forEach(ch -> {
            // 不是当前的 channel,转发消息
            if (channel != ch) {
                //                ch.writeAndFlush("[ 客户端 ]" + channel.remoteAddress() + " 发送了消息：" + msg + "\n");
                // 使用特殊分隔符解决粘包拆包的问题
                ch.writeAndFlush("[ 客户端 ]" + channel.remoteAddress() + " 发送了消息：" + msg + "\n" + "_");
            }
            //回显自己发送的消息给自己
            else {
                ch.writeAndFlush("[ 自己 ]发送了消息：" + msg + "\n");
            }
        });
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 关闭通道
        ctx.close();
    }
}