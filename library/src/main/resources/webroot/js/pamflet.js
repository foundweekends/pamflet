$(function() {
    var load = function() {
        window.location = this.href + document.location.search;
    };
    var prev = function() {
        $("a.page.prev").first().each(load);
    };
    var next = function() {
        $("a.page.next").first().each(load);
    }
    var present = function() {
        for (i in document.styleSheets) {
            var sheet = document.styleSheets[i];
            if (sheet.media && sheet.media.mediaText.indexOf("min-device-width") > -1)
                sheet.disabled = true;
        }
    }
    if (window.location.search.indexOf("present") > -1) {
        present();
    }
    $(document).keydown(function (event) {
        if (event.ctrlKey && event.which == 116) {
            event.preventDefault();
            window.location = window.location + "?present";
        } else if (event.altKey || event.ctrlKey || event.shiftKey || event.metaKey) {
            return;
        } else if (event.which == 37) {
            prev();
        } else if (event.which == 39) {
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
});
