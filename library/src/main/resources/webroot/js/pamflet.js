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
    container = $(".container")[0];
    var orig = null;
    container.ontouchstart = function(e){
        if(e.touches.length == 1 && e.changedTouches.length == 1){
            orig = e.touches[0];
        } else orig = null;
    };
    var horiz = function(touch) {
        return orig && Math.abs(
            (touch.clientY - orig.clientY) / (touch.clientX - orig.clientX)
        ) < 1;
    }
    container.ontouchmove = function(e){
        if(e.touches.length == 1){
            var touch = e.touches[0];
            if (horiz(touch)) {
                e.preventDefault();
                container.style.left = (touch.clientX - orig.clientX) + "px";
            }
        }
    };
});
