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

import application.net.Network;
import application.net.core.Packet;
import application.objects.UserAndFriend;
import application.requests.FriendRequest;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import javax.swing.JOptionPane;

public class OptionsMenu extends PopupMenu 
{       
        private MenuItem mniDelete = new MenuItem( "Excluir amigo(a)" );
        
        private final Object lock = new Object();
        
        public OptionsMenu( final InetAddress serverAddress,
                            final int serverPort,
                            final String user,
                            final String friend )
        {
                add( mniDelete );
                mniDelete.addActionListener( new ActionListener()
                {
                        @Override
                        public void actionPerformed( ActionEvent me )
                        {
                                synchronized ( lock )
                                {
                                        try
                                        {
                                                Packet packet = Packet.forUpload( serverAddress,
                                                                                  serverPort,
                                                                                  new FriendRequest( FriendRequest.FRIEND_DELETE_REQUEST ));
                                                
                                                UserAndFriend userAndFriend = new UserAndFriend();
                                                userAndFriend.setUser( user );
                                                userAndFriend.setFriend( friend );
                                                Network net = new Network();
                                                byte[] information = userAndFriend.toBytes();
                                                net.getUploadManager().add( packet );                                                
                                                boolean result = net.upload( packet, information );
                                                
                                                if ( result == false )
                                                {
                                                        JOptionPane.showMessageDialog( null, "Não foi possível excluir " + friend 
                                                                                             + " porque ocorreu uma falha de conexão!");
                                                }
                                        }
                                        catch ( Exception e )
                                        {
                                                throw new InternalError( e.toString() );
                                        }
                                }
                        }
                }
                );
        }
}