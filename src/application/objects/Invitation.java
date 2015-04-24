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

public class Invitation extends UserAndFriend
{
        public static final int ACCEPTED = 0;
        
        public static final int DECLINED = 1;
        
        private int answer = -1;
        
        public Invitation() throws Exception
        {}       
        
        public int getAnswer()
        {
                if ( answer != ACCEPTED && answer != DECLINED )
                        throw new RuntimeException( "answer não fixado!" );
                
                return answer;
        }        
        
        public void setAnswer( final int answer )
        {
                if ( answer != ACCEPTED && answer != DECLINED )
                        throw new IllegalArgumentException( "answer é inválido!" );
                
                this.answer = answer;
        }
}