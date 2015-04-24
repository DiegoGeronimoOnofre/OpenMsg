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

package application.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/*
 * O objeto da classe derivada desta
 * pode ser convertido em array de bytes
 * para posteriormente ser convertido
 * novamente para um novo objeto.
 * 
 * OBS:
 * Classes derivadas desta podem conter 
 * somente campos de valores primitivos ou
 * <code>String</code> ou arrays de tipos 
 * primitivos ou arrays de <code>String</code> 
 * e somente um construtor sem parâmetros
 **/

public abstract class Convertible
{   
    private static final byte BOOLEAN = 0;
    private static final byte CHAR    = 1;
    private static final byte BYTE    = 2;
    private static final byte SHORT   = 3;
    private static final byte INT     = 4;
    private static final byte LONG    = 5;
    private static final byte FLOAT   = 6;
    private static final byte DOUBLE  = 7;
    private static final byte STRING  = 8;       

    private static final byte BOOLEAN_ARRAY = 9;
    private static final byte CHAR_ARRAY    = 10;
    private static final byte BYTE_ARRAY    = 11;
    private static final byte SHORT_ARRAY   = 12;
    private static final byte INT_ARRAY     = 13;
    private static final byte LONG_ARRAY    = 14;
    private static final byte FLOAT_ARRAY   = 15;
    private static final byte DOUBLE_ARRAY  = 16;
    private static final byte STRING_ARRAY  = 17;       
    
    private static Field[] getFields( Class<? extends Convertible> cls )
    {
        List<Field> list = new ArrayList<Field>( 100 );
        
        while ( cls != Convertible.class )
        {
            Field[] fields = cls.getDeclaredFields();

            for ( int i = 0; i < fields.length; i++ )
            {
                final Field f = fields[i];
                final int mod = f.getModifiers();
                
                if ( ! Modifier.isFinal( mod ) && 
                     ! Modifier.isStatic( mod ) &&
                     ( f.getType().isPrimitive() || f.getType() == String.class )   )
                {
                    list.add( f );
                }
                else if ( ! Modifier.isStatic( mod ) && f.getType().isArray() )
                {
                    list.add( f );
                }

            }
            
            cls =  cls.getSuperclass().asSubclass( Convertible.class );
        }
        
        return list.toArray( new Field[]{} );
    }
    
    private static byte findPrimitiveType( Class<?> cls )
    {
        if ( cls == boolean.class )
            return BOOLEAN;
        else if ( cls == char.class )
            return CHAR;
        else if ( cls == byte.class )
            return BYTE;
        else if ( cls == short.class )
            return SHORT;
        else if ( cls == int.class )
            return INT;
        else if ( cls == long.class )
            return LONG;
        else if ( cls == float.class )
            return FLOAT;
        else if ( cls == double.class )
            return DOUBLE;
        else if ( cls == String.class )
            return STRING;  
        else
            return -1;          
    }
    
    private static byte findArrayType( Class<?> cls )
    {
        if ( cls == boolean[].class )
            return BOOLEAN_ARRAY;
        else if ( cls == char[].class )
            return CHAR_ARRAY;
        else if ( cls == byte[].class )
            return BYTE_ARRAY;
        else if ( cls == short[].class )
            return SHORT_ARRAY;
        else if ( cls == int[].class )
            return INT_ARRAY;
        else if ( cls == long[].class )
            return LONG_ARRAY;
        else if ( cls == float[].class )
            return FLOAT_ARRAY;
        else if ( cls == double[].class )
            return DOUBLE_ARRAY;
        else if ( cls == String[].class )
            return STRING_ARRAY;
        else
            return -1;       
    }
    
    /* Se o tipo não for primitivo ou 
     * String ou array de primitivo ou
     * array de <code>String</code>, 
     * então este método retorna <code>null</code>
     **/
    
    private static Class<?> findClass( final int type )
    {
        switch ( type )
        {
            case BOOLEAN :
                return boolean.class;
            case CHAR :
                return char.class;
            case BYTE :
                return byte.class;
            case SHORT :
                return short.class;
            case INT :
                return int.class;
            case LONG :
                return long.class;
            case FLOAT :
                return float.class;
            case DOUBLE :
                return double.class;
            case STRING :
                return String.class;

            case BOOLEAN_ARRAY :
                return boolean[].class;
            case CHAR_ARRAY :
                return char[].class;
            case BYTE_ARRAY :
                return byte[].class;
            case SHORT_ARRAY :
                return short[].class;
            case INT_ARRAY :
                return int[].class;
            case LONG_ARRAY :
                return long[].class;
            case FLOAT_ARRAY :
                return float[].class;
            case DOUBLE_ARRAY :
                return double[].class;
            case STRING_ARRAY :
                return String[].class;
            default :
                return null;
        }
    }
    
