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

import application.util.Core;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.BindException;
import java.net.InetAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

class XRequest extends Request
{
        public static final byte CLASS_ID = 7;    
        
        public static final byte IDENT_PORT_REQUEST = 0;       
        
        public XRequest( byte requestID ) throws Exception
        {
                super( requestID );
        }
}

public class IdentifierManager {
    
    private static final int START_PORT = 40000;
    
    private static final int END_PORT   = 40009;
    
    private static int identifierPort = -1;
    
    private static long localIdentifier = Double.doubleToRawLongBits( Math.random() );
    
    private static long getNextLocalIdentifier()
    {
        localIdentifier++;
        return localIdentifier;
    }
    
    private static boolean isBound( final int port ){
        try{
            PacketSocket ps = new PacketSocket(port,1);
            ps.close();
            return false;
        }
        catch ( BindException e ){
            return true;
        }
        catch ( Exception e ){
            throw new InternalError(e.toString());
        }
    }
    
    /*Este método obtem e retorna o próximo valor 
     * de identifier perguntando para a 
     * primeira instância de aplicativo 
     * inicializada.
     */
    
    public static final int SERVER_APPLICATION = 0;
    public static final int CLIENT_APPLICATION = 1;
    
    private static int applicationType = -1;
    
    public static long getNextIdentifier(){
        
        if ( applicationType == SERVER_APPLICATION ){
            return getNextLocalIdentifier();
        }
        else if ( applicationType == CLIENT_APPLICATION ){
            if ( identifierPort == -1 )
                throw new InternalError("identifierPort == -1. Verifique se o método config foi invocado!");

            RequestSocket rs = new RequestSocket(10000l, 1, Core.LONG_SIZE);

            try{
                InetAddress localhost = InetAddress.getLocalHost();
                XRequest request = new XRequest(XRequest.IDENT_PORT_REQUEST);
                Packet requestPacket = Packet.createSimpleRequestForIdentifierManager(localhost, 
                                                                                     identifierPort, 
                                                                                     request, 
                                                                                     0, 
                                                                                     new byte[]{});
                Packet resultPacket = rs.request(requestPacket);

                if ( resultPacket == null ){
                    javax.swing.JOptionPane.showMessageDialog(null,
                                                              "O aplicativo será interrompido, porque um erro fatal ocorreu!"
                                                            + "\n O motivo deste problema, "
                                                            + "\n pode ter sido um processo que talvez tenha"
                                                            + "\n sido finalizado de forma incorreta!");
                    System.exit(0);
                    return -1;
                }
                else
                    return Core.toLongValue( resultPacket.getData() );
            }
            catch ( Exception e ){
                throw new InternalError(e.toString());
            }
        }
        else
            throw new InternalError("applicationType inválido!");
    }

    private static class XThread extends Thread
    {
        final Object lock = new Object();

        {
            setName("IdentifierManager");
        }
        
        @Override
        public void run()
        {
            PacketSocket ps = null;

            for ( int port = START_PORT; port <= END_PORT; port++ ){
                try{
                    ps = new PacketSocket(port,0);
                    identifierPort = port;
                    break;
                }
                catch ( Exception e ){
                }
            }

            if ( identifierPort == -1 ){
                javax.swing.JOptionPane.showMessageDialog( null, "O aplicativos será interrompido, "
                                                                 + "porque ocorreu um erro fatal!");
                System.exit(0);
            }

            xwait();

            do{
                try{
                    Packet receivePacket = ps.receive();
                    Request request = receivePacket.getRequest();

                    if ( request instanceof XRequest ){
                        if ( request.getRequestID() == XRequest.IDENT_PORT_REQUEST ){
                            final long identifier = getNextLocalIdentifier();
                            Packet sendPacket = Packet.createSimpleResult( receivePacket.getIdentifier(), 
                                                                           receivePacket.getAddress(),
                                                                           receivePacket.getPort(),
                                                                           receivePacket.getRequest(),
                                                                           receivePacket.getOrder(),
                                                                           Core.toLongBytes(identifier));
                            ps.send(sendPacket);
                        }
                    }
                }
                catch (Exception e){
                }
            } while (true);
        }

        public void xwait()
        {
            synchronized ( lock ){
                try
                {
                    lock.notify();
                    lock.wait();
                    lock.notify();
                }
                catch ( Exception e ){
                    throw new InternalError(e.toString());
                }
            }            
        }
    }    
    
    static
    {
        try {
            Request.addRequestClass(XRequest.class);
        }
        catch ( Exception e ){
            throw new InternalError(e.toString());
        }      
    }
    
