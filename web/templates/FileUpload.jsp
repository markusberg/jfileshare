<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.sectra.jfileshare.objects.Conf"%>
<%@page import="com.sectra.jfileshare.objects.FileItem"%>
<html>

    <head>
        <title>Upload File</title>
        <link rel="stylesheet" href="<%= request.getContextPath()%>/styles/file.css" type="text/css" />

        <script type="text/javascript">

            function uploadComplete(status) {
                clearTimeout(uploadProgress);
                updateProgress(0, 0);
                ToggleVisibility('UploadForm');
                ToggleVisibility('Status');

                LogoutTimer.start();
                var messageBox = document.createElement('div');
                messageBox.setAttribute('id', 'messagebox_'+status);
                messageBox.innerHTML = domUploadIFrame.document.getElementById("msg").innerHTML;
                domMessageBoxes.appendChild(messageBox);
            }

            function processXMLResponse() {
                if ( oAjax.readyState == 4 ) {
                    if ( oAjax.status == 200 ) {
                        var xml = oAjax.responseXML;
                        var bytesRead = xml.getElementsByTagName("bytesRead")[0].firstChild.data;
                        var contentLength = xml.getElementsByTagName("contentLength")[0].firstChild.data;
                        updateProgress(bytesRead, contentLength);
                        oAjax = null;
                    }
                }
            }


            function checkUploadProgress() {
                var url = contextPath + "/ajax/file/receiver";
                if ( oAjax != null ) {
                    // A request is already in progress.
                    return;
                }
                oAjax = getAjaxObject();
                oAjax.onreadystatechange = processXMLResponse;
                oAjax.open("GET", url, true);
                oAjax.send(null);
            }

            function humanReadable(iBytes) {
                if (iBytes < 4096 ) {
                    return iBytes + " B";
                }
                if (iBytes < (1024*1024)) {
                    return (iBytes/1024).toFixed(2) + " KiB";
                }
                return (iBytes/1024/1024).toFixed(2) + " MiB";
            }

            function updateProgress(bytesRead, contentLength) {
                var completion;
                var statusText;
                if (contentLength == 0) {
                    completion = 0;
                    statusText = "Not yet started";
                } else {
                    completion = bytesRead/contentLength;
                    statusText = Math.floor(completion*100) + "% (" + humanReadable(bytesRead) + " / " + humanReadable(contentLength) + ")";
                }

                var iWidth = domProgressBar.offsetWidth-2;
                domBar.style.width = Math.floor(completion*iWidth) + "px";
                domStatusText.innerHTML = statusText;
            }

            function initUpload() {
                LogoutTimer.stop();
                ToggleVisibility('UploadForm');
                ToggleVisibility('Status');
                domProgressBar = document.getElementById("ProgressBar");
                domBar = document.getElementById("Bar");
                domStatusText = document.getElementById("StatusText");
                uploadProgress = setInterval("checkUploadProgress()", 1000);
                domUploadIFrame = document.getElementById("UploadIFrame").contentWindow;
                domMessageBoxes = document.getElementById("MessageBoxes");
                domMessageBoxes.innerHTML = '';
            }

            var domProgressBar;
            var domBar;
            var domStatusText;
            var domUploadIFrame;
            var domMessageBoxes;
            var uploadProgress;
            var oAjax = null;

        </script>

    </head>

    <body>
        <%@include file="/WEB-INF/jspf/MessageBoxes.jspf"%>
        <div id="UploadForm" style="display: block;">
            <form action="<%=request.getContextPath()%>/ajax/file/receiver" method="post" onsubmit="initUpload();" target="UploadIFrame" enctype="multipart/form-data">
                <table>
                    <tr>
                        <th style="text-align:right;">Select file to upload:</th>
                        <td><input id="fileinput" type="file" name="file" /></td>
                    </tr>
                    <tr>
                        <th></th>
                        <td class="note">Note: the maximum allowed file size is <%=FileItem.humanReadable(((Conf) getServletContext().getAttribute("conf")).getFileSizeMax()) %></td>
                    </tr>
                    <tr>
                        <th style="text-align:right;">File password:</th>
                        <td><input type="text" class="textentry" name="password" />
                            <span class="note">Note: leave blank in order to leave file without password protection</span>
                        </td>
                    </tr>
                    <tr><td>&nbsp;</td></tr>
                    <tr>
                        <td></td>
                        <td><input type="submit" name="submit" value="Upload file" /></td>
                    </tr>
                    <tr>
                        <td></td>
                        <td class="note"><strong>Important safety tip:</strong> even though
                            the url is difficult to guess, some browsers may exchange the url
                            data with third parties. To protect sensitive data, setting a file
                            password is a good idea. This password will be required before a
                            user is allowed to download the file.</td>
                    </tr>
                </table>

            </form>
        </div>
        <div id="Status" style="display: none;">
            <div>Upload status: <span id="StatusText">Not yet started</span></div>
            <div id="ProgressBar">
                <div id="Bar"></div>
            </div>
        </div>
        <iframe name="UploadIFrame" id="UploadIFrame" src="" style="display: none;"></iframe>
    </body>

</html>

