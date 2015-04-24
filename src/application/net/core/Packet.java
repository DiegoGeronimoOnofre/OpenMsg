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

import application.util.Core;

import java.net.DatagramPacket;
import java.net.InetAddress;

/*OBS:
 *  O tamanho máximo do buffer do DatagramPacket deve ser maior ou
 * igual ao tamanho da estrutura + o tamanho da mensagem de exceção
 * ( mensagemDeExcecaoComoString.getBytes().length )
 * para que seja possível armazenar todos
 * os caracteres da mensagem de exceção
 * no buffer do DatagramPacket.
 */

public class Packet
{
        //Para solicitar um pacote que contém informações que possibilita baixar outra informação
        public static final byte SMART_REQUEST_TYPE = 0;
        
        //Para solicitar um pedaço de informação
        public static final byte SIMPLE_REQUEST_TYPE = 1;
        
        public static boolean isRequestType( byte packetType )
        {
                if ( packetType == SMART_REQUEST_TYPE )
                        return true;
                else if ( packetType == SIMPLE_REQUEST_TYPE )
                        return true;
                
                return false;
        }
        
        //Pacote que contém informações para baixar outras informações
        public static final byte SMART_RESULT_TYPE = 2;
        
        //Pacote com um pedaço de uma informação
        public static final byte SIMPLE_RESULT_TYPE = 3;      
        
        public static boolean isResultType( byte packetType )
        {
                if ( packetType == SMART_RESULT_TYPE )
                        return true;
                else if ( packetType == SIMPLE_RESULT_TYPE )
                        return true;
                
                return false;
        }        

        /*Quando o servidor ( lembrando que o cliente 
         * também pode ser servidor ) não sabe 
         * responder o pedido retorna este tipo de pacote
         */
        public static final byte EXCEPTION_TYPE = 4;
        
        public static boolean isExceptionType( byte packetType )
        {
                return packetType == EXCEPTION_TYPE;
        }        
        
        //Exceções de redes
        public static final byte NETWORK_EXCEPTION = 0;
        
        //Quando o CLASS_ID de um classe não é encontrado na lista de classe de Request
        public static final byte UNKNOWN_CLASS_ID_EXCEPTION = 1;
        
        //Quando o valor de requestID não é igual a nenhuma constante de request na classe correspondente
        public static final byte UNKNOWN_REQUEST_ID_EXCEPTION = 2;
        
        //Quando a estrutura do DatagramPacket não está em um formato próprio para a conversão
        public static final byte INVALID_PACKET_FORMAT_EXCEPTION = 3;
        
        
        //Número de bytes que a estrutura ocupa no pacote
        static final int STRUCT_SIZE = 20;       
        
        //Índice inicial para obter o identificador
        private static final int INITIAL_IDENTIFIER_INDEX = 0;        
        
        //Índice exato que armazena o tipo de pacote
        private static final int PACKET_TYPE_INDEX = 8;        
        
        //Índice exato que armazena o pedido
        private static final int REQUEST_ID_INDEX = 9;
        
        //ìndice da classe do pedido
        private static final int CLASS_ID_INDEX = 10;        

        //ìndice da classe de exceção
        private static final int EXCEPTION_CLASS_INDEX = 11;        
        
        //Índice inicial para obter a ordem
        private static final int INITIAL_ORDER_INDEX = 12;
        
        //Índice inicial para obter o número total de pacotes de uma informação
        private static final int INITIAL_TOTAL_PACKETS_INDEX = 16;              
        
        public static void configIdentifierManager(final int appType)
        {
                IdentifierManager.config(appType);
        }        
        
        public static long nextIdentifier()
        {
                return IdentifierManager.getNextIdentifier();
        }
        
        private InetAddress address;
        
        private int port;
        
        private final long identifier;
        
        private final byte packetType;
        
        private final Request request;
        
        private final NetworkException exception;
        
        private final int order;
        
        private int totalPackets;
        
        private final byte[] data;
        
        private Packet( InetAddress address,
                                     final int port,
                                     final long identifier,
                                     final byte packetType,
                                     final Request request,
                                     final NetworkException exception,
                                     final int order,
                                     final int totalPackets,
                                     final byte[] data
                                   )
        {                    
                if ( port < 0 || port > 0xFFFF )
                        throw new IllegalArgumentException( "port não armazena um número de porta válida" );                
                
               this.address = address;
               this.port = port;
               this.identifier = identifier;
               this.packetType = packetType;
               this.request = request;
               this.exception = exception;
               this.order = order;
               this.totalPackets = totalPackets;
               this.data = data;
                
        }
 
