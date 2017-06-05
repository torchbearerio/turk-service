$(document).ready(function() {
    // Add selection to streetview image
    var img = $('#streetView');
    var h = img.get(0).naturalHeight;
    var w = img.get(0).naturalWidth;
    img.imgAreaSelect({
        handles: true,
        imageHeight: h,
        imageWidth: w,
        onSelectEnd: handleSelectionChanged
    });

    // Preview mode handling
    if (window.previewMode) {
        var btn = $("#submitBtn");
        btn.val("Accept HIT to continue");
        btn.prop("disabled", true);
    }
});

var handleSelectionChanged = function(img, selection) {
    var btn = $("#submitBtn");
    btn.prop("disabled", false);

    var form = $('.turkForm');
    form.find("input[name='x1']").val(selection.x1);
    form.find("input[name='x2']").val(selection.x2);
    form.find("input[name='y1']").val(selection.y1);
    form.find("input[name='y2']").val(selection.y2);
};