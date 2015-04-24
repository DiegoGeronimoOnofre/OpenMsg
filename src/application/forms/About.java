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

import application.Application;
import application.forms.util.Useful;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author Diego
 */
public class About extends JFrame{
    public About()
    {
        final Desktop desktop = Desktop.getDesktop();
        JLabel lblWeb = new JLabel("Web Site:");
        JLabel lblWebSite = new JLabel(Application.WEB_SITE);
        lblWebSite.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblWebSite.setForeground(Color.blue);

        lblWebSite.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent me)
            {
                try{
                    URI uri = new URI(Application.WEB_ADDRESS);
                    desktop.browse(uri);
                }
                catch ( Exception e){
                    javax.swing.JOptionPane.showMessageDialog(null,"Não foi possível visualizar o website!");
                }
            }
        });

        String version = "Versão:" + Application.APPLICATION_VERSION;
        String developer = "Desenvolvido por:";
        String developerName =  Application.APPLICATION_DEVELOPER;
        String email1 = Application.CONTACT_EMAIL1;
        String email2 = Application.CONTACT_EMAIL2;

        JLabel lblVersion       = new JLabel(version);
        JLabel lblDeveloper     = new JLabel(developer);
        JLabel lblDeveloperName = new JLabel(developerName);
        JLabel lblEmail1        = new JLabel(email1);
        JLabel lblEmail2        = new JLabel(email2);
        JLabel lblEmails        = new JLabel("Emails:");

        final int defaultW = 175;
        lblVersion.setSize(new Dimension(defaultW,15));
        lblDeveloper.setSize(new Dimension(defaultW,15));
        lblDeveloperName.setSize(new Dimension(defaultW,15));
        lblEmail1.setSize(new Dimension(defaultW,15));
        lblEmail2.setSize(new Dimension(defaultW,15));
        lblEmails.setSize(new Dimension(defaultW,15));
        lblWeb.setSize(new Dimension(defaultW,15));
        lblWebSite.setSize(new Dimension(defaultW,15));

        final int defaultLeft = 50;
        final int incTop = 20;
        lblVersion.setLocation(new Point(defaultLeft,5 + incTop));
        lblDeveloper.setLocation(new Point(defaultLeft,25 + incTop));
        lblDeveloperName.setLocation(new Point(defaultLeft,40 + incTop));
        lblEmails.setLocation(new Point(defaultLeft,60 + incTop));
        lblEmail1.setLocation(new Point(defaultLeft,75 + incTop));
        lblEmail2.setLocation(new Point(defaultLeft,90 + incTop));
        lblWeb.setLocation(new Point(defaultLeft,110 + incTop));
        lblWebSite.setLocation(new Point(defaultLeft,125 + incTop));

        lblEmail1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblEmail1.setForeground(Color.blue);

        lblEmail2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblEmail2.setForeground(Color.blue);

        lblEmail1.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent me)
            {
                try{
                    URI uri = new URI("mailto:"+Application.CONTACT_EMAIL1);
                    desktop.browse(uri);
                }
                catch ( Exception e){
                    javax.swing.JOptionPane.showMessageDialog(null,
                            "Problema ao executar a aplicação padrão de email!");
                }
            }
        });

        lblEmail2.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent me)
            {
                try{
                    URI uri = new URI("mailto:"+Application.CONTACT_EMAIL2);
                    desktop.browse(uri);
                }
                catch ( Exception e){
                    javax.swing.JOptionPane.showMessageDialog(null,
                            "Problema ao executar a aplicação padrão de email!");
                }
            }
        });

        final int w = 257;
        final int h = 215;
        this.setTitle("Sobre " + Application.APPLICATION_NAME);
        this.setSize(w, h);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.setLocation(Useful.getCenterPoint(w, h));
        Container contentPane = this.getContentPane();
        contentPane.setLayout(null);
        contentPane.add(lblVersion);
        contentPane.add(lblDeveloper);
        contentPane.add(lblDeveloperName);
        contentPane.add(lblEmails);
        contentPane.add(lblEmail1);
        contentPane.add(lblEmail2);
        contentPane.add(lblWeb);
        contentPane.add(lblWebSite);
        Useful.setDefaultImageIcon(this);
    }
    
}
