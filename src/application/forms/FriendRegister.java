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

import application.forms.util.NicknameHandler;
import application.forms.util.Useful;
import application.net.Network;
import application.net.core.Packet;
import application.objects.Invitation;
import application.objects.InvitationResult;
import application.requests.InvitationRequest;
import application.util.Convertible;

import java.awt.Container;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class FriendRegister extends JFrame
{
        private JLabel lblNickName = new JLabel( "Nickname" );
       
        private JTextField tfdNickName = new JTextField();     
        
        private JButton btnInvite = new JButton( "Convidar" );        
        
        private JButton btnClose = new JButton( "Fechar" );        
        
        private final UsersWindow usersWindow;

        private class WindowHandler extends WindowAdapter
        {
                @Override
                public void windowClosing( WindowEvent we )
                {
                        FriendRegister.this.dispose();
                }
        } 
        
        private class KeyHandler extends KeyAdapter
        {
            @Override
            public void keyTyped(KeyEvent ke)
            {
                if ( ke.getKeyChar() == (char) KeyEvent.VK_ENTER )
                    FriendRegister.this.validateAndInvite();
            }
        }
        
        private void validateAndInvite()
        {
            try
            {
                final String text = tfdNickName.getText();    
                
                if ( !Useful.isValid(text) )
                {
                    JOptionPane.showMessageDialog(FriendRegister.this, 
                                                  "Os caractere(s) digitado(s) é/são inválido(s)" );                    
                    return;
                }

                if ( text.equals( "" ) )
                {
                        JOptionPane.showMessageDialog( FriendRegister.this, 
                                                       "Nickname está vazio!" );
                        return;
                }

                final String friend = text.toLowerCase().trim();    

                if ( friend.equals( usersWindow.getNickName().toLowerCase().trim() ) )
                {
                        JOptionPane.showMessageDialog( FriendRegister.this, 
                                                       "Você não pode convidar a você mesmo!" );
                        return;                                          
                }

                final long maxTime = application.Application.getDefaultMaxTime( usersWindow.getServerAddress(),
                                                                                usersWindow.getServerPort()  );

                if ( maxTime == -1 )
                {
                        JOptionPane.showMessageDialog( FriendRegister.this, 
                                                       "Falha de conexão ao enviar o convite!" );
                        return;                                        
                }                                        

                final String user = usersWindow.getNickName();                                              
                Packet p = Packet.forRequestObject( usersWindow.getServerAddress(), 
                                                    usersWindow.getServerPort(),
                                                    new InvitationRequest( InvitationRequest.INVITATION_REQUEST ));

                Invitation invitation = new Invitation();
                invitation.setUser( user );
                invitation.setFriend( friend );
                Convertible result = Network.requestObject( p, 
                                                            invitation, 
                                                            maxTime );

                InvitationResult invitationResult = ( InvitationResult ) result;             

                if ( invitationResult == null )
                {
                        JOptionPane.showMessageDialog( FriendRegister.this, 
                                                       "Falha de conexão ao enviar o convite!" );
                        return;
                }

                if ( invitationResult.getBoolean() )
                {
                        java.util.List<String> invitationList = usersWindow.getInvitationList();
                        invitationList.add( friend );
                        JOptionPane.showMessageDialog( FriendRegister.this, 
                                                       "Convite efetuado com sucesso!" );
                        FriendRegister.this.dispose();
                }
                else
                        JOptionPane.showMessageDialog( FriendRegister.this, 
                                                       invitationResult.getMessage() );

                }
                catch ( Exception e )
                {
                    throw new InternalError( e.toString() );
                }
        }
        
        public FriendRegister( final UsersWindow usersWindow )
        {
                final int w = 247;
                final int h = 140;
                setSize( w, h );
                Point p = application.forms.util.Useful.getCenterPoint( w, h );
                setLocation( p );
                setTitle( "Cadastro de Amigo(a)" );
                setResizable( false );
                application.forms.util.Useful.setDefaultImageIcon( this );
                addWindowListener( new WindowHandler() );
                this.usersWindow = usersWindow;
                
                lblNickName.setSize( 100, 20 );
                lblNickName.setLocation( 20, 10 );
                
                tfdNickName.setSize( 200, 20 );
                tfdNickName.setLocation( 20, 30 );
                tfdNickName.addKeyListener( new KeyHandler() );
                tfdNickName.addKeyListener( new NicknameHandler() );
                
                btnInvite.setSize( 100, 20 );
                btnInvite.setLocation( 20, 60 );
                btnInvite.addKeyListener(new KeyHandler());
                btnInvite.addMouseListener( new MouseAdapter()
                {
                        @Override
                        public void mouseClicked( MouseEvent me )
                        {
                                if ( me.getButton() != MouseEvent.BUTTON1 )
                                        return;
                                
                                FriendRegister.this.validateAndInvite();
                        }
                });

                btnClose.setSize( 100, 20 );
                btnClose.setLocation( 121, 60 );     
                btnClose.addMouseListener( new MouseAdapter()
                { 
                       @Override
                        public void mouseClicked( MouseEvent me )
                        {
                                if ( me.getButton() != MouseEvent.BUTTON1 )
                                        return;                                        

                                FriendRegister.this.dispose();
                        } 
                } );                
                
                Container contentPane = getContentPane();
                contentPane.setLayout( null );
                contentPane.add( lblNickName );
                contentPane.add( tfdNickName );
                contentPane.add( btnInvite );
                contentPane.add( btnClose );
        }
}