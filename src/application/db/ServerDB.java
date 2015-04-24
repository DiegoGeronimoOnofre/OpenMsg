
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

package application.db;

import application.net.Network;
import application.net.core.Packet;
import application.objects.Friends;
import application.objects.NickName;
import application.requests.FriendRequest;
import application.util.Convertible;
import application.Application;
import application.objects.Bool;
import application.objects.Invitation;
import application.requests.InvitationRequest;

import java.net.InetAddress;

public class ServerDB
{       
        public static Friends getFriends( InetAddress serverAddress, 
                                          final int serverPort, 
                                          String nickName )
        {
                if ( serverAddress == null )
                        throw new IllegalArgumentException( "serverAddress é null" );

                if ( serverPort < 0 || serverPort > 0xFFFF )
                        throw new IllegalArgumentException( "serverPort não armazena um número de porta válida" );
                
                if ( nickName == null )
                        throw new IllegalArgumentException( "nickName é null" );
                
                try
                {
                        final long maxTime = Application.getDefaultMaxTime( serverAddress, serverPort );
                        
                        if ( maxTime == -1 )
                                return null;
                        
                        Packet packet = Packet.forRequestObject( serverAddress,
                                                                 serverPort,
                                                                 new FriendRequest( FriendRequest.FRIENDS_REQUEST ));
                        NickName nn = new NickName();
                        nn.setNickName( nickName );
                        Convertible obj = Network.requestObject( packet, nn, maxTime );
                        
                        if ( obj == null )
                                return null;
                        
                        Friends friends = ( Friends ) obj;
                        return friends;
                }
                catch ( Exception e )
                {
                        throw new InternalError( e.toString() );
                }
                
        }     
        
        public static final int FALSE = 0;
        
        public static final int TRUE = 1;
        
        public static final int CONNECTION_ERROR = 2;
        
        public static int linkExists( InetAddress serverAddress, 
                                      int serverPort, 
                                      String user, 
                                      String friend )
        {
                if ( serverAddress == null )
                        throw new IllegalArgumentException( "serverAddress é null" );

                if ( serverPort < 0 || serverPort > 0xFFFF )
                        throw new IllegalArgumentException( "serverPort não armazena um número de porta válida" );
                
                if ( user == null )
                        throw new IllegalArgumentException( "user é null" );        

                if ( friend == null )
                        throw new IllegalArgumentException( "friend é null" );    
                
                try
                {

                        final long maxT = application.Application.getDefaultMaxTime( serverAddress, serverPort );
                        
                        if ( maxT == -1 )
                                return CONNECTION_ERROR;
                        
                        Packet packet = Packet.forRequestObject( serverAddress,
                                                                 serverPort,
                                                                 new InvitationRequest( InvitationRequest.LINK_EXISTS_REQUEST )
                                                                );
                       
                        Invitation invitation = new Invitation();
                        invitation.setUser( user );
                        invitation.setFriend( friend );
                        final long maxTime = Network.DEFAULT_INTERVAL * Network.DEFAULT_TRY_COUNT;
                        Convertible obj = Network.requestObject( packet, 
                                                                 invitation, 
                                                                 maxTime + maxT );
                        
                        if ( obj == null )
                                return CONNECTION_ERROR;
                        
                        Bool result = ( Bool ) obj;
                        
                        if ( result.getBoolean() )
                                return TRUE;
                        else
                                return FALSE;
                }
                catch ( Exception e )
                {
                        throw new InternalError( e.toString() );
                }
        }

        public static int isFriend( InetAddress serverAddress, 
                                      int serverPort, 
                                      String user, 
                                      String friend )
        {
                if ( serverAddress == null )
                        throw new IllegalArgumentException( "serverAddress é null" );

                if ( serverPort < 0 || serverPort > 0xFFFF )
                        throw new IllegalArgumentException( "serverPort não armazena um número de porta válida" );
                
                if ( user == null )
                        throw new IllegalArgumentException( "user é null" );        

                if ( friend == null )
                        throw new IllegalArgumentException( "friend é null" );    
                
                try
                {

                        final long maxT = application.Application.getDefaultMaxTime( serverAddress, serverPort );
                        
                        if ( maxT == -1 )
                                return CONNECTION_ERROR;
                        
                        Packet packet = Packet.forRequestObject( serverAddress,
                                                                 serverPort,
                                                                 new FriendRequest( FriendRequest.IS_FRIEND_REQUEST )
                                                                );
                       
                        Invitation invitation = new Invitation();
                        invitation.setUser( user );
                        invitation.setFriend( friend );
                        final long maxTime = Network.DEFAULT_INTERVAL * Network.DEFAULT_TRY_COUNT;
                        Convertible obj = Network.requestObject( packet, 
                                                                 invitation, 
                                                                 maxTime + maxT );
                        
                        if ( obj == null )
                                return CONNECTION_ERROR;
                        
                        Bool result = ( Bool ) obj;
                        
                        if ( result.getBoolean() )
                                return TRUE;
                        else
                                return FALSE;
                }
                catch ( Exception e )
                {
                        throw new InternalError( e.toString() );
                }
        }
}