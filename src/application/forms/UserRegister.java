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

import application.forms.util.AbstractDefaultUserStatus;
import application.forms.util.NicknameHandler;
import application.forms.util.PasswordHandler;
import application.forms.util.Useful;
import application.net.Network;
import application.net.core.Packet;
import application.net.core.Request;
import application.net.core.RequestSocket;
import application.objects.Register;
import application.objects.RegisterResult;
import application.requests.RegisterRequest;
import application.util.Convertible;
import application.util.Core;

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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import static javax.swing.JOptionPane.showMessageDialog;

class UserRegister extends JFrame
{
        private JLabel lblNickName = new JLabel( "Nickname" );
       
        private JTextField tfdNickName = new JTextField();
        
        private JLabel lblPassword = new JLabel( "Senha" );
        
        private JPasswordField pwfPassword = new JPasswordField();
        
        private JLabel lblConfirmPassword = new JLabel( "Confirmação de senha" );
        
        private JPasswordField pwfConfirmPassword = new JPasswordField();
        
        private JButton btnRegister = new JButton( "Cadastrar" );
        
        private JButton btnClose = new JButton( "Fechar" );
        
        private final InetAddress serverAddress; 
        
        private final int serverPort;
        
        private class KeyHandler extends KeyAdapter
        {
            @Override
            public void keyTyped(KeyEvent me)
            {
                if ( me.getKeyChar() == (char) KeyEvent.VK_ENTER )
                    UserRegister.this.validateAndRegister();
            }
        }
        
        /* Esta função retorna -1 se ocorrer falha na conexão         
         */
        
        private long getRegisterMaxTime()
        {
                try
                {
                      Packet first = Packet.createSmartRequest( serverAddress, 
                                                                serverPort, 
                                                                new RegisterRequest( RegisterRequest.REGISTER_MAX_TIME_REQUEST ), 
                                                                new byte[]{} ); 
                      
                      RequestSocket rs = Network.createDefaultRequestSocket();
                      Packet second = rs.request( first );
                      
                      if ( second == null )
                              return -1;
                      
                      if ( second.getPacketType() != Packet.SMART_RESULT_TYPE )
                              throw new InternalError( "second.getPacketType() != Packet.SMART_RESULT_TYPE" );                      
                      
                      Request request = second.getRequest();
                      
                      if ( ! ( request instanceof RegisterRequest ) )
                              throw new InternalError( "! ( request instanceof RegisterRequest )" );
                      
                      if ( request.getRequestID() != RegisterRequest.REGISTER_MAX_TIME_REQUEST )
                              throw new InternalError( "request.getRequestID() != RegisterRequest.REGISTER_MAX_TIME_REQUEST" );
                      
                      byte[] data = second.getData();
                      
                      if ( data == null )
                              throw new InternalError( "data é null" );
                      
                      if ( data.length != Core.LONG_SIZE )
                              throw new InternalError( "data.length != Core.LONG_SIZE" );                      
                      
                      final long result = Core.toLongValue( data );
                      return result;
                }
                catch ( Exception e )
                {
                        throw new InternalError( e.toString() );
                }

        }
                
        private boolean validateFields() 
        {
                final String nick = tfdNickName.getText();
                final String password = new String( pwfPassword.getPassword() );
                final String confirmPassword = new String( pwfConfirmPassword.getPassword() );
                
                if ( !Useful.isValid(nick) )
                {
                    JOptionPane.showMessageDialog(UserRegister.this, 
                                                  "O nickname digitado possui caractere(s) inválido(s)" );                    
                    return false;
                }                 
                
                if ( nick.equalsIgnoreCase( "" )  )
                {
                        showMessageDialog( this, "Nickname está vazio!" );
                        return false;
                }
                
                if (! Useful.isValid(password) )
                {
                    JOptionPane.showMessageDialog(UserRegister.this, "A senha digitada possui caractere(s) inválido(s).");
                    return false;
                }                
                
                if ( password.equals( "" )  )
                {
                        showMessageDialog( this, "A senha está vazia!" );
                        return false;
                }                

                if ( confirmPassword.equals( "" )  )
                {
                        showMessageDialog( this, "A confirmação de senha está vazia!" );
                        return false;
                }                       
                
                if ( ! ( password.equals( confirmPassword ) ) )
                {
                        showMessageDialog( this, "As senhas estão diferentes!" );
                        return false;
                }
                
                return true;
        }
        
        /*Retorna <code>null</code> se ocorrer falha na conexão
         */
        
        private RegisterResult register() throws Exception
        {
                final long maxTime = getRegisterMaxTime();
                
                if ( maxTime == -1 )
                        return null;                
                
                final String nick = AbstractDefaultUserStatus.prepareNickName( tfdNickName.getText() );
                final char[] password = pwfPassword.getPassword();     
                Packet p = Packet.forRequestObject( serverAddress, 
                                                    serverPort, 
                                                    new RegisterRequest( RegisterRequest.REGISTER_REQUEST ) );
                
                Register register = new Register();
                register.setNickName( nick );
                register.setPassword( password );               
                Convertible obj = Network.requestObject( p, register, maxTime );
                
                if ( obj == null )
                        return null;
                
                if ( ! ( obj instanceof RegisterResult ) )
                        throw new InternalError( "! ( obj instanceof RegisterResult )" );
                
                return ( RegisterResult ) obj;
        }
        
