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

import application.forms.util.UserStatus;
import java.net.InetAddress;

public class XStatus extends Status
{
        private byte[] address;     
        
        public XStatus() throws Exception
        {}
        
        public byte[] getAddress()
        {
                if ( address == null )
                        throw new RuntimeException( "address == null" );                
                
                return address;
        }
        
        public void setAddress( byte[] address )
        {
                if ( address == null )
                        throw new IllegalArgumentException( "address é null" );       
                
                this.address = address; 
        }
        
        public static XStatus UserStatusToXStatus( UserStatus us )
        {
                if ( us == null )
                        throw new IllegalArgumentException( "us é null" );
                
                try
                {
                        XStatus result = new XStatus();
                        result.setNickName( us.getNickName() );
                        InetAddress address = us.getAddress();
                        
                        if ( address != null )
                                result.address = address.getAddress();
                        else
                                result.address = null;
                        
                        result.setMainPort( us.getPort() );
                        result.setStatus( us.getStatus() );
                        return result;
                }
                catch ( Exception e )
                {
                        throw new InternalError();
                }
        }
      
}