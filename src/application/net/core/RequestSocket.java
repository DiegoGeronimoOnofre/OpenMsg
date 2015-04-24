/**
The MIT License (MIT)
Copyright (c) 2015 Diego Geronimo D Onofre
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files OpenMsg, to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package application.net.core;

import java.net.InetAddress;

/*OBS:
 *  O tamanho máximo do buffer do DatagramPacket deve ser maior ou
 * igual ao tamanho da estrutura + o tamanho da mensagem de exceção
 * ( mensagemDeExcecaoComoString.getBytes().length )
 * para que seja possível armazenar todos
 * os caracteres da mensagem de exceção
 * no buffer do DatagramPacket.
 */

public class RequestSocket 
{
        private final long interval;

        private final int tryNumber;
        
        /*Tamanho máximo do buffer do pacote de recebimento*/
        private final int maxLength;               

        public RequestSocket( final long interval, 
                              final int tryNumber, 
                              final int maxLength )
        {               
                this.interval = interval;
                this.tryNumber = tryNumber;
                this.maxLength = maxLength;
        }   
        
        public long getInterval()
        {
                return interval;
        }
        
        public int getTryNumber()
        {
                return tryNumber;
        }
        
        public int getMaxLength()
        {
                return maxLength;
        }       
        
        private class XTh extends Thread
        {        
                public static final int TERMINATED = 0;

                public static final int RUNNING = 1;

                private int status = RUNNING;                               
                
                private final PacketSocket socket;
                
                private final Object sync = new Object();                
                
                private final Packet requestPacket;
                
                private Packet receivePacket = null;  
                
                public XTh( PacketSocket socket, 
                            Packet requestPacket )
                {                      
                        this.socket = socket;
                        this.requestPacket = requestPacket;
                }
                
                public Object getSync()
                {
                        return sync;
                }
                
                public Packet getReceivePacket()
                {
                        synchronized ( sync )
                        {
                                return receivePacket;
                        }
                }
                
                public void terminate()
                {      
                        this.status = TERMINATED;
                } 
                
                public void close()
                {
                        synchronized ( sync )
                        {
                                try
                                {
                                        if ( status == RUNNING )
                                        {
                                                terminate();
                                                PacketSocket pst = new PacketSocket( maxLength );
                                                InetAddress localHost = InetAddress.getLocalHost();
                                                Packet p = Packet.createSmartResult( requestPacket.getIdentifier(),
                                                                                     localHost,
                                                                                     socket.getLocalPort(),
                                                                                     requestPacket.getRequest(),
                                                                                     new byte[]{} );
                                                pst.send( p );
                                                sync.wait();
                                                pst.close(); 
                                        }
                                        
                                        socket.close();
                                }
                                catch ( Exception e )
                                {
                                        throw new InternalError( e.toString() );
                                }   
                        }
                }
                
                @Override
                public void run()
                {
                        
                        while ( status == RUNNING )
                        {
                                try
                                {                         
                                        synchronized ( sync )
                                        {
                                                if ( status == TERMINATED )
                                                {
                                                        sync.notify();
                                                        return;
                                                }
                                        }
                                        
                                        Packet p = socket.receive();
                                        
                                        synchronized ( sync )
                                        {
                                                if ( status == TERMINATED )
                                                {
                                                        sync.notify();
                                                        return;
                                                }

                                                if ( p.getIdentifier() == requestPacket.getIdentifier() )                                               
                                                {
                                                        receivePacket = p;
                                                        terminate();
                                                        sync.notify();
                                                        return;
                                                }
                                        }

                                }
                                catch ( InvalidPacketFormatException e )
                                {}
                                catch ( Exception e )
                                {
                                        throw new InternalError( e.toString() );
                                }
                        }
                }
        }        
        
        public Packet request( Packet requestPacket )
        {
                if ( requestPacket == null )
                        throw new IllegalArgumentException( "requestPacket é null" );

                if ( ! Packet.isRequestType( requestPacket.getPacketType() ) )
                        throw new IllegalArgumentException( "requestPacket não é um pacote de pedido" );

                try
                {
                        PacketSocket socket = new PacketSocket( maxLength );                                    
                        XTh th = new XTh( socket, requestPacket );
                        th.start();

                        for ( int i = 1; i <= tryNumber; i++ )
                        {  
                                socket.send( requestPacket );                              
                                final Object sync = th.getSync();

                                synchronized ( sync )
                                {

                                        if ( th.getReceivePacket() != null )
                                        {
                                                socket.close();
                                                return th.getReceivePacket();
                                        }
                                        else
                                                sync.wait( interval );
                                        
                                        if ( th.getReceivePacket() != null )
                                        {
                                                socket.close();
                                                return th.getReceivePacket();
                                        }
                                }
                        }

                        th.close();
                        return null;
                }
                catch ( Exception e )
                {
                        throw new InternalError( e.toString() );
                }
        }
        
}    