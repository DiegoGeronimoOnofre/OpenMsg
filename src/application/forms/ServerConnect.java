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
package application.forms;

import application.Application;
import application.net.Network;
import application.net.core.IdentifierManager;
import application.net.core.Packet;
import application.net.core.Request;
import application.net.core.RequestSocket;
import application.requests.NetworkRequest;
import application.requests.UpdateRequest;

import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.InetAddress;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import static javax.swing.JOptionPane.*;

public class ServerConnect extends JFrame
{       
        private JMenuBar menuBar = new JMenuBar();
    
        private JLabel lblServer = new JLabel( "Servidor" );
        
        private JTextField tfdServer = new JTextField();
        
        private JButton btnConnect = new JButton( "Conectar" );
        
        private JButton btnExit = new JButton( "Sair" );     
        
        private void terminate()
        {
                if ( application.net.core.IdentifierManager.isIdentifierManager() )
                        dispose();
                else
                        System.exit(0);        
        }
        
        private class KeyHandler extends KeyAdapter
        {
            @Override
            public void keyTyped(KeyEvent ke)
            {
                if ( ke.getKeyChar() == (char) KeyEvent.VK_ENTER )
                    ServerConnect.this.connect();
            }
        }
        
        public ServerConnect()
        {
                final int w = 250;
                final int h = 155;
                Point p = application.forms.util.Useful.getCenterPoint( w, h );
                setSize( w, h );
                setLocation( p );
                setTitle( "Servidor" );
                setResizable( false );     
                application.forms.util.Useful.setDefaultImageIcon( this );
                
                setJMenuBar(menuBar);
                JMenu help = new JMenu("Ajuda");
                help.setMnemonic('A');
                JMenuItem manual = new JMenuItem("Manual");
                manual.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent ae)
                    {
                        try{
                            String manualPath = Application.getMainPath() + File.separatorChar + "Manual.pdf";
                            java.awt.Desktop.getDesktop().open(new File(manualPath));
                        }
                        catch ( Exception e ){
                            JOptionPane.showMessageDialog(null, 
                                                            " Infelizmente um problema ocorreu ao abrir o manual.\n"
                                                          + " Este erro pode ter sido gerado, porque\n"
                                                          + " neste computador não há nenhum leitor pdf.\n"
                                                          + " Por favor, instale um leitor pdf e vincule a extenção pdf\n"
                                                          + " com este leitor e tente abrir o manual novamente.");
                        }
                    }
                });
                help.add(manual);
                menuBar.add(help);
                
                lblServer.setSize( 100, 20 );
                lblServer.setLocation( 20, 10 );
                
                tfdServer.setSize( 200, 20 );
                tfdServer.setLocation( 20, 30 );
                tfdServer.addKeyListener(new KeyHandler());
                Session session = null;
                
                try{
                    session = Session.getSession();
                }
                catch ( Throwable t ){}
                
                if ( session != null && ! session.getServer().equals("") )
                    tfdServer.setText( session.getServer() );
                else
                    tfdServer.setText("192.168.0.1");
                
                final WindowAdapter windowHandler = new WindowAdapter() 
                {
                        @Override
                        public void windowClosing( WindowEvent we )
                        {
                                terminate();
                        }
                };
                
                addWindowListener(windowHandler);      
                btnConnect.setSize( 100, 20 );
                btnConnect.setLocation( 20, 60 );
                btnConnect.addKeyListener(new KeyHandler());
                btnConnect.addMouseListener( new MouseAdapter()
                        {
                        @Override
                                public void mouseClicked( MouseEvent me )
                                {
                                        if ( me.getButton() == MouseEvent .BUTTON1 )
                                        {
                                                connect();
                                        }
                                }
                        } );
                
                btnExit.setSize( 100, 20 );
                btnExit.setLocation( 121, 60 );
                btnExit.addMouseListener( new MouseAdapter()
                {
                        @Override
                        public void mouseClicked( MouseEvent me )
                        {
                                if ( me.getButton() == MouseEvent.BUTTON1 )
                                {
                                        terminate();
                                }
                        }
                } );
                
                Container contentPane = getContentPane();
                contentPane.setLayout( null );
                contentPane.add( lblServer );
                contentPane.add( tfdServer );
                contentPane.add( btnConnect );
                contentPane.add( btnExit );
        }        
        
        /*
         * Retorna -1 se a porta não for encontrada
         **/
        
        public static int getServerPort( final InetAddress address ) throws Exception
        {
                if ( address == null )
                        throw new IllegalArgumentException( "address é null" );
                
                if ( ! Request.isAddedRequestClass( NetworkRequest.class ) )
                        throw new Exception( "a classe NetworkRequest não está "
                                              + "adicionada na lista de classes de requests" );
                
                class Threads
                {
                        private static final long maxTime = Network.DEFAULT_TRY_COUNT * Network.DEFAULT_INTERVAL;    
                        
                        private int port = -1;
                        
                        private final FirstThread firstThread = new FirstThread();
                        
                        public int getPort()
                        {                                          
                                try{
                                        firstThread.join();
                                }
                                catch ( Exception e ) {
                                        throw new InternalError( e.toString() );
                                }
                                
                                return port;
                        }
                        
                        class FirstThread extends Thread
                        {                              
                                @Override
                                public synchronized void run()
                                {
                                        try{
                                                wait( maxTime );
                                        }
                                        catch ( Exception e ) {
                                                throw new InternalError( e.toString() );
                                        }
                                }
                        }

                        class SecondThread extends Thread
                        {
                                final int port;
                                
                                public SecondThread( final int port )
                                {
                                        this.port = port;                     
                                }
                                
                                @Override
                                public void run()
                                {
                                        RequestSocket rs = new RequestSocket( Network.DEFAULT_INTERVAL,
                                                                              Network.DEFAULT_TRY_COUNT, 
                                                                              Network.DEFAULT_MAX_LENGTH );
                                        
                                        
                                        try
                                        {
                                                Packet smartRequest = Packet.createSmartRequest( address, 
                                                                                                 port, 
                                                                                                 new NetworkRequest( NetworkRequest.PORT_REQUEST ),
                                                                                                 new byte[]{} );
                                                 Packet smartResult = rs.request( smartRequest );
                                                
                                                 if ( smartResult != null )
                                                 {
                                                         final byte packetType = smartResult.getPacketType();
                                                         
                                                         if ( packetType == Packet.SMART_RESULT_TYPE )
                                                         {
                                                                 synchronized ( firstThread )
                                                                 {                                                                                                                                              
                                                                         if ( firstThread.getState() != Thread.State.TERMINATED ) 
                                                                         {                                                                               
                                                                                 Threads.this.port = port;
                                                                                 firstThread.notify();                                                                                 
                                                                         }
                                                                        
                                                                         return;
                                                                 }        
                                                         } else if ( Packet.isExceptionType( packetType ) )
                                                                 throw new InternalError( smartResult.getException().toString() );
                                                 }
                                        }
                                        catch ( Exception e )
                                        {
                                                throw new InternalError( e.toString() );
                                        }                                  
                                }
                        }     
                        
                        public void start()
                        {
                                firstThread.start();
                                
                                while ( firstThread.getState() != Thread.State.TIMED_WAITING && 
                                             firstThread.getState() != Thread.State.TERMINATED ){}
                                
                                if ( firstThread.getState() != Thread.State.TERMINATED )                                                                           
                                        for ( int iPort = Application.BEGIN_PORT; iPort <= Application.END_PORT; iPort++ )
                                                new SecondThread( iPort ).start();
                        }
                }
                
                Threads t = new Threads();
                t.start();              
                return t.getPort();             
        }
        
        static boolean isRunning( InetAddress serverAddress, 
                                  final int serverPort ) throws Exception
        {     
                if ( serverAddress == null )
                        throw new IllegalArgumentException( "serverAddress é null" );
                
                if ( serverPort < 0 || serverPort > 0xFFFF )
                        throw new IllegalArgumentException( "serverPort não armazena um número de porta válida" );                

                RequestSocket rs = Network.createDefaultRequestSocket();
                Packet p = Packet.createSmartRequest( serverAddress, 
                                                      serverPort, 
                                                      new UpdateRequest( UpdateRequest.SERVER_RUNNING_REQUEST ), 
                                                      new byte[]{} );
                Packet packet = null;
                
                try{
                    packet = rs.request( p );
                }catch ( Throwable e ){
                    return false;
                }
                
                if ( packet == null )
                        return false;
               
                if ( packet.getPacketType() != Packet.SMART_RESULT_TYPE )
                        throw new InternalError( "packet.getPacketType() é diferente de Packet.SMART_RESULT_TYPE" );
                
                Request request = packet.getRequest();
                
                if ( ! ( request instanceof UpdateRequest ) )
                        throw  new InternalError( "! ( packet.getRequest() instanceof UpdateRequest )" );
                
                if ( request.getRequestID() != UpdateRequest.SERVER_RUNNING_REQUEST )
                        throw new InternalError( "request.getRequestID() != NetworkRequest.SERVER_RUNNING_REQUEST" );
                                
                return true;
                        
        }         
        
        public static boolean isCompatible(String firstVersion, String secondVersion)
        {
            String f = firstVersion.trim();
            String s = secondVersion.trim();
            String firstValue = String.valueOf(f.charAt(0)) + f.charAt(2);
            String secondValue = String.valueOf(s.charAt(0)) + s.charAt(2);
            return firstValue.equals(secondValue);
        }
        
        public void connect()
        {
                try
                {             
                        final String address = tfdServer.getText().trim();
                        final InetAddress inet;
                       
                        if ( address.equalsIgnoreCase("localhost") || address.equalsIgnoreCase("127.0.0.1") || address.equalsIgnoreCase("::1") ){
                            showMessageDialog(null,"Localhost não permitido, utiilze o ip verdadeiro!");
                            return;
                        }
                        
                        try{
                                inet = InetAddress.getByName( address );           
                        }
                        catch ( Exception e ){
                                showMessageDialog( null, "O endereço informado é inválido ou está inacessível!" );
                                return;
                        }
                        
                        dispose();   
                        final int serverPort = getServerPort( inet );                                
                        
                        if ( serverPort == -1 )
                        {
                                showMessageDialog( null, "O servidor não está em "
                                                         + "execução\n ou o endereço é inválido!" );
                                
                                if ( !IdentifierManager.isFirstApplicationInstance())
                                    System.exit(0);
                                else
                                    return; 
                        }
                        
                        String serverVersion = Application.getServerVersion(inet, serverPort);
                        
                        if ( serverVersion == null )
                        {
                            showMessageDialog(null,"Ocorreu um problema ao conectar ao servidor.");
                            
                            if ( !IdentifierManager.isFirstApplicationInstance())
                                System.exit(0);
                            else
                                return; 
                        }
                        
                        if (!isCompatible(Application.APPLICATION_VERSION, serverVersion))
                        {
                            showMessageDialog(null, "A versão do servidor " + inet.getHostName() + " é \n"
                                                  + "incompatível com a versão deste aplicativo.\n"
                                                  + "Versão do servidor: " + serverVersion + " Versão do cliente: " 
                                                  + Application.APPLICATION_VERSION);
                            
                            if ( !IdentifierManager.isFirstApplicationInstance())
                                System.exit(0);
                            else
                                return;
                        }
                        
                        JFrame f = new Login(  InetAddress.getByName( address ), 
                                               serverPort );
                        this.dispose();
                        f.setVisible( true );                        
                }
                catch ( Throwable t )
                {
                        showMessageDialog( null, t.getMessage() );
                }              
        }       
        
}