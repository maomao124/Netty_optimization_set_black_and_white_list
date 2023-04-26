package mao.t1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.ipfilter.IpFilterRuleType;
import io.netty.handler.ipfilter.IpSubnetFilterRule;
import io.netty.handler.ipfilter.RuleBasedIpFilter;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Project name(项目名称)：Netty_optimization_set_black_and_white_list
 * Package(包名): mao.t1
 * Class(类名): Server
 * Author(作者）: mao
 * Author QQ：1296193245
 * GitHub：https://github.com/maomao124/
 * Date(创建日期)： 2023/4/26
 * Time(创建时间)： 13:42
 * Version(版本): 1.0
 * Description(描述)： 设置黑白名单
 */

@Slf4j
public class Server
{
    @SneakyThrows
    public static void main(String[] args)
    {
        //规则判断的是 Client 的 IP 和规则中的 IP 是否在同一个局域网中
        //构造方法：IpSubnetFilterRule(String ipAddress, int cidrPrefix, IpFilterRuleType ruleType)
        IpSubnetFilterRule ipSubnetFilterRule1 = new
                IpSubnetFilterRule("127.0.0.1", 8, IpFilterRuleType.REJECT);
        IpSubnetFilterRule ipSubnetFilterRule2 = new
                IpSubnetFilterRule("192.168.3.1", 24, IpFilterRuleType.REJECT);
        IpSubnetFilterRule ipSubnetFilterRule3 = new
                IpSubnetFilterRule("192.168.7.1", 24, IpFilterRuleType.REJECT);
        IpSubnetFilterRule ipSubnetFilterRule4 = new
                IpSubnetFilterRule("147.11.68.152", 16, IpFilterRuleType.REJECT);
        IpSubnetFilterRule ipSubnetFilterRule5 = new
                IpSubnetFilterRule("113.221.0.1", 16, IpFilterRuleType.REJECT);
        //白名单
        IpSubnetFilterRule ipSubnetFilterRule6 = new
                IpSubnetFilterRule("113.221.202.104", 16, IpFilterRuleType.ACCEPT);
        //基于ip的过滤器，可以自定义哪些ip或者ip范围允许通过或者被阻止，他是一个共享的handle
        //构造方法：public RuleBasedIpFilter(IpFilterRule... rules)
        RuleBasedIpFilter ruleBasedIpFilter = new RuleBasedIpFilter(
                //ipSubnetFilterRule6要加在ipSubnetFilterRule5的前面，不然不生效
                ipSubnetFilterRule6,
                ipSubnetFilterRule1,
                ipSubnetFilterRule2,
                ipSubnetFilterRule3,
                ipSubnetFilterRule4,
                ipSubnetFilterRule5);

        new ServerBootstrap()
                .group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>()
                {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception
                    {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG))
                                .addLast(new StringDecoder())
                                .addLast(ruleBasedIpFilter)
                                .addLast(new ChannelInboundHandlerAdapter()
                                {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
                                    {
                                        log.info("读事件：" + ctx.channel());
                                        super.channelRead(ctx, msg);
                                    }
                                });
                    }
                })
                .bind(8080)
                .sync();

    }
}
