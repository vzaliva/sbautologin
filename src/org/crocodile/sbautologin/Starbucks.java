import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Allows the user to accept the Starbucks Wi-Fi terms and conditions without
 * having to open a browser.
 * 
 * @author michael
 */
public class Starbucks
{
    /**
     * True if status messages should be printed to stdout.
     */
    private static boolean verbose = false;

    public static void main(String args[]) throws Exception
    {
        // get arguments
        if(args.length > 0)
        {
            verbose = args[0].equals("-v");
            if(args[0].equals("--help"))
            {
                System.out.println("Starbucks Wi-Fi Terms and Conditions Accepter");
                System.out
                        .println("Starbucks provides free Wi-Fi access, but requires that you first accept their terms and conditions.\nThis program will accept those terms and conditions without you having to open a browser window and accept them yourself.");
                System.out.println("by Michael Angstadt www.mangst.com");

                System.out.println("\nParameters:");
                System.out.println("-v\tVerbose output");
                System.out.println("--help\tDisplays this help text");

                System.out.println("\nExample: java Starbucks -v");
                System.exit(0);
            }
        }

        URL googleUrl = new URL("http://www.google.com");

        // disable the automatic following of redirects
        // a 3xx response can be used to determine whether or not the computer
        // is already connected to the Internet
        HttpURLConnection.setFollowRedirects(false);

        // try to visit a website
        print("Attempting to visit [" + googleUrl + "]...");
        HttpURLConnection conn = (HttpURLConnection) googleUrl.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(false);
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        if(responseCode == HttpURLConnection.HTTP_MOVED_TEMP)
        {
            // if you haven't accepted the terms and conditions yet, 302 is
            // returned, redirecting you to the login page

            println("FAILED with " + responseCode); // it should fail to visit
                                                    // the website

            // get the Location header, which contains the redirect URL
            String redirectUrlStr = conn.getHeaderField("Location");

            // go to the redirect URL, which is the Starbucks login page
            conn.disconnect();
            URL redirectUrl = new URL(redirectUrlStr);
            print("Downloading Starbucks login page [" + redirectUrl + "]...");
            conn = (HttpURLConnection) redirectUrl.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.setRequestMethod("GET");

            // get the HTML of the webpage
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder html = new StringBuilder();
            while((line = in.readLine()) != null)
            {
                html.append(line);
            }
            in.close();
            conn.disconnect();

            println("SUCCESS");

            // parse the form info out of the HTML
            print("Parsing Starbucks login page...");
            HtmlForm formInfo = new HtmlForm(redirectUrl, html.toString());
            println("SUCCESS");

            // prepare to submit the form
            print("Accepting the terms and conditions...");
            conn = (HttpURLConnection) formInfo.actionUrl.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod(formInfo.method);

            // output parameters to request body
            StringBuilder sb = new StringBuilder();
            for(Map.Entry<String, String> entry : formInfo.parameters.entrySet())
            {
                sb.append(URLEncoder.encode(entry.getKey(), "UTF-8") + '='
                        + URLEncoder.encode(entry.getValue(), "UTF-8") + '&');
            }
            PrintWriter out = new PrintWriter(conn.getOutputStream());
            out.print(sb.substring(0, sb.length() - 1)); // remove the last '&'
            out.flush();

            // send request
            conn.getResponseCode();
            conn.disconnect();

            // try to connect to the Internet again to see if it worked
            conn = (HttpURLConnection) googleUrl.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.setRequestMethod("GET");
            responseCode = conn.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK)
            {
                println("SUCCESS");
                println("The terms and conditions have been agreed to and you can now connect to the Internet!");
            } else
            {
                println("FAILED");
                System.err.println("Error: Approval of terms and conditions failed.");
                System.exit(1);
            }
        } else if(responseCode == HttpURLConnection.HTTP_OK)
        {
            println("SUCCESS");
            println("You are already connected to the Internet.");
        } else
        {
            println("ERROR");
            System.err.println("Unknown error: HTTP status code " + responseCode);
            System.exit(1);
        }
    }

    /**
     * Prints a message to stdout if verbose mode is enabled.
     * 
     * @param message the message to print
     */
    public static void print(String message)
    {
        if(verbose)
        {
            System.out.print(message);
        }
    }

    /**
     * Prints a message to stdout if verbose mode is enabled. A newline is added
     * to the end of the message.
     * 
     * @param message the message to print
     */
    public static void println(String message)
    {
        if(verbose)
        {
            System.out.println(message);
        }
    }
}

