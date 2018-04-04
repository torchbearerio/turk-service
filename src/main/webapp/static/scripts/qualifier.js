/**
 * Created by fredricvollmer on 3/20/18.
 */
var currentSlide = 0;
var slides = [];

function setButtonText(t) {
    $('.nextButton').text(t);
}

function goToSlide(n) {
    slides.css('display', 'none');
    slides.eq(n).css('display', 'block');

    if (n === slides.length - 1) {
        setButtonText("Done! (close window)")
    } else {
        setButtonText("Next")
    }
}

function handleClick () {
    if (currentSlide === (slides.length - 1)) {
        window.close();
    } else {
        goToSlide(++currentSlide);
    }
}

$(document).ready(function() {
    slides = $(".slide");
    goToSlide(0);

    $('.nextButton').click(handleClick);
});