        private Packet( InetAddress address,
                                     final int port,
                                     final byte packetType,
                                     final Request request,
                                     final NetworkException exception,
                                     final int order,
                                     final int totalPackets,
                                     final byte[] data
                                   )
        {                           
                this( address, 
                          port,
                          nextIdentifier(), 
                          packetType, 
                          request, 
                          exception, 
                          order, 
                          totalPackets, 
                          data );
        }        
        
        public static Packet createSmartRequest( final InetAddress address, 
                                                 final int port,
                                                 final Request request,     
                                                 final byte[] data
                                                 )
        {
               if ( address == null )
                       throw new IllegalArgumentException( "address é null" );
               
               if ( request == null )
                       throw new IllegalArgumentException( "request é null" );               

               if ( data == null )
                       throw new IllegalArgumentException( "data é null" );               
               
                return new Packet( address, 
                                   port, 
                                   Packet.SMART_REQUEST_TYPE,
                                   request,
                                   null,
                                   -1,
                                   -1,
                                   data );
        }

        public static Packet createSimpleRequest( final InetAddress address, 
                                                  final int port,
                                                  final Request request,
                                                  final int order,
                                                  final byte[] data
                                                  ) 
        {
               if ( address == null )
                       throw new IllegalArgumentException( "address é null" );
               
               if ( request == null )
                       throw new IllegalArgumentException( "request é null" );               
               
               if ( order <  0 )
                       throw new IllegalArgumentException( "order não pode ser menor que (zero) 0." );
               
               if ( data == null )
                       throw new IllegalArgumentException( "data é null" );                  

                return new Packet( address, 
                                   port, 
                                   Packet.SIMPLE_REQUEST_TYPE,
                                   request,
                                   null,
                                   order,
                                   -1,
                                   data );
        }    
        
        private static long localIdentifier = Double.doubleToRawLongBits( Math.random() );
        
        static Packet createSimpleRequestForIdentifierManager( final InetAddress address, 
                                                               final int port,
                                                               final Request request,
                                                               final int order,
                                                               final byte[] data
                                                              )
        {
               if ( address == null )
                       throw new IllegalArgumentException( "address é null" );
               
               if ( request == null )
                       throw new IllegalArgumentException( "request é null" );               
               
               if ( order <  0 )
                       throw new IllegalArgumentException( "order não pode ser menor que (zero) 0." );
               
               if ( data == null )
                       throw new IllegalArgumentException( "data é null" );                  

               localIdentifier++;
               return new Packet( address, 
                                   port,
                                   localIdentifier,
                                   Packet.SIMPLE_REQUEST_TYPE,
                                   request,
                                   null,
                                   order,
                                   -1,
                                   data );            
        }

        public static Packet createSmartResult( final long identifier,
                                                final InetAddress address, 
                                                final int port,
                                                final Request request,  
                                                final int totalPackets,
                                                final byte[] data
                                                ) 
        {
               if ( address == null )
                       throw new IllegalArgumentException( "address é null" );
               
               if ( request == null )
                       throw new IllegalArgumentException( "request é null" );               
               
               if ( totalPackets < 1 )
                       throw new IllegalArgumentException( "totalPackets não pode ser menor que (um) 1" );
               
               if ( data == null )
                       throw new IllegalArgumentException( "data é null" );                  
                
                return new Packet( address, 
                                   port, 
                                   identifier,
                                   Packet.SMART_RESULT_TYPE,
                                   request,
                                   null,
                                   -1,
                                   totalPackets,
                                   data );
        }       
        
        public static Packet createSmartResult( final long identifier,
                                                final InetAddress address, 
                                                final int port,
                                                final Request request,
                                                final byte[] data
                                                ) 
        {
               if ( address == null )
                       throw new IllegalArgumentException( "address é null" );
               
               if ( request == null )
                       throw new IllegalArgumentException( "request é null" );               
               
               if ( data == null )
                       throw new IllegalArgumentException( "data é null" );                  
                
                return new Packet( address, 
                                   port, 
                                   identifier,
                                   Packet.SMART_RESULT_TYPE,
                                   request,
                                   null,
                                   -1,
                                   -1,
                                    data );
        }
        
