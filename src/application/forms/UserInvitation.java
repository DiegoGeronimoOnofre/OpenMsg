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
import application.net.Network;
import application.net.core.Packet;
import application.objects.Invitation;
import application.requests.InvitationRequest;

import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class UserInvitation extends JFrame
{
        private JLabel lblNickName = new JLabel();
  
        private JLabel lblMessage = new JLabel();
        
        private JButton btnAccept = new JButton( "Aceitar" );
        
        private JButton btnDecline = new JButton( "Recusar" ); 
        
        private final UsersWindow usersWindow;
        
        private final String friend;

        private class WindowHandler extends WindowAdapter
        {
                @Override
                public void windowClosing( WindowEvent we )
                {
                       decline();
                }
        }            
        
        public void accept( boolean showMessage )
        {
                try
                {
                        final String user = usersWindow.getNickName();                                              
                        Packet p = Packet.forUpload( usersWindow.getServerAddress(), 
                                                     usersWindow.getServerPort(),
                                                     new InvitationRequest( InvitationRequest.INVITATION_ANSWER_REQUEST ) );

                        Invitation invitation = new Invitation();
                        invitation.setAnswer( Invitation.ACCEPTED );
                        invitation.setUser( user );
                        invitation.setFriend( friend );
                        Network network = new Network();
                        byte[] information = invitation.toBytes();
                        network.getUploadManager().add( p );
                        final boolean result = network.upload( p, information );             

                        if ( showMessage && result == false )
                                JOptionPane.showMessageDialog( null, "Falha de conexão ao aceitar o convite" );           
                }
                catch ( Exception e )
                {
                        throw new InternalError( e.toString() );
                }
        }
        
        private void decline()
        {
                try
                {
                        final String user = usersWindow.getNickName();                                              
                        Packet p = Packet.forUpload( usersWindow.getServerAddress(), 
                                                     usersWindow.getServerPort(),
                                                     new InvitationRequest( InvitationRequest.INVITATION_ANSWER_REQUEST ) );

                        Invitation invitation = new Invitation();
                        invitation.setAnswer( Invitation.DECLINED );
                        invitation.setUser( user );
                        invitation.setFriend( friend );
                        Network network = new Network();
                        byte[] information = invitation.toBytes();
                        network.getUploadManager().add( p );
                        final boolean result = network.upload( p, information );             

                        if ( result == false )
                                JOptionPane.showMessageDialog( null, "Falha de conexão ao recusar o convite" );                             
                }
                catch ( Exception e )
                {
                        throw new InternalError( e.toString() );
                }
        }
        
        public UserInvitation( UsersWindow usersWindow, String friend )
        {
                this.usersWindow = usersWindow;
                this.friend = friend;
                final int w = 250;
                final int h = 120;
                setSize( w, h );
                Point p = application.forms.util.Useful.getCenterPoint( w, h );
                setLocation( p );
                String nn = usersWindow.getNickName();
                setTitle( application.Application.APPLICATION_NAME + "-" + nn );
                setResizable( false );
                application.forms.util.Useful.setDefaultImageIcon( this );
                addWindowListener( new WindowHandler() );                
                
                lblNickName.setSize( 230, 20 );
                lblNickName.setLocation( 20, 10 );
                lblNickName.setText( AbstractDefaultUserStatus.prepareNickName( friend ) );

                lblMessage.setSize( 200, 20 );
                lblMessage.setLocation( 20, 20 );
                lblMessage.setText( "deseja ter você como amigo(a)." );

                btnAccept.setSize( 100, 20 );
                btnAccept.setLocation( 20, 40 );
                btnAccept.addMouseListener( new MouseAdapter()
                {
                        @Override
                        public void mouseClicked( MouseEvent me )
                        {
                                if ( me.getButton() != MouseEvent.BUTTON1 )
                                        return;
                                
                                accept( true );
                                UserInvitation.this.dispose();
                        }
                });

                btnDecline.setSize( 100, 20 );
                btnDecline.setLocation( 121, 40 );
                btnDecline.addMouseListener( new MouseAdapter()
                {
                        @Override
                        public void mouseClicked( MouseEvent me )
                        {
                                if ( me.getButton() != MouseEvent.BUTTON1 )
                                        return;                    
                                
                                decline();
                                UserInvitation.this.dispose();
                        }
                });
                
                Container contentPane = getContentPane();
                contentPane.setLayout( null );
                contentPane.add( lblNickName );
                contentPane.add( lblMessage );
                contentPane.add( btnAccept );
                contentPane.add( btnDecline );
        }
}