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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JPanel;

class DefaultUsers extends JPanel implements Users
{        
        private static final int DEFAULT_SPACE = 2;
        
        final JPanel pnlMain = new JPanel()
        {
                @Override
                public Component add( Component comp )
                {
                        Component result = super.add( comp );
                        final int count = getComponentCount();
                        
                        if ( comp instanceof UserStatus )
                                setPreferredSize( new Dimension( 0, count * ( ( UserStatus ) comp ).getThisHeight() +
                                                                                                     count * DEFAULT_SPACE ) );

                        return result;
                }
                
                @Override
                public void remove( int index )
                {
                        super.remove( index );
                        final int count = getComponentCount();
                        
                        if ( count > 0 )
                        {
                                UserStatus user = ( UserStatus ) getComponent( 0 );                                
                                setPreferredSize( new Dimension( 0, count * user.getThisHeight() +
                                                                                                     count * DEFAULT_SPACE ) );                                      
                                
                        }
                        else
                                setPreferredSize( new Dimension( 0,0 ) );                  
                }
        };    
        
        private final Object defaultUsersSync = new Object();
        
        @Override
        public Object getDefaultUsersSync()
        {
                return defaultUsersSync;
        }
        
        public DefaultUsers()
        {                         
                pnlMain.setBackground( new Color( 220, 220, 220 ) );
                pnlMain.setLayout( new GridLayout( 0, 1, 0, DEFAULT_SPACE ) );        
                
                setLayout( new BorderLayout( 0, 1 ) );                
                add( pnlMain, BorderLayout.NORTH );              
        }   
        
        private int findUserStatus( String nickName )
        {
                synchronized ( defaultUsersSync )
                {
                        String preparedNick = DefaultUserStatus.prepareNickName( nickName );
                        
                        for ( int i = 0; i < pnlMain.getComponentCount(); i++ )
                        {
                                Component comp = pnlMain.getComponent( i );
                                UserStatus user = ( UserStatus ) comp;

                                if ( user.getNickName().equalsIgnoreCase( preparedNick ) )
                                        return i;
                        }

                        return -1;        
                }
        }
        
        @Override
        public boolean nickNameExists( String nickName )
        {
                if ( nickName == null )
                        throw new IllegalArgumentException( "nickName é null" );
                
                final int index = findUserStatus( nickName );
                
                if ( index != -1 )
                        return true;
                else
                        return false;
        }
        
        @Override
        public void addUserStatus( UserStatus userStatus )
        {
                synchronized ( defaultUsersSync )
                {
                        if ( userStatus == null )
                                throw new IllegalArgumentException( "userStatus é null" );

                        if ( ! ( userStatus instanceof Component ) )
                                throw new IllegalArgumentException( "userStatus não é uma"
                                                                                                        + " instância de Component" );
                        if ( nickNameExists( userStatus.getNickName() ) )
                                throw new IllegalArgumentException( "nickName: " + userStatus.getNickName() + " já está sendo usado" );

                        pnlMain.add( ( Component ) userStatus );
                }
        }
        
        @Override
        public int getUserStatusCount()
        {
                synchronized ( defaultUsersSync )
                {
                        return pnlMain.getComponentCount();
                }
        }
        
        @Override
        public UserStatus getUserStatus( String nickName )
        {
                synchronized ( defaultUsersSync )
                {
                        if ( nickName == null )
                                throw new IllegalArgumentException( "nickName é null" );                        
                        
                        final int index = findUserStatus( nickName );
                                
                        if ( index != -1 )
                                return getUserStatus( index );
                        else
                                return null;
                }
        }
        
        @Override
        public UserStatus getUserStatus( final int index )
        {
                synchronized ( defaultUsersSync )
                {
                        if ( index < 0 || index > getUserStatusCount() )
                                throw new IllegalArgumentException( "index é inválido " + index );
                        
                        return ( UserStatus ) pnlMain.getComponent( index );
                }
        }        
        
        @Override
        public void removeUserStatus( final int index )
        {
                synchronized ( defaultUsersSync )
                {
                        if ( index < 0 || index > getUserStatusCount() )
                                throw new IllegalArgumentException( "index é inválido " + index );                        
                        
                        pnlMain.remove( index );
                }
        }
        
        /*
         * Se encontrar o nome, então remove 
         * o usuário e retorna <code>true</code>,
         * senão, não remove o usuário e retorna false
         */
        
        @Override
        public boolean removeUserStatus( String nickName )
        {
                synchronized ( defaultUsersSync )
                {
                        if ( nickName == null )
                                throw new IllegalArgumentException( "nickName é null" );     

                        final int index = findUserStatus( nickName );

                        if ( index != -1 )
                        {
                                removeUserStatus( index );
                                return true;
                        }
                        else
                                return false;
                }
        }
}