$(function() {
    var prev = function() {
        $("a.page.prev").first().each(function() {
            window.location = this.href
        });
    };
    var next = function() {
        $("a.page.next").first().each(function() {
            window.location = this.href
        });
    }
    $(document).keyup(function (event) {
        if (event.keyCode == 37) {
            prev();
        } else if (event.keyCode == 39) {
            next();
        }
    });
    $(document).bind("swiperight", prev);
    $(document).bind("swipeleft", next);
});