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
    $(document).bind("swiperight", prev);
    $(document).bind("swipeleft", next);
});
