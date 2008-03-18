function ajaxRequest(action, param,value){
                          var url="";
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



function selectall(mainelement){

    var elements = document.getElementsByTagName("input");
    for ( var i = 0; i < elements.length; i++){
            if ( elements[i].type=="checkbox" && elements[i].className=="deletesel"){
                if (mainelement.checked){
                elements[i].checked = true;
                    } else {
                    elements[i].checked=false;
                }

            }
        }
    
}