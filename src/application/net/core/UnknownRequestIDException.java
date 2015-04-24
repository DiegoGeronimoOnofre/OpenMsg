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
package application.net.core;

public class UnknownRequestIDException extends NetworkException
{
        private Packet exceptionPacket;
        
        private final byte unknownRequestID;
        
        private final Class<? extends Request> requestClass;
        
        public UnknownRequestIDException( final byte unknownRequestID, 
                                                                              Class<? extends Request> requestClass,
                                                                              String message ) throws Exception
        {
                super( message );
                
                if ( requestClass == null )
                        throw new IllegalArgumentException( "requestClass é null" );
                
                if ( ! Request.isAddedRequestClass( requestClass ) )
                        throw new NetworkException( "requestClass não foi adicionada na lista de classes de Request" );
                
                this.unknownRequestID = unknownRequestID;
                this.requestClass = requestClass;
        }
        
        public UnknownRequestIDException( final byte unknownRequestID, 
                                                                              Class<? extends Request> requestClass ) throws Exception
        {
                this( unknownRequestID, requestClass, null );
        }
        
        public byte getUnknownRequestID()
        {
                return unknownRequestID;
        }
        
        public Class<? extends Request> getRequestClass()
        {
                return requestClass;
        }
        
        void setExceptionPacket( Packet exceptionPacket )
        {                
                this.exceptionPacket = exceptionPacket;
        }        
        
        public Packet getExceptionPacket()
        {
                return exceptionPacket;
        }
        
        @Override
        byte getClassIdentifier()
        {
                return Packet.UNKNOWN_REQUEST_ID_EXCEPTION;
        }        
}