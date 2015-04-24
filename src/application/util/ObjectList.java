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

public class ObjectList<E>
{
        private ListNode<E> firstNode;

        private ListNode<E> current;

        private ListNode<E> lastNode;

        private int count = 0;  

        public ObjectList()
        {   
                clearList();
        }

        public int size()
        {
                return count;
        }

        public boolean isFirst()
        {
                if ( isEmpty() )
                        throw new RuntimeException( "ObjectList está vazio" );

                final ListNode<E> prior = current.getPriorNode();

                if ( prior == null )
                        return true;
                else
                        return false;
        }

        public boolean isLast()
        {
                if ( isEmpty() )
                        throw new RuntimeException( "ObjectList está vazio" );

                final ListNode<E> next = current.getNextNode();

                if ( next == null )
                        return true;
                else
                        return false;
        }

        public void first()
        {   
                if ( isEmpty() )
                        throw new RuntimeException( "ObjectList está vazio" );

                current = firstNode;
        }

        public void last()
        {
                if ( isEmpty() )
                        throw new RuntimeException( "ObjectList está vazio" );

                current = lastNode;
        }

        public boolean prior()
        {
                if ( isEmpty() )
                        throw new RuntimeException( "ObjectList está vazio" );

                final ListNode<E> prior = current.getPriorNode();

                if ( prior != null )
                {    
                        current = prior;
                        return true;
                }
                else
                        return false;
        }

        public boolean next()
        {
                if ( isEmpty() )
                        throw new RuntimeException( "ObjectList está vazio" );

                final ListNode<E> next = current.getNextNode();

                if ( next != null )
                {    
                        current = next;
                        return true;
                }
                else
                        return false;
        }

        public E remove()
        {
                if ( isEmpty() )
                        throw new RuntimeException( "ObjectList está vazio" );

                final ListNode<E> prior       = current.getPriorNode();
                final ListNode<E> next        = current.getNextNode();
                final ListNode<E> oldCurrent  = current;        
                final E oldElement            = current.getElement();        

                current.dispose();

                if ( prior != null )
                        current = prior;
                else if ( next != null )
                        current = next;
                else
                {
                        clearList();
                        return oldElement;
                }

                if ( oldCurrent == firstNode )
                        firstNode = current;

                if ( oldCurrent == lastNode )
                        lastNode = current;          

                count--;        
                return oldElement;
        }    

        public void add( E element )
        {                     
                if ( isEmpty() ) 
                        current = lastNode = firstNode = new ListNode<E>( element );
                else 
                        lastNode = new ListNode<E>( lastNode, element );

                count++;
        }

        public E get()
        {
                if (  isEmpty() )
                        throw new RuntimeException( "A lista está vazia!" );

                return current.getElement();
        }

        public void set( E element )
        {
                if ( isEmpty() )
                        throw new RuntimeException( "A lista está vazia!" );

                current.setElement( element );
        }     

        private void  clearList()
        {
                current = lastNode = firstNode = null;
                count = 0;
        }

        public void clear()
        {
                clearList();
        }

        public boolean isEmpty()
        {
                return current == null;
        }

        @Override
        public String toString()
        {
                if ( isEmpty() )
                    return "{}";

                final ListNode<E> focus = current;
                StringBuilder buf = new StringBuilder();
                buf.append( '{' );

                try
                {
                        first();

                        do
                        {
                                Object o = get();
                                buf.append( o.toString() );

                                if ( ! isLast() )
                                    buf.append( ",\n" );

                        } while ( next() );
                }
                catch ( Exception e )
                {}

                current = focus;
                buf.append( '}' );        
                return buf.toString();
        }
}
