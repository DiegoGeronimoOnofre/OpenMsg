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

public class Queue<E>
{
        private ObjectList<E> list = new ObjectList<E>();
        
        public synchronized void add( E element )
        {
                list.add( element );
        }
        
        public synchronized void clear()
        {
                list.clear();
        }
        
        public synchronized E remove()
        {
                if ( isEmpty() )
                        throw new RuntimeException( "a fila está vazia" );
                
                list.first();
                return list.remove();
        }
        
        public synchronized E get()
        {
                if ( isEmpty() )
                        throw new RuntimeException( "a fila está vazia" );                
                
                list.first();
                return list.get();
        }
        
        public boolean isEmpty()
        {
                return list.isEmpty();
        }
        
        public synchronized int size()
        {
                return list.size();
        }
        
        @Override
        public synchronized String toString()
        {
                return list.toString();
        }
}