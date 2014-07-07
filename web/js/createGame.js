/**
 * methods for creating a new game from form data
 * @author Nir Zarko <nirster@gmail.com>
 */

'use strict';


$(document).ready(function() {

    $("#fileuploader").uploadFile({
        url: "upload",
        onSuccess: function(files, data, xhr) {
            if (data.wasCreated === true) {
                _showGoodAlert();
            }
            else {
                _showBadAlert(data.error);
            }
        }
    });


    $('#createForm').submit(function(event) {
        event.preventDefault();
        bj.createGame();
    });

});

var bj = (function() {
    // private
    var _showGoodAlert = function() {
        var html = '<div class="alert alert-success"><b>Success! </b> Game created.</div>';
        $('#message').html(html);
        _blinkEffect('#blink');
    };

    var _showBadAlert = function(msg) {
        var html = '<div class="alert alert-warning"><b>Oops! </b>' + msg + '</div>';
        $('#message').html(html);
        setTimeout("$('#message').hide()", 3000);
    };

    var _getFormData = function() {
        return {
            humanPlayers: $('#humanPlayers').val(),
            computerPlayers: $('#computerPlayers').val(),
            roomName: $('#roomName').val()
        };
    };

    var _isInputValid = function() {
        return Number($('#humanPlayers').val()) + Number($('#computerPlayers').val()) <= 6 &&
                Number($('#humanPlayers').val()) >= 1;
    };

    var _showBadInputError = function() {
        $('#message').html(_createHtmlAlert);
        setTimeout("$('#message').hide()", 3000);
    };

    var _createHtmlAlert = function() {
        var html = '<div class="alert alert-warning"><b>Oops!</b> Amount of players must be six at most with at least one human player.</div>';
        return html;
    };

    var _blinkEffect = function(selector) {
        $(selector).fadeOut('slow', function() {
            $(this).fadeIn('slow', function() {
                _blinkEffect(this);
            });
        });
    };

    var createGame = function() {
        if (_isInputValid()) {
            $.ajax({
                url: 'createGame',
                type: "POST",
                dataType: 'json',
                data: _getFormData(),
                success: function(data) {
                    if (data.wasCreated === true) {
                        _showGoodAlert();
                    }
                    else {
                        _showBadAlert(data.error);
                    }
                }
            });
        }

        else {
            _showBadInputError();
        }
    };

    return {
        createGame: createGame
    };
})();