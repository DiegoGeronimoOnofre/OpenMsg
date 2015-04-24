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
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;
import javax.swing.ImageIcon;

public class NewUserStatus extends AbstractDefaultUserStatus
{
        private final Messenger messenger;
        
        private final String mainNickName;
        
        private final UsersWindow usersWindow;
        
        private final OptionsMenu optionsMenu;
        
        public NewUserStatus(     final UsersWindow usersWindow,
                                  final String mainNickName,
                                  final ImageIcon userImage, 
                                  final ImageIcon onLineImage, 
                                  final ImageIcon offLineImage, 
                                  final String nickName,
                                  final InetAddress address,
                                  final int port,
                                  final int status
                                        )
        {
                super( userImage, 
                       onLineImage, 
                       offLineImage, 
                       nickName, 
                       address, 
                       port, 
                       status );
                
                this.mainNickName = mainNickName;
                this.usersWindow  = usersWindow;
                this.optionsMenu  = new OptionsMenu( usersWindow.getServerAddress(),
                                                     usersWindow.getServerPort(),
                                                     mainNickName,
                                                     nickName );
                
                MouseHandler mh   = new MouseHandler();
                addMouseListener( mh );
                add( optionsMenu );
                messenger = new Messenger(this);
        }      
        
        private class MouseHandler extends MouseAdapter
        {
                @Override
                public void mouseClicked( MouseEvent me )
                {
                        final int clickCount = me.getClickCount();
                        
                        if ( clickCount == 1 && me.getButton() == 3 )
                        {
                                Component comp = me.getComponent();
                                final int x = me.getX();
                                final int y = me.getY();
                                optionsMenu.show( comp, x, y );
                        }
                        else if ( getStatus() == ON_LINE && me.getButton() == MouseEvent.BUTTON1 )
                        {
                                if ( clickCount == 2 )
                                {
                                        final Object lock = messenger.getLock();
                                        
                                        synchronized ( lock )
                                        {
                                                if ( ! messenger.isInit() )
                                                {
                                                        try
                                                        {
                                                                messenger.init( mainNickName, NewUserStatus.this );
                                                        }
                                                        catch ( Exception e )
                                                        {
                                                                throw new InternalError( e.toString() );
                                                        }
                                                }
                                        }

                                        messenger.setVisible( true );                                                
                                }
                        }
                }
        }
        
        public Messenger getMessenger()
        {
                return messenger;
        }
        
        public UsersWindow getUsersWindow()
        {
                return usersWindow;
        }
}