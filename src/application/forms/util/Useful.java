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

import application.objects.XStatus;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.net.InetAddress;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class Useful
{
        public static boolean isNumber( final char c )
        {
                if ( c >= '0' && c <= '9' )
                        return true;
                else
                        return false;
        }
        
        public static boolean isLetter( final char c )
        {
                if ( c >= 'a' && c <= 'z' )
                        return true;                

                if ( c >= 'A' && c <= 'Z' )
                        return true;  
                
                return false;
        }   
        
        public static boolean isValid( final char c )
        {
                if ( Useful.isNumber( c ) )
                        return true;
                else if ( Useful.isLetter( c ) )
                        return true;
                else
                        return false;
        }  
        
        public static boolean isValid( String text )
        {
                for ( int i = 0; i < text.length(); i++ )
                {
                        final char c = text.charAt( i );
                        
                        if ( ! isValid( c ) )
                                return false;
                }
                
                return true;
        }
        
        public static void setDefaultImageIcon( JFrame frame )
        {
                final char sep = java.io.File.separatorChar;
                String path = application.Application.getMainPath() + sep + "imgs" + sep + "DefaultIcon.png";  
                java.awt.Image icon = new ImageIcon( path ).getImage();
                frame.setIconImage( icon );
        }
        
        public static Point getCenterPoint( final int w, final int h )
        {
                Toolkit tk = Toolkit.getDefaultToolkit();
                Dimension d = tk.getScreenSize();
                final int x = ( int ) ( d.getWidth() / 2 ) - ( w / 2 );  
                final int y = ( int ) ( d.getHeight() / 2 ) - ( h / 2 );  
                return new Point( x, y );
        }            
        
        public static Users createDefaultUsers()
        {
                return new DefaultUsers();
        }
        
        public static UserStatus createDefaultUserStatus( String nickName, 
                                                          InetAddress address, 
                                                          final int port, 
                                                          final int status )
        {                        
                final char sep = java.io.File.separatorChar;
                String path = application.Application.getMainPath() + sep + "imgs";
                String user = path + sep + "User.png";                
                String onLine = path + sep + "OnLine.png";                
                String offLine = path + sep +"OffLine.png";
                DefaultUserStatus result = new DefaultUserStatus(   new ImageIcon( user ), 
                                                                    new ImageIcon( onLine ), 
                                                                    new ImageIcon( offLine ), 
                                                                    nickName, 
                                                                    address,
                                                                    port,
                                                                    status );
                return result; 
        } 

       public static UserStatus createDefaultUserStatus( XStatus inf ) throws Exception
       {
                if ( inf == null )
                        throw new IllegalArgumentException( "inf é null" );               
               
                if ( inf.getAddress() == null && inf.getMainPort() != -1 )
                        throw new IllegalArgumentException( " inf.getAddress() == null && inf.getMainPort() != -1" );

                if ( inf.getAddress() != null && inf.getMainPort() == -1 )
                        throw new IllegalArgumentException( "inf.getAddress() != null && inf.getMainPort() == -1" );             
               
               final InetAddress address;
              
               if ( inf.getAddress() != null )
                        address = InetAddress.getByAddress( inf.getAddress() );
               else
                       address = null;
               
               return createDefaultUserStatus( inf.getNickName(), 
                                               address,
                                               inf.getMainPort(),
                                               inf.getStatus()
                                               );
        } 
}