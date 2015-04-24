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
import java.util.List;
import java.util.Arrays;

public class Friends extends Convertible
{
        private String[] friends;
        
        public Friends() throws Exception
        {}
        
        public List<String> getFriends()
        {
                if ( friends == null )
                        throw new RuntimeException( "friends == null" );
                
                return Arrays.asList( friends );
        }
        
        public void setFriends( List<String> friends )
        {
                if ( friends == null )
                        throw new IllegalArgumentException( "friends é null" );                
                
                for ( int i = 0; i < friends.size(); i++ )
                {
                        String friend = friends.get( i );
                        
                        if ( friend == null )
                                throw new IllegalArgumentException( "A lista friends contém elemento(s) nulo(s)" );
                }
                
                this.friends = friends.toArray( new String[]{} );
        }
}