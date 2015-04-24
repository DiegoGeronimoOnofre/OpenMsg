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
package application.requests;

public class UpdateRequest extends application.net.core.Request
{
        public static final byte CLASS_ID = 4;
        
        public static final byte LOGON_REQUEST = 0;
        
        public static final byte LOGOFF_REQUEST = 1;
        
        public static final byte USER_STATUS_REQUEST = 2;
        
        public static final byte STATUS_UPDATE_REQUEST = 3;
        
        public static final byte CONNECTED_USER_REQUEST = 4;
        
        public static final byte SERVER_CLOSING_REQUEST = 5;
        
        public static final byte SCREEN_UPDATE_REQUEST = 6;
        
        public static final byte SERVER_RUNNING_REQUEST = 7;   
        
        public static final byte DISCONNECT_REQUEST = 8;        

        public static final byte TYPING_TEXT_REQUEST = 9;
        
        public UpdateRequest( byte requestID ) throws Exception
        {
                super( requestID );
        }
}