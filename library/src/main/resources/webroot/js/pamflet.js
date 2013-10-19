$(function() {
    var load = function() {
        window.location = this.href;
    };
    var prev = function() {
        $("a.page.prev").first().each(load);
    };
    var next = function() {
        $("a.page.next").first().each(load);
    }
    $(document).keyup(function (event) {
        if (event.altKey || event.ctrlKey || event.shiftKey || event.metaKey)
            return;
        if (event.keyCode == 37) {
            prev();
        } else if (event.keyCode == 39) {
            next();
        }
    });
    var show_message = "show table of contents";
    var hide_message = "hide table of contents";
    $(".collap").collapse({
        "head": "h4",
        show: function () {
            this.animate({ 
                height: "toggle"
            }, 300);
            this.prev(".toctitle").children("a").text(hide_message);
        },
        hide: function () {
            this.animate({
                height: "toggle"
            }, 300);
            this.prev(".toctitle").children("a").text(show_message); 
        }
    });
    $(".collap a.tochead").show();
    $(".collap a.tochead").click(function(event){
        $(".toctitle").children("a").click();
    });
    $(".collap .toctitle a").text(show_message);

    var getParameterByName = function(name) {
        name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
        var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
            results = regex.exec(location.search);
        return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
    }
    var color_scheme = getParameterByName("color_scheme");
    if (color_scheme) {
        $("body").attr("class", "color_scheme-" + color_scheme);
        if (window.localStorage) {
            window.localStorage.color_scheme = color_scheme;
        }
    } else {
        if (window.localStorage) {
            if (window.localStorage.color_scheme) {
                $("body").attr("class", "color_scheme-" + window.localStorage.color_scheme);
            }
        }
    }
});
