<%@page contentType="text/html" pageEncoding="UTF-8"%>
<html>

    <head>
        <title>Upload File</title>
        <link rel="stylesheet" href="<%= request.getContextPath()%>/styles/file.css" type="text/css" />

        <script type="text/javascript">

            function processXMLResponse() {
                if ( oAjax.readyState == 4 ) {
                    if ( oAjax.status == 200 ) {
                        var xml = oAjax.responseXML;
                        var bytesRead = xml.getElementsByTagName("bytesRead")[0].firstChild.data;
                        var bytesTotal = xml.getElementsByTagName("bytesTotal")[0].firstChild.data;
                        var completion = bytesRead/bytesTotal;

                        domStatusText.innerHTML = "Upload status: " + Math.ceil(completion*100) + "% ("+ humanReadable(bytesRead) + " / " + humanReadable(bytesTotal) + ")";
                        changeWidth( completion );
                        oAjax = null;
                    } else {
                        alert( "Error: " + oAjax.statusText );
                    }
                }
            }


            function checkUploadProgress() {
                var url = urlBase + "/ajax/uploadprogress";

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

            function changeWidth( completion ) {
                var iWidth = domProgressBar.offsetWidth-2;
                var iProgress = Math.ceil( completion * iWidth );
                if ( iProgress > iWidth ) {
                    iProgress = iWidth;
                }
                domBar.style.width = iProgress + "px";
            }

            // This is incomplete... might finish this in the future.
            // The plan is to allow several simultaneous uploads. When we do,
            // we will need some mechanism to separate the uploadlisteners
            // from one another.
            function upidNegotiate() {
                var iRandom = Math.floor(Math.random()*1000);
                domIdUpload.value=iRandom;
                idUpload = iRandom;
            }

            function initUpload() {
                clearTimeout(logoutTimer);
                ToggleVisibility('uploadform');
                ToggleVisibility('Status');
                domProgressBar = document.getElementById("ProgressBar");
                domBar = document.getElementById("Bar");
                domStatusText = document.getElementById("StatusText");
                domIdUpload = document.getElementById("upid");
                setInterval('checkUploadProgress()', 1000);
            }

            var domProgressBar;
            var domBar;
            var domStatusText;
            var domIdUpload;
            var uploadstatus;
            var urlBase = "<%= request.getContextPath()%>";
            var oAjax = null;

        </script>

    </head>

    <body>
        <%@include file="/WEB-INF/jspf/MessageBoxes.jspf"%>
        <div id="uploadform" style="display: block;">
            <form action="<%= request.getContextPath()%>/file/upload/" method="post" onsubmit="initUpload();" enctype="multipart/form-data">
                <input type="hidden" name="action" value="fileupload" />

                <table>
                    <tr>
                        <th style="text-align:right;">Select file to upload:</th>
                        <td><input id="fileinput" type="file" name="file" /></td>
                    </tr>
                    <tr>
                        <th style="text-align:right;">File password:</th>
                        <td><input type="text" name="password" />
                            <span class="note">Note: leave blank in order to leave file without password protection</span>
                        </td>
                    </tr>
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
            <div id="StatusText">Status: </div>
            <div id="ProgressBar">
                <div id="Bar"></div>
            </div>
        </div>

    </body>

</html>

