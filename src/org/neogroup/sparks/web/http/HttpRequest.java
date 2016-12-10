
package org.neogroup.sparks.web.http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequest {
    
    private static final String QUERY_PARAMETERS_REGEX = "[&]";
    private static final String QUERY_PARAMETER_VALUES_REGEX = "[=]";
    private static final String FILE_ENCODING_SYSTEM_PROPERTY_NAME = "file.encoding";
    private static final String URI_SEPARATOR = "/";
    
    private final HttpExchange exchange;
    private Map<String,String> parameters;
    private byte[] body;
    
    public HttpRequest(HttpExchange exchange) {
        this.exchange = exchange;
    }

    public String getMethod() {
        return exchange.getRequestMethod();
    }

    public byte[] getBody() {
        
        if (body == null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                InputStream inputStream = exchange.getRequestBody();
                int read = inputStream.read();
                while (read != -1) {
                    byteArrayOutputStream.write(read);
                    read = inputStream.read();
                }
            }
            catch (Exception ex) {
                throw new RuntimeException("Error reading request body !!");
            }
            body = byteArrayOutputStream.toByteArray();
        }
        return body;
    }
    
    public URI getUri() {
        return exchange.getRequestURI();
    }

    public Headers getHeaders() {
        return exchange.getRequestHeaders();
    }
    
    public String getQuery() {
        return exchange.getRequestURI().getRawQuery();
    }
    
    public String getPath() {
        return exchange.getRequestURI().getRawPath();
    }
    
    public List<String> getPathParts() {
        String path = getPath();
        String[] pathTokens = path.split(URI_SEPARATOR);
        return Arrays.asList(pathTokens);
    }
    
    public Map<String,String> getParameters() {
        
        if (parameters == null) {
            parameters = new HashMap<>();
            appendParametersFromQuery(getQuery());
            String requestContentType = getHeaders().getFirst(HttpHeader.CONTENT_TYPE);
            if (requestContentType != null && requestContentType.equals(HttpHeader.APPLICATION_FORM_URL_ENCODED)) {
                appendParametersFromQuery(new String(getBody()));
            }
        }
        return parameters;
    }
    
    public String getParameter (String name) {
        return getParameters().get(name);
    }

    public boolean hasParameter (String name) {
        return getParameters().containsKey(name);
    }

    private void appendParametersFromQuery (String query) {

        try {
            if (query != null) {
                String pairs[] = query.split(QUERY_PARAMETERS_REGEX);
                for (String pair : pairs) {
                    String param[] = pair.split(QUERY_PARAMETER_VALUES_REGEX);
                    String key = null;
                    String value = null;
                    if (param.length > 0) {
                        key = URLDecoder.decode(param[0], System.getProperty(FILE_ENCODING_SYSTEM_PROPERTY_NAME));
                    }
                    if (param.length > 1) {
                        value = URLDecoder.decode(param[1], System.getProperty(FILE_ENCODING_SYSTEM_PROPERTY_NAME));
                    }
                    parameters.put(key, value);
                }
            }
        }
        catch (Exception ex) {
            throw new RuntimeException("Error reading request parameters !!");
        }
    }
}