    private static byte[] toValueBytes( final byte type, final Object value )
    {
        switch ( type )
        {
            case BOOLEAN :
            {
                final boolean bValue = ( Boolean ) value;
                return new byte[]{ Core.toBooleanByte( bValue ) };
            }
            case CHAR :
            {
                final char cValue = ( Character ) value;
                return Core.toCharBytes( cValue );
            }
            case BYTE :
            {
                final byte byteValue = ( Byte ) value;
                return new byte[]{ byteValue };
            }
            case SHORT :
            {
                final short sValue = ( Short ) value;
                return Core.toShortBytes( sValue );
            }                        
            case INT :
            {
                final int iValue = ( Integer ) value;
                return Core.toIntBytes( iValue );
            }                        
            case LONG :
            {
                final long lValue = ( Long ) value;
                return Core.toLongBytes( lValue );
            }                        
            case FLOAT :
            {
                final float fValue = ( Float ) value;
                return Core.toFloatBytes( fValue );
            }                        
            case DOUBLE :
            {
                final double dValue = ( Double ) value;
                return Core.toDoubleBytes( dValue );
            }   
            default : 
                throw new InternalError( "typo desconhecido: " + type );
        }    
    }
    
    private static void addBytes( List<Byte> list, byte[] bytes )
    {
        for ( int i = 0; i < bytes.length; i++ )
            list.add( bytes[i] );
    }
    
    /*
     * Este método converte o Objeto em 
     * um array de bytes
     * 
     * 
     * O nome da classe é armazenado da 
     * seguinte forma no array de bytes
     *  1      2
     *  ## - #####
     * 
     * 1 - Tamanho do nome da classe
     * 2 - Nome da classe
     * 
     * 
     * Se o tipo do campo for primitivo, então as informações do
     * campo do objeto ( ? extends Convertible ) são gravadas da seguinte forma no 
     * array de bytes:
     *  
     *  1      2     3    4
     *  ## - ##### - # - ### 
     * 
     * 1 - Tamanho do nome do campo
     * 2 - Nome do campo
     * 3 - Tipo do campo
     * 4 - Valor do campo
     * 
     *
     * Se o tipo do campo for <code>String</code>, então as informações do
     * campo do objeto ( ? extends Convertible ) são gravadas da seguinte forma no 
     * array de bytes:
     *  
     *  1      2     3    4     5        6  
     *  ## - ##### - # - #### - # - #############
     * 
     * 1 - Tamanho do nome do campo
     * 2 - Nome do campo
     * 3 - Tipo do campo
     * 4 - Tamanho da <code>String</code>
     * 5 - Para determinar se a <code>String</code> é <code>null</code>
     * 6 - Os caracteres da <code>String</code>
     * 
     * 
     * Se o tipo do campo for um array de tipo primitivo, então as informações do
     * campo do objeto ( ? extends Convertible ) são gravadas da seguinte forma no 
     * array de bytes:
     *  
     *  1      2     3    4     5    6    7   8
     *  ## - ##### - # - #### - # - ## - ## - ##
     * 
     * 1 - Tamanho do nome do campo
     * 2 - Nome do campo
     * 3 - Tipo do campo
     * 4 - Tamanho do array
     * 5 - Para determinar se o array é <code>null</code>
     * 6 - Primeiro valor do array
     * 7 - Segundo valor do array
     * 8 - Terceiro valor do array
     * 
     * 
     * Se o tipo do campo for um array de String, então as informações do
     * campo do objeto ( ? extends Convertible ) são gravadas da seguinte forma no 
     * array de bytes:
     *  
     *  1      2     3    4     5    6     7      8
     *  ## - ##### - # - #### - # - #### - # - #######
     * 
     * 1 - Tamanho do nome do campo
     * 2 - Nome do campo
     * 3 - Tipo do campo
     * 4 - Tamanho do array
     * 5 - Para determinar se o array é <code>null</code>
     * 6 - Tamanho da primeira <code>String</code>
     * 7 - Para determinar se a primeira <code>String</code> é <code>null</code>
     * 8 - Caracteres da primeira <code>String</code>
     * 
     **/
    
