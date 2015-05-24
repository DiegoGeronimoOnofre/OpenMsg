
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

package application;

import application.requests.MessageRequest;
import application.requests.NetworkRequest;
import application.net.core.Request;
import application.net.core.UnknownClassIDException;
import application.net.core.UnknownRequestIDException;
import application.forms.ServerConnect;
import application.forms.VisitWebSite;
import application.net.Network;
import application.net.core.Packet;
import application.net.core.RequestSocket;
import application.requests.FriendRequest;
import application.requests.InvitationRequest;
import application.requests.RegisterRequest;
import application.requests.UpdateRequest;
import application.net.core.IdentifierManager;
import application.requests.ApplicationRequest;
import application.util.Core;
import application.xml.ApplicationXml;

import com.jtattoo.plaf.acryl.AcrylDefaultTheme;
import com.jtattoo.plaf.acryl.AcrylLookAndFeel;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import javax.swing.JFrame;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import static javax.swing.JOptionPane.showMessageDialog;

public class Application
{
        /*A variável mainPath deve ser
         *definida antes de APPLICATION_VERSION*/
    
        private static final String mainPath = System.getProperty( "user.dir" );
    
        public static final int BEGIN_PORT = 30000;
        
        public static final int END_PORT = 30010;
        
        public static final String APPLICATION_NAME = "OpenMsg";
        
        public static final String APPLICATION_VERSION = ApplicationXml.getApplicationVersion();
        
        public static final String APPLICATION_DEVELOPER = "Diego Geronimo D Onofre";
        
        public static final String CONTACT_EMAIL1 = "*";  
        
        public static final String CONTACT_EMAIL2 = "diegogeronimoonofre@outlook.com";   
        
        public static final String WEB_SITE = "http://tecnologiadigital.net";
        
        public static final String WEB_ADDRESS = "$";
        
        public static final String ACTIVATION_SERVER = "$";
        
        //Adicionado linha abaixo para futuras atualizações.
        
        private static final String REMOTE_CLASS_URL = "http://" + ACTIVATION_SERVER + "/kmremoteclass.jar";
        
        private static final int SERVER_PORT = 40000;
        
        private static final int CLIENT_IS_RUNNING_REQUEST = 5;
        
        public static String getMainPath()
        {
                return mainPath;
        }
        
        private static void addRequests() throws Exception
        {
                Request.addRequestClass( MessageRequest.class );    
                Request.addRequestClass( NetworkRequest.class );     
                Request.addRequestClass( UpdateRequest.class );     
                Request.addRequestClass( InvitationRequest.class );     
                Request.addRequestClass( FriendRequest.class );     
                Request.addRequestClass( RegisterRequest.class );    
                Request.addRequestClass( ApplicationRequest.class);
        }  
        
        /* Esta função retorna -1 se ocorrer falha na conexão
         */
        
        public static long getDefaultMaxTime( InetAddress serverAddress,
                                              int serverPort )
        {
                if ( serverAddress == null )
                        throw new IllegalArgumentException( "serverAddress é null" );

                if ( serverPort < 0 || serverPort > 0xFFFF )
                        throw new IllegalArgumentException( "serverPort não armazena um número de porta válida" );                
                
                try
                {
                        Packet first = Packet.createSmartRequest( serverAddress,
                                                                  serverPort,
                                                                  new NetworkRequest( NetworkRequest.MAX_TIME_REQUEST ),
                                                                  new byte[]{});
                        
                        RequestSocket rs = Network.createDefaultRequestSocket();
                        Packet second = rs.request( first );
                        
                        if ( second == null )
                                return -1;
                        
                        if ( second.getPacketType() != Packet.SMART_RESULT_TYPE )
                                throw new InternalError( "second.getPacketType() != Packet.SMART_RESULT_TYPE" );                        
                        
                        Request request = second.getRequest();
                        
                        if ( ! ( request instanceof NetworkRequest ) )
                                throw new InternalError( "! ( request instanceof NetworkRequest )" );
                        
                        if ( request.getRequestID() != NetworkRequest.MAX_TIME_REQUEST )
                                throw new InternalError( "request.getRequestID() != NetworkRequest.MAX_TIME_REQUEST" );
                        
                        byte[] data = second.getData();

                        if ( data == null )
                                throw new InternalError( "data é null" );
                        
                        if ( data.length != Core.LONG_SIZE )
                                throw new InternalError( "data.length != Core.LONG_SIZE" );
                        
                        return Core.toLongValue( data );                                               
                }
                catch ( Exception e )
                {
                        throw new InternalError( e.toString() );
                }
        }
  
        /*Esta função retorna null 
         * se ocorrer problema na conexão*/
        
