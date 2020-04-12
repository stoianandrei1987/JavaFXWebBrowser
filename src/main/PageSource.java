package main;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;



public class PageSource {
    public static String getPageSourceHTMLString() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            IOClass.printDocument(Main.getView().getEngine().getDocument(), os);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        String lhs = new String(os.toByteArray());
        String ns = lhs.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");

        /*
         finds start and end tags without params (&lt;\/?[\w+\-]+&gt;):
         finds start tags with param(s) : (&lt;\S+)(\s[a-zA-z01-9*:]+="\S*")(&gt;)
         finds end of start tag with param : (")(&gt;)
         end of self-closing tag with param : (")(\/)(&gt;)

         */


        String repl1 = "<SPAN class=\"blue\">$1</SPAN><SPAN " +
                "class=\"green\">$2</SPAN><SPAN class=\"blue\">$3</SPAN>";
        ns = ns.replaceAll("(&lt;/?[\\w+\\-]+&gt;)", "<SPAN CLASS=\"blue\">$0</SPAN>");
        ns = ns.replaceAll("(&lt;\\S+\\s)(\\S+=\".*\"\\s?)+(/?&gt;)", repl1);
       // ns = ns.replaceAll("(\")(&gt;)", "$0</SPAN><SPAN CLASS=\"blue\">$1</SPAN>");
        String styleString = "<style>.blue{color:blue;}.green{color:green;}</style>";
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><title>Page source : </title>"+
                styleString+"</head><body><pre><code>");
        sb.append(ns);
        sb.append("</code></pre></body></html>");

        return sb.toString();
    }
}
