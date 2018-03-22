$(document).ready(function () {
    $('.turkForm #description').on('input', function () {
        var value = $(this).val();
        var submitBtn = $('#submitBtn');
        if (value && value.length) {
            submitBtn.prop("disabled", false);
            submitBtn.val('Submit');
        } else {
            submitBtn.prop("disabled", true);
            submitBtn.val('Enter a description to submit');
        }
    });

    // Preview mode handling
    if (window.previewMode) {
        var btn = $("#submitBtn");
        btn.val("Accept HIT to continue");
        btn.prop("disabled", true);
    }
});