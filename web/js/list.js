/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */


var gameList = (function() {
    
    var _loginInfo = {};
    
    var _createHtmlRow = function(gameDetails) {
        var humans = gameDetails.humanPlayers;
        var joinedHumans = gameDetails.joinedHumanPlayers;
        var humansString = joinedHumans + '/' + humans;
        var computers = gameDetails.computerizedPlayers;
        var name = gameDetails.name;
        var status = gameDetails.status;
        var htmlClass = status === 'ACTIVE' ? 'warning' : 'active'; // bootstrap class
        var btnHtml = _createButton(name, status);

        var row = '<tr class="' + htmlClass + '">' +
                '<td>' + name + '</td>' +
                '<td>' + humansString + '</td>' +
                '<td>' + computers + '</td>' +
                '<td>' + status + '</td>' +
                '<td>' + btnHtml + '</td>' +
                '</tr>';

        return row;
        
    };

    var _joinGame = function(gameName, playerName, playerMoney) {
        var money = parseInt(playerMoney, 10);
        if (money <= 0 || money > 10000 || isNaN(money)) {
            money = 1000;
        }
        $.ajax({url: 'login', type: 'POST', dataType: 'json',
            data: {gameAction: 'join', roomName: gameName, playerName: playerName, playerMoney: money}})
                .done(function(json) {
                    if (json.success === true) {
                        location.replace(json.gamePage);
                    }
                    else {
                        alert(json.error);
                    }
                });
    };

    var _setupButtons = function() {
        $('#gamesTableBody :button').each(function() {
            $(this).on('click', function() {
                $('#modalTitle').text('Joining Game: ' + $(this).attr('id'));
                var $that = $(this);
                $('#modalJoinButton').off('click').on('click', function() {
                    var gameName = $that.attr('id');
                    _loginInfo.name = gameName;
                    var playerName = $('#playerName').val();
                    var playerMoney = $('#playerMoney').val();
                    _joinGame(gameName, playerName, playerMoney);
                });
            });
        });
    };

    var _createButton = function(game, status) {
        var res;
        if (status === 'ACTIVE' || status === 'FINISHED') {
            res = '<button type="button" disabled data-toggle="modal" data-target="#joinModal" class="btn btn-default btn-info btn-sm" id="' + game + '">Join</button>';
        }
        else {
            res = '<button type="button" data-toggle="modal" data-target="#joinModal" class="btn btn-default btn-info btn-sm" id="' + game + '">Join</button>';
        }
        return res;
    };

    var _blinkEffect = function(selector) {
        $(selector).fadeOut('slow', function() {
            $(this).fadeIn('slow', function() {
                _blinkEffect(this);
            });
        });
    };

    var _showEmpty = function() {
        $('#message').fadeIn();
        _blinkEffect('#blink');
    };

    var _hideEmpty = function() {
        $('#message').hide();
    };

    var update = function() {
        var rows = [];

        $.getJSON('list', function(data) {
            if (data.length === 0)
                _showEmpty();

            else {
                _hideEmpty();
                $.each(data, function(idx, gameDetails) {
                    var row = _createHtmlRow(gameDetails);
                    rows.push(row);
                });

                $('#gamesTableBody').html(rows.join(''));
                _setupButtons();
            }
        });
    };
    
    return {
        update: update
    };
})();

$(document).ready(function() {
    gameList.update();
    
    setInterval(gameList.update, 4 * 1000); 
    
    $('#refreshButton').click(gameList.update);
});