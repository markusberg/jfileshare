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
                        var iPercent = Math.ceil(bytesRead/bytesTotal*100);

                        domStatusText.innerHTML = "Upload status: " + iPercent + "% ("+ humanReadable(bytesRead) + " / " + humanReadable(bytesTotal) + ")";
                        changeWidth( iPercent );
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

            function changeWidth( iPercent ) {
                var iProgress = Math.ceil( (iPercent/100) * iWidth );
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
                ToggleVisibility('uploadform');
                ToggleVisibility('Status');
                iWidth = document.getElementById("ProgressBar").offsetWidth - 2;
                domBar = document.getElementById("Bar");
                domStatusText = document.getElementById("StatusText");
                domIdUpload = document.getElementById("upid");
                setInterval('checkUploadProgress()', 1000);
            }

            var iWidth;
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

                <p>Please select file to upload:
                    <input id="fileinput" type="file" name="file" />
                    <input type="submit" name="submit" value="Upload file" />
                </p>
                <table>
                    <tr>
                        <td><input name="usepw" value="true" type="checkbox" onclick="ToggleVisibility('pwlabel');" /></td>
                        <td>Require password</td>
                    </tr>
                    <tr>
                        <td></td>
                        <td class="note">Even though the url is hard to guess, some browsers may exchange the url data with third parties.To protect sensitive data, additional password is a good idea. This password will be required before user is allowed to download the file.</td>
                    </tr>
                    <tr>
                        <td></td>
                        <td id="pwlabel" style="display: none;">Password: <input type="text" name="password" /></td>
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

