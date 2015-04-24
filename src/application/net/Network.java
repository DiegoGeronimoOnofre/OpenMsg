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

import application.net.core.InvalidPacketFormatException;
import application.net.core.NetworkException;
import application.net.core.Packet;
import application.net.core.RequestSocket;
import application.net.core.PacketSocket;
import application.util.Convertible;
import application.util.Core;

import java.net.InetAddress;
import java.util.List;
import java.util.ArrayList;

public class Network
{    
        public static final long DEFAULT_INTERVAL = 500;
        
        public static final int DEFAULT_TRY_COUNT = 10;
        
        public static final int DEFAULT_MAX_LENGTH  = 150;
        
        public static final int DEFAULT_PIECE_LENGTH  = 150;
        
        private final XManager downloadManager = new XManager( Manager.DOWNLOAD_TYPE );    
        
        private final XManager downloadList = new XManager( Manager.DOWNLOAD_TYPE );    

        private final XManager sendObjectManager = new XManager( Manager.SEND_OBJECT_TYPE );    
        
        private final XManager sendObjectList = new XManager( Manager.SEND_OBJECT_TYPE );    
        
        private final XManager uploadManager = new XManager( Manager.UPLOAD_TYPE );         
        
        private final XManager uploadList = new XManager( Manager.UPLOAD_TYPE );         
        
        public Manager getDownloadManager()
        {
                return downloadManager;
        }
        
        public Manager getUploadManager()
        {
                return uploadManager;
        }

        public Manager getSendObjectManager()
        {
                return sendObjectManager;
        }           
        
        public static boolean isEquals( InetAddress first, InetAddress second )
        {
                if ( first == null )
                        throw new IllegalArgumentException( "first é null" );

                if ( second == null )
                        throw new IllegalArgumentException( "second é null" );
                
                byte[] firstAddress = first.getAddress();
                byte[] secondAddress = second.getAddress();
                
                if ( firstAddress.length == secondAddress.length )
                {
                        for ( int j = 0; j < firstAddress.length; j++ )
                                if ( firstAddress[j] != secondAddress[j] )
                                        return false;
                        
                        return true;
                }
                else
                        return false;                
        }        
        
        
        public static RequestSocket createDefaultRequestSocket()
        {
                return new RequestSocket( Network.DEFAULT_INTERVAL, 
                                          Network.DEFAULT_TRY_COUNT, 
                                          Network.DEFAULT_MAX_LENGTH );
        }        
        
