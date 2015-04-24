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

public class Blocker
{
        private Queue<Executable> queue = new Queue<Executable>();

        private boolean running = false;

        private final Object lock = new Object();             

        private class XThread extends Thread
        {
                protected boolean waiting = false;

                protected boolean executing = false;

                {
                        setName( Blocker.this.getClass().getSimpleName() + "-Servidor de mensagens" );
                }
                
                public boolean isWaiting()
                {
                        return waiting;
                }

                public boolean isExecuting()
                {
                        return executing;
                }

                @Override
                public void run()
                {
                        running = true;                        

                        synchronized ( lock )
                        {
                                lock.notify();
                        }

                        while ( true )
                        {
                                synchronized ( lock )
                                {
                                        try
                                        {                                   
                                                waiting = true;
                                                lock.notify();
                                                lock.wait();
                                                waiting = false;
                                                executing = true;
                                        }
                                        catch ( Exception e )
                                        {
                                                throw new InternalError( e.toString() );
                                        }
                                }

                                while ( true )
                                {
                                        final Executable func;

                                        synchronized ( lock )
                                        {
                                                if ( ! queue.isEmpty() )
                                                        func = queue.remove();
                                                else
                                                {
                                                        executing = false;
                                                        lock.notify();
                                                        break;
                                                }
                                        }

                                        Thread thread = new Thread()
                                        {
                                                @Override
                                                public void run()
                                                {
                                                        func.execute();
                                                }
                                        };
                                          
                                        thread.start();
                                        
                                        try
                                        {
                                                thread.join();
                                        }
                                        catch ( Exception e )
                                        {
                                                throw new InternalError( e.toString() );
                                        }
                                }
                        }
                }            
        }

        private XThread t;

        public void init()
        {
                synchronized ( lock )
                {
                        if ( running )
                                throw new RuntimeException( "Blocker já está em execução." );

                        t = new XThread();                 
                        t.start();  

                        try
                        {
                                lock.wait();
                        }
                        catch ( Exception e )
                        {
                                throw new InternalError( e.toString() );
                        }
                }
        }

        public void terminate()
        {
                synchronized ( lock )
                {
                        if ( ! running )
                                throw new RuntimeException( "Blocker não está em execução." );

                        running = false;
                }
        }

        public void execute( Executable func )
        {
                synchronized ( lock )
                {
                        if ( ! running )
                                throw new RuntimeException( "Blocker não está em execução." );

                        queue.add( func );                

                        if ( ! t.isWaiting() && ! t.isExecuting() )
                        {
                                try
                                {
                                        lock.wait();
                                }
                                catch ( Exception e )
                                {
                                        throw new InternalError( e.toString() );
                                }
                        }
                        
                        lock.notify();            
                }
        }
}