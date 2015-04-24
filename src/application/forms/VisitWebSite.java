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

import application.forms.util.Useful;
import application.xml.ApplicationXml;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author Diego
 */
public class VisitWebSite extends JFrame{
    
    private JLabel lblVisitWebSite = new JLabel("Deseja visitar o site do desenvolvedor?");
    
    private JButton btnYes = new JButton("Sim");
    
    private JButton btnNo = new JButton("Não");
    
    private JCheckBox cbxDefault = new JCheckBox("Não perguntar novamente!");
    
    public VisitWebSite()
    {
        final int w = 250;
        final int h = 130;
        this.setTitle("Visite o Web Site");
        this.setSize(w, h);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Point p = Useful.getCenterPoint(w, h);
        Point newPoint = new Point(p.x, p.y/2);
        this.setLocation(newPoint);
        
        btnYes.setSize(100, 20);
        btnNo.setSize(100, 20);
        
        btnYes.setLocation(20, 35);
        btnNo.setLocation(130, 35);

        cbxDefault.setSize(200, 20);
        cbxDefault.setLocation(20, 65);

        lblVisitWebSite.setSize(250, 20);
        lblVisitWebSite.setLocation(20, 10);
        
        btnYes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me)
            {
                if ( cbxDefault.isSelected() ){
                    ApplicationXml.visitWebSite(false);
                }
                
                try{
                    String webSite = application.Application.WEB_ADDRESS;
                    URI uri = new URI(webSite); 
                    Desktop desk = Desktop.getDesktop();
                    desk.browse(uri);
                    VisitWebSite.this.setVisible(false);
                }
                catch ( Exception e ){
                    javax.swing.JOptionPane.showMessageDialog(null, "Não foi possível visualizar o Web Site!");
                }
            }
        });

        btnNo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me)
            {
                if ( cbxDefault.isSelected() ){
                    ApplicationXml.visitWebSite(false);
                }            
                
                VisitWebSite.this.setVisible(false);
            }
        });
        
        Container contentPane = this.getContentPane();
        contentPane.setLayout(null);
        contentPane.add(lblVisitWebSite);
        contentPane.add(btnYes);
        contentPane.add(btnNo);
        contentPane.add(cbxDefault);
        Useful.setDefaultImageIcon(this);
    }
    
}