        public byte[] download( Packet smartPacket ) throws Exception
        {
                if ( smartPacket == null )
                        throw new IllegalArgumentException( "smartPacket é null" );
                
                final InetAddress host;
                
                if ( smartPacket.getPacketType() == Packet.SMART_REQUEST_TYPE )
                        host = InetAddress.getLocalHost();
                else if ( smartPacket.getPacketType() == Packet.SMART_RESULT_TYPE )
                        host = smartPacket.getAddress();
                else
                        throw new IllegalArgumentException( "smartPacket não é um "
                                                            + "SMART_REQUEST_TYPE ou "
                                                            + "SMART_RESULT_TYPE." );  
                
                if ( ! downloadManager.isAdded( smartPacket.getIdentifier(), host ) )
                        throw new RuntimeException( "identifier: " + smartPacket.getIdentifier() + " e host: " + host + " não adicionados." );
                
                synchronized ( downloadList )
                {
                        if ( downloadList.isAdded( smartPacket.getIdentifier(), host ) )
                                throw new RuntimeException( "O fluxo de download relacionado com o identifier: " + smartPacket.getIdentifier() +
                                                            " e host: " + host + " ja está em execução.");

                        downloadList.add( smartPacket.getIdentifier(), host );
                }
                
                RequestSocket r = new RequestSocket( DEFAULT_INTERVAL, 
                                                     DEFAULT_TRY_COUNT, 
                                                     DEFAULT_MAX_LENGTH );
                
                if ( smartPacket.getPacketType() == Packet.SMART_REQUEST_TYPE )
                        smartPacket = r.request( smartPacket );                                                                                                      
                
                if ( smartPacket == null )
                        return null;
                
                if ( Packet.isExceptionType( smartPacket.getPacketType() ) )
                        throw smartPacket.getException();                
                
                if ( smartPacket.getPacketType() != Packet.SMART_RESULT_TYPE )
                        throw new NetworkException( "smartPacket.getPacketType() "
                                                                                + "!= Packet.SMART_RESULT_TYPE" );
                
                if ( smartPacket.getTotalPackets() < 1 )
                        throw new IllegalArgumentException( "smartPacket.getTotalPackets é menor que um" );
               
                final int totalPackets = smartPacket.getTotalPackets();
                List<Byte> result = new ArrayList<Byte>();
                final byte[] bytesIdentifier = Core.toLongBytes( smartPacket.getIdentifier() );
                
                for ( int order = 0; order < totalPackets; order++ )
                {                               
                        Packet requestPacket = Packet.createSimpleRequest( smartPacket.getAddress(), 
                                                                           smartPacket.getPort(), 
                                                                           smartPacket.getRequest(),
                                                                           order,
                                                                           bytesIdentifier );

                        Packet resultPacket = r.request( requestPacket );

                        if ( resultPacket == null )
                                return null;
                                
                        if ( resultPacket.getPacketType() == Packet.EXCEPTION_TYPE )
                                throw resultPacket.getException();            
                        
                        if ( resultPacket.getPacketType() != Packet .SIMPLE_RESULT_TYPE )
                                throw new NetworkException( "resultPacket.getPacketType() "
                                                                                        + "!= Packet .SIMPLE_RESULT_TYPE" ); 
                                
                        byte[] buf = resultPacket.getData();

                        for ( byte b : buf )
                                result.add( b );                   
                                         
                }

                Packet requestPacket = Packet.createSimpleRequest( smartPacket.getAddress(), 
                                                                   smartPacket.getPort(), 
                                                                   smartPacket.getRequest(),
                                                                   totalPackets,
                                                                   bytesIdentifier );
                
                /*Este pacote é enviado para o host que faz o upload,
                 * para avisar que o download foi concluído 
                 * e dessa forma os fluxos de upload, então, podem ser 
                 * interrompidos no momento certo.
                 * Isto evita que os fluxo de upload 
                 * fiquem ativos mais tempo
                 * desnecessáriamente.
                 */
                
                r.request( requestPacket );          
                
                final byte[] res = new byte[result.size()];
                
                for ( int i = 0; i < result.size(); i++ )
                        res[i] = result.get( i );                
                
                 if ( ! downloadManager.remove( smartPacket.getIdentifier(), host ) )
                        throw new InternalError( "Identifier ou host não encontrado "
                                                                        + "na lista do downloadManager." );

                 if ( ! downloadList.remove( smartPacket.getIdentifier(), host ) )
                        throw new InternalError( "Identifier ou host não encontrado na "
                                                                        + "lista do downloadRunning." );              
                
                return res;                
        } 
        
        public static class Th extends Thread
        {
                public static final int TERMINATED = 0;

                public static final int RUNNING = 1;

                private int status = RUNNING;

                public int getStatus()
                {
                        return status;
                }

                public void terminate()
                {      
                        this.status = TERMINATED;
                }
        }               
        
        public static Convertible requestObject( final Packet smartPacket,
                                                 Convertible obj ) throws Exception
        {
                return requestObject( smartPacket, obj, DEFAULT_TRY_COUNT * DEFAULT_INTERVAL );
        }
        
