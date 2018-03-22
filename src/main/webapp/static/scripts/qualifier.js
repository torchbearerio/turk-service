/**
 * Created by fredricvollmer on 3/20/18.
 */
var currentSlide = 0;
var slides = [];

function setButtonText(t) {
    $('.nextButton').text(t);
}

function disableNextButton() {
    $('.nextButton').prop('disabled', true);
}

function goToSlide(n) {
    slides.css('display', 'none');
    slides.eq(n).css('display', 'block');

    if (n === slides.length - 1) {
        setButtonText("Do your first HIT!")
    } else {
        setButtonText("Next")
    }
}

function handleClick () {
    if (currentSlide === (slides.length - 1)) {
        setButtonText("Loading...");
        disableNextButton();
        Cookies.set('torchbearer_qualified_' + window.hitType, 'true');
        window.location.reload();
    } else {
        goToSlide(++currentSlide);
    }
}

$(document).ready(function() {
    slides = $(".slide");
    goToSlide(0);

    $('.nextButton').click(handleClick);
});