    public final byte[] toBytes()
    {
        //Lista para armazenar o objeto
        List<Byte> list = new ArrayList<Byte>( 250 );
        Class<? extends Convertible> cls = getClass();
        
        /*
         * clsLength armazena o número
         * total de bytes que o nome da 
         * classe vai ocupar no array
         */
        
        if ( cls.getName().length() > 0x7fff )
            throw new UnsupportedOperationException( "Número de bytes do nome da "
                                                     + "classe é maior que o suportado." );
        
        final byte[] clsLength = Core.toShortBytes( ( ( short ) cls.getName().length() ) );
        //Armazena o tamanho do nome da classe
        addBytes( list, clsLength );
        final byte[] className = cls.getName().getBytes();       
        //Armazena o nome da classe
        addBytes( list, className );
        Field[] fields = getFields( this.getClass() );
        
        for ( int i = 0; i < fields.length; i++ )
        {
            final Field f = fields[i];
            
            if ( f.getName().length() > 0x7fff )
                throw new UnsupportedOperationException( "Número de bytes do nome do "
                                                         + "campo \"" + f.getName() + "\" é "
                                                         + "maior que o suportado." );            
            
            final byte[] fieldLength = Core.toShortBytes( ( ( short ) f.getName().length() ) );
            final byte[] fieldName = f.getName().getBytes();
            final byte fieldType;
            
            if ( f.getType().isArray() )
                fieldType = findArrayType( f.getType() );
            else if ( f.getType().isPrimitive() || f.getType() == String.class )
                fieldType = findPrimitiveType( f.getType() );
            else
                throw new InternalError( f.getName() );
            
            if ( fieldType == -1 )
                throw new InternalError( "Tipo desconhecido: " + fieldType );
            
            final Object value;
            f.setAccessible( true );                     
            //Armazena o tamanho do nome do campo
            addBytes( list, fieldLength );
            //Armazena o nome do campo
            addBytes( list, fieldName );            
            //Armazena o tipo do campo
            list.add( fieldType );
            
            try 
            {
                value = f.get( this );            
            }
            catch ( Exception e )
            {
                throw new InternalError( e.toString() );
            }            
                       
            if ( f.getType().isArray() )
            {
                final int arrayLen;
                
                if ( value != null )
                    arrayLen = Array.getLength( value );
                else
                    arrayLen = 0;
                
                final byte[] arrayLength = Core.toIntBytes( arrayLen );
                //Armazena o tamanho do array
                addBytes( list, arrayLength );
                
                /*
                 * Se o array for <code>null</code>,
                 * então armazena <code>true</code>,
                 * senão armazena <code>false</code>
                 */
                
                if ( value == null )
                    list.add( Core.toBooleanByte( true ) );
                else
                    list.add( Core.toBooleanByte( false ) );
                
                if ( value != null )
                {
                    switch ( fieldType )
                    {
                        case BOOLEAN_ARRAY:
                        {
                            for ( int j = 0; j < arrayLen; j++ )
                            {
                                final boolean val      = Array.getBoolean( value, j );
                                final byte booleanByte = Core.toBooleanByte( val );
                                list.add( booleanByte );
                            }

                            break;
                        }
                        case CHAR_ARRAY:
                        {
                            for ( int j = 0; j < arrayLen; j++ )
                            {
                                final char val         = Array.getChar( value, j );
                                final byte[] charBytes = Core.toCharBytes( val );
                                addBytes( list, charBytes );
                            }

                            break;
                        }
                        case BYTE_ARRAY:
                        {
                            for ( int j = 0; j < arrayLen; j++ )
                            {
                                final byte val = Array.getByte( value, j );                          
                                list.add( val );
                            }

                            break;
                        }
                        case SHORT_ARRAY:
                        {
                            for ( int j = 0; j < arrayLen; j++ )
                            {
                                final short val         = Array.getShort( value, j );
                                final byte[] shortBytes = Core.toShortBytes( val );
                                addBytes( list, shortBytes );
                            }

                            break;
                        }                        
                        case INT_ARRAY:
                        {
                            for ( int j = 0; j < arrayLen; j++ )
                            {
                                final int val         = Array.getInt( value, j );
                                final byte[] intBytes = Core.toIntBytes( val );
                                addBytes( list, intBytes );
                            }

                            break;
                        }                        
                        case LONG_ARRAY:
                        {
                            for ( int j = 0; j < arrayLen; j++ )
                            {
                                final long val         = Array.getLong( value, j );
                                final byte[] longBytes = Core.toLongBytes( val );
                                addBytes( list, longBytes );
                            }

                            break;
                        }                        
                        case FLOAT_ARRAY:
                        {
                            for ( int j = 0; j < arrayLen; j++ )
                            {
                                final float val         = Array.getFloat( value, j );
                                final byte[] floatBytes = Core.toFloatBytes( val );
                                addBytes( list, floatBytes );
                            }

                            break;
                        }                        
                        case DOUBLE_ARRAY:
                        {
                            for ( int j = 0; j < arrayLen; j++ )
                            {
                                final double val         = Array.getDouble( value, j );
                                final byte[] doubleBytes = Core.toDoubleBytes( val );
                                addBytes( list, doubleBytes );
                            }

                            break;
                        }                        
                        case STRING_ARRAY:
                        {
                            final byte[] empty    = Core.toIntBytes( 0 );
                            final byte trueValue  = Core.toBooleanByte( true );
                            final byte falseValue = Core.toBooleanByte( false );

                            for ( int j = 0; j < arrayLen; j++ )
                            {
                                final String val = ( String ) Array.get( value, j );
                                final byte[] stringBytes;

                                if ( val != null )
                                    stringBytes = val.getBytes();
                                else
                                    stringBytes = null;

                                final byte[] stringLength;

                                if ( stringBytes != null )
                                    stringLength = Core.toIntBytes( stringBytes.length );
                                else
                                    stringLength = null;

                                if ( stringLength != null )
                                    addBytes( list, stringLength );
                                else
                                    addBytes( list, empty );

                                if ( stringBytes != null )
                                {
                                    list.add( falseValue );                                                          
                                    addBytes( list, stringBytes );
                                }
                                else
                                    list.add( trueValue );
                            }

                            break;
                        }                        

                        default :
                            throw new InternalError( String.valueOf( fieldType ) );
                    }
                }
            }                                     
            else if ( fieldType == STRING )
            {
                final byte[] string;
                    
                if ( value != null )
                    string = ( ( String ) value ).getBytes();
                else
                    string = null;
                
                final byte[] stringLength;
                
                if ( string != null )
                    stringLength = Core.toIntBytes( string.length );
                else
                    stringLength = Core.toIntBytes( 0 );
                
                /*Armazena como um <code>int</code> 
                 * o tamanho da <code>String</code>
                 **/ 
                 addBytes( list, stringLength );

                 /*Se a <code>String</code> 
                  * for <code>null</code>,
                  * então armazena <code>true</code>, 
                  * senão armazena <code>false</code>
                  */
                if ( string == null )
                    list.add( Core.toBooleanByte( true ) );
                else
                    list.add( Core.toBooleanByte( false ) );
                                            
                if ( string != null )
                    addBytes( list, string );            
            }
            else
            {
                final byte[] valueBytes = toValueBytes( fieldType, value );
                addBytes( list, valueBytes );       
            }
        }
        
        final byte[] result = new byte[list.size()];
        
        for ( int i = 0; i < result.length; i++ )
            result[i] = list.get( i );
        
        return result;
    }
    
