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
package application.forms;

import application.net.Network;
import application.net.core.Packet;
import application.objects.XMessage;
import application.requests.MessageRequest;
import application.objects.Bool;
import application.objects.TypingText;
import application.requests.UpdateRequest;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class Messenger extends JFrame
{
        private JPanel pnlMessenger = new JPanel();
        
        private JPanel pnlOperations = new JPanel();
        
        private JButton btnSendMessage = new JButton( "Enviar(Ctrl+Enter)" );
        
        private JTextArea txaReceiveMessage = new JTextArea();
        
        private JTextArea txaSendMessage = new JTextArea();
        
        private JLabel lblTypingText = new JLabel();
        
        private boolean markedAsTypingText = false;
        
        private JScrollPane spnReceiveMessage;
        
        private JScrollPane spnSendMessage;
        
        private final NewUserStatus newUserStatus;
        
        public Messenger( NewUserStatus newUserStatus )
        {
                final int w = 350;
                final int h = 250;
                Point p = application.forms.util.Useful.getCenterPoint( w, h );
                setSize( w, h );
                setLocation( p );
                setDefaultCloseOperation( DISPOSE_ON_CLOSE );   
                application.forms.util.Useful.setDefaultImageIcon( this );
                
                this.newUserStatus = newUserStatus;
                Container contentPane = getContentPane();
                contentPane.setLayout( new BorderLayout( 0, 2 ) );
                contentPane.setBackground( new Color( 220, 220, 220 ) );
                
                pnlMessenger.setLayout( new GridLayout( 2, 1, 0, 2 ) );
                txaReceiveMessage.setEditable( false );
                Font font = new Font( Font.DIALOG, Font.PLAIN, 14 );
                txaSendMessage.setFont( font );
                txaSendMessage.addFocusListener( new FocusAdapter()
                {
                    private application.util.ApplicationThread th; 
                    
                    @Override
                    public void focusGained(FocusEvent fe)
                    {
                        final NewUserStatus us = Messenger.this.newUserStatus;
                        if ( th != null )
                            th.terminate();
                        
                        th = new application.util.ApplicationThread()
                        {
                            @Override
                            public void run()
                            {
                                while ( true ) {
                                    try{
                                        Thread.sleep(1000);
                                    }
                                    catch ( Exception e ){
                                        throw new InternalError(e.toString());
                                    }
                                    
                                    InetAddress inet = us.getAddress();
                                    int port = us.getPort();
                                    
                                    if ( inet != null && port != -1 )
                                    {
                                        try
                                        {
                                            Packet p = Packet.forUpload(inet, 
                                                                        port, 
                                                                        new UpdateRequest(UpdateRequest.TYPING_TEXT_REQUEST));
                                            Network net = new Network();
                                            TypingText typingText = new TypingText();
                                            typingText.setMessage(txaSendMessage.getText());
                                            typingText.setTypingText(Messenger.this.isTypingText());
                                            typingText.setSender(us.getUsersWindow().getNickName());
                                            typingText.setRecipient(us.getNickName());
                                            byte[] information = typingText.toBytes();
                                            net.getUploadManager().add(p);
                                            net.upload(p, information);
                                        }
                                        catch ( Exception e )
                                        {
                                            throw new InternalError(e.toString());
                                        }
                                    }
                                    
                                    if ( getStatus() == TERMINATED )
                                        return;
                                }
                            }
                        };
                        
                        th.setDaemon(false);
                        th.setName("Fluxo para notificar (TypingText)");
                        th.start();
                    }

                    @Override
                    public void focusLost(FocusEvent fe)
                    {
                        if ( th != null )
                            th.terminate();
                    }
                });
                
                txaReceiveMessage.setFont( font );
                
                spnReceiveMessage = new JScrollPane( txaReceiveMessage, 
                                                     JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                     JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );                  

                spnSendMessage = new JScrollPane( txaSendMessage, 
                                                  JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                  JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );                  
                
                pnlMessenger.add( spnReceiveMessage );
                pnlMessenger.add( spnSendMessage );
                
                pnlOperations.setLayout( new BorderLayout() );
                pnlOperations.setPreferredSize( new Dimension( 0, 20 ) );
                btnSendMessage.setPreferredSize( new Dimension( 80, 20 ) );
                lblTypingText.setPreferredSize( new Dimension(w,20));
                pnlOperations.add( btnSendMessage, BorderLayout.SOUTH );
                pnlOperations.add( lblTypingText, BorderLayout.WEST);

                contentPane.add( pnlMessenger, BorderLayout.CENTER );
                contentPane.add( pnlOperations, BorderLayout.SOUTH );
        }      
        
        public final void setTypingTextToLabel(boolean value)
        {           
            if ( value )
            {
                lblTypingText.setText( newUserStatus.getNickName() + " está digitando uma mensagem");
                pnlOperations.setPreferredSize(new Dimension(0,45));
            }            
            else
            {
                lblTypingText.setText("");
                pnlOperations.setPreferredSize(new Dimension(0,20));
            }
            
            markedAsTypingText = value;
        }
        
        public boolean isMarkedAsTypingText()
        {
            return markedAsTypingText;
        }
        
        public boolean isTypingText()
        {
            return ! txaSendMessage.getText().isEmpty();
        }
        
        private static boolean sendMessage(  String sender,
                                             String recipient,
                                             InetAddress address, 
                                             int port, 
                                             String message )
        {
                if ( address == null )
                        throw new IllegalArgumentException( "address é null" );

                if ( port < 0 || port > 0xFFFF )
                        throw new IllegalArgumentException( "port não armazena um número de porta válida" );  
                
                if ( message == null )
                        throw new IllegalArgumentException( "message é null" );
                
                try
                {
                        Packet p = Packet.forRequestObject( address, 
                                                            port, 
                                                            new MessageRequest( MessageRequest.SEND_MESSAGE_REQUEST ) );
                               
                        XMessage m = new XMessage();
                        m.setSender( sender );
                        m.setRecipient( recipient );
                        m.setMessage( message );
                        Bool bool = ( Bool ) Network.requestObject( p, m );
                        
                        if ( bool == null )
                                return false;
                        
                        if ( bool.getBoolean() )
                                return true;
                        else
                                return false;
                }
                catch ( Exception e )
                {
                        throw new InternalError( e.toString() );
                }
        }
        
        private final Object updateScrollPaneSync = new Object();
        
        private void updateScrollPane()
        {
                synchronized ( updateScrollPaneSync )
                {
                        Runnable func = new Runnable()
                        {
                                @Override
                                public void run()
                                {
                                        JScrollBar sb = spnReceiveMessage.getVerticalScrollBar();
                                        sb.setValue( sb.getMaximum() );                
                                }
                        };

                        SwingUtilities.invokeLater( func );  
                }
        }
        
        private final Object appendSync = new Object();
        
        public void append( JTextArea txa,  String s )
        {
                synchronized ( appendSync )
                {
                        txa.append( s );
                }
        }
        
        public void addMessage( String nickName, String message )
        {
                if ( ! isInit() )
                        throw new RuntimeException( "Messenger não foi inicializado" );
                
                if ( message == null )
                        throw new IllegalArgumentException( "message" );
                
                String msg = "\n\n" + nickName + " diz:\n" + message;
                append( txaReceiveMessage, msg );
                updateScrollPane();
        }

        public void addErrorMessage( String nickName, String message )
        {
                if ( ! isInit() )
                        throw new IllegalStateException( "Messenger não foi inicializado" );
                
                if ( message == null )
                        throw new IllegalArgumentException( "message" );
                
                String msg = "\n\nFalha ao enviar a mensagem:\n " + message+ " \n para " + nickName;
                append( txaReceiveMessage, msg );
                updateScrollPane();
        }
        
        private class MouseHandler extends MouseAdapter
        {
                private final String sender;
                
                private final String recipient;
                
                private final NewUserStatus us;
                
                public MouseHandler( String sender, 
                                     String recipient, 
                                     NewUserStatus us )
                {
                        this.us = us;
                        this.sender = sender;
                        this.recipient = recipient;
                }
                
                @Override
                public void mouseClicked( MouseEvent me )
                {
                        if ( me.getButton() != MouseEvent.BUTTON1 )
                                return;      
                        
                        send( sender, 
                              recipient, 
                              us );
                }
        }
        
        private final Object getTextSync = new Object();
        
        private String getText( final JTextArea txa )
        {
                synchronized ( getTextSync )
                {
                        return txa.getText();
                }
        }
        
        private final Object setTextSync = new Object();        

        private void setText( final JTextArea txa, String s )
        {
                synchronized ( setTextSync )
                {
                        txa.setText( s );
                }
        }
        
        private void send( String sender, 
                           String recipient, 
                           NewUserStatus us )
        {
                String txt = getText( txaSendMessage );
                
                if ( txt.equals( "" ) )
                        return;
                
                if ( us.getAddress() == null )
                        throw new IllegalArgumentException( "us.getAddress() é null" );

                final int port = us.getPort();
                
                if ( port < 0 || port > 0xFFFF )
                        throw new IllegalArgumentException( "port não armazena um número de porta válida" );  

                final boolean result = sendMessage( sender,
                                                    recipient,
                                                    us.getAddress(), 
                                                    us.getPort(), 
                                                   txt );

                if ( result == true )
                        addMessage( sender, txt );
                else
                        addErrorMessage( us.getNickName(), txt );     
                
                setText( txaSendMessage, "" );
                
                if ( result == false )
                {
                        UsersWindow usersWindow = us.getUsersWindow();
                        usersWindow.checkConnection( us );
                }
        }
        
        private class KeyHandler extends KeyAdapter
        {
                private final String sender;
                
                private final String recipient;
                
                private final NewUserStatus us;
                
                public KeyHandler( String sender, 
                                   String recipient, 
                                   NewUserStatus us )
                {
                        this.us = us;
                        this.sender = sender;
                        this.recipient = recipient;
                }
                
                @Override
                public void keyTyped( KeyEvent ke )
                {
                        /* se ke.isControlDown() for removido, ou seja
                         * deixar somente ke.getKeyChar() == KeyEvent.VK_ENTER, então
                         * não será possível quebrar as linhas do texto que
                         * ainda não foi enviado.
                         */
                        if ( ke.isControlDown() && ke.getKeyChar() == KeyEvent.VK_ENTER )
                        {
                                send( sender, 
                                      recipient, 
                                      us );
                                
                                ke.consume();
                        }
                }
        }
        
        private boolean initialized = false;
        
        private final Object initSync = new Object();
        
        public Object getLock()
        {
                return initSync;
        }
        
        public boolean isInit()
        {
                synchronized ( initSync )
                {
                        return initialized;
                }
        }
        
        public void init( String sender, NewUserStatus us )
        {
                synchronized ( initSync )
                {
                        if ( us== null )
                                throw new IllegalArgumentException( "us é null" );

                        if ( us.getAddress() == null )
                                throw new IllegalArgumentException( "us.getAddress()  é null" );

                        final int port = us.getPort();

                        if ( port < 0 || port > 0xFFFF )
                                throw new IllegalArgumentException( "port não armazena um"
                                                                     + " número de porta válida" );  

                        if ( ! initialized )
                        {
                                setTitle( sender + " - " + us.getNickName() );
                                KeyHandler kh = new KeyHandler( sender, 
                                                                us.getNickName(), 
                                                                us );
                                
                                txaSendMessage.addKeyListener( kh );
                                MouseHandler mh = new MouseHandler( sender, 
                                                                    us.getNickName(), 
                                                                    us );
                                
                                btnSendMessage.addMouseListener( mh );
                                initialized = true; 
                        }
                        else
                                throw new RuntimeException( "Messenger já foi inicializado" );
                }
        }
}