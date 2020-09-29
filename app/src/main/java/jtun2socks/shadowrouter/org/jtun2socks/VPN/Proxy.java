package jtun2socks.shadowrouter.org.jtun2socks.VPN;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Proxy extends Thread {

    private VPNService service;
    public short port;

    public Proxy(VPNService service, int port){
        this.service = service;
        this.port = (short) port;
    }

    @Override
    public void run(){
        try{
            Socket socket;
            ServerSocket serverSocket = new ServerSocket(port);
            port = (short) (serverSocket.getLocalPort() & 0xFFFF);
            Log.e("info", "VPNtoSocket VPN started on port: "+serverSocket.getLocalPort());

            while((socket = serverSocket.accept()) != null && !isInterrupted()){
                (new Tunnel(socket)).start();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public class Tunnel extends Thread {

        private Socket socket, server;
        private InputStream serverIn;
        private OutputStream serverOut;

        public Tunnel(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run(){
            try{
                NatSessionManager.NatSession session = NatSessionManager.getSession((short) socket.getPort());
                if(session != null){
                    clientIn = socket.getInputStream();
                    clientOut = socket.getOutputStream();

                    InetSocketAddress destination = new InetSocketAddress("192.168.0.7", 8080);

                    server = new Socket();
                    server.bind(new InetSocketAddress(0));
                    service.protect(server);
                    server.setSoTimeout(5000);
                    server.connect(destination, 5000);

                    serverIn = server.getInputStream();
                    serverOut = server.getOutputStream();

                    serverOut.write(0x05); //VERSION
                    serverOut.write(0x01); //NMETHOD
                    serverOut.write(0x00); //METHODS

                    //WE RECEIVE
                    //VERSION - 0x05
                    //METHOD  - 0x00

                    if(getByte() == 0x05 && getByte() == 0x00){
                        serverOut.write(0x05); //VERSION
                        serverOut.write(0x01); //COMMAND - CONNECT  |  0x02 - BIND  |  0x03 - UDP
                        serverOut.write(0x00); //RSV - UNSURE WHAT IT DOES...

                        if(socket.getInetAddress() instanceof Inet6Address){ //ADDRESS TYPE -  0x01 - IPV4  |  0x03 - DOMAIN  |  0x04  -  IPV6
                            serverOut.write(0x04);
                        }else if(socket.getInetAddress() instanceof Inet4Address){
                            serverOut.write(0x01);
                        }

                        serverOut.write(socket.getInetAddress().getAddress()); //ADDRESS

                        serverOut.write((byte) ((session.remotePort & 0xFF00) >> 8)); //PORT
                        serverOut.write((byte) (session.remotePort & 0xFF));


                        byte reply = getReplyCommand();

                        if(reply == 0x00){
                            relay();
                        }
                    }

                }
            }catch(Exception e){
                //e.printStackTrace();
            }finally{
                quickClose(socket);
                quickClose(server);
            }
        }

        public byte getReplyCommand(){
            getByte(); //SOCKS VERSION - NO NEED TO VERIFY - WE ALREADY KNOW IT WILL WORK
            byte reply = getByte();
            getByte(); //RSV
            getByte(); //ATYPE

            byte[] address = new byte[4]; //ADDRESS
            address[0] = getByte();
            address[1] = getByte();
            address[2] = getByte();
            address[3] = getByte();

            getByte(); //PORT
            getByte();

            return reply;
        }

        public byte getByte(){
            int bit;
            while(!socket.isClosed()){
                try{
                    bit = serverIn.read();
                }catch(Exception e){
                    continue;
                }
                return (byte)bit;
            }
            return -1;
        }

        public void relay(){
            Thread thread = new Thread(new Runnable(){
                @Override
                public void run(){
                    try{
                        while(!socket.isClosed() && !server.isClosed() && !socket.isInputShutdown() && !server.isOutputShutdown() && !isInterrupted()){
                            byte[] buffer = new byte[4096];
                            int length;

                            try{
                                length = clientIn.read(buffer);
                            }catch(InterruptedIOException e){
                                length = 0;
                            }catch(IOException e){
                                length = -1;
                            }catch(Exception e){
                                length = -1;
                            }

                            if(length < 0){
                                socket.shutdownInput();
                                server.shutdownOutput();
                                break;
                            }else if(length > 0){
                                try{
                                    serverOut.write(buffer, 0, length);
                                    serverOut.flush();
                                }catch(Exception e){
                                }
                            }
                        }
                    }catch(Exception e){
                    }
                }
            });

            thread.start();

            try{
                byte[] buffer = new byte[4096];
                int length;

                while(!socket.isClosed() && !server.isClosed() && !server.isInputShutdown() && !socket.isOutputShutdown() && !thread.isInterrupted()){
                    try{
                        length = serverIn.read(buffer);
                    }catch(InterruptedIOException e){
                        length = 0;
                    }catch(IOException e){
                        length = -1;
                    }catch(Exception e){
                        length = -1;
                    }

                    if(length < 0){
                        server.shutdownInput();
                        socket.shutdownOutput();
                        break;
                    }else if(length > 0){
                        try{
                            clientOut.write(buffer, 0, length);
                            clientOut.flush();
                        }catch(Exception e){
                        }
                    }
                }

                thread.interrupt();
            }catch(Exception e){
            }
        }

        public void quickClose(Socket socket){
            try{
                if(!socket.isOutputShutdown()){
                    socket.shutdownOutput();
                }
                if(!socket.isInputShutdown()){
                    socket.shutdownInput();
                }

                socket.close();
            }catch(Exception e){
                //e.printStackTrace();
            }
        }
    }
}
