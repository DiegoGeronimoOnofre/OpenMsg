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
package application.objects;

import application.util.Convertible;

public class UserAndFriend extends Convertible
{
        private String user;
        
        private String friend;
        
        public UserAndFriend() throws Exception
        {}
        
        public String getUser()
        {
                if ( user == null )
                        throw new RuntimeException( "user == null" );                
                
                return user;
        }
        
        public String getFriend()
        {
                if ( friend == null )
                        throw new RuntimeException( "friend == null" );                
                
                return friend;
        }       
        
        public void setUser( String user )
        {
                if ( user == null )
                        throw new IllegalArgumentException( "user é null" );                
                
                this.user = user;
        }

        public void setFriend( String friend )
        {
                if ( friend == null )
                        throw new IllegalArgumentException( "friend é null" );                
                
                this.friend = friend;
        }
}