        public static Packet createSimpleResult( final long identifier,
                                                 final InetAddress address, 
                                                 final int port,
                                                 final Request request,  
                                                 final int order,
                                                 final byte[] data
                                                 )
        {
               if ( address == null )
                       throw new IllegalArgumentException( "address é null" );

               if ( request == null )
                       throw new IllegalArgumentException( "request é null" );
               
               if ( data == null )
                       throw new IllegalArgumentException( "data é null" );
               
               if ( order < 0 )
                       throw new IllegalArgumentException( "order não pode ser menor que (zero) 0." );
                
                return new Packet( address, 
                                   port, 
                                   identifier,
                                   Packet.SIMPLE_RESULT_TYPE,
                                   request,
                                   null,
                                   order,
                                   -1,
                                   data );
        }   
        
        public static Packet createNetworkException( final long identifier,
                                                     final InetAddress address, 
                                                     final int port,
                                                     NetworkException exception 
                                                                                        ) 
        {
               if ( address == null )
                       throw new IllegalArgumentException( "address é null" );
                       
               if ( exception == null )
                       throw new IllegalArgumentException( "exception é null" );         
                
                return new Packet( address, 
                                   port, 
                                   identifier,
                                   Packet.EXCEPTION_TYPE,
                                   null,
                                   exception,
                                   -1,
                                   -1,
                                   null );
        }           
        
        
        public static Packet forUpload( final InetAddress address, 
                                        final int port,
                                        final Request request
                                                                        ) 
        {
                return Packet.createSmartResult( nextIdentifier(),
                                                 address,
                                                 port,
                                                 request,
                                                 new byte[]{} );
        }
        
        public static Packet forDownload( final InetAddress address,
                                          final int port,
                                          Request request
                                          ) 
        {
                return Packet.createSmartRequest( address, 
                                                  port, 
                                                  request, 
                                                  new byte[]{} );
        }    
        
        public static Packet forRequestObject(  final InetAddress address, 
                                                final int port,
                                                final Request request
                                                )
        {
                return createSmartRequest( address, 
                                           port, 
                                           request, 
                                           new byte[]{} );
        }                 

        public InetAddress getAddress()
        {
                return address;
        }
        
        public void setAddress( InetAddress address )
        {
                if ( address == null )
                        throw new IllegalArgumentException( "address é null" );
                
                this.address = address;
        }
        
        public int getPort()
        {
                return port;
        }
        
        public void setPort( final int port )
        {
                if ( port < 0 || port > 0xFFFF )
                        throw new IllegalArgumentException( "port não armazena um número de porta válida" );
                
                this.port = port;
        }        
        
        public long getIdentifier()
        {
                return identifier;
        }     
        
        public byte getPacketType()
        {
                return packetType;
        }       
         
        public Request getRequest()
        {
                if ( isExceptionType( packetType ) )
                        throw new RuntimeException( "Este tipo de pacote não contém Request." );
                
                return request;
        }
        
        public NetworkException getException()
        {
                if ( ! isExceptionType( packetType ) )
                        throw new RuntimeException( "Este tipo de pacote não contém exceção" );
                
                return exception;
        }
        
        public int getOrder()
        {
                if ( packetType != SIMPLE_RESULT_TYPE && packetType != SIMPLE_REQUEST_TYPE )
                        throw new RuntimeException( "Neste tipo de pacote não contém order" );
                
                return order;
        }
        
        public int getTotalPackets()
        {
                if ( packetType != SMART_RESULT_TYPE )
                        throw new RuntimeException( "Neste tipo de pacote não contém totalPackets" );

                return totalPackets;
        }

        public void setTotalPackets( final int totalPackets )
        {
                if ( packetType != SMART_RESULT_TYPE )
                        throw new RuntimeException( "Neste tipo de pacote não se pode fixar totalPackets" );
                
                if ( totalPackets < 1 )
                        throw new IllegalArgumentException( "totalPackets não pode ser menor que um" );

                this.totalPackets = totalPackets;
        }
        
        public byte[] getData()
        {
                if ( ! isRequestType( packetType ) && ! isResultType( packetType ) )
                        throw new RuntimeException( "Neste tipo de pacote não contém dados" );
                
                return data;
        }
        
