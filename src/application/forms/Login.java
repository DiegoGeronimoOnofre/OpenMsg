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

import application.forms.util.UserStatus;
import application.forms.util.NicknameHandler;
import application.forms.util.PasswordHandler;
import application.forms.util.Useful;
import application.net.Network;
import application.net.core.Packet;
import application.net.core.PacketSocket;
import application.objects.LogonResult;
import application.requests.UpdateRequest;

import java.awt.Container;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import static javax.swing.JOptionPane.showMessageDialog;

public class Login extends JFrame
{
        private JLabel lblStatus = new JLabel( "Status" );

        private JComboBox cbxStatus = new JComboBox( new Object[]{ UsersWindow.ON_LINE, 
                                                                   UsersWindow.OFF_LINE } );
        
        private JLabel lblNickName = new JLabel( "Nickname" );
       
        private JTextField tfdNickName = new JTextField();
        
        private JLabel lblPassword = new JLabel( "Senha" );
        
        private JPasswordField pwfPassword = new JPasswordField();
        
        private JButton btnLogin = new JButton( "Entrar" );
        
        private JButton btnNewRegister = new JButton( "Novo" );
        
        private JButton btnExit = new JButton( "Sair" );
        
        private final InetAddress serverAddress; 
        
        private final int serverPort;
        
        private void terminate()
        {
                if ( application.net.core.IdentifierManager.isIdentifierManager() )
                        dispose();
                else
                        System.exit(0);        
        }
        
        private class WindowHandler extends WindowAdapter
        {
                @Override
                public void windowClosing( WindowEvent we )
                {
                        terminate();
                }
        }
        
        private class KeyHandler extends KeyAdapter
        {
            @Override
            public void keyTyped(KeyEvent ke)
            {
                if ( ke.getKeyChar() == KeyEvent.VK_ENTER )
                    Login.this.validateAndLogin();
            }
        }
        
        {
                final int w = 259;
                final int h = 220;
                setSize( w, h );
                Point p = application.forms.util.Useful.getCenterPoint( w, h );
                setLocation( p );
                setTitle( "Login" );
                setResizable( false );
                application.forms.util.Useful.setDefaultImageIcon( this );
                addWindowListener( new WindowHandler() );
                Session session = null;
                
                try{
                    session = Session.getSession();
                }
                catch ( Throwable t ){}
                
                lblStatus.setSize( 100, 20 );
                lblStatus.setLocation( 20, 10 );
                
                cbxStatus.setEditable( false );
                cbxStatus.setSize( 100, 20 );
                cbxStatus.setLocation( 20, 30 );
                cbxStatus.addKeyListener(new KeyHandler());

                if ( (session != null) && session.getStatus().equals(UsersWindow.ON_LINE) )
                    cbxStatus.setSelectedIndex(0);
                else if ( (session != null) && session.getStatus().equals(UsersWindow.OFF_LINE)) 
                    cbxStatus.setSelectedIndex(1);
                else
                    cbxStatus.setSelectedIndex(0);   
                
                lblNickName.setSize( 100, 20 );
                lblNickName.setLocation( 20, 50 );
                
                tfdNickName.setSize( 213, 20 );
                tfdNickName.setLocation( 20, 70 );
                tfdNickName.addKeyListener( new NicknameHandler() );
                tfdNickName.addKeyListener(new KeyHandler());

                if ( (session != null) && !session.getNickName().equals("") )
                    tfdNickName.setText(session.getNickName());
                
                lblPassword.setSize( 100, 20 );
                lblPassword.setLocation( 20, 90 );
                
                pwfPassword.setSize( 213, 20 );
                pwfPassword.setLocation( 20, 110 );       
                pwfPassword.addKeyListener( new PasswordHandler() );
                pwfPassword.addKeyListener(new KeyHandler());
                
                if ( (session != null) && ! session.getPassword().equals("") )
                    pwfPassword.setText(session.getPassword());
                
                btnLogin.setSize( 70, 20 );
                btnLogin.setLocation( 20, 140 );
                btnLogin.addKeyListener(new KeyHandler());
                btnLogin.addMouseListener( new MouseAdapter()
                {                  
                        @Override
                        public void mouseClicked( MouseEvent me )
                        {
                                if ( me.getButton() == MouseEvent.BUTTON1 )
                                {
                                    Login.this.validateAndLogin();
                                }
                        }
                } );
                
                btnNewRegister.setSize( 70, 20 );
                btnNewRegister.setLocation( 91, 140 );          
                btnNewRegister.addMouseListener( new MouseAdapter()
                        { 
                               @Override
                                public void mouseClicked( MouseEvent me )
                                {
                                        if ( me.getButton() != MouseEvent.BUTTON1 )
                                                return;
                                        
                                        try
                                        {
                                                new UserRegister( serverAddress, serverPort ).setVisible( true );
                                                Login.this.dispose();
                                        }
                                        catch ( Exception e )
                                        {
                                                throw new InternalError( "btnNewRegister.MouseClicked()" );
                                        }
                                } 
                        } );                 
                
                btnExit.setSize( 70, 20 );
                btnExit.setLocation( 162, 140 );
                btnExit.addMouseListener( new MouseAdapter()
                        { 
                               @Override
                                public void mouseClicked( MouseEvent me )
                                {
                                        if ( me.getButton() != MouseEvent.BUTTON1 )
                                                return;                                        
                                        
                                        try
                                        {
                                                terminate();    
                                        }
                                        catch ( Exception e )
                                        {
                                                throw new InternalError( "btnCancel.MouseClicked()" );
                                        }
                                } 
                        } );                
                                       
                Container contentPane = getContentPane();
                contentPane.setLayout( null );
                contentPane.add( lblStatus );
                contentPane.add( cbxStatus );
                contentPane.add( lblNickName );
                contentPane.add( tfdNickName );
                contentPane.add( lblPassword );
                contentPane.add( pwfPassword );
                contentPane.add( btnLogin );
                contentPane.add( btnNewRegister );
                contentPane.add( btnExit );
        }
        
