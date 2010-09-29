function ToggleVisibility( sName, style ) {
    var sStyle = style==null?"block":style;
    var domName = document.getElementById( sName );
    domName.style.display = ( domName.style.display=="none" ? sStyle : "none" );
}

function getAjaxObject() {
    var oAjax;

    try {
        // Opera 8.0+, Firefox, Safari
        oAjax = new XMLHttpRequest();
    } catch (e) {
        // Internet Explorer Browsers
        try {
            oAjax = new ActiveXObject("Msxml2.XMLHTTP");
        } catch (e) {
            try {
                oAjax = new ActiveXObject("Microsoft.XMLHTTP");
            } catch (e) {
                // Something went wrong
                alert( "Error: " + e );
                return false;
            }
        }
    }
    return oAjax;
}