        private static void fillStruct( final byte[] buf, 
                                        final long identifier, 
                                        final byte packetType, 
                                        final byte requestID, 
                                        final byte classID, 
                                        final byte exceptionClass,
                                        final int order, 
                                        final int totalPackets )
        {
                final byte[] identifierBytes = Core.toLongBytes( identifier ); 
                final byte[] orderBytes = Core.toIntBytes( order );
                final byte[] totalPacketBytes = Core.toIntBytes( totalPackets );

                for ( int i = INITIAL_IDENTIFIER_INDEX; i < INITIAL_IDENTIFIER_INDEX + Core.LONG_SIZE; i++ )
                        buf[i] = identifierBytes[i - INITIAL_IDENTIFIER_INDEX];

                buf[PACKET_TYPE_INDEX] = packetType;
                buf[REQUEST_ID_INDEX] = requestID;
                buf[CLASS_ID_INDEX] = classID;
                buf[EXCEPTION_CLASS_INDEX] = exceptionClass;

                for ( int i = INITIAL_ORDER_INDEX; i < INITIAL_ORDER_INDEX + Core.INT_SIZE; i++ )
                        buf[i] = orderBytes[i - INITIAL_ORDER_INDEX];

                for ( int i = INITIAL_TOTAL_PACKETS_INDEX; i < INITIAL_TOTAL_PACKETS_INDEX + Core.INT_SIZE; i++ )
                        buf[i] = totalPacketBytes[i - INITIAL_TOTAL_PACKETS_INDEX];       
        }
        
        private static void fillData( byte[] buf, byte[] data )
        {
                for ( int i = STRUCT_SIZE; i < buf.length; i++ )
                        buf[i] = data[i - STRUCT_SIZE];
        }      
        
        static long readIdentifier( byte[] buf )
        {
                final byte[] identifierBytes = new byte[Core.LONG_SIZE];
                
                for ( int i = INITIAL_IDENTIFIER_INDEX; i < INITIAL_IDENTIFIER_INDEX + Core.LONG_SIZE; i++ )
                        identifierBytes[i - INITIAL_IDENTIFIER_INDEX] = buf[i];
                
                return Core.toLongValue( identifierBytes );
        }
        
        private static byte readPacketType( byte[] buf )
        {
                return buf[PACKET_TYPE_INDEX];
        }

        private static byte readRequestID( byte[] buf )
        {
                return buf[REQUEST_ID_INDEX];
        }

        private static byte readClassID( byte[] buf )
        {
                return buf[CLASS_ID_INDEX];
        }

        private static byte readExceptionClass( byte[] buf )
        {
                return buf[EXCEPTION_CLASS_INDEX];
        }
        
        private static int readOrder( byte[] buf )
        {
                final byte[] orderBytes = new byte[Core.INT_SIZE];
                
                for ( int i = INITIAL_ORDER_INDEX; i < INITIAL_ORDER_INDEX + Core.INT_SIZE; i++ )
                        orderBytes[i - INITIAL_ORDER_INDEX] = buf[i];
                
                return Core.toIntValue( orderBytes );
        }

        private static int readTotalPackets( byte[] buf )
        {
                final byte[] totalPacketsBytes = new byte[Core.INT_SIZE];
                
                for ( int i = INITIAL_TOTAL_PACKETS_INDEX; i < INITIAL_TOTAL_PACKETS_INDEX + Core.INT_SIZE; i++ )
                        totalPacketsBytes[i - INITIAL_TOTAL_PACKETS_INDEX] = buf[i];
                
                return Core.toIntValue( totalPacketsBytes );
        }
        
        private static byte[] readData( byte[] buf, final int length )
        {
                 byte[] data = new byte[length - STRUCT_SIZE];
                
                 for ( int i = STRUCT_SIZE; i < length; i++ )
                         data[i - STRUCT_SIZE] = buf[i];
                 
                 return data;
        }
        
        private static boolean isValidPacketType( final byte packetType )
        {
                if ( isRequestType( packetType ) ) 
                        return true;
                else if ( isResultType( packetType ) )
                        return true;
                else if  ( isExceptionType( packetType ) )
                        return true;
                
                return false;

        }