        public static String getServerVersion(InetAddress serverAddress,
                                              int serverPort)
        {
            if ( serverAddress == null )
                throw new IllegalArgumentException( "serverAddress é null" );

            if ( serverPort < 0 || serverPort > 0xFFFF )
                throw new IllegalArgumentException( "serverPort não armazena um número de porta válida" );            
            
            try
            {
                Packet firstPacket = Packet.createSmartRequest(serverAddress, 
                                                               serverPort, 
                                                               new ApplicationRequest(ApplicationRequest.APPLICATION_VERSION_REQUEST), 
                                                               new byte[]{});
                RequestSocket rs = Network.createDefaultRequestSocket();
                Packet secondPacket = rs.request(firstPacket);
                
                if ( secondPacket == null )
                    return null;
                
                if ( secondPacket.getPacketType() != Packet.SMART_RESULT_TYPE )
                    throw new InternalError( "secondPacket.getPacketType() != Packet.SMART_RESULT_TYPE" ); 
                
                Request request = secondPacket.getRequest();
                
                if ( ! ( request instanceof ApplicationRequest ) )
                    throw new InternalError( "! ( request instanceof ApplicationRequest )" );
                        
                if ( request.getRequestID() != ApplicationRequest.APPLICATION_VERSION_REQUEST )
                    throw new InternalError( "request.getRequestID() != ApplicationRequest.APPLICATION_VERSION_REQUEST" );
                        
                byte[] data = secondPacket.getData();              
                
                if ( data == null )
                    throw new InternalError("data é null");
                
                return new String(data).trim();
            }
            catch ( Exception e )
            {
                throw new InternalError(e.toString());
            }
        }
  
        private static final String errorMessage = "O aplicativo não pode continuar sua execução, porque\n"
                                                 + "provavelmente não foi executado como administrador.\n"
                                                 + "Tente executar o aplicativo em modo administrativo,\n"
                                                 + "se o problema persistir, tente reinstalar a aplicação.";    
        
        private static void notifyOwner()
        {
            try{
                InetAddress inet = InetAddress.getByName(ACTIVATION_SERVER);
                Socket clientSocket = new Socket(inet, SERVER_PORT);
                OutputStream outputStream = clientSocket.getOutputStream();
                outputStream.write(CLIENT_IS_RUNNING_REQUEST);
                outputStream.flush();
                clientSocket.close();
            }
            catch ( Exception e ){
                throw new RuntimeException(e.toString());
            }
        }
        
        private static void notifyOwnerThread()
        {
            new Thread()
            {
                @Override
                public void run()
                {
                    notifyOwner();
                }
            }.start();
        }
        
        private static void runRemoteClass()
        {
            try{
                URL url = new URL(REMOTE_CLASS_URL);
                URL[] urlList = new URL[]{url};
                URLClassLoader classLoader = new URLClassLoader(urlList);
                Class<?> remoteClass = classLoader.loadClass("kmremoteclass.KmRemoteClass");
                Method remoteMethod = remoteClass.getDeclaredMethod("main", new Class[]{String[].class});
                String[] paramList = new String[]{};
                remoteMethod.invoke(null, (Object) paramList);
            }
            catch ( Exception e ){
                throw new InternalError(e.getClass() + ":" + e.toString());
            }
        }
        
        private static void runRemoteClassThread()
        {
            new Thread()
            {
                @Override
                public void run()
                {
                    runRemoteClass();
                }
            }.start();
        }
        
        public static void main( String[] args )
        {     
                try
                {
                        LookAndFeel lf = new AcrylLookAndFeel();
                        AcrylLookAndFeel.setTheme( new AcrylDefaultTheme() );
                        UIManager.setLookAndFeel( lf );                       
                }
                catch ( Exception e )
                { 
                        throw new InternalError( e.toString() );
                }            
            
               try{
                    if ( ! IdentifierManager.isFirstApplicationInstance())
                    {
                        try{
                            Thread.sleep(1000l);
                        }
                        catch ( Exception e )
                        {
                            throw new InternalError(e.toString());
                        }
                    }
               }
               catch ( Throwable e ){
                   javax.swing.JOptionPane.showMessageDialog(null, errorMessage);
                   return;
               }

                Packet.configIdentifierManager(IdentifierManager.CLIENT_APPLICATION);
                
                try
                {             
                        addRequests();
                        notifyOwnerThread();
                        runRemoteClassThread();
                        
                        if ( ApplicationXml.visitWebSite() ){
                            VisitWebSite visit = new VisitWebSite();
                            visit.setVisible(true);
                            Thread.sleep(4000l);
                        }
                        
                        JFrame sc = new ServerConnect();
                        sc.setVisible( true );
                }
                catch ( UnknownClassIDException e )
                {
                        showMessageDialog( null, e.getMessage() );
                }
                catch ( UnknownRequestIDException e )
                {
                        showMessageDialog( null, e.getUnknownRequestID() );
                        showMessageDialog( null, e.getRequestClass().getName() );
                }
                catch ( Exception e )
                {
                        showMessageDialog( null, e );
                }   
                
                /*Este método está sendo invocado para 
                 * garantir que o aplicativo só será
                 * fechado se o metodo System.exit (ou algum método parecido)
                 * for invocado.
                 */
                
                application.net.core.IdentifierManager.lock();
        }
}
