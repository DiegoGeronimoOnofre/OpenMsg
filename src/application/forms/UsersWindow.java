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

import application.db.ServerDB;
import application.forms.util.AbstractDefaultUserStatus;
import application.forms.util.ProgressBarWindow;
import application.forms.util.Useful;
import application.forms.util.UserStatus;
import application.forms.util.Users;
import application.net.Manager;
import application.net.Network;
import application.net.core.InvalidPacketFormatException;
import application.net.core.NetworkException;
import application.net.core.Packet;
import application.net.core.PacketSocket;
import application.objects.NickName;
import application.objects.St;
import application.objects.XStatus;
import application.requests.UpdateRequest;
import application.util.Convertible;
import application.net.core.Request;
import application.objects.Status;
import application.objects.XMessage;
import application.objects.Bool;
import application.objects.Friends;
import application.objects.Invitation;
import application.requests.InvitationRequest;
import application.requests.MessageRequest;
import application.util.Blocker;
import application.util.Core;
import application.util.Executable;
import application.util.Queue;
import application.Application;
import application.objects.TypingText;

import java.awt.BorderLayout;
import java.awt.Point;
import javax.swing.JFrame;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class UsersWindow extends JFrame
{
        private Users users = Useful.createDefaultUsers();
        
        private About about = new About();
        
        final JPanel pnlInfo = new JPanel()
        {
            private final JLabel lblInfo = new JLabel();
            
            {
                setPreferredSize(new Dimension(0, 15));
                setLayout(null);
                lblInfo.setLocation(0, 0);
                lblInfo.setSize(300, 15);
                this.add(lblInfo);
            }
            
            @Override
            public void paint(java.awt.Graphics g)
            {           
                int friendCount = users.getUserStatusCount();
                
                if ( friendCount == 0 )
                    lblInfo.setText("Nenhum amigo registrado!");
                else
                    lblInfo.setText( String.valueOf(friendCount) + " Amigo(s) registrado(s)" );
                
                super.paint(g);    
            }
        };  
        
        private JPanel pnlOptions = new JPanel();
        
        private JLabel lblStatus = new JLabel( "Status:" );
        
        public static final String ON_LINE = "Online";
        
        public static final String OFF_LINE = "Offline";      
        
        private JComboBox cbxStatus;
        
        private final String nickName;
        
        private final InetAddress serverAddress;
        
        private final int serverPort;                        
        
        private final PacketSocket ps;
        
        private final Network network = new Network(); 
        
        private final JMenuBar menuBar = new JMenuBar();
        
        private class XList extends ArrayList<String>
        {
                protected final Object listLock = new Object();
                
                public Object getListLock()
                {
                        return listLock;
                }

                public boolean exists( String friend )
                {
                        synchronized ( listLock )
                        {
                                for ( int i = 0; i < size(); i++ )
                                {
                                        String nick = get( i );
                                        
                                        if ( nick.toLowerCase().trim().equals( friend.toLowerCase().trim() ) )
                                                return true;
                                }
                                
                                return false;
                        }
                }
                
                @Override
                public boolean add( String friend )
                {
                        synchronized ( listLock )
                        {
                                if ( friend == null )
                                        throw new IllegalArgumentException( "friend é null" );
                                
                                return super.add( friend );
                        }
                }  
                
                public String remove( String friend )
                {                        
                        synchronized ( listLock )
                        {
                                if ( friend == null )
                                        throw new IllegalArgumentException( "friend é null" );                                
                                
                                final int size = size();
                                
                                for ( int  i = 0; i < size; i++ )
                                {
                                        String f = get( i );

                                        if ( f.toLowerCase().trim().equals( friend.toLowerCase().trim() ) )
                                                return remove( i );
                                }
                                
                                return null;
                        }                        
                } 
                
                public void transfer( String friend )
                {
                        synchronized ( listLock )
                        {
                                if ( friend == null )
                                        throw new IllegalArgumentException( "friend é null" );

                                remove( friend );
                                UserStatus us = getUserStatus( friend );
                                addUS( us );  
                        }
                }                
                
        }
        
        private final XList invitationList = new XList();
        
        public List<String> getInvitationList()
        {
                return invitationList;
        }        
        
        private final XList newFriendsList = new XList();   
        
        private void addUS( final UserStatus us )
        {
                class Exe extends Executable
                {
                        @Override
                        public void run()
                        {
                                synchronized ( users.getDefaultUsersSync() )
                                {
                                        if ( us == null )
                                                throw new IllegalArgumentException( "us é null" );

                                        String nick = us.getNickName();
                                        UserStatus userStatus = users.getUserStatus( nick );

                                        if ( userStatus == null )
                                                users.addUserStatus( us );
                                        else
                                        {
                                                InetAddress address = us.getAddress();
                                                
                                                if ( address != null )
                                                        userStatus.setAddress( address );
                                                
                                                final int port = us.getPort();
                                                
                                                if ( port != -1 )
                                                        userStatus.setPort( port );
                                                
                                                userStatus.setStatus( us.getStatus() );
                                        }
                                }
                        }
                }
                
                Exe exe = new Exe();
                addUSBlocker.execute( exe );
                exe.expect();               
        }
        
        private void logoff()
        {
                try
                {
                        Packet p = Packet.forUpload( serverAddress,
                                                     serverPort,
                                                     new UpdateRequest( UpdateRequest.LOGOFF_REQUEST )
                                                                             );

                        Network net = new Network();
                        Status status = new Status();
                        status.setNickName( nickName );
                        final int localPort = ps.getLocalPort();
                        status.setMainPort( localPort );
                        net.getUploadManager().add( p );
                        net.upload( p, status.toBytes() );
                }
                catch ( Exception e )
                {
                        throw new InternalError( e.toString() );
                }            
        }
        
        private final Blocker messageBlocker = new Blocker();
        
        private final Blocker addUSBlocker = new Blocker();        
        
        private final MainThread mainThread = new MainThread();
        
        private void terminate()
        {
                logoff();

                if ( application.net.core.IdentifierManager.isIdentifierManager() )
                {
                        mainThread.terminate();
                        final Object lock = users.getDefaultUsersSync();

                        synchronized ( lock )
                        {
                                final int size = users.getUserStatusCount();

                                for ( int i = 0; i < size; i++ )
                                {
                                        UserStatus us = users.getUserStatus(i);
                                        Messenger messenger = ( ( NewUserStatus ) us ).getMessenger();
                                        messenger.dispose();
                                }
                        }

                        dispose();
                }
                else
                        System.exit(0);        
        }
        
        
        
        
        {              
                final int w = 400;
                final int h = 250;
                Point p = application.forms.util.Useful.getCenterPoint( w, h );
                setSize( w, h );
                setLocation( p );
                application.forms.util.Useful.setDefaultImageIcon( this );
                messageBlocker.init();
                addUSBlocker.init();
                
                final WindowAdapter windowHandler = new WindowAdapter()
                {
                        @Override
                        public void windowClosing( WindowEvent we )
                        {
                                terminate();
                        }
                };                
                
                addWindowListener( windowHandler );
                
                Container contentPane = getContentPane();
                contentPane.setLayout( new BorderLayout() );                    
                JScrollPane spn = new JScrollPane( ( ( Component ) users ), 
                                                      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                      JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );  
                
                contentPane.add( spn, BorderLayout.CENTER );               
                pnlOptions.setLayout( new BorderLayout() );               
                lblStatus.setPreferredSize( new Dimension( 80, 0 ) );
                pnlOptions.setPreferredSize( new Dimension( 0, 20 ) );
                pnlOptions.add( lblStatus, BorderLayout.WEST );
                contentPane.add( pnlOptions, BorderLayout.NORTH );    
                contentPane.add(pnlInfo,BorderLayout.SOUTH);
        }
        
        
        
        
        {
                setJMenuBar( menuBar );
                JMenu options = new JMenu( "Principal" );
                options.setMnemonic('P');
                JMenuItem mniNewFriend = new JMenuItem( "Novo(a) Amigo(a)" );
                JMenuItem mniExit = new JMenuItem( "Sair" );
                mniNewFriend.setMnemonic( 'N' );
                mniNewFriend.addActionListener( new ActionListener()
                {
                        @Override
                        public void actionPerformed( ActionEvent me )
                        {
                                FriendRegister friendRegister = new FriendRegister( UsersWindow.this );
                                friendRegister.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
                                friendRegister.setVisible( true );
                        }
                }
                );        
                
                mniExit.setMnemonic( 'S' );
                mniExit.addActionListener( new ActionListener()
                {
                        @Override
                        public void actionPerformed( ActionEvent me )
                        {
                                terminate();
                        }
                }
                );
                
                options.add( mniNewFriend );
                options.add( mniExit );                                    
                
                JMenu help = new JMenu("Ajuda");
                help.setMnemonic('A');
                JMenuItem mniAbout = new JMenuItem("Sobre");
                mniAbout.setMnemonic( 'S' );
                mniAbout.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent me)
                    {
                        about.setVisible(true);                                                         
                    }
                });
                
                JMenuItem mniManual = new JMenuItem("Manual");
                mniManual.setMnemonic('M');
                mniManual.addActionListener(new ActionListener()
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
                
                help.add(mniAbout);
                help.add(mniManual);
                menuBar.add( options );
                menuBar.add( help );
        }
        
        public UsersWindow( String nickName,
                            InetAddress serverAddress, 
                            final int serverPort,
                            final int status,
                            PacketSocket socket
                             ) throws Exception
        {                               
                if ( nickName == null )
                        throw new IllegalArgumentException( "nickName é null" );
                        
                if ( serverAddress == null )
                        throw new IllegalArgumentException( "serverAddress é null" );        
                
                if ( serverPort < 0 || serverPort > 0xFFFF )
                        throw new IllegalArgumentException( "serverPort não armazena um número de porta válida" );    
                
                if ( socket == null )
                        throw new IllegalArgumentException( "socket é null" );                              
                 
                this.nickName = AbstractDefaultUserStatus.prepareNickName( nickName );
                setTitle( Application.APPLICATION_NAME + "-" + this.nickName );
                this.serverAddress = serverAddress;
                this.serverPort = serverPort;  
                this.ps = socket;
                final Object[] statusOptions;
                
                if ( status  == UserStatus.ON_LINE )
                       statusOptions = new Object[]{ ON_LINE, OFF_LINE };
                else if ( status == UserStatus.OFF_LINE )
                       statusOptions = new Object[]{ OFF_LINE, ON_LINE };
                else
                        throw new InternalError();
                
                cbxStatus = new JComboBox( statusOptions );                   
                cbxStatus.setEditable( false );              
                cbxStatus.setPreferredSize( new Dimension( 100, 0 ) );
                cbxStatus.addItemListener( new ItemListener()
                {                                                         
                        @Override
                        public void itemStateChanged( ItemEvent ie )
                        {              
                                if ( ie.getStateChange() == ItemEvent.DESELECTED )
                                        return;                                         
                                
                                final int status;
                                Object item = ie.getItem();
                                
                                if ( item.equals( ON_LINE ) )
                                        status = UserStatus.ON_LINE;
                                else if ( item.equals( OFF_LINE ) )
                                        status = UserStatus.OFF_LINE;
                                else
                                        throw new InternalError();                               
                                
                                try
                                {
                                        Status st = new Status();
                                        st.setNickName( UsersWindow.this.nickName );
                                        st.setStatus( status );
                                        st.setMainPort( ps.getLocalPort() );
                                        Packet p = Packet.forUpload( UsersWindow.this.serverAddress, 
                                                                     UsersWindow.this.serverPort, 
                                                                     new UpdateRequest( UpdateRequest.STATUS_UPDATE_REQUEST )
                                                                                             );

                                        Network net = new Network();
                                        net.getUploadManager().add( p );
                                        final boolean updated = net.upload( p, st.toBytes() );    
                                        
                                        if ( ! updated )
                                        {
                                                JOptionPane.showMessageDialog( null, "A conexão com o servidor foi perdida. "
                                                                                     + "\nO " + Application.APPLICATION_NAME + " será interrompido." );
                                                System.exit( 0 );
                                        }
                                }
                                catch ( Exception e )
                                {
                                        throw new InternalError( e.toString() );
                                }
                        }
                } );           
                
                pnlOptions.add( cbxStatus, BorderLayout.CENTER );                
        }       
        
        private final Object loadFriendsSync = new Object();
        
        private boolean loading = true;
        
        private class LoadFriendsThread extends Thread
        {                       
                private final ProgressBarWindow progress = new ProgressBarWindow();
                
                public LoadFriendsThread()
                {
                        JProgressBar pbrProgress = progress.getProgressBar();
                        pbrProgress.setStringPainted( true );
                        pbrProgress.setString( "Carregando..." );
                }
                
                public ProgressBarWindow getProgressBarWindow()
                {
                        return progress;
                }
                
                @Override
                public void run()
                {
                        synchronized ( loadFriendsSync )
                        {
                                try
                                {               
                                        Friends fs = ServerDB.getFriends( serverAddress, 
                                                                          serverPort, 
                                                                          nickName );

                                        if ( fs == null )
                                        {
                                                JOptionPane.showMessageDialog( null, "Falha de conexão ao fazer o login!" );     
                                                System.exit( 0 );   
                                        }

                                        List<String> friends = fs.getFriends();   
                                        final int inc;
                                        final int size = friends.size();

                                        if ( size == 0 )
                                                inc = 0;
                                        else if (  100 >= size )
                                                inc = 100 / size;
                                        else
                                                inc = size / 100;

                                        final JProgressBar pbrProgress = progress.getProgressBar();
                                        int count = 0;

                                        for ( int i = 0; i < friends.size(); i++ )
                                        {                                             
                                                final int currentValue = pbrProgress.getValue();

                                                class Runn implements Runnable
                                                {
                                                        private final int value;

                                                        public Runn( final int value )
                                                        {
                                                                this.value = value;
                                                        }

                                                        @Override
                                                        public void run()
                                                        {
                                                                pbrProgress.setValue( value );                                                                       
                                                        }
                                                }

                                                if ( 100 >= size )
                                                {
                                                        for ( int j = currentValue; j < currentValue + inc; j++ )
                                                        {
                                                                try
                                                                {
                                                                        Thread.sleep( 10l );
                                                                        SwingUtilities.invokeAndWait( new Runn( j ) );
                                                                }
                                                                catch ( Exception e )
                                                                {}
                                                        }
                                                }
                                                else if ( count % inc == 0 )
                                                        SwingUtilities.invokeAndWait( new Runn( currentValue + 1 ) );

                                                String nick = friends.get( i );
                                                UserStatus us = getUserStatus( nick );                                                      
                                                addUS( us );                                    
                                                count++;      
                                        }                                        

                                        pbrProgress.setValue( 100 );
                                        Thread.sleep( 100l );
                                        progress.dispose();                     
                                        showGUI();                                      
                                }
                                catch ( Throwable e )
                                {
                                        JOptionPane.showMessageDialog( null, e );
                                        System.exit( 0 );
                                }
                                
                                Executable exe = new Executable()
                                {
                                 
                                        @Override
                                        public void run()
                                        {
                                                addAllMsg();              
                                                loading = false;
                                        }
                                };
                                
                                messageBlocker.execute( exe );
                                exe.expect();
                                loadFriendsSync.notifyAll();
                        }
                }
        }         
        
        private void addAllMsg()
        {
                while ( ! messageQueue.isEmpty() )
                {
                        XMessage msg = messageQueue.remove();
                        addMsg( msg );
                }
        }
        
        private Queue<XMessage> messageQueue = new Queue<XMessage>();

        private void addMsg( XMessage msg )
        {
                String sender = msg.getSender();
                
                if ( newFriendsList.exists( sender ) )
                        newFriendsList.transfer( sender );

                if ( ! loading )
                        updateGUI();
                                                                                                                         
                NewUserStatus us = ( NewUserStatus ) users.getUserStatus( sender );                                                                                               

                if ( us != null )
                {
                        if ( us.getAddress() == null || us.getPort() == -1 )
                        {
                                JOptionPane.showMessageDialog( UsersWindow.this, "Um erro fatal ocorreu ao receber uma mensagem.\n"
                                                                                + "Desculpe o inconveniente.\n"
                                                                                + Application.APPLICATION_NAME + " será interrompido!" );
                                System.exit( 0 );
                        }
                        
                        Messenger m = us.getMessenger();
                        final Object lock = m.getLock();
                        
                        synchronized ( lock )
                        {
                                if ( ! m.isInit() )                                                                                              
                                        m.init( nickName, us );   
                        }

                        if ( ! m.isShowing() )
                                m.setVisible( true );

                        m.addMessage( sender, msg.getMessage() );
                }  
        }
        
        public void showGUI()
        {
                Runnable func = new Runnable()
                {
                        @Override
                        public void run()
                        {         
                                validate();         
                                setVisible(true);
                                repaint();
                        }
                };

                SwingUtilities.invokeLater( func );  
        }
        
        public void updateGUI()
        {
                validate();            
                repaint();
        }
        
        private UserStatus getUserStatus( String nick )
        {
                try
                {
                        NickName n = new NickName();
                        n.setNickName( nick );
                        Packet p = Packet.forRequestObject( serverAddress, 
                                                            serverPort, 
                                                            new UpdateRequest( UpdateRequest.USER_STATUS_REQUEST ) );

                        Convertible convertible = Network.requestObject( p, n );

                        if ( convertible == null )
                        {
                                JOptionPane.showMessageDialog( null, "Falha de conexão ao atualizar o status de " + nick + ""
                                                                    + "\n O " + Application.APPLICATION_NAME + " será interrompido!" );
                                System.exit( 0 );
                        }              

                        if ( convertible instanceof XStatus )
                        {
                                XStatus inf = ( XStatus ) convertible;
                                return createUserStatus( inf );
                        }
                        else if ( convertible instanceof St )
                        {
                                St st = ( St ) convertible;
                                return createUserStatus(   st.getNickName(),
                                                           null,
                                                           -1,
                                                           st.getStatus() );
                        }
                        else
                                throw new InternalError();
                }
                catch ( Exception e )
                {
                        throw new InternalError( e.toString() );
                }
        }
        
        public UserStatus createUserStatus(  String nick, 
                                             InetAddress address, 
                                             final int port, 
                                             final int status )
        {                        
                final char sep = java.io.File.separatorChar;
                String path = application.Application.getMainPath() + sep + "imgs";
                String user = path + sep + "User.png";                
                String onLine = path + sep + "OnLine.png";                
                String offLine = path + sep +"OffLine.png";
                UserStatus result = new NewUserStatus(     this,
                                                           nickName,
                                                           new ImageIcon( user ), 
                                                           new ImageIcon( onLine ), 
                                                           new ImageIcon( offLine ), 
                                                           nick, 
                                                           address,
                                                           port,
                                                           status );
                return result; 
        }         
        
        private UserStatus createUserStatus( XStatus inf ) throws Exception
        {
                if ( inf == null )
                        throw new IllegalArgumentException( "inf é null" );                
                
                if ( inf.getAddress() == null && inf.getMainPort() != -1 )
                        throw new IllegalArgumentException( " inf.getAddress() == null && inf.getMainPort() != -1" );

                if ( inf.getAddress() != null && inf.getMainPort() == -1 )
                        throw new IllegalArgumentException( "inf.getAddress() != null && inf.getMainPort() == -1" );                         
                
                final InetAddress address;
                
                if ( inf.getAddress() == null )
                        address = null;
                else
                        address = InetAddress.getByAddress( inf.getAddress() );
                
                return createUserStatus(  inf.getNickName(), 
                                          address, 
                                          inf.getMainPort(), 
                                          inf.getStatus() );
        }
        
        public void checkConnection( UserStatus friend )
        {
                if ( friend == null )
                        throw new IllegalArgumentException( "friend é null" );
                
                try
                {
                        Packet p = Packet.forUpload( serverAddress,
                                                     serverPort,
                                                     new UpdateRequest( UpdateRequest.CONNECTED_USER_REQUEST )  );

                        Network net = new Network();
                        XStatus xst = XStatus.UserStatusToXStatus( friend );
                        xst.setRecipient( UsersWindow.this.nickName );
                        byte[] information = xst.toBytes();
                        net.getUploadManager().add( p );
                        net.upload( p, information );
                }
                catch ( Exception e )
                {
                        throw new InternalError( e.toString() );
                }
        }
        
        private interface DownloadFunction
        {
                public void run( byte[] information );
        }        
        
        private class DownloadThread extends Thread
        {
                private final Network network;
                
                private final Packet p;
                
                private final DownloadFunction function;
                
                public DownloadThread( Network network, 
                                       Packet p, 
                                       DownloadFunction function )
                {
                        this.network = network;
                        this.p = p;
                        this.function = function;
                }
                
                @Override
                public void run()
                {
                        try
                        {
                                byte[] information = network.download( p );
                                
                                if ( information != null )
                                        function.run( information );
                        }
                        catch ( Exception e )
                        {
                                throw new InternalError( e.toString() );
                        }                                             
                }
        }   
        
        final Manager downloadManager = network.getDownloadManager();
        final Manager sendObjectManager = network.getSendObjectManager();

        private class MainThread extends Network.Th
        {   
                @Override
                public void run()
                {                                                          
                        synchronized ( MainThread.this )
                        {
                                this.notify();
                        }                                

                        while ( true )
                        {
                                try
                                {                        
                                        if ( getStatus() == Network.Th.TERMINATED )
                                            return;
                                    
                                        final Packet p = ps.receive();                                             
                                        
                                        if ( getStatus() == Network.Th.TERMINATED )
                                            return;
                                        
                                        Request request = p.getRequest();

                                        if ( p.getPacketType() == Packet.SMART_REQUEST_TYPE )
                                        {
                                                if ( request instanceof UpdateRequest )
                                                {
                                                        if ( request.getRequestID() == UpdateRequest.DISCONNECT_REQUEST )
                                                        {
                                                                new Thread()
                                                                {
                                                                        @Override
                                                                        public void run()
                                                                        {
                                                                                try
                                                                                {
                                                                                        Packet packet = Packet.createSmartResult( p.getIdentifier(),
                                                                                                                                  p.getAddress(),
                                                                                                                                  p.getPort(),
                                                                                                                                  p.getRequest(),
                                                                                                                                  new byte[]{} );

                                                                                        try{
                                                                                            ps.send( packet );        
                                                                                        }
                                                                                        catch ( Exception e ){
                                                                                            System.out.println(e.getClass().getName() + "-" + e.getMessage());
                                                                                        }
                                                                                        String nick = new String( p.getData() );
                                                                                        
                                                                                        if ( UsersWindow.this.nickName.toLowerCase().trim().equals( nick.toLowerCase().trim() ) )
                                                                                        {
                                                                                                JOptionPane.showMessageDialog( UsersWindow.this, "Esta instância de aplicativo " + Application.APPLICATION_NAME + " será\n"
                                                                                                                                               + "interrompida, por a conexão com o servidor foi perdida!" );
                                                                                                System.exit( 0 );
                                                                                        }
                                                                                }
                                                                                catch ( Exception e )
                                                                                {
                                                                                        throw new InternalError( e.toString() );
                                                                                }
                                                                        }
                                                                }.start();
                                                        }
                                                        else if ( request.getRequestID() == UpdateRequest.SCREEN_UPDATE_REQUEST )
                                                        {
                                                                new Thread()
                                                                {
                                                                        @Override
                                                                        public void run()
                                                                        {
                                                                                try
                                                                                {
                                                                                        Packet packet = Packet.createSmartResult( p.getIdentifier(),
                                                                                                                                  p.getAddress(),
                                                                                                                                  p.getPort(),
                                                                                                                                  p.getRequest(),
                                                                                                                                  new byte[]{} );

                                                                                        ps.send( packet );      
                                                                                        String nick = new String( p.getData() ); 
                                                                                        newFriendsList.transfer( nick );        

                                                                                        if ( ! loading )                                                                                                       
                                                                                                updateGUI();
                                                                                }
                                                                                catch ( Exception e  )
                                                                                {
                                                                                        throw new InternalError( e.toString() );
                                                                                }
                                                                        }
                                                                }.start();
                                                        }
                                                        else if ( request.getRequestID() == UpdateRequest.SERVER_CLOSING_REQUEST )
                                                        {
                                                                Packet packet = Packet.createSmartResult( p.getIdentifier(),
                                                                                                          p.getAddress(),
                                                                                                          p.getPort(),
                                                                                                          p.getRequest(),
                                                                                                          new byte[]{} );

                                                                ps.send( packet );                                                                       
                                                                JOptionPane.showMessageDialog( UsersWindow.this, Application.APPLICATION_NAME + " será interrompido, \n"
                                                                                                                 + "por que o servidor foi finalizado!" );

                                                                System.exit( 0 );
                                                        }                                                               
                                                        else if ( request.getRequestID() == UpdateRequest.CONNECTED_USER_REQUEST )
                                                        {
                                                                new Thread()
                                                                {
                                                                        @Override
                                                                        public void run()
                                                                        {
                                                                                try
                                                                                {
                                                                                        String nick = new String( p.getData() );
                                                                                        String preparedNick = AbstractDefaultUserStatus.prepareNickName( nick );
                                                                                        final byte result;

                                                                                        if ( preparedNick.equals( nickName ) )
                                                                                                result = Core.toBooleanByte( true );
                                                                                        else
                                                                                                result = Core.toBooleanByte( false );

                                                                                        Packet packet = Packet.createSmartResult( p.getIdentifier(),
                                                                                                                                  p.getAddress(),
                                                                                                                                  p.getPort(),
                                                                                                                                  p.getRequest(),
                                                                                                                                  new byte[]{ result } );

                                                                                        ps.send( packet );
                                                                                }
                                                                                catch ( Exception e )
                                                                                {
                                                                                        throw new InternalError( e.toString() );
                                                                                }                                     
                                                                        }
                                                                }.start();
                                                        }    
                                                }
                                        }
                                        else if ( p.getPacketType() == Packet.SMART_RESULT_TYPE )
                                        { 
                                                if ( request instanceof InvitationRequest )
                                                {
                                                        if ( request.getRequestID() == InvitationRequest.INVITATION_REQUEST )
                                                        {                                                                                
                                                                class Function implements DownloadFunction
                                                                {
                                                                        @Override
                                                                        public void run( byte[] information )
                                                                        {
                                                                                synchronized ( loadFriendsSync )
                                                                                {                                                                                                                                                                                             
                                                                                        try
                                                                                        {
                                                                                                if ( loading )
                                                                                                        loadFriendsSync.wait();                                                                                                

                                                                                                Invitation invitation = ( Invitation ) Convertible.bytesToObject( information );
                                                                                                String user           = invitation.getUser();
                                                                                                String friend         = invitation.getFriend();
                                                                                                
                                                                                                if ( user.toLowerCase().trim().equals( nickName.toLowerCase().trim() ) )
                                                                                                {
                                                                                                        UserInvitation userInvitation = new UserInvitation( UsersWindow.this, friend );
                                                                                                        userInvitation.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
                                                                                                        
                                                                                                        synchronized ( invitationList.getListLock() )
                                                                                                        {                                                                                                                                                                                                                                        
                                                                                                                if ( invitationList.exists( friend ) )
                                                                                                                {
                                                                                                                        userInvitation.accept( false );
                                                                                                                        userInvitation.dispose();
                                                                                                                        invitationList.remove( friend );
                                                                                                                }
                                                                                                                else 
                                                                                                                {
                                                                                                                        final int result = ServerDB.linkExists( serverAddress, 
                                                                                                                                                                serverPort,
                                                                                                                                                                user,
                                                                                                                                                                friend );             
                                                                                                                        if ( result == ServerDB.TRUE )
                                                                                                                        {
                                                                                                                                userInvitation.accept( false );
                                                                                                                                userInvitation.dispose();
                                                                                                                                invitationList.remove( friend );                                                                                                                        
                                                                                                                        }
                                                                                                                        else if ( result == ServerDB.FALSE ^ 
                                                                                                                                  result == ServerDB.CONNECTION_ERROR )
                                                                                                                                userInvitation.setVisible( true );                                                                                                              
                                                                                                                }
                                                                                                        }
                                                                                                }
                                                                                        }
                                                                                        catch ( Exception e )
                                                                                        {
                                                                                                throw new InternalError( e.toString() );
                                                                                        }
                                                                                }
                                                                        }  
                                                                }

                                                                if ( downloadManager.add( p ) )
                                                                        new DownloadThread( network, p,  new Function() ).start();
                                                        }
                                                        else if ( request.getRequestID()  == InvitationRequest.INVITATION_ANSWER_REQUEST )
                                                        {
                                                                class Function implements DownloadFunction
                                                                {
                                                                        @Override
                                                                        public void run( byte[] information )
                                                                        {
                                                                                try
                                                                                {
                                                                                        Invitation invitation = ( Invitation ) Convertible.bytesToObject( information );
                                                                                        
                                                                                        if ( invitation.getUser().toLowerCase().trim().equals( nickName.toLowerCase().trim() ) )
                                                                                        {
                                                                                                final int answer = invitation.getAnswer();
                                                                                                String friend = invitation.getFriend();
                                                                                                
                                                                                                if ( answer == Invitation.ACCEPTED )                                                                                                                                                                                          
                                                                                                        newFriendsList.add( friend );                                                                                                
                                                                                                else if ( answer == Invitation.DECLINED )
                                                                                                {
                                                                                                        invitationList.remove( friend );
                                                                                                        JOptionPane.showMessageDialog(null, 
                                                                                                                                      nickName + ", " + friend + " não aceitou o seu convite!", 
                                                                                                                                      Application.APPLICATION_NAME + "-" + nickName, 
                                                                                                                                      JOptionPane.INFORMATION_MESSAGE);
                                                                                                }
                                                                                                else
                                                                                                        throw new InternalError( "answer != Invitation.ACCEPTED &&"
                                                                                                                               + " answer != Invitation.DECLINED" );
                                                                                        }
                                                                                }
                                                                                catch ( Exception e )
                                                                                {
                                                                                        throw new InternalError( e.toString() );
                                                                                }                                                                                        
                                                                        }
                                                                }

                                                                if ( downloadManager.add( p ) )
                                                                        new DownloadThread( network, p,  new Function() ).start();                                                                            
                                                        }
                                                }
                                                else if ( request instanceof UpdateRequest )
                                                {
                                                        if ( request.getRequestID() == UpdateRequest.TYPING_TEXT_REQUEST )
                                                        {
                                                            class Function implements DownloadFunction
                                                            {
                                                                @Override
                                                                public void run(byte[] information)
                                                                {
                                                                    synchronized ( loadFriendsSync )
                                                                    {
                                                                        try
                                                                        {
                                                                            if ( loading )
                                                                                loadFriendsSync.wait();
                                                                            
                                                                            TypingText typingText = (TypingText) Convertible.bytesToObject(information);
                                                                            String recipient = typingText.getRecipient().toLowerCase().trim();
                                                                            String mNickName = UsersWindow.this.nickName.toLowerCase().trim();

                                                                            if ( recipient.equals(mNickName) )
                                                                            {
                                                                                UserStatus us = users.getUserStatus(typingText.getSender());

                                                                                if ( us != null )
                                                                                {
                                                                                    Messenger messenger = ((NewUserStatus)us).getMessenger();
                                                                                    messenger.setTypingTextToLabel(typingText.isTypingText());  
                                                                                    messenger.validate();
                                                                                    messenger.repaint();
                                                                                }
                                                                            }
                                                                        }
                                                                        catch ( Exception e )
                                                                        {
                                                                            throw new InternalError(e.toString());
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            
                                                            if ( downloadManager.add( p ) )
                                                                        new DownloadThread( network, p,  new Function() ).start();
                                                        }
                                                        else if ( request.getRequestID() == UpdateRequest.STATUS_UPDATE_REQUEST )
                                                        {
                                                                class Function implements DownloadFunction
                                                                {                    
                                                                        @Override
                                                                        public void run( byte[] information )
                                                                        {
                                                                                synchronized ( loadFriendsSync )
                                                                                {                                                                                       
                                                                                        try
                                                                                        {                                                                                                                 
                                                                                                St status = ( St ) Convertible.bytesToObject( information );
                                                                                                String preparedRecipient = AbstractDefaultUserStatus.prepareNickName( status.getRecipient() );
                                                                                                
                                                                                                if ( nickName.equals( preparedRecipient ) )
                                                                                                {           
                                                                                                        newFriendsList.transfer( status.getNickName() );
                                                                                                        UserStatus us = users.getUserStatus( status.getNickName() );

                                                                                                        if ( us != null )
                                                                                                        {
                                                                                                                if ( status instanceof XStatus )
                                                                                                                {
                                                                                                                        XStatus xst = ( XStatus ) status;
                                                                                                                        us.setAddress( InetAddress.getByAddress( xst.getAddress() ) );
                                                                                                                        us.setPort( xst.getMainPort());
                                                                                                                }

                                                                                                                us.setStatus( status.getStatus() );

                                                                                                                if ( ! loading )
                                                                                                                        updateGUI();  
                                                                                                        }
                                                                                                }
                                                                                        }
                                                                                        catch ( Exception e )
                                                                                        {
                                                                                                throw new InternalError( e.toString() );
                                                                                        }   
                                                                                }
                                                                        }
                                                                }

                                                                if ( downloadManager.add( p ) )
                                                                        new DownloadThread( network, p,  new Function() ).start();
                                                        }
                                                }
                                                else if ( request instanceof MessageRequest )
                                                {
                                                        if ( request.getRequestID() == MessageRequest.SEND_MESSAGE_REQUEST )            
                                                        {
                                                                class Function implements application.net.Run
                                                                {
                                                                        private Convertible obj = null;

                                                                        public Convertible getObj()
                                                                        {
                                                                                return obj;
                                                                        }

                                                                        @Override
                                                                        public Convertible run( Convertible obj )
                                                                        {                                                                                                                                                                           
                                                                                try
                                                                                {
                                                                                        XMessage mn = ( XMessage ) obj;
                                                                                        String recipient = mn.getRecipient();
                                                                                        String sender = mn.getSender();
                                                                                        Bool bool = new Bool();                                                                                                

                                                                                        if ( recipient.toLowerCase().trim().equals( UsersWindow.this.nickName.toLowerCase().trim() ) )
                                                                                        {            
                                                                                                UserStatus userS  = users.getUserStatus( sender );
                                                                                                boolean exists = newFriendsList.exists( sender );
                                                                                                
                                                                                                if ( userS == null && ! exists )
                                                                                                {
                                                                                                        final int result = ServerDB.isFriend( serverAddress, 
                                                                                                                                                serverPort, 
                                                                                                                                                recipient, 
                                                                                                                                                sender );
                                                                                                        if ( result == ServerDB.TRUE )
                                                                                                        {
                                                                                                                invitationList.transfer( sender );
                                                                                                                bool.setBoolean( true );
                                                                                                                this.obj = obj;
                                                                                                        }
                                                                                                        else if ( result == ServerDB.FALSE ^ 
                                                                                                                  result == ServerDB.CONNECTION_ERROR )
                                                                                                                bool.setBoolean( false );
                                                                                                        
                                                                                                }
                                                                                                else    
                                                                                                {
                                                                                                        bool.setBoolean( true ); 
                                                                                                        this.obj = obj;
                                                                                                }
                                                                                        }
                                                                                        else
                                                                                                bool.setBoolean( false );

                                                                                        return bool;
                                                                                }
                                                                                catch ( Exception e )
                                                                                {
                                                                                        throw new InternalError( e.toString() );
                                                                                }
                                                                        }
                                                                }

                                                                if ( sendObjectManager.add( p ) )
                                                                {
                                                                        new Thread()
                                                                        {
                                                                                @Override
                                                                                public void run()
                                                                                {
                                                                                        try
                                                                                        {
                                                                                                Function func = new Function();
                                                                                                boolean result = network.sendObject( p, func );                                                                                                                                                                                   

                                                                                                if ( result )
                                                                                                {
                                                                                                        Convertible obj = func.getObj();

                                                                                                        if ( obj != null )
                                                                                                        {
                                                                                                                final XMessage mn = ( XMessage ) obj;                                                                                                                                                                                                                                                                                                                                                      
                                                                                                                Executable exe = new Executable()
                                                                                                                {
                                                                                                                        @Override
                                                                                                                        public void run()
                                                                                                                        {
                                                                                                                                if ( loading )
                                                                                                                                        messageQueue.add( mn );
                                                                                                                                else                                                                                                                      
                                                                                                                                        addMsg( mn );
                                                                                                                        }
                                                                                                                };

                                                                                                                messageBlocker.execute( exe );
                                                                                                        }
                                                                                                }    
                                                                                        }                                                                                                       
                                                                                        catch ( Exception e )
                                                                                        {
                                                                                                throw new InternalError( e.toString() );
                                                                                        }
                                                                                }
                                                                        }.start();
                                                                }
                                                        }
                                                }
                                        }
                                }
                                catch ( InvalidPacketFormatException e )
                                {}
                                catch ( Exception e )
                                {                                               
                                        if ( e instanceof NetworkException )
                                                throw new InternalError( e.toString() );                                                

                                        if ( ps.isClosed() )
                                                return;
                                }
                        }
                }
        }         
        
        private void initialize() throws Exception
        {                                                                                                
                mainThread.setPriority( Thread.MAX_PRIORITY );
                mainThread.setName( mainThread.getClass().getSimpleName() + "." + nickName );
                LoadFriendsThread lfThread = new LoadFriendsThread();                
                lfThread.setPriority( Thread.MIN_PRIORITY );
                lfThread.setName( lfThread.getClass().getSimpleName() );               

                synchronized ( mainThread )
                {
                        mainThread.start();      
                        mainThread.wait();
                }                

                ProgressBarWindow progress = lfThread.getProgressBarWindow();
                progress.setVisible( true );
                lfThread.start();    
        }
        
        private final Object initSync = new Object();
        
        private boolean initialized = false;
        
        public void init() throws Exception
        {
                synchronized ( initSync )
                {                         
                        if ( ! initialized )
                        {
                                initialized = true;                                
                                initialize();                                                    
                        }
                        else
                                throw new Exception( "UsersWindow já está em execução." );
                }
        }
        
        public String getNickName()
        {
                return nickName;
        }
        
        public int getStatus()
        {
                if ( cbxStatus.getSelectedItem().equals( ON_LINE ) )
                        return UserStatus.ON_LINE;
                else
                        return UserStatus.OFF_LINE;
        }   
 
        public InetAddress getServerAddress()
        {
                return  serverAddress;
        }

        public int getServerPort()
        {
                return  serverPort;
        }
}