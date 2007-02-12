
<%@ page contentType="text/html;charset=UTF-8" language="java" %><html>
  <head><title>Uploader</title>
     <link rel="stylesheet" href="/styles/uploader.css" type="text/css" /> 

      <script type="text/javascript">
           
  var w = 20;
            var t;
            var p = "q";
            var x = 0;
            var y = 100;
            var q = 5;
            var f = 0;
            var g = 0;
            var h = 40;
            var v = 290;
            var n = 0;
            var o = 0;
            var m = 0;

            var ran_number;

            function changecolor(newcolor){if(p=="r"&& document.getElementById){document.getElementById('r').style.backgroundColor = newcolor; return;}
              if(document.layers){ // browser="NN4";
               document.layers["q"].bgColor = newcolor;
            }
              if(document.all){ // browser="IE";
               document.all.q.style.backgroundColor = newcolor;
            }
              if(!document.all && document.getElementById){ // browser="NN6+ or IE5+";
               document.getElementById('q').style.backgroundColor = newcolor;
            }
            }

            function changewidth(){
             if(x>396&&f==0){f=0;return;}
             if(x<101&&f==1){f=0;return;}
             if(f)q=-5;if(!f)q=5;x=x+q;
             e=document.getElementById("b");
             e.style.width = x + 'px';
              t=setTimeout("changewidth();",0);
            }


            function getRand(){
                ran_number = Math.floor(Math.random()*1000);
                document.getElementById("upid").value=ran_number;
                alert(document.getElementById("upid").value);

                return ran_number;
            }


          function upidNegotiate(){
              var ran_number = Math.floor(Math.random()*1000);
              document.getElementById("upid").value=ran_number;
              ajaxRequest("setunid","unid",ran_number);
              return false;

          }

          function triggered() {
              var response;
              var status;
              var unid;
              if ((xmlhttp.readyState == 4) && (xmlhttp.status == 200)) {
				  response = xmlhttp.responseXML;
				  status =  response.getElementsByTagName('status')[0].firstChild.data;
				  unid = response.getElementsByTagName('unid')[0].firstChild.data;

                  var upiddive = document.getElementById("upiddiv");
                  upiddive.innerHTML="Negotiated upid: " + unid;
                  var theform = document.getElementById("uploadform");
                  alert(theform);
                  try {
                     theform.submit();
                      } catch (e){
                      
                      alert(e);
                  }
                  
              } else {
                  //alert("Something nasty happened");
              }



          }

         function ajaxRequest(action, param,value){
            if (param == null ){
                url = "/ajax/?action=" + action;
                } else {
                url = "/ajax/?action=" + action + "&" + param + "=" + value;
                }

            try {

               xmlhttp = window.XMLHttpRequest?new XMLHttpRequest():
                    new ActiveXObject("Microsoft.XMLHTTP");

             }
             catch (e) {
                    alert(e);
             }

             xmlhttp.onreadystatechange = triggered;
             xmlhttp.open("GET", url);
             xmlhttp.send(null);

        }

      </script>
  </head>
  <body>Please upload file<br />
  <!--<form action="/upload/" method="post" id="uploadform" onsubmit="return upidNegotiate();"><input type="hidden" name="action" value="bar" /></form>-->
  <form action="/upload/" method="post" id="uploadform" onsubmit="return upidNegotiate();" enctype="multipart/form-data">
      <input id="upid" type="hidden" name="upid" value="" />
      <input type="hidden" name="action" value="sendfile" />
      <input type="file" name="file" />

      <input type="submit" name="submit" value="Send"/>

  </form>

  <div id="p" style="width: 400px;" onclick="changewidth()">
      <div id="b" style="width: 1px;"></div>
      </div>

  <div style="width:100px; height: 100px; border: 1px solid black;" id="upiddiv">Foo</div>
  </body>
<script type="text/javascript">
    progress();
</script>
</html>