    private static int getPrimitiveTypeLength( final Class<?> cls )
    {
        if ( cls == boolean.class )
            return Core.BOOLEAN_SIZE;
        else if ( cls == char.class )
            return Core.CHAR_SIZE;
        else if ( cls == byte.class )
            return Core.BYTE_SIZE;
        else if ( cls == short.class )
            return Core.SHORT_SIZE;
        else if ( cls == int.class )
            return Core.INT_SIZE;
        else if ( cls == long.class )
            return Core.LONG_SIZE;
        else if ( cls == float.class )
            return Core.FLOAT_SIZE;
        else if ( cls == double.class )
            return Core.DOUBLE_SIZE;
        else
            return -1;            
    }
    
    /*
     * Se o campo não for encontrado, 
     * então retorna <code>null</code>
     **/
    
    private static Field findField( String name, Field[] fields )
    {       
        for ( int i = 0; i < fields.length; i++ )
        {
            final Field f = fields[i];
            
            if ( name.equals( f.getName() ) )
                return f;
        }
        
        return null;
    }
    
    /*
     * Este método converte o array de 
     * bytes em um objeto de classe derivada de 
     * <code>Convertible</code>
     * 
     * public class XObj extends Convertible
     * {
     *      private String s = "String value";
     * 
     *      public boolean equals( Object o )
     *      {
     *          return s.equals( ( ( XObj ) o ).s );
     *      }
     * }
     * 
     * XObj first = new XObj();
     * byte[] bytes = c.toBytes();
     * XObj second = ( XObj ) Convertible.bytesToObject( bytes );
     * 
     * first.equals( second ) == true
     * 
     **/
    
