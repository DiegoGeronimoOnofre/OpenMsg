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

import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

public final class Session 
{
    private static final String path = application.Application.getMainPath() + File.separatorChar + "Session.xml";
    
    private Session(String status,
                    String nickName, 
                    String password, 
                    String server)
    {
        this.status   = status.trim();
        this.nickName = nickName.trim();
        this.password = password.trim();
        this.server   = server.trim();
    }
    
    private String status = "";
    
    private String nickName = "";
    
    private String password = "";
    
    private String server = "";
    
    public String getStatus()
    {
        return status;
    }
    
    public String getNickName()
    {
        return nickName;
    }
    
    public String getPassword()
    {
        return password;
    }
    
    public String getServer()
    {
        return server;
    }
    
    public void setStatus(String status)
    {
        if ( status == null )
            throw new IllegalArgumentException("status é null");
        
        this.status = status;
    }
    
    public void setNickName(String nickName)
    {
        if ( nickName == null )
            throw new IllegalArgumentException("nickName não pode ser null");
        
        this.nickName = nickName;
    }
    
    public void setPassword(String password)
    {
        if (password == null)
            throw new IllegalArgumentException("password não pode ser null");
        
        this.password = password;
    }
    
    public void setServer(String server)
    {
        if ( server == null )
            throw new IllegalArgumentException("server não pode ser null");
        
        this.server = server;
    }
    
    public void store()
    {
        try
        {        
            Document document = getDocument(path);
            Element rootElement = document.getDocumentElement();
            Element sessionElement = (Element) rootElement.getElementsByTagName("Session").item(0);
            sessionElement.setAttribute("status"  , status.trim());
            sessionElement.setAttribute("nickName", nickName.trim());
            sessionElement.setAttribute("password", password.trim());
            sessionElement.setAttribute("server"  , server.trim());
      
            DOMSource domSource = new DOMSource(document);  
            StreamResult sr = new StreamResult(new File(path));  
            Transformer transformer = TransformerFactory.newInstance().newTransformer();  
            transformer.transform(domSource, sr); 
        }
        catch (Exception e)
        {
            throw new InternalError(e.toString());
        }
    }
    
    private static Document getDocument(String p)
    {            
        try
        {
            File xmlFile = new File(p);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document document = documentBuilder.parse(xmlFile);
            return document;
        }
        catch ( Exception e )
        {
            throw new InternalError(e.toString());
        }
    }
    
    public static Session getSession()
    { 
        try
        {
            Document document = getDocument(path);
            Element rootElement = document.getDocumentElement();
            Element sessionElement = (Element) rootElement.getElementsByTagName("Session").item(0);
            final String status = sessionElement.getAttribute("status");
            final String nName = sessionElement.getAttribute("nickName");
            final String pword = sessionElement.getAttribute("password");
            final String server = sessionElement.getAttribute("server");
            Session session = new Session(status,
                                          nName,
                                          pword,
                                          server);
            return session;
        }
        catch ( Exception e )
        {
            throw new InternalError(e.toString());
        }
    }
}
