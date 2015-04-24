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
package application.net;

import application.net.core.Packet;
import java.net.InetAddress;
import java.util.List;
import java.util.ArrayList;

class XManager implements Manager
{        
        private static class IdentHost
        {
                private final long identifier;

                /* <code>host</code> é o endereço 
                 * do computador que gerou o identificador.
                 */
                private final InetAddress host;

                public IdentHost( final long identifier, final InetAddress host )
                {
                        this.identifier = identifier;
                        this.host = host;
                }

                public long getIdentifier()
                {
                        return identifier;
                }

                public InetAddress getHost()
                {
                        return host;
                }
                
                @Override
                public String toString()
                {
                        StringBuilder buf = new StringBuilder();
                        buf.append( "[identifier=" );
                        buf.append( identifier );
                        buf.append( ",host=" );
                        buf.append( host );
                        buf.append( ']' );
                        return buf.toString();
                }
        }
       
        private List<IdentHost> identHostList = new ArrayList<IdentHost>();

        private final int type;
        
        public XManager( final int type )
        {
                if ( type != DOWNLOAD_TYPE && 
                      type != UPLOAD_TYPE &&
                      type != SEND_OBJECT_TYPE  )
                        throw new IllegalArgumentException( "type deve ser DOWNLOAD_TYPE "
                                                                                                + "ou UPLOAD_TYPE ou"
                                                                                                + "SEND_OBJECT_TYPE" );
                
                this.type = type;
        }
        
        private int findIndex( final long identifier, InetAddress host )
        {
                for ( int i = 0; i < identHostList.size(); i++ )
                {
                        final IdentHost identHost = identHostList.get( i );

                        if ( identHost.getIdentifier() == identifier )
                                if ( Network.isEquals( host, identHost.getHost() ) )
                                        return i;                    
                }

                return -1;        
        }
        
        public synchronized boolean isAdded( final long identifier, InetAddress host )
        {
                final int index = findIndex( identifier, host );
                
                if ( index != -1 )
                        return true;
                else
                        return false;
        }

        public synchronized boolean add( final long identifier, InetAddress host )
        {
                if ( host == null )
                        throw new IllegalArgumentException( "host é null" );                        

                if ( ! isAdded( identifier, host ) )
                {
                      identHostList.add( new IdentHost( identifier, host ) );
                      return true;
                }                    
                else 
                        return false;
        }
        
        @Override
        public boolean add( Packet p )
        {
                if ( p == null )
                        throw new IllegalArgumentException( "p é null" );
                
                IllegalArgumentException msg = new IllegalArgumentException( "smartPacket não é um "
                                                                                                                                       + "SMART_REQUEST_TYPE ou "
                                                                                                                                       + "SMART_RESULT_TYPE." ); 
                
                final InetAddress localHost;
                
                try
                {
                        localHost = InetAddress.getLocalHost();
                }
                catch ( Exception e )
                {
                        throw new InternalError( e.toString() );
                }
                
                if ( type == DOWNLOAD_TYPE )
                {
                        if ( p.getPacketType() == Packet.SMART_REQUEST_TYPE )
                                return add( p.getIdentifier(), localHost );
                        else if ( p.getPacketType() == Packet.SMART_RESULT_TYPE )
                                return add( p.getIdentifier(), p.getAddress() );
                        else
                                throw msg;
                }
                else if ( type == UPLOAD_TYPE )
                {
                        if ( p.getPacketType() == Packet.SMART_REQUEST_TYPE )
                                return add( p.getIdentifier(), p.getAddress() );                                
                        else if ( p.getPacketType() == Packet.SMART_RESULT_TYPE )
                                return add( p.getIdentifier(), localHost );                                
                        else
                                throw msg;                       
                }           
                else if ( type == SEND_OBJECT_TYPE ) 
                {
                        if ( p.getPacketType() == Packet.SMART_RESULT_TYPE )                             
                                return add( p.getIdentifier(), p.getAddress() );
                        else
                                throw new IllegalArgumentException( "smartPacket não é um "
                                                                                                       + "SMART_RESULT_TYPE" ); 
                                
                }         
                else
                        throw new InternalError();
        }

        public synchronized boolean remove( final long identifier, InetAddress host )
        {
                final int index = findIndex( identifier, host );

                if ( index != -1 )
                {
                        identHostList.remove( index );
                        return true;
                }
                else
                        return false;
        }

        @Override
         public String toString()
        {
                return identHostList.toString();
        }
}    