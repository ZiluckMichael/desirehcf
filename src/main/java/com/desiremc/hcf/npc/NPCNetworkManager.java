package com.desiremc.hcf.npc;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.server.v1_12_R1.EnumProtocol;
import net.minecraft.server.v1_12_R1.EnumProtocolDirection;
import net.minecraft.server.v1_12_R1.NetworkManager;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketListener;

import javax.crypto.SecretKey;
import java.net.SocketAddress;

@SuppressWarnings("rawtypes")
public class NPCNetworkManager extends NetworkManager
{

    public NPCNetworkManager()
    {
        super(EnumProtocolDirection.SERVERBOUND);
    }

    @Override
    public void channelActive(ChannelHandlerContext channelhandlercontext) throws Exception
    {

    }

    @Override
    public void setProtocol(EnumProtocol enumprotocol)
    {

    }

    @Override
    public void channelInactive(ChannelHandlerContext channelhandlercontext)
    {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelhandlercontext, Throwable throwable)
    {

    }

    @Override
    protected void a(ChannelHandlerContext channelhandlercontext, Packet packet)
    {

    }

    @Override
    public void setPacketListener(PacketListener packetlistener)
    {

    }

    @Override
    public void sendPacket(Packet packet)
    {

    }

    @Override
    public void sendPacket(Packet packet, GenericFutureListener genericfuturelistener, GenericFutureListener... agenericfuturelistener)
    {

    }

    @Override
    public SocketAddress getSocketAddress()
    {
        return new SocketAddress()
        {
            private static final long serialVersionUID = 7094105914152195115L;
        };
    }

    @Override
    public boolean isLocal()
    {
        return false;
    }

    @Override
    public void a(SecretKey secretkey)
    {

    }

    @Override
    public boolean isConnected()
    {
        return true;
    }

    @Override
    public void stopReading()
    {

    }

    @Override
    public void setCompressionLevel(int i)
    {

    }

    @Override
    public void handleDisconnection()
    {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelhandlercontext, Packet object) throws Exception
    {

    }

}
