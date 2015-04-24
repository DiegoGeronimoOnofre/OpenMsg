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

class ListNode<E>
{
        private ListNode<E> nextNode;

        private ListNode<E> priorNode;

        private E element;

        public ListNode( E element )
        {
                this( null, element );
        }

        public ListNode( ListNode<E> priorNode, E element )
        {
                this.priorNode = priorNode;
                this.element   = element;

                if ( priorNode != null )
                        priorNode.nextNode = this;            
        }

        public ListNode<E> getNextNode()
        {
                return nextNode;
        }

        public ListNode<E> getPriorNode()
        {
                return priorNode;
        }

        public E getElement()
        {
                return element;
        }

        public void setElement( E element )
        {
                this.element = element;
        }

        public void dispose()
        {      
                if ( priorNode != null )
                        priorNode.nextNode = nextNode;

                if ( nextNode != null )
                        nextNode.priorNode = priorNode;       

                element   = null;
                nextNode  = null;
                priorNode = null;
        }

        @Override
        public String toString()
        {
                final ListNode<E> prior = priorNode;
                final ListNode<E> next  = nextNode;
                StringBuilder buf = new StringBuilder();
                buf.append( '{' );

                if ( prior == null )
                        buf.append( "priorNode=null," );
                else
                {
                        buf.append( "priorNode.element=" );
                        final Object priorElement = prior.getElement(); 

                        if ( priorElement == null )
                                buf.append( "null," );
                        else
                        {
                                buf.append( priorElement.toString() );
                                buf.append( ',' );
                        }
                }

                if ( element == null )
                         buf.append( "element=null," );
                else
                {
                        buf.append( "element=" );
                        buf.append( element.toString() );
                        buf.append( ',' );
                }

                if ( next == null )
                        buf.append( "nextNode=null" );
                else
                {
                        buf.append( "nextNode.element=" );
                        final Object nextElement = next.getElement(); 

                        if ( nextElement == null )
                                buf.append( "null" );
                        else
                                buf.append( nextElement.toString() );
                }        

                buf.append( '}' );    
                return buf.toString();
        }
}