        private class MouseHandler extends MouseAdapter
        {
                @Override
                public void mouseClicked( MouseEvent me )
                {
                      if ( me.getButton() != MouseEvent.BUTTON1 )
                               return;                        
                      
                      validateAndRegister();
                }
        }
        
        private void validateAndRegister()
        {
                try
                {
                        if ( validateFields() )
                        {
                                if ( ! ServerConnect.isRunning( serverAddress, serverPort ) )
                                {
                                        showMessageDialog( UserRegister.this, "Falha de conexão ao fazer o cadastro!" );
                                        return;
                                }

                                RegisterResult registerResult = register();

                                if ( registerResult == null )
                                {
                                        showMessageDialog( UserRegister.this, "Falha de conexão ao fazer o cadastro!" );
                                        return;                                                                                
                                }        

                                if ( registerResult.isRegistered() ) 
                                {
                                        showMessageDialog( UserRegister.this, "Cadastro efetuado com sucesso!" );
                                        UserRegister.this.dispose();
                                        createLogon(tfdNickName.getText(), new String(pwfPassword.getPassword())).setVisible( true );
                                }
                                else
                                {
                                        showMessageDialog( UserRegister.this, registerResult.getMessage() );
                                        return;                                         
                                }
                        }
                 }
                catch ( Exception e )
                {
                        showMessageDialog( UserRegister.this, e );
                }        
        }
        
        private Login createLogon(String nickName, 
                                  String password)
        {
                try{                                                      
                        return new Login( serverAddress, 
                                          serverPort,
                                          nickName,
                                          password);
                }
                catch ( Exception e ){
                        throw new InternalError( "btnCancel.MouseClicked()" );
                }                
        }
        
        private class WindowHandler extends WindowAdapter
        {
                @Override
                public void windowClosing( WindowEvent we )
                {
                        UserRegister.this.dispose();
                        createLogon(null, null).setVisible( true );
                }
        }        
        
        {
                final int w = 247;
                final int h = 220;
                setSize( w, h );
                Point p = application.forms.util.Useful.getCenterPoint( w, h );
                setLocation( p );
                setTitle( "Cadastro de Usuário" );
                setResizable( false );
                application.forms.util.Useful.setDefaultImageIcon( this );
                addWindowListener( new WindowHandler() );
                
                lblNickName.setSize( 100, 20 );
                lblNickName.setLocation( 20, 10 );
                
                tfdNickName.setSize( 200, 20 );
                tfdNickName.setLocation( 20, 30 );
                tfdNickName.addKeyListener( new KeyHandler());
                tfdNickName.addKeyListener( new NicknameHandler() );
                
                lblPassword.setSize( 100, 20 );
                lblPassword.setLocation( 20, 50 );
                
                pwfPassword.setSize( 200, 20 );
                pwfPassword.setLocation( 20, 70 );
                pwfPassword.addKeyListener( new KeyHandler() );
                pwfPassword.addKeyListener( new PasswordHandler() );

                lblConfirmPassword.setSize( 200, 20 );
                lblConfirmPassword.setLocation( 20, 90 );
                
                pwfConfirmPassword.setSize( 200, 20 );
                pwfConfirmPassword.setLocation( 20, 110 );
                pwfConfirmPassword.addKeyListener( new KeyHandler());
                pwfConfirmPassword.addKeyListener( new PasswordHandler() );

                btnRegister.setSize( 100, 20 );
                btnRegister.setLocation( 20, 140 );
                btnRegister.addKeyListener(new KeyHandler());
                btnRegister.addMouseListener( new MouseHandler() );

                btnClose.setSize( 100, 20 );
                btnClose.setLocation( 121, 140 );
                btnClose.addMouseListener( new MouseAdapter()
                { 
                       @Override
                        public void mouseClicked( MouseEvent me )
                        {
                                if ( me.getButton() != MouseEvent.BUTTON1 )
                                        return;                                        

                                UserRegister.this.dispose();
                                createLogon(null,null).setVisible( true );
                        } 
                } );
                
                Container contentPane = getContentPane();
                contentPane.setLayout( null );
                contentPane.add( lblNickName );
                contentPane.add( tfdNickName );
                contentPane.add( lblPassword );
                contentPane.add( pwfPassword );
                contentPane.add( lblConfirmPassword );
                contentPane.add( pwfConfirmPassword );
                contentPane.add( btnRegister );
                contentPane.add( btnClose );        
        }        
        
        public UserRegister( InetAddress serverAddress, 
                             final int serverPort )
        {
                if ( serverAddress == null )
                        throw new IllegalArgumentException( "serverAddress é null" );
                
                if ( serverPort < 0 || serverPort > 0xFFFF )
                        throw new IllegalArgumentException( "serverPort não armazena um número de porta válida" );      
                
                this.serverAddress = serverAddress;
                this.serverPort = serverPort;
        }        
}