    public static Convertible bytesToObject( byte[] bytes ) throws Exception
    {
        if ( bytes == null )
            throw new IllegalArgumentException( "bytes é null." );
        
        if ( bytes.length < Core.SHORT_SIZE )
            throw new Exception( "bytes não contém um Objeto válido." );
        
        final short clsLength = Core.toShortValue( new byte[]{ bytes[0], bytes[1] } );

        if ( clsLength + Core.SHORT_SIZE > bytes.length )
            throw new Exception( "bytes não contém um Objeto válido." );
        
        final byte[] clsName = Arrays.copyOfRange( bytes, 
                                                   Core.SHORT_SIZE, 
                                                   Core.SHORT_SIZE 
                                                   + clsLength );

        ClassLoader cl = ClassLoader.getSystemClassLoader();
        final String className = new String( clsName );
        final Convertible obj;
        final Class<? extends Convertible> cls;

        try
        {   
            cls = cl.loadClass( className ).asSubclass( Convertible.class );
            obj = cls.getConstructor().newInstance();
        }
        catch ( ClassNotFoundException e )
        {
            throw new Exception( "bytes não contém um Objeto válido." );
        }
        catch ( InstantiationException e )
        {
            throw new InternalError( e.toString() );
        }
        catch ( InvocationTargetException e )
        {
            throw new InvocationTargetException( e.getCause(), "A exceção \"" + 
                                                 e.getCause().toString() + "\" não "
                                                 + " permite gerar instâncias de \"" 
                                                 + className + "\"" );
        }

        final Field[] fields = getFields( cls ); 
        
        for ( int i = clsLength + Core.SHORT_SIZE; i < bytes.length; )
        {
            if ( i + Core.SHORT_SIZE > bytes.length )
                throw new Exception( "bytes não contém um Objeto válido." );
            
            final short fldLength = Core.toShortValue( new byte[]{ bytes[i], bytes[i + 1] } );
            i = i + Core.SHORT_SIZE;
            
            if ( i + fldLength > bytes.length )
                throw new Exception( "bytes não contém um Objeto válido." );
                
            final byte[] fldName = Arrays.copyOfRange( bytes, i, i + fldLength ); 
            i = i + fldLength;
            final String fieldName = new String( fldName );
            
            if ( i + Core.BYTE_SIZE > bytes.length )
                throw new Exception( "bytes não contém um Objeto válido." );
            
            final byte fieldType = bytes[i];
            i = i + Core.BYTE_SIZE;            
            final Field f = findField( fieldName, fields );
               
            if ( f == null )
                throw new Exception( "bytes não contém um Objeto válido." );
            
            /*
             * Verifica se o tipo do campo que está 
             * no vetor de bytes é diferente ao tipo
             * do campo que está na estrutura da classe
             */
            
            if ( findClass( fieldType ) != f.getType() )
                throw new Exception( "O tipo do campo \"" + fieldName + "\" que está"
                                   + " no vetor de bytes não é igual ao que está"
                                   + "na classe \"" + cls.getName() + "\"" );

            if ( ! f.getType().isArray() && Modifier.isFinal( f.getModifiers() ) )
                throw new Exception( "O campo \"" + f.getName() + "\" da classe \""
                                     + cls.getName() + "\" é final." );

            if ( Modifier.isStatic( f.getModifiers() ) )
                throw new Exception( "O campo \"" + f.getName() + "\" da classe \""
                                    + cls.getName() + "\" é static." );

            f.setAccessible( true );

            if ( f.getType().isArray() )
            {
                if ( i + Core.INT_SIZE > bytes.length )
                    throw new Exception( "bytes não contém um Objeto válido." );
                
                final int arrayLength = Core.toIntValue( new byte[]{ bytes[i],
                                                                     bytes[i + 1], 
                                                                     bytes[i + 2],   
                                                                     bytes[i + 3]  } );
                
                i = i + Core.INT_SIZE;
                
                if ( i + Core.BOOLEAN_SIZE > bytes.length )
                    throw new Exception( "bytes não contém um Objeto válido." );
                
                if ( bytes[i] != 0 && bytes[i] != 1 )
                    throw new Exception( "bytes não contém um Objeto válido." );
                    
                final boolean isNullArray = Core.toBooleanValue( bytes[i] );
                i = i + Core.BOOLEAN_SIZE;
                
                if ( isNullArray && arrayLength != 0 )
                    throw new Exception( "bytes não contém um Objeto válido." );
                
                if ( ! isNullArray )
                {
                    switch ( fieldType )
                    {
                        case BOOLEAN_ARRAY :
                        {     
                            if ( i + Core.BOOLEAN_SIZE * arrayLength > bytes.length )
                                throw new Exception( "bytes não contém um Objeto válido." );                        

                            Object array = Array.newInstance( boolean.class, arrayLength );

                            for ( int j = 0; j < arrayLength; j++ )
                            {
                                final boolean value = Core.toBooleanValue( bytes[i] );
                                Array.setBoolean( array, j, value );
                                i = i + Core.BOOLEAN_SIZE;
                            }

                            f.set( obj, array );
                            break;
                        }
                        case CHAR_ARRAY :
                        {
                            if ( i + Core.CHAR_SIZE * arrayLength > bytes.length )
                                throw new Exception( "bytes não contém um Objeto válido." );                         

                            Object array = Array.newInstance( char.class, arrayLength );

                            for ( int j = 0; j < arrayLength; j++ )
                            {
                                final char value = Core.toCharValue( new byte[]{ bytes[i], 
                                                                                 bytes[i + 1] } );
                                Array.setChar( array, j, value );
                                i = i + Core.CHAR_SIZE;
                            }

                            f.set( obj, array );
                            break;
                        }
                        case BYTE_ARRAY :
                        {  
                            if ( i + Core.BYTE_SIZE * arrayLength > bytes.length )
                                throw new Exception( "bytes não contém um Objeto válido." );                         

                            Object array = Array.newInstance( byte.class, arrayLength );

                            for ( int j = 0; j < arrayLength; j++ )
                            {
                                final byte value = bytes[i];
                                Array.setByte( array, j, value );
                                i = i + Core.BYTE_SIZE;
                            }

                            f.set( obj, array );
                            break;
                        }
                        case SHORT_ARRAY :
                        {     
                            if ( i + Core.SHORT_SIZE * arrayLength > bytes.length )
                                throw new Exception( "bytes não contém um Objeto válido." );                          

                            Object array = Array.newInstance( short.class, arrayLength );

                            for ( int j = 0; j < arrayLength; j++ )
                            {
                                final short value = Core.toShortValue( new byte[]{ bytes[i], 
                                                                                   bytes[i + 1] } );
                                Array.setShort( array, j, value );
                                i = i + Core.SHORT_SIZE;
                            }

                            f.set( obj, array );
                            break;
                        }
                        case INT_ARRAY :
                        {   
                            if ( i + Core.INT_SIZE * arrayLength > bytes.length )
                                throw new Exception( "bytes não contém um Objeto válido." );                          

                            Object array = Array.newInstance( int.class, arrayLength );

                            for ( int j = 0; j < arrayLength; j++ )
                            {
                                final int value = Core.toIntValue( new byte[]{ bytes[i],
                                                                               bytes[i + 1], 
                                                                               bytes[i + 2], 
                                                                               bytes[i + 3] 
                                                                               } );
                                Array.setInt( array, j, value );
                                i = i + Core.INT_SIZE;
                            }

                            f.set( obj, array );
                            break;
                        }
                        case LONG_ARRAY :
                        { 
                            if ( i + Core.LONG_SIZE * arrayLength > bytes.length )
                                throw new Exception( "bytes não contém um Objeto válido." );                          

                            Object array = Array.newInstance( long.class, arrayLength );

                            for ( int j = 0; j < arrayLength; j++ )
                            {
                                final long value = Core.toLongValue( new byte[]{ bytes[i],
                                                                                 bytes[i + 1],
                                                                                 bytes[i + 2],
                                                                                 bytes[i + 3],
                                                                                 bytes[i + 4],
                                                                                 bytes[i + 5],
                                                                                 bytes[i + 6],
                                                                                 bytes[i + 7]
                                                                               } );
                                Array.setLong( array, j, value );
                                i = i + Core.LONG_SIZE;
                            }

                            f.set( obj, array );
                            break;
                        }
                        case FLOAT_ARRAY :
                        {  
                            if ( i + Core.FLOAT_SIZE * arrayLength > bytes.length )
                                throw new Exception( "bytes não contém um Objeto válido." );                          

                            Object array = Array.newInstance( float.class, arrayLength );

                            for ( int j = 0; j < arrayLength; j++ )
                            {
                                final float value = Core.toFloatValue( new byte[]{ bytes[i], 
                                                                                   bytes[i + 1], 
                                                                                   bytes[i + 2], 
                                                                                   bytes[i + 3]
                                                                                  } );
                                Array.setFloat( array, j, value );
                                i = i + Core.FLOAT_SIZE;
                            }

                            f.set( obj, array );
                            break;
                        }
                        case DOUBLE_ARRAY :
                        {  
                            if ( i + Core.DOUBLE_SIZE * arrayLength > bytes.length )
                                throw new Exception( "bytes não contém um Objeto válido." );                          

                            Object array = Array.newInstance( double.class, arrayLength );

                            for ( int j = 0; j < arrayLength; j++ )
                            {
                                final double value = Core.toDoubleValue( new byte[]{ bytes[i],
                                                                                     bytes[i + 1],
                                                                                     bytes[i + 2],
                                                                                     bytes[i + 3],
                                                                                     bytes[i + 4],
                                                                                     bytes[i + 5],
                                                                                     bytes[i + 6],
                                                                                     bytes[i + 7]
                                                                                   });
                                Array.setDouble( array, j, value );
                                i = i + Core.DOUBLE_SIZE;
                            }

                            f.set( obj, array );
                            break;
                        }
                        case STRING_ARRAY :
                        {                       
                            Object array = Array.newInstance( String.class, arrayLength );

                            for ( int j = 0; j < arrayLength; j++ )
                            {
                                if ( i + Core.INT_SIZE > bytes.length )
                                    throw new Exception( "bytes não contém um Objeto válido." );                              

                                final int stringLength = Core.toIntValue( new byte[]{ bytes[i],
                                                                                      bytes[i + 1],      
                                                                                      bytes[i + 2],      
                                                                                      bytes[i + 3]      
                                                                                    } );

                                i = i + Core.INT_SIZE;

                                if ( i + Core.BYTE_SIZE + stringLength > bytes.length )
                                    throw new Exception( "bytes não contém um Objeto válido." );  

                                if ( bytes[i] != 0 && bytes[i] != 1 )
                                    throw new Exception( "bytes não contém um Objeto válido." );  

                                final boolean isNull = Core.toBooleanValue( bytes[i] );  
                                i = i + Core.BOOLEAN_SIZE;
                                final String string;
                                

                                if ( i + stringLength > bytes.length )
                                    throw new Exception( "bytes não contém um Objeto válido." );                                

                                if ( ! isNull )
                                    string = new String( Arrays.copyOfRange( bytes, i, i + stringLength  ) );
                                else
                                    string = null;

                                if ( isNull && stringLength != 0 )
                                    throw new Exception( "bytes não contém um Objeto válido." );

                                Array.set( array, j, string );
                                i = i + stringLength;
                            }

                            f.set( obj, array );
                            break;
                        }
                    }
                }
                else
                    f.set( obj, null );
            }
            else if ( fieldType == STRING )
            {
                if ( i + Core.INT_SIZE > bytes.length )
                    throw new Exception( "bytes não contém um Objeto válido." );
                
                final int stringLength = Core.toIntValue( new byte[]{ bytes[i], 
                                                                      bytes[i + 1],  
                                                                      bytes[i + 2],  
                                                                      bytes[i + 3]  
                                                                    } );
                i = i + Core.INT_SIZE;
                
                if ( i + Core.BOOLEAN_SIZE > bytes.length )
                    throw new Exception( "bytes não contém um Objeto válido." );
                
                if ( bytes[i] != 0 && bytes[i] != 1 )
                    throw new Exception( "bytes não contém um Objeto válido." );                  
                
                final boolean isNull = Core.toBooleanValue( bytes[i] );
                i = i + Core.BOOLEAN_SIZE;
                final String string;

                if ( i + stringLength > bytes.length )
                    throw new Exception( "bytes não contém um Objeto válido." );
                
                if ( ! isNull )
                    string = new String( Arrays.copyOfRange( bytes, i, i + stringLength ) );
                else 
                    string = null;

                i = i + stringLength;
                f.set( obj, string );
            }
            else
            {
                if ( i + getPrimitiveTypeLength( f.getType() ) > bytes.length )
                    throw new Exception( "bytes não contém um Objeto válido." );
                
                switch ( fieldType )
                {
                    case BOOLEAN :
                    {
                        final boolean bValue = Core.toBooleanValue( bytes[i] );
                        f.set( obj, bValue );
                        i = i + Core.BOOLEAN_SIZE;
                        break;
                    }
                    case CHAR :
                    {
                        final char cValue = Core.toCharValue( new byte[]{ bytes[i], bytes[i + 1] } );
                        f.set( obj, cValue );
                        i = i + Core.CHAR_SIZE;
                        break;
                    }
                    case BYTE :
                    {
                        final byte bValue = bytes[i];
                        f.set( obj, bValue );
                        i = i + Core.BYTE_SIZE;
                        break;
                    }
                    case SHORT :
                    {
                        final short sValue = Core.toShortValue( new byte[]{ bytes[i], bytes[i + 1] } );
                        f.set( obj, sValue );
                        i = i + Core.SHORT_SIZE;
                        break;
                    }
                    case INT :
                    {
                        final int iValue = Core.toIntValue( new byte[]{ bytes[i], 
                                                                        bytes[i + 1], 
                                                                        bytes[i + 2], 
                                                                        bytes[i + 3], 
                                                                      } );
                        f.set( obj, iValue );
                        i = i + Core.INT_SIZE;
                        break;
                    }
                    case LONG :
                    {
                        final long lValue = Core.toLongValue( new byte[]{ bytes[i], 
                                                                          bytes[i + 1], 
                                                                          bytes[i + 2], 
                                                                          bytes[i + 3], 
                                                                          bytes[i + 4], 
                                                                          bytes[i + 5], 
                                                                          bytes[i + 6], 
                                                                          bytes[i + 7] 
                                                                      } );
                        f.set( obj, lValue );
                        i = i + Core.LONG_SIZE;
                        break;
                    }
                    case FLOAT :
                    {
                        final float fValue = Core.toFloatValue( new byte[]{ bytes[i], 
                                                                            bytes[i + 1], 
                                                                            bytes[i + 2], 
                                                                            bytes[i + 3]
                                                                          } );
                        f.set( obj, fValue );
                        i = i + Core.FLOAT_SIZE;
                        break;
                    }
                    case DOUBLE :
                    {
                        final double dValue = Core.toDoubleValue( new byte[]{ bytes[i], 
                                                                              bytes[i + 1], 
                                                                              bytes[i + 2], 
                                                                              bytes[i + 3], 
                                                                              bytes[i + 4], 
                                                                              bytes[i + 5], 
                                                                              bytes[i + 6], 
                                                                              bytes[i + 7] 
                                                                              } );
                        f.set( obj, dValue );
                        i = i + Core.DOUBLE_SIZE;
                        break;
                    }                        
                }
            }
        }

        return obj;                  
    }
    
