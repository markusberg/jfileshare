/**
 *  Copyright 2011 SECTRA Imtec AB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * @author      Markus Berg <markus.berg @ sectra.se>
 * @version     1.14
 * @since       2014-08-21
 */
package nu.kelvin.jfileshare.filters;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SetCachingPolicyFilter implements Filter {

    private String excludePath;
    private static final Logger logger =
            Logger.getLogger(SetCachingPolicyFilter.class.getName());

    public void init(FilterConfig config)
            throws ServletException {
        this.excludePath = config.getInitParameter("excludePath");
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String pathRequest = req.getRequestURI();
        String pathExclude = req.getContextPath() + this.excludePath;
        // logger.log(Level.INFO, "Exclude pattern: {0}", pathExclude);
        // logger.log(Level.INFO, "Request path: {0}", pathRequest);

        if (!pathRequest.startsWith(pathExclude)) {
            HttpServletResponse resp = (HttpServletResponse) response;
            resp.setHeader("Pragma", "no-cache");
            resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0");
            resp.setDateHeader("Expires", -1);
            resp.setDateHeader("Last-Modified", System.currentTimeMillis() - 1000 * 60 * 30);
            // } else {
            // logger.log(Level.INFO, "Excluding this request from caching policy: {0}", pathRequest);
        }

        chain.doFilter(request, response);
    }
}
