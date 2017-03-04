var nounForm = [
        {
            id: 'noun',
            title: "What's inside the box?",
            placeholder: "What is it?",
            fieldType: 'text',
            instruction: "In the image to the left, what is inside the green box? For example, you could type bank, house, or sign"
        }
];

var descriptionForm = [
    {
        id: 'adjectives',
        title: 'How would you describe it?',
        fieldType: 'text',
        instruction: "Please enter words which describe the {{noun}}. For example, if this was a brick house, you could type large red brick"
    }
];

/***********************************************/
var questionBox;
var turkForm;

var returnData = [];

$(document).ready(function() {
    questionBox = $(".questionPart");
    turkForm = $(".turkForm");

    drawStreetviewCanvas();

    buildQuestion(nounForm);
});

function drawStreetviewCanvas() {
    var r = window.rectangle;
    var selectedPoint = window.selectedPoint;
    var rightColumn = $("#right-column");

    var canvas = document.getElementById('streetviewCanvas');
    var ctx = canvas.getContext('2d');

    // Set canvas size
    ctx.canvas.width = rightColumn.innerWidth();
    ctx.canvas.height = rightColumn.innerHeight();

    var img = new Image();
    img.onload = function () {
        var hRatio = canvas.width / img.width;
        var vRatio = canvas.height / img.height;
        var ratio = Math.min(hRatio, vRatio);
        var adjustedWidth = img.width * ratio;
        var adjustedHeight = img.height * ratio;

        ctx.drawImage(img, 0, 0, img.width, img.height, 0, 0, adjustedWidth, adjustedHeight);

        // Draw sampled rects
        var x = r.x1 * adjustedWidth;
        var y = r.y1 * adjustedHeight;
        var w = (r.x2 - r.x1) * adjustedWidth;
        var h = (r.y2 - r.y1) * adjustedHeight;

        ctx.beginPath();
        ctx.lineWidth = "2";
        ctx.strokeStyle = "green";
        ctx.rect(x, y, w, h);
        ctx.stroke();
    };

    img.src = "https://maps.googleapis.com/maps/api/streetview?heading=" + selectedPoint.bearing + "&size=1500x400&fov=180&location="
        + selectedPoint.lat + "," + selectedPoint.long + "&pitch=10&key=AIzaSyCXyMat0qa3WpiXmjsm7k3x21IGHQlrkv8";
}

function buildQuestion(questionJson) {
    var formFields = $("<div></div>");

    questionBox.empty();

    questionJson.forEach(function(field, index) {
        var af = index === 0 ? 'autofocus' : '';

        var qDiv = $("<div></div>");
        var title = $("<span class='title'>" + field.title + "</span>");
        var instruction = $("<span class='instruction'>" + field.instruction + "</span>");
        var input = $("<input type=" + field.fieldType + " name=" + field.id + " placeholder='" + field.placeholder + "' id=" + field.id + " " + af + "/>");
        qDiv.append([title, instruction, input]);
        formFields.append(qDiv);
    });

    var nextBtn = $('<input id="nextBtn" type="button" class="button blue nextBtn" value="Next"/>');
    nextBtn.on('click', nextQuestion);

    if (window.previewMode) {
        nextBtn.val("Accept HIT to continue");
        nextBtn.prop("disabled", true);
    }

    questionBox.append(formFields);
    questionBox.append(nextBtn);
}

function nextQuestion() {
    questionBox.find("input").each(function(index, input) {
        if (input.type === 'button') return;
        returnData.push({name: input.id, value: input.value});
    });

    showActivityIndicator();

    $.ajax({
       url: "objectdescription/next",
        method: "post",
        contentType: "application/json",
        dataType: "json",
        data: JSON.stringify(returnData),
        success: function(data) {
            if (data.status === 'complete') {
                return submitAndComplete();
            }

            buildQuestion(data.form);
        }
    });
}

function showActivityIndicator() {
    questionBox.empty();

    var activityIndicator = $('<div class="activityIndicator">Please wait...</div>');

    questionBox.append(activityIndicator);
}

function submitAndComplete() {
    turkForm.find("#description").val(JSON.stringify(returnData));
    turkForm.submit();
}