        public int getStatus()
        {
                if ( cbxStatus.getSelectedItem().equals( UsersWindow.ON_LINE ) )
                        return UserStatus.ON_LINE;
                else
                        return UserStatus.OFF_LINE;
        }        
        
        private void validateAndLogin()
        {
            try
            {
                String nickNameText = tfdNickName.getText();
                
                if ( !Useful.isValid(nickNameText) )
                {
                    JOptionPane.showMessageDialog(Login.this, 
                                                  "O nickname digitado possui caractere(s) inválido(s)" );                    
                    return;
                }                
                
                if ( nickNameText.equals( "" ) )
                {
                        showMessageDialog( Login.this, "Nickname está vazio!" );
                        return;
                }

                char[] pword = pwfPassword.getPassword();
                
                if ( !Useful.isValid(new String(pword)) )
                {
                    JOptionPane.showMessageDialog(Login.this, "A senha digitada possui caractere(s) inválido(s).");
                    return;
                }
                
                if ( new String( pword ).equals( "" ) )
                {
                        showMessageDialog( Login.this, "A Senha está vazia!" );
                        return;
                }

                if ( ! ServerConnect.isRunning( serverAddress, serverPort ) )
                {
                       showMessageDialog( Login.this, "Falha de conexão ao fazer o login!" );
                       return;
                }     
                
                login();
            }
            catch ( Exception e )
            {
                throw new InternalError(e.toString());
            }
        }
        
        private void login() throws Exception
        {  
                final long maxT = application.Application.getDefaultMaxTime( serverAddress, serverPort );
                
                if ( maxT == -1 ){
                        JOptionPane.showMessageDialog( Login.this, 
                                                       "Falha de conexão ao fazer o login!" );
                        return;                
                }
                
                Packet p = Packet.forRequestObject( serverAddress, 
                                                    serverPort, 
                                                    new UpdateRequest( UpdateRequest.LOGON_REQUEST )
                                                   );                         
                PacketSocket ps = new PacketSocket( Network.DEFAULT_MAX_LENGTH );
                application.objects.Logon logon = new application.objects.Logon();                                             
                logon.setNickName( tfdNickName.getText().toLowerCase().trim() );
                logon.setStatus( Login.this.getStatus() );                    
                logon.setMainPort( ps.getLocalPort() );    
                logon.setPassword( pwfPassword.getPassword() );
                final long maxTime = Network.DEFAULT_TRY_COUNT * Network.DEFAULT_INTERVAL;
                LogonResult logonResult = ( LogonResult ) Network.requestObject( p, logon, maxTime + maxTime + maxT );                   
                
                if ( logonResult != null )
                {
                        if ( logonResult.isConnected() )
                            
                            
                        {
                                Login.this.dispose();

                                try{
                                    Session session = Session.getSession();
                                    session.setStatus((String) cbxStatus.getSelectedItem());
                                    session.setNickName(tfdNickName.getText().trim());
                                    session.setPassword(new String(pwfPassword.getPassword()));
                                    session.setServer(serverAddress.getHostName());
                                    session.store();
                                }
                                catch ( Throwable t ){}
                                    
                                UsersWindow  usersWindow = new UsersWindow( tfdNickName.getText(), 
                                                                            serverAddress, 
                                                                            serverPort,
                                                                            getStatus(),
                                                                            ps );                

                                usersWindow.init();        
                        }
                        else
                        {
                                JOptionPane.showMessageDialog( Login.this, logonResult.getMessage() );
                                ps.close();                        
                        }
                }
                else
                {
                        JOptionPane.showMessageDialog( Login.this, "Falha de conexão ao fazer o login!" );
                        ps.close();
                }
        }
        
        public Login( InetAddress serverAddress, 
                      final int serverPort )
        {
                if ( serverAddress == null )
                        throw new IllegalArgumentException( "serverAddress é null" );
                
                if ( serverPort < 0 || serverPort > 0xFFFF )
                        throw new IllegalArgumentException( "port não armazena um número de porta válida" );                

                this.serverAddress = serverAddress;
                this.serverPort = serverPort;
        }
        
        public Login(InetAddress serverAddress, 
                      final int serverPort,
                      String nickName,
                      String password)
        {
            this(serverAddress, serverPort);

            if ( nickName != null )
                tfdNickName.setText(nickName);
            
            if ( password != null )
                pwfPassword.setText(password);
        }
}