        DatagramPacket toDatagramPacket()
        {              
                final byte[] buf;
                
                if ( isRequestType( packetType ) ^ isResultType( packetType ))
                {
                        buf = new byte[STRUCT_SIZE  + data.length];                           
                        fillData( buf, data );
                }
                else if (  isExceptionType( packetType ) )
                {
                        String exceptionMessage = exception.getMessage();
                        
                        if ( exceptionMessage != null )
                        {
                                buf = new byte[ STRUCT_SIZE + exceptionMessage.getBytes().length ];
                                fillData( buf, exceptionMessage.getBytes() );
                        }
                        else
                                buf = new byte[STRUCT_SIZE];
                }
                else
                        throw new InternalError( "Ajustar função toDatagramPacket "
                                                                    + "para funcionar com o novo tipo" );
                
                final byte exceptionClassIdentifier;
                        
                if ( exception != null )        
                        exceptionClassIdentifier = exception.getClassIdentifier();
                else 
                        exceptionClassIdentifier = -1;
                
                byte requestID = -1;
                byte classID = -1;
                
                if ( isExceptionType( packetType ) )
                {
                        if ( exception instanceof UnknownClassIDException )
                                classID = ( ( UnknownClassIDException ) exception ).getUnknownClassID();
                                
                        else if ( exception instanceof UnknownRequestIDException )
                        {
                                UnknownRequestIDException urIDe = ( UnknownRequestIDException ) exception;
                                requestID = urIDe.getUnknownRequestID();
                                
                                try
                                {
                                        classID = Request.getClassID( urIDe.getRequestClass() );
                                }
                                catch ( Exception e )
                                {
                                        throw new InternalError( e.getMessage() );
                                }
                        }
                        else if ( exception instanceof InvalidPacketFormatException ||
                                       exception instanceof NetworkException )
                        {
                                requestID = -1;
                                classID = -1;
                        }
                }
                else if ( request != null )
                {
                        requestID = request.getRequestID();
                        classID = request.getClassID();
                }
                else
                        throw new InternalError( "request é null e o pacote não é de exceção" );
                
                fillStruct( buf, 
                            identifier, 
                            packetType, 
                            requestID, 
                            classID, 
                            exceptionClassIdentifier, 
                            order, 
                            totalPackets );                
                
                return new DatagramPacket( buf, buf.length, address, port );
        }        
        
