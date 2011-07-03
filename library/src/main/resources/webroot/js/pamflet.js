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
    document.body.ontouchstart = function(e){
        var width = $(document).width();
        if(e.touches.length == 1 && 
           e.changedTouches.length == 1 &&
           Math.abs(e.touches[0].screenX - width/2) > (width / 4)
          ){
            orig = e.touches[0];
            e.preventDefault();
        }
    };
    document.body.ontouchend = function(e){
        reset();
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
