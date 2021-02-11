package au.gov.qld.bdm.documentproduction;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.iki.elonen.NanoHTTPD;

public class MockCas {
    private static final Logger LOG = LoggerFactory.getLogger(MockCas.class);
    private static final String USER_RESPONSE =
            "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>"
                    + "    <cas:version>2-bdm</cas:version>"
                    + "        <cas:authenticationSuccess>"
                    + "                <cas:user>%s</cas:user>"
                    + "                <cas:attributes>%n"
                    + "                        <cas:username>%s</cas:username>%n"
                    + "                        <cas:fullName>%s</cas:fullName>%n"
                    + "                        <cas:email>%s</cas:email>%n"
                    + "                        <cas:agency>%s</cas:agency>%n"
                    + "                        <cas:roles>%s</cas:roles>%n"
                    + "                </cas:attributes>%n"
                    + "        </cas:authenticationSuccess>"
                    + "</cas:serviceResponse>";
    private final NanoHTTPD httpd;
    private final Map<String, String> casResponseData = new HashMap<>();

    public static void main(String[] args) throws Exception {
    	startServer(args);
        while(true) {
        	Thread.sleep(1000);
        }
    }

	public static void startServer(String[] args) {
		int port = 5000;
    	for (String arg : args) {
    		if (arg.startsWith("port=")) {
    			port = Integer.parseInt(arg.substring(arg.indexOf("=") + 1, arg.length()));
    			System.out.println("Setting port to " + port);
    		}
    	}
    	
        new MockCas(8443, "http://localhost:" + port + "/user/", "DOCUMENT_PRODUCTION_LOCAL").start();
	}
    
    public MockCas(int port, final String redirect, String role) {
        casResponseData.put("username", "Steven.Baker@justice.qld.gov.au");
        casResponseData.put("email", "Steven.Baker@justice.qld.gov.au");
        casResponseData.put("fullName", "Steven Baker");
        casResponseData.put("agency", "RBDM");
        casResponseData.put("roles", role);
        
        this.httpd = new NanoHTTPD(port) {
            public Response serve(IHTTPSession session) {
                String uri = session.getUri();
                if (uri.contains("favicon")) {
                    return newFixedLengthResponse(Response.Status.OK, "application/ico", "some icon");
                }

                LOG.info("Request to {}", uri);
                for (Entry<String, String> entry : casResponseData.entrySet()) {
                    if (uri.contains(entry.getKey())) {
                        String value = defaultString(session.getParameters().get("val").get(0)).trim();
                        LOG.info("Setting {} to {}", entry.getKey(), value);
                        if (isBlank(value)) {
                            return null;
                        }
                        
                        entry.setValue(value);
                        return newFixedLengthResponse(Response.Status.ACCEPTED, NanoHTTPD.MIME_PLAINTEXT, "set " + value + " to " + entry.getKey());
                    }
                }
                
                if (uri.contains("login")) {
                    LOG.info("Redirecting to validate url");
                    //assertThat("Redirects should not contain path params or WAF blocks access", allowedReferers, hasItem(session.getParms().get("service")));
                    NanoHTTPD.Response response = newFixedLengthResponse(Response.Status.REDIRECT, NanoHTTPD.MIME_PLAINTEXT, "redirect");
                    response.addHeader("Location", redirect + "?ticket=" + new Random().nextInt(Integer.MAX_VALUE));
                    return response;
                } else if (uri.contains("serviceValidate")) {
                    String responseData = String.format(USER_RESPONSE, casResponseData.get("username"), casResponseData.get("username"), casResponseData.get("fullName"), 
                            casResponseData.get("email"), casResponseData.get("agency"), casResponseData.get("roles"));
                    LOG.info("Returning user data\n{}", responseData);
                    return newFixedLengthResponse(Response.Status.OK, "application/xml", responseData);
                } else if (uri.contains("logout")) {
                    LOG.info("Redirecting to service so it can login again");
                    NanoHTTPD.Response response = newFixedLengthResponse(Response.Status.REDIRECT, NanoHTTPD.MIME_PLAINTEXT, "redirect");
                    response.addHeader("Location", redirect);
                    return response;
                }

                throw new IllegalStateException("Unknown request to " + uri);
            }
        };
    }

    public MockCas start() {
        try {
            httpd.start();
            LOG.info("Started mock cas");
        } catch (IOException e) {
            LOG.warn(e.getMessage());
        }
        return this;
    }

    public MockCas stop() {
        httpd.stop();
        return this;
    }

}