/**
 * An exception that is thrown when there is a problem parsing form data out of
 * an HTML page.
 * 
 * @author michael
 */
class InvalidFormException extends Exception
{
    public InvalidFormException(String message)
    {
        super(message);
    }
}

/**
 * Parses HTML form data out of an HTML page.
 * 
 * @author michael
 */
class HtmlForm
{

    /**
     * All of the input parameters on the form and their values.
     */
    public Map<String, String>   parameters;

    /**
     * The HTTP method the form uses (e.g. "POST"). Will be in upper-case.
     */
    public String                method;

    /**
     * The value of the form's "action" attribute.
     */
    public URL                   actionUrl;

    /**
     * The regex pattern used to find a form element in HTML.
     */
    private static final Pattern formPattern      = Pattern.compile("<form(.*?)>", Pattern.CASE_INSENSITIVE
                                                          | Pattern.DOTALL);

    /**
     * The regex pattern to find a form input parameter in HTML.
     */
    private static final Pattern inputPattern     = Pattern.compile("<input(.*?)>", Pattern.CASE_INSENSITIVE
                                                          | Pattern.DOTALL);

    /**
     * The regex pattern used to parse out all of the attributes from a HTML
     * tag. The source string must contain only the attributes, not the tag name
     * or brackets. Extracts attributes that are in the following formats:
     * <ul>
     * <li>foo="bar"</li>
     * <li>foo='bar'</li>
     * <li>foo=bar</li>
     * <li>foo (no value)</li>
     * </ul>
     */
    private static final Pattern attributePattern = Pattern
                                                          .compile("([\\w:\\-]+)(=(\"(.*?)\"|'(.*?)'|([^ ]*))|(\\s+|\\z))");

    /**
     * Parses information about a form out of an HTML page.
     * 
     * @param url the URL of the HTML page
     * @param html the HTML page
     * @throws MalformedURLException if the "action" attribute of the tag isn't
     *             a valid URL (can be absolute or relative)
     * @throws InvalidFormException if there is a problem parsing or finding the
     *             form
     */
    public HtmlForm(URL url, String html) throws MalformedURLException, InvalidFormException
    {
        // get the action URL and method of the form
        Matcher matcher = formPattern.matcher(html);
        if(matcher.find())
        {
            Map<String, String> attributes = parseAttributes(matcher.group(1));

            // get action URL
            String action = attributes.get("action");
            if(action != null)
            {
                actionUrl = new URL(url, action);
            } else
            {
                throw new InvalidFormException("No \"action\" attribute found in the form.");
            }

            // get method
            method = attributes.get("method");
            if(method == null)
            {
                method = "GET";
            } else
            {
                method = method.toUpperCase(); // it must be in upper case in
                                               // order for HttpURLConnection to
                                               // recognize it
            }
        } else
        {
            throw new InvalidFormException("No form found in the HTML.");
        }

        // pull out all parameters in the form
        parameters = new HashMap<String, String>();
        matcher = inputPattern.matcher(html);
        while(matcher.find())
        {
            Map<String, String> attributes = parseAttributes(matcher.group(1));

            // ignore buttons
            String type = attributes.get("type");
            if(type != null && (type.equalsIgnoreCase("submit") || type.equalsIgnoreCase("button")))
            {
                continue;
            }

            String name = attributes.get("name");
            if(name != null)
            {
                String value = attributes.get("value");
                if(value == null)
                {
                    value = "";
                }
                parameters.put(name, value);
            }
        }
    }

    /**
     * Given a String that contains only the attributes of a tag, parse out all
     * the attributes and their values into a Map.
     * 
     * @param attributesStr all of the tag's attributes. Should not contain the
     *            tag name or the brackets. For example:
     *            "doubleQuotes=\"typical usage\" attrWithNoValue noQuotes=uglyHtml singleQuotes='an alternative'"
     * @return the attributes and their values
     */
    private static Map<String, String> parseAttributes(String attributesStr)
    {
        Map<String, String> attributes = new HashMap<String, String>();

        Matcher matcher = attributePattern.matcher(attributesStr);
        while(matcher.find())
        {
            String key = matcher.group(1);
            String value = null;
            if(matcher.group(2).trim().length() == 0)
            {
                // it's an attribute with no value
                value = "";
            } else
            {
                for(int i = 4; i <= 6; i++)
                {
                    String g = matcher.group(i);
                    if(g != null)
                    {
                        value = g;
                        break;
                    }
                }
            }
            attributes.put(key, value.trim());
        }

        return attributes;
    }
}
