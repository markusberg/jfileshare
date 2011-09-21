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
 * @version     1.6
 * @since       2011-09-21
 */
package com.sectra.jfileshare.servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

/***
 * Simple http forwarding servlet allowing link compatibility with older versions
 * of jfileshare
 * @author markus
 */
public class LinkCompatibilityServlet extends HttpServlet {
    private static final Logger logger =
            Logger.getLogger(LinkCompatibilityServlet.class.getName());

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String PathInfo = req.getPathInfo().substring(1);
        String md5sum = PathInfo.split("_SECTRA_")[0];
        int fid = Integer.parseInt(PathInfo.split("_SECTRA_")[1]);

        logger.log(Level.INFO, "Access requested to file: {0}", fid);

        String url = req.getContextPath();
        if (req.getServletPath().equals("/download/view")) {
            url += "/file/view/" + fid + "?md5=" + md5sum;
        } else if (req.getServletPath().equals("/download/get")) {
            url += "/file/download/" + fid + "?md5=" + md5sum;
        }
        resp.sendRedirect(url);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}
