var selections = [];

$(window).on("load", function () {
    // Add selection to streetview images
    var img = $('#streetViewAt').first();
    if (img.length) {
        var h = img.prop("naturalHeight");
        var w = img.prop("naturalWidth");
        selections.push(img.imgAreaSelect({
            instance: true,
            handles: true,
            imageHeight: h,
            imageWidth: w,
            onSelectEnd: function (img, selection) {
                handleSelectionChanged(img, selection, "at");
            }
        }));
    }

    img = $('#streetViewJustBefore').first();
    if (img.length) {
        h = img.prop("naturalHeight");
        w = img.prop("naturalWidth");
        selections.push(img.imgAreaSelect({
            instance: true,
            handles: true,
            imageHeight: h,
            imageWidth: w,
            onSelectEnd: function (img, selection) {
                handleSelectionChanged(img, selection, "justBefore");
            }
        }));
    }

    img = $('#streetViewBefore').first();
    if (img.length) {
        h = img.prop("naturalHeight");
        w = img.prop("naturalWidth");
        selections.push(img.imgAreaSelect({
            instance: true,
            handles: true,
            imageHeight: h,
            imageWidth: w,
            onSelectEnd: function (img, selection) {
                handleSelectionChanged(img, selection, "before");
            }
        }));
    }

    // Preview mode handling
    if (window.previewMode) {
        var btn = $("#submitBtn");
        btn.val("Accept HIT to continue");
        btn.prop("disabled", true);
    }
});

var handleSelectionChanged = function (img, selection, position) {
    var btn = $("#submitBtn");

    var form = $('.turkForm');
    form.find("input[name='x1_" + position + "']").val(selection.x1);
    form.find("input[name='x2_" + position + "']").val(selection.x2 - 1);   // convert to array form
    form.find("input[name='y1_" + position + "']").val(selection.y1);
    form.find("input[name='y2_" + position + "']").val(selection.y2 - 1);   // convert to array form

    var selectionCount = 0;
    selections.forEach(function (s) {
        var sel = s.getSelection();
        if (sel.width > 0 || sel.height > 0)
            selectionCount++;
    });

    if (selectionCount === selections.length) {
        btn.prop("disabled", false);
        btn.val("Submit");
    }
};