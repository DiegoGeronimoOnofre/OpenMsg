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

public class Core
{

    //Número de bytes que contém um valor do tipo boolean
    public static final int BOOLEAN_SIZE = 1;   

    //Número de bytes que contém um valor do tipo byte
    public static final int BYTE_SIZE = 1;   

    //Número de bytes que contém um valor do tipo char
    public static final int CHAR_SIZE = 2;   

    //Número de bytes que contém um valor do tipo short
    public static final int SHORT_SIZE = 2;   

    //Número de bytes que contém um valor do tipo int
    public static final int INT_SIZE = 4;   

    //Número de bytes que contém um valor do tipo long
    public static final int LONG_SIZE = 8;    

    //Número de bytes que contém um valor do tipo float
    public static final int FLOAT_SIZE = 4;    

    //Número de bytes que contém um valor do tipo double
    public static final int DOUBLE_SIZE = 8;   

    public static boolean toBooleanValue( final byte value )
    {
        if ( value != 0 && value != 1 )
            throw new IllegalArgumentException( "value não é 0 ou 1" );

        if ( value == 1 )
            return true;
        else
            return false;
    }                

    public static byte toBooleanByte( final boolean value )
    {
        if ( value )
            return 1;
        else
            return 0;
    }

    /*
     * public static byte[] toTypeBytes( final type value )
     */

    public static byte[] toCharBytes( final char c )
    {
        final byte[] result = new byte[CHAR_SIZE];
        final int code = c;
        result[0] = ( byte ) ( ( code >>> 8 ) & 0xFF );
        result[1] = ( byte ) ( ( code ) & 0xFF );                     
        return result;
    }

    public static byte[] toShortBytes( final short s )
    {
        final byte[] result = new byte[SHORT_SIZE];         
        result[0] = ( byte ) ( ( s >>>  8 ) & 0xFF );                           
        result[1] = ( byte ) ( ( s ) & 0xFF );                                                 
        return result;   
    }

    public static byte[] toIntBytes( final int i )
    {      
        final byte[] result = new byte[INT_SIZE];         
        result[0] = ( byte ) ( ( i >>>  24 ) & 0xFF );               
        result[1] = ( byte ) ( ( i >>>  16 ) & 0xFF );
        result[2] = ( byte ) ( ( i >>>  8 )  & 0xFF );                           
        result[3] = ( byte ) ( ( i )         & 0xFF );                             
        return result;
    }  

    public static byte[] toFloatBytes( final float f )
    {
        final int intValue = Float.floatToIntBits( f );
        return toIntBytes( intValue );
    }

    public static byte[] toLongBytes( final long l )
    {      
        final byte[] result = new byte[LONG_SIZE];
        result[0] = ( byte ) ( ( l >>>  56 ) & 0xFF );              
        result[1] = ( byte ) ( ( l >>>  48 ) & 0xFF );               
        result[2] = ( byte ) ( ( l >>>  40 ) & 0xFF );               
        result[3] = ( byte ) ( ( l >>>  32 ) & 0xFF );            
        result[4] = ( byte ) ( ( l >>>  24 ) & 0xFF );               
        result[5] = ( byte ) ( ( l >>>  16 ) & 0xFF );
        result[6] = ( byte ) ( ( l >>>  8 )  & 0xFF );                           
        result[7] = ( byte ) ( ( l )         & 0xFF );                             
        return result;
    } 

    public static byte[] toDoubleBytes( final double d )
    {
        final long longValue = Double.doubleToLongBits( d );
        return toLongBytes( longValue );
    }     

    /*
     * public static type toTypeValue( final byte[] bytes )
     */

    public static char toCharValue( final byte[] bytes )
    {
        if ( bytes == null )
                throw new IllegalArgumentException( "bytes é null" );            

        if ( bytes.length != CHAR_SIZE )
            throw new IllegalArgumentException( "bytes.length é diferente de "+ CHAR_SIZE );

        final int first  = ( ( ( ( int ) bytes[0] ) << 8 ) & 0xFF00 );
        final int second = ( ( ( ( int ) bytes[1] ) ) & 0xFF );            
        return ( char ) ( first + second );
    }

    public static short toShortValue( final byte[] bytes )
    {
        if ( bytes == null )
                throw new IllegalArgumentException( "bytes é null" );

        if ( bytes.length != SHORT_SIZE )
                throw new IllegalArgumentException( "bytes.length é diferente de " + SHORT_SIZE );

        final int first  = ( ( ( ( int ) bytes[0] ) << 8 ) & 0xFF00 );
        final int second = ( ( ( ( int ) bytes[1] ) ) & 0xFF );            
        return ( short ) ( first + second );                

    }

    public static int toIntValue( final byte[] bytes )
    {
        if ( bytes == null )
                throw new IllegalArgumentException( "bytes é null" );

        if ( bytes.length != INT_SIZE )
                throw new IllegalArgumentException( "bytes.length é diferente de " + INT_SIZE );

        final int[] result = new int[INT_SIZE];
        result[0] = bytes[0];
        result[1] = bytes[1];
        result[2] = bytes[2];
        result[3] = bytes[3];

        for ( int i = 0; i < result.length; i++ )
                result[i] = ( result[i] & 0xFF );

        return ( ( result[0] << 24 ) + 
                       ( result[1] << 16 ) + 
                       ( result[2] << 8  ) + 
                         result[3] );
    } 

    public static float toFloatValue( final byte[] bytes )
    {
        final int intValue = toIntValue( bytes );
        return Float.intBitsToFloat( intValue );
    }

    public static long toLongValue( final byte[] bytes ) 
    {
        if ( bytes == null )
                throw new IllegalArgumentException( "bytes é null" );

        if ( bytes.length != LONG_SIZE )
                throw new IllegalArgumentException( "bytes.length é diferente de " + LONG_SIZE );

        final long[] result = new long[LONG_SIZE];
        result[0] = bytes[0];
        result[1] = bytes[1];
        result[2] = bytes[2];
        result[3] = bytes[3];
        result[4] = bytes[4];
        result[5] = bytes[5];
        result[6] = bytes[6];
        result[7] = bytes[7];

        for ( int i = 0; i < result.length; i++ )
                result[i] = ( result[i] & 0xFF );

        return ( ( result[0] << 56 ) +
                       ( result[1] << 48 ) + 
                       ( result[2] << 40 ) + 
                       ( result[3] << 32 ) + 
                       ( result[4] << 24 ) + 
                       ( result[5] << 16 ) + 
                       ( result[6] << 8  ) + 
                       result[7] );
    }   

    public static double toDoubleValue( final byte[] bytes )
    {
        final long longValue = toLongValue( bytes );
        return Double.longBitsToDouble( longValue );
    }
}