    private static List<Class<? extends Convertible>> classList = new ArrayList<Class<? extends Convertible>>();
    
    private static final Object classListLock = new Object();
    
    private static boolean isAdded( Class<? extends Convertible> cls )
    {
        for ( int i = 0; i < classList.size(); i++ )
            if ( classList.get( i ) == cls )
                return true;
        
        return false;
    }
    
    private static void checkConstructor( Class<? extends Convertible>  cls ) throws Exception
    {
        Constructor[] constructors = cls.getConstructors();
        final String msg = "Classes derivadas de \"" + Convertible.class.getName() 
                               + "\" devem conter somente um construtor e "
                               + "o mesmo não deve conter parâmetros.";
        
        if ( constructors.length != 1 )
            throw new Exception( msg );
        
        if ( constructors[0].getParameterTypes().length != 0 )
            throw new Exception( msg );    
    }
    
    private static Field[] getAllFields( Class<? extends Convertible> cls )
    {
        List<Field> list = new ArrayList<Field>( 100 );
        
        while ( cls != Convertible.class )
        {
            Field[] fields = cls.getDeclaredFields();

            for ( int i = 0; i < fields.length; i++ )
            {
                final Field f = fields[i];
                list.add( f );
            }
            
            cls =  cls.getSuperclass().asSubclass( Convertible.class );
        }
        
        return list.toArray( new Field[]{} );
    }    
    
