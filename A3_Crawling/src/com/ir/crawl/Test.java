package com.ir.crawl;

/* Import list */
import java.net.URL;
import java.util.HashMap;
import java.io.IOException;
import com.ir.global.Utils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.HttpClient;
import crawlercommons.robots.BaseRobotRules;
import org.apache.http.protocol.HttpContext;
import org.apache.http.client.methods.HttpGet;
import crawlercommons.robots.SimpleRobotRules;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.entity.BufferedHttpEntity;
import crawlercommons.robots.SimpleRobotRulesParser;
import org.apache.http.impl.client.DefaultHttpClient;
import crawlercommons.robots.SimpleRobotRules.RobotRulesMode;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 24, 2015
 */

public class Test
{
    private static HttpClient client;
    private static HttpContext context;
    private static SimpleRobotRulesParser parser;

    private HashMap<String, BaseRobotRules> disallowedLinks;

    static
    {
        client  = new DefaultHttpClient();
        context = new BasicHttpContext();
        parser  = new SimpleRobotRulesParser();
    }

    public Test()
    {
        disallowedLinks = new HashMap<>();
    }

    public boolean isRobotAllowed(String urlStr)
        throws IOException
    {
        URL url = new URL(urlStr);
        String domain = url.getProtocol() + "://" +
                        url.getHost() +
                        (url.getPort() > -1 ? ":" + url.getPort() : "");
        BaseRobotRules rules = disallowedLinks.get(domain);

        if(rules == null)
        {
            HttpResponse res = client.execute(new HttpGet(domain + "/robots.txt"), context);
            if(res.getStatusLine().getStatusCode() == 404 &&
               res.getStatusLine() != null)
            {
                rules = new SimpleRobotRules(RobotRulesMode.ALLOW_ALL);
                EntityUtils.consume(res.getEntity());
            }
            else
            {
                rules = parser.parseContent(domain,
                                            IOUtils.toByteArray(new BufferedHttpEntity(res.getEntity()).getContent()),
                                            "text/plain",
                                            "ASBot");
            }
            disallowedLinks.put(domain, rules);
        }

        return rules.isAllowed(urlStr);
    }

    /**
     * Main method for unit testing.
     * @param args
     *            Program arguments.
     */
    public static void main(String[] args)
        throws IOException
    {
        Test t = new Test();
        Utils.cout(t.isRobotAllowed("http://www.dmoz.org/search?type=advanced") + "\n");

        URL url = new URL("HTTP://www.w3schools.com:80/sql/search?q=valve");
        Utils.cout("Protocol: " + url.getProtocol() + "\n");
        Utils.cout("Host: "     + url.getHost()     + "\n");
        Utils.cout("File: "     + url.getFile()     + "\n");
        Utils.cout("Path: "     + url.getPath()     + "\n");

        /*
        String domain = "http://www.example.com/";
        String path = "a.html";
        while(path.contains("../"))
        {
            int i = domain.length() - 1;
            while(domain.charAt(i--) != '/');
            while(domain.charAt(i--) != '/');
            domain = domain.substring(0, i + 2);
            path = path.replaceFirst("../", "");
        }
        Utils.cout(domain + path + "\n");
        */
    }
}
/* End of Test.java */