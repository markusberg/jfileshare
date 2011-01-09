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

var LogoutTimer = (function(){
    var idTimer;
    var timeStart = 0;
    var timeTimeout = 1000*60*30;
    return {
        start: function() {
            idTimer = setTimeout(function() {
                window.location=contextPath+"/logout?reason=inactivity";
            }, timeTimeout);
            var dateNow = new Date();
            timeStart = dateNow.getTime();
        },
        stop: function() {
            timeStart = 0;
            clearTimeout(idTimer);
        },
        restart: function() {
            this.stop();
            this.start();
        },
        forceLogout: function() {
            window.location=contextPath+"/logout?reason=sessionexpired";
        },
        getTimeUntilLogout: function() {
            if (timeStart == 0) {
                return 0;
            }
            var dateNow = new Date();
            return timeStart + timeTimeout - dateNow.getTime();
        }
    };
})();