        public static Convertible requestObject( final Packet smartPacket,
                                                 Convertible obj,
                                                 final long maxTime ) throws Exception
        {                                           
                if ( smartPacket == null )
                        throw new IllegalArgumentException( "smartPacket é null." ); 
                
                if ( obj == null )
                        throw new IllegalArgumentException( "obj é null" );
                
                if ( smartPacket.getPacketType() != Packet.SMART_REQUEST_TYPE )
                        throw new IllegalArgumentException( "smartPacket não é um "
                                                            + "SMART_REQUEST_TYPE" );

                InetAddress host = InetAddress.getLocalHost();
                Network net = new Network();
                Manager downloadManager = net.getDownloadManager();
                XManager uploadManager = ( XManager ) net.getUploadManager();
                final PacketSocket ps = new PacketSocket( DEFAULT_MAX_LENGTH );
                byte[] information = obj.toBytes();
               
                if ( uploadManager.add( smartPacket.getIdentifier(), host ) )
                {
                        final boolean result = net.privateUpload( ps, 
                                                                  smartPacket,
                                                                  host,
                                                                  information,
                                                                  false );
                        
                        if ( result == false )
                                return null;
                }
                else
                        throw new InternalError();
                
                final Object sync = new Object();
                
                class XThread extends Th
                {
                        Packet packet = null;                                      
                        
                        @Override
                        public void run()
                        {
                                while ( getStatus() == RUNNING )
                                {
                                        try
                                        {                                             
                                                synchronized ( sync )
                                                {
                                                        if ( getStatus() == TERMINATED )
                                                        {
                                                                sync.notify();
                                                                return;
                                                        }
                                                }
                                                 
                                                final Packet p = ps.receive();                                   
                                                
                                                synchronized ( sync )
                                                {
                                                        if ( getStatus() == TERMINATED )
                                                        {
                                                                sync.notify();
                                                                return;
                                                        }
                                                }

                                                if ( p.getIdentifier() == smartPacket.getIdentifier() )
                                                {
                                                        synchronized ( sync )
                                                        {
                                                                packet = p;
                                                                terminate();
                                                                ps.close();
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
                        
                        public Packet getReceivePacket()
                        {
                                return packet;
                        }
                }
                
                XThread xThread = new XThread();                           
                xThread.start();
                
                synchronized ( sync )
                {
                        sync.wait( maxTime );
                        
                        if ( xThread.getStatus() == Th.RUNNING )
                        {
                                xThread.terminate();
                                PacketSocket pst = new PacketSocket( DEFAULT_MAX_LENGTH );
                                InetAddress localHost = InetAddress.getLocalHost();
                                Packet p = Packet.createSmartRequest( localHost,
                                                                      ps.getLocalPort(),
                                                                      smartPacket.getRequest(),
                                                                      new byte[]{} );
                                pst.send( p );
                                sync.wait();                                
                                pst.close();      
                        }                       
                                        
                        if ( ! ps.isClosed() )
                                ps.close();
                }
                
                final Packet packet = xThread.getReceivePacket();
                
                if ( packet != null )
                {
                        if ( downloadManager.add( packet ) )
                        {
                                byte[] result = net.download( packet );
                                
                                if ( result != null )
                                        return Convertible.bytesToObject( result );
                                else 
                                        return null;
                        }
                        else
                                throw new InternalError();
                }
                else
                        return null;
        }
        
        public boolean sendObject( Packet smartResult, Run run  ) throws Exception
        {
                if ( smartResult == null )
                        throw new IllegalArgumentException( "smartResult é null" );

                if ( run == null )
                        throw new IllegalArgumentException( "run é null" );
                
                if ( smartResult.getPacketType() != Packet.SMART_RESULT_TYPE  )
                        throw new IllegalArgumentException( "smartResult não é um SMART_RESULT_TYPE" );
                
                if ( ! sendObjectManager.isAdded( smartResult.getIdentifier(), smartResult.getAddress() ) )
                        throw new RuntimeException( "identifier: " + smartResult.getIdentifier() 
                                                     + " e host: " + smartResult.getAddress() + " não adicionados." );       
                
                synchronized ( sendObjectList )
                {
                        if ( sendObjectList.isAdded( smartResult.getIdentifier(), smartResult.getAddress() ) )
                                throw new RuntimeException( "O fluxo de envio de objeto relacionado"
                                                            + " com o identifier: " + smartResult.getIdentifier() + 
                                                            " e host: " + smartResult.getAddress() + " ja está em execução.");                

                        sendObjectList.add( smartResult.getIdentifier(), smartResult.getAddress());                
                }
                
                downloadManager.add( smartResult );
                byte[] information = download( smartResult );
                
                if ( information == null )
                        return false;
                
                Convertible obj = Convertible.bytesToObject( information );
                Convertible result = run.run( obj );
                
                if ( result == null )
                        return false;
                
                Packet sendPacket = Packet.createSmartResult( smartResult.getIdentifier(), 
                                                              smartResult.getAddress(), 
                                                              smartResult.getPort(), 
                                                              smartResult.getRequest(),
                                                              new byte[]{} );
                
                uploadManager.add( sendPacket.getIdentifier(), sendPacket.getAddress() );
                final boolean res = privateUpload( new PacketSocket( DEFAULT_MAX_LENGTH ), 
                                                   sendPacket, 
                                                   sendPacket.getAddress(), 
                                                   result.toBytes(),
                                                   true );
                
                if ( ! sendObjectManager.remove( smartResult.getIdentifier(), smartResult.getAddress() ) )
                        throw new InternalError( "Identifier ou host não encontrado"
                                                                        + " na lista do sendObjectManager." );
                
                 if ( ! sendObjectList.remove( smartResult.getIdentifier(), smartResult.getAddress() ) )
                        throw new InternalError( "Identifier ou host não encontrado"
                                                                        + " na lista do sendObjectRunning." );            
                 return res;
        }
        
        
        public boolean upload( Packet smartPacket,
                                                   byte[] information ) throws Exception
        {
                if ( smartPacket == null )
                        throw new IllegalArgumentException( "smartPacket é null." );
                
                if ( information == null )
                        throw new IllegalArgumentException( "information é null." );       
                
                final InetAddress host;
                
                if ( smartPacket.getPacketType() == Packet.SMART_REQUEST_TYPE )
                        host = smartPacket.getAddress();
                else if ( smartPacket.getPacketType() == Packet.SMART_RESULT_TYPE )
                        host = InetAddress.getLocalHost();
                else        
                        throw new IllegalArgumentException( "smartPacket não é um "
                                                                                               + "SMART_REQUEST_TYPE ou "
                                                                                               + "SMART_RESULT_TYPE." );                  
                
                PacketSocket ps = new PacketSocket( DEFAULT_MAX_LENGTH );     
                return privateUpload( ps, 
                                      smartPacket, 
                                      host, 
                                      information,
                                      true );
        }        
        
        /* 
         * <code>host</code> é o endereço do 
         * computador que gerou o identificador.
         */          
        
        private boolean privateUpload( final PacketSocket ps, 
                                       Packet smartPacket,
                                       InetAddress host,
                                       final byte[] information,
                                       final boolean closePs )
        {                     
                if ( ! uploadManager.isAdded( smartPacket.getIdentifier(), host ) )
                        throw new RuntimeException( "identifier: " + smartPacket.getIdentifier() 
                                                    + " e host: " + host + " não adicionados." );       
                
                synchronized ( uploadList )
                {
                        if ( uploadList.isAdded( smartPacket.getIdentifier(), host ) )
                                throw new RuntimeException( "O fluxo de upload relacionado"
                                                            + " com o identifier: " + smartPacket.getIdentifier() + 
                                                            " e host: " + host + " ja está em execução.");                


                        uploadList.add( smartPacket.getIdentifier(), host );             
                }
                
                final Packet packet;
                final int totalPackets = ( information.length / DEFAULT_PIECE_LENGTH ) + 1; 
                
                if ( smartPacket.getPacketType() == Packet.SMART_REQUEST_TYPE )
                {
                        packet = Packet.createSmartResult( smartPacket.getIdentifier(),
                                                           smartPacket.getAddress(),
                                                           smartPacket.getPort(),
                                                           smartPacket.getRequest(),
                                                           totalPackets,
                                                           new byte[]{}
                                                                                          );
                }
                else if ( smartPacket.getPacketType() == Packet.SMART_RESULT_TYPE )
                        packet = smartPacket;
                else
                        throw new InternalError();               

                /*
                 * XThread envia um pacote  para o host 
                 * que faz download, para que o host
                 * que faz download saber a porta que 
                 * deve fazer as solicitações.
                 */
                
                class XThread extends Th
                {
                        @Override
                        public void run()
                        {
                                try
                                {                                       
                                        for ( int i = 0; i < DEFAULT_TRY_COUNT; i++ )
                                        {
                                                ps.send( packet );
                                                Thread.sleep( DEFAULT_INTERVAL );      
                                                
                                                if ( getStatus() == TERMINATED )
                                                        return;
                                        }
                                        
                                        terminate();
                                }
                                catch ( Exception e )
                                {
                                        throw new InternalError( e.toString() );
                                }
                        }
                }             
                
                final XThread xThread = new XThread();
                final Object sync = new Object();
                
                /*
                 * YThread é responsável 
                 * por responder as solicitações
                 * do host que faz o download
                 */
                
                class YThread extends Th
                {
                        @Override
                        public void run()
                        {                                              
                                final int totalPackets = ( information.length / DEFAULT_PIECE_LENGTH ) + 1;      
                                
                                while ( getStatus() == RUNNING )
                                {
                                        try
                                        {      
                                                synchronized ( sync )
                                                {
                                                        if ( getStatus() == TERMINATED )
                                                        {
                                                                sync.notify();
                                                                return;
                                                        }
                                                }
                                                
                                                final Packet sPacket = ps.receive();
                                                
                                                synchronized( sync )
                                                {                                                                                                      
                                                        if ( getStatus() == TERMINATED )
                                                        {
                                                                sync.notify();
                                                                return;
                                                        }
                                                }

                                                if ( sPacket.getPacketType() == Packet.SIMPLE_REQUEST_TYPE )
                                                {
                                                        byte[] data = sPacket.getData();

                                                        if ( data == null )
                                                                continue;

                                                        if ( data.length != Core.LONG_SIZE )
                                                                continue;

                                                        if ( packet.getIdentifier() != Core.toLongValue( data )  )
                                                                continue;
                                                        
                                                        if ( xThread.getStatus() == RUNNING );
                                                                xThread.terminate();
                                                        
                                                        //Para saber se o download foi concluído.
                                                        if ( sPacket.getOrder() == totalPackets )
                                                        {
                                                                /*
                                                                 * Este pacote é enviado para o outro host, 
                                                                 * para que o outro host não envie 
                                                                 * pacotes para este host desnecessáriamente.                                                       
                                                                 */
                                                                        
                                                                ps.send( sPacket );      
                                                                
                                                                synchronized ( sync )
                                                                {                                                
                                                                        terminate();
                                                                        sync.notify();
                                                                        return;
                                                                }
                                                        }

                                                        final int initialIndex = sPacket.getOrder() * DEFAULT_PIECE_LENGTH;
                                                        final byte[] piece;

                                                        if ( information.length >= ( sPacket.getOrder() + 1 ) * DEFAULT_PIECE_LENGTH )
                                                                piece = new byte[DEFAULT_PIECE_LENGTH];
                                                        else
                                                                piece = new byte[information.length - initialIndex];

                                                        for ( int i = initialIndex; i < initialIndex + DEFAULT_PIECE_LENGTH && i < information.length; i++ )
                                                                piece[i - initialIndex] =  information[i];

                                                        Packet p = Packet.createSimpleResult( sPacket.getIdentifier(),
                                                                                              sPacket.getAddress(),
                                                                                              sPacket.getPort(),
                                                                                              sPacket.getRequest(),
                                                                                              sPacket.getOrder(),
                                                                                              piece );

                                                        ps.send( p );
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
                
                packet.setTotalPackets( totalPackets );                                                                                 
                final long maxTimeByPacket = DEFAULT_TRY_COUNT * DEFAULT_INTERVAL;               
                final long maxTime = ( maxTimeByPacket * totalPackets ) + maxTimeByPacket;                 
                YThread yThread = new YThread( );               
                yThread.start();    
                xThread.start();                    
                final boolean result;
                
                try
                {
                        synchronized ( sync )
                        {
                                sync.wait( maxTime );
                                
                                if ( xThread.getStatus() == Th.RUNNING )
                                        xThread.terminate();
                                
                                if ( yThread.getStatus() == Th.RUNNING )
                                {
                                        yThread.terminate();
                                        PacketSocket pst = new PacketSocket( DEFAULT_MAX_LENGTH );
                                        InetAddress localHost = InetAddress.getLocalHost();
                                        Packet p = Packet.createSmartRequest( localHost,
                                                                              ps.getLocalPort(),
                                                                              smartPacket.getRequest(),
                                                                              new byte[]{} );
                                        pst.send( p );
                                        result = false;
                                        sync.wait();
                                        pst.close();                                        
                                }
                                else
                                        result = true;
                                
                                if ( closePs )
                                        ps.close();       
                        }
                }
                catch ( Exception e )
                {
                        throw new InternalError( e.toString() );
                }
                
                if ( ! uploadManager.remove( smartPacket.getIdentifier(), host ) )
                        throw new InternalError( "Identifier ou host não encontrado"
                                                                        + " na lista do uploadManager." );

                 if ( ! uploadList.remove( smartPacket.getIdentifier(), host ) )
                        throw new InternalError( "Identifier ou host não encontrado"
                                                                        + " na lista do uploadRunning." );        
 
                 return result;
        }
}