        static Packet datagramPacketToPacket( DatagramPacket datagramPacket ) throws NetworkException, Exception
        {      
                if ( datagramPacket == null )
                        throw new IllegalArgumentException( "datagramPacket é null" );
        
                final byte[] buf = datagramPacket.getData();

                if ( datagramPacket.getLength() < STRUCT_SIZE )
                        throw new InvalidPacketFormatException( datagramPacket  );

                final long identifier = readIdentifier( buf ); 
                final byte packetType = readPacketType( buf );
                final byte requestID = readRequestID( buf );
                final byte classID = readClassID( buf );
                final byte exceptionClass = readExceptionClass( buf );
                final int order = readOrder( buf );
                final int totalPackets = readTotalPackets( buf );
                byte[] data = readData( buf, datagramPacket.getLength() );

                if ( ! isValidPacketType( packetType ) )
                        throw new InvalidPacketFormatException( datagramPacket );               

                if ( order < -1 || totalPackets < -1 )
                        throw new InvalidPacketFormatException( datagramPacket );

                if ( order > -1 && totalPackets > -1 )
                        throw new InvalidPacketFormatException( datagramPacket );
                
                if ( ( packetType == SMART_REQUEST_TYPE  || isExceptionType( packetType ) ) && (  order != - 1 ||  totalPackets != -1 )  )
                        throw new InvalidPacketFormatException( datagramPacket );                
                
                if ( ( packetType == SIMPLE_REQUEST_TYPE || packetType == SIMPLE_RESULT_TYPE ) && ( order < 0 || totalPackets != -1 ) )
                        throw new InvalidPacketFormatException( datagramPacket );

                final String messageException = new String( data );
                
                if ( ! isExceptionType( packetType ) )
                {
                        if ( ! Request.isAddedRequestClass( classID ) )      
                        {
                                UnknownClassIDException exception = new UnknownClassIDException( classID );        
                                Packet exceptionPacket = createNetworkException( identifier, 
                                                                                 datagramPacket.getAddress(), 
                                                                                 datagramPacket.getPort(), 
                                                                                 exception );
                                exception.setExceptionPacket( exceptionPacket );
                                throw exception;
                        }
                        else
                        {
                               Class<? extends Request> requestClass = Request.getRequestClass( classID );     

                               if ( ! Request.isValidRequestID( requestID, requestClass ) )
                               {
                                       UnknownRequestIDException exception = new UnknownRequestIDException( requestID, requestClass );
                                       Packet exceptionPacket = createNetworkException( identifier, 
                                                                                        datagramPacket.getAddress(), 
                                                                                        datagramPacket.getPort(), 
                                                                                        exception );
                                       exception.setExceptionPacket( exceptionPacket );
                                       throw exception;
                               }
                        }    
                }

                final Request request; 

                if ( ! isExceptionType( packetType ) )
                        request = Request.createRequest( requestID, classID );
                else 
                        request = null;

                final NetworkException exception;

                if ( isExceptionType( packetType )  )
                        data = null;

                if ( isExceptionType( packetType ) )
                {
                        if ( exceptionClass == INVALID_PACKET_FORMAT_EXCEPTION )
                                exception = new InvalidPacketFormatException( datagramPacket );
                        else if ( exceptionClass == UNKNOWN_CLASS_ID_EXCEPTION )
                                exception = new UnknownClassIDException( classID, messageException );
                        else if ( exceptionClass == UNKNOWN_REQUEST_ID_EXCEPTION )
                                exception = new UnknownRequestIDException( requestID, 
                                                                           Request.getRequestClass( classID ),
                                                                           messageException );
                        else if ( exceptionClass == NETWORK_EXCEPTION )
                                exception = new NetworkException( messageException );
                        else
                                throw new InternalError( "datagramPacketToPacket" );
                }
                else 
                        exception = null;

                Packet result = new Packet( datagramPacket.getAddress(), 
                                                                    datagramPacket.getPort(), 
                                                                    identifier, 
                                                                    packetType, 
                                                                    request, 
                                                                    exception,
                                                                    order, 
                                                                    totalPackets, 
                                                                    data );
                
                if ( exception instanceof UnknownClassIDException )
                        ( ( UnknownClassIDException ) exception ).setExceptionPacket( result );
                else if ( exception instanceof UnknownRequestIDException )
                        ( ( UnknownRequestIDException ) exception ).setExceptionPacket( result );

                 return result;               
        }
        
        @Override
        public String toString()
        {
                StringBuilder buf = new StringBuilder();
                buf.append( "[address=" );
                buf.append( address.toString() );
                buf.append( ",port=" );
                buf.append( port );
                buf.append( ",packetType=" );
                
                if ( packetType == SMART_REQUEST_TYPE )
                        buf.append( "SMART_REQUEST_TYPE" );
                else if ( packetType == SIMPLE_REQUEST_TYPE )
                        buf.append( "SIMPLE_REQUEST_TYPE" );
                else if ( packetType == SMART_RESULT_TYPE )
                        buf.append( "SMART_RESULT_TYPE" );
                else if ( packetType == SIMPLE_RESULT_TYPE )
                        buf.append( "SIMPLE_RESULT_TYPE" );
                else if ( isExceptionType( packetType ) )
                {
                        buf.append( "EXCEPTION_TYPE" );
                        NetworkException except;
                        
                        try
                        {
                                except = getException();
                        }
                        catch ( Exception e )
                        {
                                throw new InternalError( "Packet.toString()" );
                        }
                        
                        buf.append( ",exception=" );
                        
                        if ( except instanceof UnknownClassIDException )
                                buf.append( "UnknownClassIDException" );
                        else if ( except instanceof UnknownRequestIDException )
                                buf.append( "UnknownRequestIDException" );
                        else if ( except instanceof NetworkException )        
                                buf.append( "NetworkException" );
                                
                        buf.append( ']' );
                        return buf.toString();
                }
                
                buf.append( ",request=" );
                buf.append( request.getClass().getSimpleName() );
                buf.append( '.' );
                buf.append( request.getRequestName() );
                
                if ( packetType == SIMPLE_REQUEST_TYPE || packetType == SIMPLE_RESULT_TYPE  )
                {
                        buf.append( ",order=" );
                        buf.append( order );
                }

                if ( packetType == SMART_RESULT_TYPE  )
                {
                        buf.append( ",totalPackets=" );
                        buf.append( totalPackets );
                }
                
                buf.append( ']' );
                return buf.toString();
        }
}