    private static void checkFields( Class<? extends Convertible> cls ) throws Exception
    {
        Field[] fields = getAllFields( cls );
        
        for ( int i = 0; i < fields.length; i++ )
        {
            final Field f = fields[i];
            final int mod = f.getModifiers();
            final Class<?> t = f.getType();
            final Class<?> compType = t.getComponentType();
            final String msg = "O campo \"" + f.getName() + "\" da classe \"" 
                                + f.getDeclaringClass().getName()
                                + "\" é variável, de instância"
                                + " e não é um tipo primitivo, String ou array de tipo "
                                + "primitivo ou array de String";
            
            if ( ! Modifier.isStatic( mod ) &&
                 t.isArray()                && 
                 ! compType.isPrimitive()   && 
                 compType != String.class )       
            {
                throw new Exception( msg );                
            }
            
            if ( ! t.isArray()                &&
                 ! Modifier.isFinal( mod )    &&
                 ! Modifier.isStatic( mod )   &&
                 ! t.isPrimitive()            &&
                 t != String.class
               )
            {
                throw new Exception( msg );               
            }
        }
    }
    
    private static void addClass( Class<? extends Convertible> cls ) throws Exception
    {
        checkConstructor( cls );
        checkFields( cls );
        classList.add( cls );
    }
    
    public Convertible() throws Exception
    {
            synchronized ( classListLock )
            {
                if ( ! isAdded( this.getClass() ) )
                    addClass( this.getClass() );
            }
    }   
}