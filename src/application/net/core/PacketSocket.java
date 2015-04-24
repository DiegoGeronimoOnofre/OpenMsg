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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/*OBS:
 *  O tamanho máximo do buffer do DatagramPacket deve ser maior ou
 * igual ao tamanho da estrutura + o tamanho da mensagem de exceção
 * ( mensagemDeExcecaoComoString.getBytes().length )
 * para que seja possível armazenar todos
 * os caracteres da mensagem de exceção
 * no buffer do DatagramPacket.
 */

public class PacketSocket 
{
        /*
         * Tamanho máximo do pacote de recebimento
         */
        
        private final int maxLength;
        
        private final DatagramSocket socket;
        
        private final int localPort;
        
        public PacketSocket( final int localPort, final int maxLength ) throws Exception
        {
                this.maxLength = maxLength;
                this.socket = new DatagramSocket( localPort );
                this.localPort = localPort;
        }
        
        public PacketSocket( final int maxLength ) throws SocketException
        {
                this.maxLength = maxLength;
                this.socket = new DatagramSocket();
                this.localPort = socket.getLocalPort();
        }
        
        public void close()
        {
                socket.close();
        }
        
        public boolean isClosed()
        {
                return socket.isClosed();
        }
        
        public int getLocalPort()
        {
                return localPort;
        }
        
        public int getMaxLength()
        {
                return maxLength;
        }
        
        public Packet receive() throws Exception
        {
                byte[] buf = new byte[Packet.STRUCT_SIZE + maxLength];
                DatagramPacket datagramPacket = new DatagramPacket( buf, buf.length );
                socket.receive( datagramPacket );
                return Packet.datagramPacketToPacket( datagramPacket );
        }
        
        public void send( Packet packet ) throws Exception
        {
                if ( packet == null )
                        throw new Exception( "packet é null" );
                
                DatagramPacket datagramPacket = packet.toDatagramPacket();
                socket.send( datagramPacket );
                
        }
}