    private static XThread mainThread = new XThread();

    static void config( final int appType )
    {       
        if ( appType != SERVER_APPLICATION &&
             appType != CLIENT_APPLICATION )
        {
            throw new InternalError("appType != SERVER_APPLICATION && "
                                  + "appType != CLIENTE_APPLICATION");
        }
        
        applicationType = appType;
        
        if ( applicationType == CLIENT_APPLICATION )
        {
            for ( int port = START_PORT; port <= END_PORT; port++ ){
                final boolean isBound = isBound(port);

                if ( isBound ){
                    try
                    {
                        RequestSocket rs = new RequestSocket(100l, 1, Core.LONG_SIZE);
                        InetAddress localhost = InetAddress.getLocalHost();
                        XRequest request = new XRequest(XRequest.IDENT_PORT_REQUEST);
                        Packet requestPacket = Packet.createSimpleRequestForIdentifierManager(localhost, 
                                                                                              port, 
                                                                                              request, 
                                                                                              0, 
                                                                                              new byte[]{});
                        Packet resultPacket = rs.request(requestPacket);

                        if ( resultPacket != null ){
                            if ( requestPacket.getIdentifier() == resultPacket.getIdentifier() ){
                                identifierPort = port;
                                return;
                            }
                        }
                    }
                    catch ( Exception e ){
                        throw new InternalError(e.toString());
                    }
                }
            }

            mainThread.start();
            mainThread.xwait();
        }
    }
    
    private static boolean isFirstInvocation = true;
    
    private static boolean isFirstApplication = false;
    
    public static boolean isFirstApplicationInstance()
    {
        if ( isFirstInvocation ){
            isFirstApplication = isFirstApplication();
            return isFirstApplication;
        }
        else
            return isFirstApplication;
    }
    
    private static boolean isFirstApplication()
    {         
        if ( ! isFirstInvocation ){
            throw new InternalError("O método 'isFirstApplicationInstance' "
                                  + "não deve ser invocado mais de uma vez!");
        }
        else
            isFirstInvocation = false;
        
        try
        {
           final String executionFilePath = System.getProperty("user.dir") + 
                                            File.separatorChar + "executionFile.txt";

           final File executionFile = new File(executionFilePath); 
           boolean exists = true;

           if ( ! executionFile.exists())
                exists = executionFile.createNewFile();

           if ( exists )
           {
               RandomAccessFile raf = new RandomAccessFile(executionFile,"rw"); 
               FileChannel fileChannel = raf.getChannel();
               final FileLock fileLock;
               
               try{
                   fileLock = fileChannel.tryLock();
               }
               catch ( OverlappingFileLockException e )
               {
                   return true;
               }
               catch ( Exception e )
               {
                   return true;
               }

               if ( fileLock != null )
                   return true;
               else
                   return false;
           }
           else
           {
                javax.swing.JOptionPane.showMessageDialog(null,"O aplicativo não pode iniciar, "
                                                               + "porque um erro fatal ocorreu!");
                System.exit(0);
                return false;
           }
        }
        catch ( Exception e )
        {
            throw new InternalError(e.toString());
        }
    }      
    
    /*Retorna true se esta instância
     * de aplicativo contém um fluxo
     * em execução que responde por solicitações
     */
    
    public static boolean isIdentifierManager()
    {
        if ( applicationType == -1 )
            throw new InternalError("O método config não foi invocado!");
        
        if ( applicationType != SERVER_APPLICATION &&
             applicationType != CLIENT_APPLICATION )
        {
            throw new InternalError("appType != SERVER_APPLICATION && "
                                  + "appType != CLIENTE_APPLICATION");
        }        
        
        if ( applicationType != CLIENT_APPLICATION )
            throw new InternalError(" O método isIdentifierManager não deve ser "
                                  + " invocado por tipos de aplicativos diferentes"
                                  + " de CLIENT_APPLICATION");
        
        return mainThread.isAlive();
    }    
    
    public static void lock()
    {
        Thread th = new Thread("IdentifierManager.lock()")
        {
            private final Object lock = new Object();

            @Override
            public void run()
            {
                synchronized (lock)
                {
                    try{
                        lock.wait();
                    }
                    catch ( Exception e ){
                        throw new InternalError(e.toString());
                    }
                }
            }
        };

        th.setDaemon(false);
        th.start();  
        
        try{
            th.join();
        }
        catch ( Exception e ){
            throw new InternalError(e.toString());
        }
    }
}