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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public abstract class Request
{      
        //Armazena o nome do campo de identificação de classe. Deve ser definido nas classes que extende a classe Request
        //senão é gerado uma exceção
        static final String CLASS_ID_FIELD_NAME = "CLASS_ID"; 
        
        //Identificação do pedido em forma númerica para armazenar no pacote
        private final byte requestID;
        
        //armazena o nome do campo que está relacionado com requestID
        private final String requestName;
        
       /*Os três métodos a baixo devem ser finais porque há a possibilidade dos valores requestID
         *  , requestName e CLASS_ID serem alterados se os dois métodos a baixo forem sobrescritos, já que é possível retornar qualquer valor.
        */
        
        public final byte getRequestID()
        {
                return requestID;
        }   
        
        public final String getRequestName()
        {
                return requestName;
        }       
        
        public final byte getClassID()
        {
                try
                {                                    
                        Field f = this.getClass().getField( CLASS_ID_FIELD_NAME );
                        return ( ( Byte ) f.get( null ) ).byteValue();
                }
                catch ( Exception e )
                {
                        throw new InternalError( "getClassID" );
                }
        }
        
        private static List<Class<? extends Request>> classList = new ArrayList<Class<? extends Request>>();      
        
        public static List<Class<? extends Request>> getRequestClassList()
        {
                return ( List ) ( ( ArrayList ) classList ).clone();
        }
        
        public static boolean isAddedRequestClass( Class<?> requestClass )
        {
                for ( Class<? extends Request> cls : classList )
                        if ( cls == requestClass )
                                return true;
                
                return false;
        } 
        
        public static boolean isAddedRequestClass( final byte classID )
        {
                for ( Class<? extends Request> cls : classList )
                        if ( getRequestClassID( cls ) == classID )
                                return true;
                
                return false;
        }

        private static byte getRequestClassID( Class<? extends Request> requestClass )
        {
                try
                {
                        Field f = requestClass.getField( CLASS_ID_FIELD_NAME );
                        return ( ( Byte ) f.get( null ) ).byteValue();
                }
                catch ( Exception e )
                {
                        throw new InternalError( "getRequestClassID( Class<? extends Request> requestClass )" );
                }
        }
        
        public static byte getClassID( Class<? extends Request> requestClass ) throws Exception
        {
                if ( ! isAddedRequestClass( requestClass ) )
                        throw new Exception( requestClass.getName() + " não foi adicionada na lista de classe de Request." );                
                
                return getRequestClassID( requestClass );
        }
        
        //Se o classID não for encontrado, então este método retorna <code>null</code>
        public static Class<? extends Request> getRequestClass( byte classID )
        {
                for ( Class<? extends Request> requestClass : classList )
                        if ( getRequestClassID( requestClass ) == classID )
                                return requestClass;
                
                return null;
        }
        
        /*Verifica se o <code>requestID</code> passado como parâmetro é 
         * válido para o <code>requestClass</code> passado como parâmetro
         */

        public static boolean isValidRequestID( final byte requestID, Class<? extends Request> requestClass )
        {
                Field[] fields = getRequestFields( requestClass );
                
                try
                {
                        for ( Field f : fields )
                        {
                                byte value = ( ( Byte ) f.get( null ) ).byteValue();

                                if ( value == requestID )
                                        return true;
                        }

                        return false;
                }
                catch ( Exception e )
                {
                        throw new InternalError( "isValidRequestID" );
                }
        }           
        
        private static boolean fieldExists( Class<?> cls, String field ) throws Exception
        {
                try
                {
                        cls.getField( field );
                        return true;
                }
                catch ( Exception e )
                {
                        if ( e instanceof NoSuchFieldException )
                                return false;
                        
                        throw e;
                }               
        }
        
        private static boolean isValidClassIDField( Field classIDField )
        {   
                final int mod = classIDField.getModifiers();
                
                if ( Modifier.isPublic( mod ) && 
                      Modifier.isStatic( mod ) && 
                      Modifier.isFinal( mod ) &&
                      classIDField.getType() == byte.class )
                        return true;
                else
                        return false;
                        
        }
        
        private static void checkRequestFields( Class<? extends Request> requestClass ) throws Exception
        {
                Field[] fields = getRequestFields( requestClass );
                
                if ( fields.length == 0  )      
                        throw new Exception( "Não existe nenhum campe de REQUEST em " + requestClass.getName() );
                
                for ( int i = 0; i < fields.length; i++ )
                {
                        Field firstField = fields[i];
                        final byte first = ( ( Byte ) firstField.get( null ) ).byteValue();
                        
                        for ( int j = 0; j < fields.length; j++ )
                        {
                                if ( i != j  )
                                {
                                        Field secondField = fields[j];
                                        final byte second = ( ( Byte ) secondField.get( null ) ).byteValue();
                                        
                                        if ( first == second )
                                                throw new Exception(  "A constante de REQUEST " + firstField.getName()  +"\n"
                                                                                         +  " está com o mesmo valor de requestID que \n"
                                                                                         + " a constante de REQUEST " + secondField.getName() + "\n"
                                                                                         + " em " + requestClass.getName() );
                                }
                        }
                }        
        }
        
        private static void checkClassID( Class<? extends Request> requestClass ) throws Exception
        {
                for ( int i = 0; i < classList.size(); i++ )
                {
                        final Class<? extends Request> cls = classList.get( i );                                                              
                        final byte firstClassID = getRequestClassID( cls );
                        final byte secondClassID = getRequestClassID( requestClass );

                        if ( firstClassID == secondClassID )
                                throw new Exception( "O valor de " + CLASS_ID_FIELD_NAME + " : " + firstClassID + " está sendo usado pela classe\n" 
                                                                      + cls.getName() + "."+ " Por isso, para adicionar a classe " + requestClass.getName() + "\n" 
                                                                      + " é necessário mudar o valor de " + CLASS_ID_FIELD_NAME + "\n"
                                                                      + "Para saber quais " + CLASS_ID_FIELD_NAME +  "s estão sendo usados\n"
                                                                      + "Invoque o método Request.getRequestClassList()");
                }            
        }
        
        private static void checkConstructors( Class<? extends Request> requestClass ) throws Exception
        {
                Constructor[] constructors = requestClass.getConstructors();                
                
                if ( constructors.length == 1 )
                {
                        Class<?>[]  param = constructors[0].getParameterTypes();                       
                        
                        Exception except = new Exception( "Em " + requestClass.getName() + " deve conter o construtor:\n public " 
                                                                        + requestClass.getSimpleName() + "( byte requestID ) throws Exception" );
                        
                        if ( param.length != 1 )
                                throw except;
                        
                        if ( param[0] != byte.class )
                                throw except;
                }
                else
                        throw new Exception( "Classes derivadas de Request devem conter somente um construtor. \n"
                                                               + " O construtor de " + requestClass.getName() + " deve ser da seguinte forma: \n public " 
                                                                        + requestClass.getSimpleName() + "( byte requestID ) throws Exception" );                
        }
        
        public static void addRequestClass( Class<? extends Request> requestClass ) throws Exception
        {
                if ( requestClass == null )
                        throw new Exception( "requestClass é null" );
                
                if ( isAddedRequestClass( requestClass )  )
                        throw new Exception( "A classe " + requestClass.getName() + " já está adicionada na lista de classes Request." );                
                  
                if ( ! fieldExists( requestClass, CLASS_ID_FIELD_NAME ) )
                        throw new Exception( "O campo " + CLASS_ID_FIELD_NAME + " não foi definido na classe " + requestClass.getName() );               

                if ( ! isValidClassIDField( requestClass.getField( CLASS_ID_FIELD_NAME ) ) )
                        throw new Exception( "O campo " + CLASS_ID_FIELD_NAME + " da classe " + 
                                                               requestClass.getName()  + " deve ser definido da seguinte\n"
                                                              + " Maneira: public static final byte " + CLASS_ID_FIELD_NAME + " = valor"  );
                                       
                checkConstructors( requestClass );
                checkClassID( requestClass );
                checkRequestFields( requestClass );
                
                classList.add( requestClass );
        }
        
        private static boolean isRequestField( Field f )
        {              
                final int mod = f.getModifiers();

                if ( Modifier.isPublic( mod ) && Modifier.isStatic( mod ) && Modifier.isFinal( mod ) )
                {
                        Class<?> fieldClass = f.getType();

                        if ( fieldClass == byte.class  )
                        {
                                final String fieldName = f.getName();

                                if ( fieldName.length() >= "REQUEST".length() )
                                {
                                        final String sub = fieldName.substring( fieldName.length() - "REQUEST".length(),  fieldName.length() );

                                        if ( "REQUEST".equals( sub ) )
                                                return true;
                                }
                        }
                }
                       
                return false;           
        }
        
        public static Field[] getRequestFields( Class<? extends Request> requestClass )
        {              
                Field[] fields = requestClass.getFields();
                List<Field> result = new ArrayList<Field>();
                
                for ( int i = 0; i < fields.length; i++ )                
                {
                        Field f = fields[i];
                        
                        if ( isRequestField( f ) )
                                result.add( f );
                }  
                             
                return ( Field[] )  result.toArray(  new Field[]{}  );
        }
        
        private String getRequestFieldName( final int requestID )
        {
                Field[] fields = getRequestFields( this.getClass() );
                
                try
                {
                        for ( Field f : fields )
                        {
                                final byte ID = ( ( Byte ) f.get( this ) ).byteValue();

                                if ( ID == requestID )
                                        return f.getName();
                        }
                        
                        throw new InternalError( "getRequestName" );
                }
                catch ( Exception e )
                {
                        throw new InternalError( "getRequestName" );
                }
        }                  
        
        public Request( final byte requestID ) throws Exception
        {   
                if ( ! isAddedRequestClass( this.getClass() ) )
                        throw new Exception( "A classe " + this.getClass().getName() + " não foi adicionada com a o método addRequestClass.\n"
                                                               + " Por isso, a não pode ser instanciada." );
                
                if ( ! isValidRequestID( requestID, ( Class<? extends Request> ) this.getClass() ) )
                {
                        String className = this.getClass().getName();
                        throw new Exception( "requestID \"" + requestID + "\" é inválido para a classe " + className );
                }
                
                this.requestID = requestID;
                this.requestName = getRequestFieldName( requestID );             
        }    
        
        public static Request createRequest( final byte requestID, final byte classID ) throws Exception
        {              
                for ( Class<? extends Request> cls : classList )
                {
                        final byte clsID = getRequestClassID( cls );
                        
                        if ( clsID == classID )
                        {
                                Constructor c = cls.getConstructor( byte.class );
                                
                                try
                                {
                                        /* Precisa do try porque não gera diretamente a exceção que 
                                         * está relacionada com constructor da classe de Request
                                         * e assim uma outra exceção. Para obter a exceção relacionada 
                                         * com o construtor tem-se que fazer o seguinte
                                         * <code>e.getCause()</code>
                                         * <code>e</code> é uma instância de exceção relacionado com o pacote "java.lang.reflect"
                                         * <code>e.getCause()</code> retorna a exceção relacionada com o construtor
                                         */
                                        return ( Request ) c.newInstance( requestID );
                                }
                                catch ( Exception e )
                                {
                                        //Pega a exceção relacionada com o construtor da classe derivada de Request
                                        throw new Exception( e.getCause() );
                                }
                        }
                }                
                
                throw new Exception( "ClassID \"" + classID + "\" não encontado na lista de classes" );
        }
        
        @Override
        public String toString()
        {
                StringBuilder buf = new StringBuilder();
                buf.append( "[requestID=" );
                buf.append( requestID );
                buf.append( ",requestName=" );
                buf.append( requestName );
                buf.append( ']' );
                return buf.toString();
        }
        
}