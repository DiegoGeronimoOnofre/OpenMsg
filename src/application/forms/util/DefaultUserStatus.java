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
package application.forms.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseListener;
import java.net.InetAddress;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

class DefaultUserStatus extends JPanel implements UserStatus
{
        private JPanel pnlUser = new JPanel()
        { 
                @Override
                public void paint( Graphics g )
                {
                        super.paint( g );
                        g.drawImage( userImage.getImage(), 0, 0, this );               
                }
        };
        
        private JPanel pnlNickName = new JPanel();
        
        private JPanel pnlStatus = new JPanel()
        {
                @Override
                public void paint( Graphics g )
                {
                        super.paint( g );

                        if ( status == ON_LINE )
                                g.drawImage( onLineImage.getImage(), 0, 0, this );
                        else
                                g.drawImage( offLineImage.getImage(), 0, 0, this );               
                }
        };     
           
        public static String prepareNickName( String nickName )
        {
                String nick = nickName.toLowerCase().trim();
                
                if ( nick.length() > 0 )
                {
                        final char c = nick.charAt( 0 );
                        final char ch = String.valueOf( c ).toUpperCase().charAt( 0 );
                        char[] chars = nick.toCharArray();       
                        chars[0] = ch;
                        return String.valueOf( chars );
                }
                else
                        return nick;                        
        }           
        
        private final JLabel lblNickName;        
        
        private final ImageIcon userImage;
        
        private final ImageIcon onLineImage;
        
        private final ImageIcon offLineImage;
        
        private final String nickName;
        
        private InetAddress address;
        
        private int port;     
        
        private int status;
        
        public DefaultUserStatus( final ImageIcon userImage, 
                                            final ImageIcon onLineImage, 
                                            final ImageIcon offLineImage, 
                                            final String nickName,
                                            final InetAddress address,
                                            final int port,
                                            final int status
                                        )
        {
                if ( userImage == null )
                        throw new IllegalArgumentException( "userImage é null" );

                if ( onLineImage == null )
                        throw new IllegalArgumentException( "onlineImage é null" );
                
                if ( offLineImage == null )
                        throw new IllegalArgumentException( "offlineImage é null" );

                if ( nickName == null )
                        throw new IllegalArgumentException( "nickName é null" );
                
                if ( port < -1 || port > 0xFFFF )
                        throw new IllegalArgumentException( "port não armazena um número de porta válida" );                
                
                if ( status != ON_LINE && status != OFF_LINE )
                        throw new IllegalArgumentException( "status inválido." );
                
                if ( onLineImage.getIconHeight() != offLineImage.getIconHeight() )
                        throw new IllegalArgumentException( "onLineImage deve ser do"
                                                                                                + " mesmo tamanho que offLineImage" );

                if ( onLineImage.getIconWidth() != offLineImage.getIconWidth() )
                        throw new IllegalArgumentException( "onLineImage deve ser do "
                                                                                                +  "mesmo tamanho que offLineImage" );

                if ( userImage.getIconHeight() != onLineImage.getIconHeight() )
                        throw new IllegalArgumentException( "userImage deve ter o "
                                                                                                +  "mesmo tamanho que as imagens de status " );

                if ( userImage.getIconWidth() != offLineImage.getIconWidth() )
                        throw new IllegalArgumentException( "userImage deve ter o "
                                                                                                +  "mesmo tamanho que as imagens de status " );
                
                this.nickName = prepareNickName( nickName );               
                setLayout( new BorderLayout() );
                setPreferredSize( new Dimension( 0, userImage.getIconWidth() ) );
                
                pnlUser.setPreferredSize( new Dimension( userImage.getIconWidth(), 
                                                                                                userImage.getIconHeight() ) );
                
                lblNickName = new JLabel( this.nickName );
                
                pnlNickName.setLayout( new BorderLayout() );                
                pnlNickName.setPreferredSize( new Dimension( 0, userImage.getIconHeight() ) );      
                pnlNickName.add( lblNickName, BorderLayout.WEST );
                
                pnlStatus.setPreferredSize( new Dimension( userImage.getIconWidth(), 
                                                                                                   userImage.getIconHeight() ) );
                
                add( pnlUser, BorderLayout.WEST );
                add( pnlNickName, BorderLayout.CENTER );
                add( pnlStatus, BorderLayout.EAST );
                
                this.userImage = userImage;
                this.onLineImage = onLineImage;
                this.offLineImage = offLineImage;
                this.address = address;
                this.port = port;
                this.status = status;        
        }
        
        @Override
        public int getThisHeight()
        {
                return userImage.getIconHeight();
        }
        
        @Override
        public String getNickName()
        {
                return nickName;
        }        
        
        @Override
        public InetAddress getAddress()
        {
                return address;
        }
        
        @Override
        public void setAddress( InetAddress address )
        {
                if ( address == null )
                        throw new IllegalArgumentException( "address é null" );
                
                this.address = address;
        }
        
        @Override
        public int getPort()
        {
                return port;
        }
        
        @Override
        public void setPort( int port )
        {                
                if ( port < 0 || port > 0xFFFF )
                        throw new IllegalArgumentException( "port não armazena um número de porta válida" );  
                
                this.port = port;
        }
        
        @Override
        public int getStatus()
        {
                return status;
        }
        
        @Override
        public void setStatus( int status )
        {
                if ( status != ON_LINE && status != OFF_LINE )
                        throw new IllegalArgumentException( "status inválido." );
                
                this.status = status;
        }
        
        @Override
        public String toString()
        {
                return "[nickName=" + nickName 
                        + ",address= " + address
                        + ",port=" + port
                        + ",status=" + ( status == ON_LINE ? "ONLINE" : "OFFLINE" ) + "]";
        }     
        
        @Override
        public void addMouseListener( MouseListener ml )
        {
                pnlNickName.addMouseListener( ml );
                lblNickName.addMouseListener( ml );
                pnlUser.addMouseListener( ml );
                pnlStatus.addMouseListener( ml );
        }
}