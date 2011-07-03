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
    var orig = null;
    var moved = false;
    var reset = function() {
        $(".container").animate({left: "0px"});
        orig = null;
        moved = false;
    }
    var edge = function(touch) {
        var width = screen.width;
        return Math.abs(touch.screenX - width/2) > (3*width / 8);
    };
    document.body.ontouchstart = function(e){
        if(e.touches.length == 1 && 
           e.changedTouches.length == 1 &&
           edge(e.touches[0])
          ){
            orig = e.touches[0];
            e.preventDefault();
        }
    };
    document.body.ontouchend = function(e){
        if(moved && e.touches.length == 0 && e.changedTouches.length == 1){
            var half = screen.width / 2;
            var touch = e.changedTouches[0];
            if (orig.screenX > half&& touch.screenX < half
                && $(".next").length > 0) {
                $(".container").animate({
                    left: "-=" + half + "px"
                }, 100, "linear", next);
            } else if (orig.screenX < half && touch.screenX > half
                       && $(".prev").length > 0) {
                $(".container").animate({
                    left: "+=" + half + "px"
                }, 100, "linear", prev);
            } else {
                reset();
            }
        } else {
            reset();
        }
    };
    document.body.ontouchmove = function(e){
        if(e.touches.length == 1){
            var touch = e.touches[0];
            if (moved || horiz(touch)) {
                moved = true;
                e.preventDefault();
                $(".container").css({
                    left: (touch.clientX - orig.clientX) + "px",
                });
            }
        }
    };
    var horiz = function(touch) {
        return orig && Math.abs(
            (touch.clientY - orig.clientY) / (touch.clientX - orig.clientX)
        ) < 1;
    }
});
