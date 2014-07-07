/**
 * game controller
 * @author Nir Zarko <nirster@gmail.com>
 */

 'use strict';

 var gameController = (function() {
     
     var _model;
     var _view;
     var _sessionInfo; // used for debugging
     var _responseEventId;
     var _handNumber = 0; // current hand
     var serverInterval; // interval ID of the server-polling task
     
    function debug(param) {
        if (window.globals.enable_debug === true)
            console.log('%c Debug-Controller: ' + param, 'color: #006600');
    }
    
                /*************************/
                /*    SERVER CALLS START */
                /*************************/
    function serverDoDouble() {
        debug('### doDouble(betNumber) ###');
        $.ajax({url: 'game', type: 'POST', dataType: 'json',
            data: {playerAction: 'double', bet: _handNumber, responseEventId: _responseEventId}})
                .done(function(response) {
                    if (response.success === true) {
                        _view.printMessage('Double performed.');
                    } else {
                        _view.printError(response.message);
                    }
                });
    }

    function serverDoSplit() {
        debug('### doSplit() ###');
        $.ajax({url: 'game', type: 'POST', dataType: 'json', 
            data: {playerAction: 'split', responseEventId: _responseEventId}})
                .done(function(response) {
                    if (response.success === true) {
                        _view.printMessage('Split performed.');
                    } else {
                        _view.printError(response.message);
                    }
                });
    }

    function serverDoStand() {
        debug('### doStand(betNumber) ###');
        $.ajax({url: 'game', type: 'POST', dataType: 'json',
            data: {playerAction: 'stand', bet: _handNumber, responseEventId: _responseEventId}})
                .done(function(response) {
                    if (response.success === true) {
                        _view.printMessage('Stand performed.');
                    } else {
                        _view.printError(response.message);
                    }
                });
    }

    function serverDoHit() {
        debug('### doHit(betNumber) ###');
        $.ajax({url: 'game', type: 'POST', dataType: 'json',
            data: {playerAction: 'hit', bet: _handNumber, responseEventId: _responseEventId}})
                .done(function(response) {
                    if (response.success === true) {
                        _view.printMessage('Hit performed.');
                    } else {
                        _view.printError(response.message);
                    }
                });
    }

    function serverDoPlaceBet(amount) {
        debug('### doPlaceBet(amount) ###');
        $.ajax({url: 'game', type: 'POST', dataType: 'json',
            data: {playerAction: 'placebet',
                money: amount,
                responseEventId: _responseEventId}})
                .done(function(response) {
                    if (response.success === true) {
                        _view.printMessage('You placed a bet of ' + amount + '$!');
                    }
                    else {
                        _view.printError(response.message);
                    }
                });
    }

    function serverDoResign() {
        $.ajax({url: 'game', type: 'POST', dataType: 'json', data: {gameAction: 'resign'}})
                .done(function(response) {
                    if (response.success === true) {
                        _view.printMessage('You will be removed when your turn comes.');
                    }
                    else {
                        _view.printError(response.message);
                    }
                    window.location.replace("index.html");
                });
    }
    
                /*****************************/
                /*    SERVER EVENTS HANDLING */
                /*****************************/
    function onHitEvent(event) {
        if (event.playerName !== window.globals.player_name) {
            _view.printMessage(event.playerName + ' is hitting');
        }
    }
    
    function onStandEvent(event) {
        if (event.playerName !== window.globals.player_name) {
            _view.printMessage(event.playerName + ' is standing');
        }
    }
    
    function onDoubleEvent(event) {
        if (event.playerName !== window.globals.player_name) {
            _view.printMessage(event.playerName + ' doubles his bet');
        }
    }
    
    function onSplitEvent(event) {
        if (event.playerName !== window.globals.player_name) {
            _view.printMessage(event.playerName + ' splits his hand');
        }
    }
    
    function onPlaceBetEvent(event) {
        if (event.playerName !== window.globals.player_name && event.playerName !== 'Dealer') {
            _view.printMessage(event.playerName + ' is betting with ' + event.money + '$');
        }
    }
    
    function onGameStart(event) {
        _view.printMessage('Game started!');
        
    }
    
    function onPlayerResigned(event) {
        debug('### onPlayerResigned(event) ###');
        if (window.globals.player_name !== event.playerName) {
            _view.printMessage(event.playerName + ' left the game.');
        }
        else {
            alert('You have been removed from the game due to inactivity.');
            clearInterval(serverInterval); // stop polling the server
        }
    }
    
    function onUserAction(event) {
        switch (event.playerAction) {
            case 'HIT':
                onHitEvent(event);
                break;
            case 'STAND':
                onStandEvent(event);
                break;
            case 'DOUBLE':
                onDoubleEvent(event);
                break;
            case 'SPLIT':
                onSplitEvent(event);
                break;
            case 'PLACE_BET':
                onPlaceBetEvent(event);
                break;
        }
    }
    
    function onPlayerTurn(event) {
        if (event.playerName !== window.globals.player_name) {
            _view.printMessage(event.playerName + ' is now playing');
        }
    }
    
    function onGameOver(event) {
        _view.printMessage('Game is over!');
        clearInterval(serverInterval);
        _model.pullPlayers();
    }
    
    function onNewRound(event) {
        _view.clearMessages();
        _view.printMessage('New round is starting!');
        _view.newRound();
        _handNumber = 0;
    }
    
    function onCardsDealt(event) {
        console.log(event);
    }
    
    function onGameWinner(event) {
        if (event.playerName === window.globals.player_name) {
            if (event.money > 0)
            _view.printMessage('You won ' + event.money + '$!');
            
            if (event.money < 0)
                 _view.printMessage('You lost ' + Number(-event.money) + '$!');
        }
        else {
            if (event.money > 0)
                _view.printMessage(event.playerName + ' won ' + event.money + '$!');
            if (event.money < 0)
                _view.printMessage(event.playerName + ' lost ' + Number(-event.money) + '$!');
        }
    }
    
    function onPromptForAction(event) {
        if (event.playerName === window.globals.player_name) {
            _responseEventId = event.id;
            _view.printUrgent('Please take action');
        }
        else {
            _view.printMessage('Waiting for ' + event.playerName + ' to make his move.');
        }
    }
    
    function proccessEvent(event) {
        switch (event.type) {
            case 'PLAYER_RESIGNED':
                onPlayerResigned(event);
                break;
            case 'USER_ACTION':
                onUserAction(event);
                break;
            case 'GAME_START':
                onGameStart(event);
                break;
            case 'GAME_OVER':
                onGameOver(event);
                break;
            case 'GAME_WINNER':
                onGameWinner(event);
                break;
            case 'PLAYER_TURN':
                onPlayerTurn(event);
                break;
            case 'CARDS_DEALT':
                onCardsDealt(event);
                break;
            case 'NEW_ROUND':
                onNewRound(event);
                break;
            case 'PROMPT_PLAYER_TO_TAKE_ACTION':
                onPromptForAction(event);
                break;
        }
    }

    function init(model, view) {
        _model = model;
        _view = view;
        
        // error handling
        $.ajaxSetup({
            error: function(xhr) {
                alert('something bad happend');
            }
        });
        
        // leave the game when the user closes his window.
        $(window).unload(function() {
            serverDoResign();
        });
        
        // init model and view
        _view.init();
        _model.init();
        
        // wire up UI events coming from view
        _view.addPlaceBetCallback(serverDoPlaceBet);
        _view.addHitCallback(serverDoHit);
        _view.addStandCallback(serverDoStand);
        _view.addDoubleCallback(serverDoDouble);
        _view.addSplitCallback(serverDoSplit);
        _view.addResignCallback(serverDoResign);
        _view.addHandChangedCallback(function(handNumber) {
            _handNumber = handNumber;
        });
        
        // get the session info (used for debugging)
        _model.addSessionInfoCallback(function(sessionInfo) {
           _sessionInfo = sessionInfo;
           window.globals.player_name = sessionInfo.playerName;
        });
        
        // "soft" game logic errors returning from the server after illegal moves and so on
        _model.addErrorCallback(function(errorJson) {
            if (errorJson.message === 'Please log in.') {
                clearInterval(serverInterval);
                window.location.replace("index.html");
            }
            else {
                _view.printError(errorJson);
            }
        });
        // "real" server error will be handled here (http 500+)
        _model.addServerErrorCallback(function (xhr) {
           clearInterval(serverInterval); 
        });
        
        // wire up events coming from the model
        _model.addPlayersCallback(function(playersDetails) {
            var remotePlayers = [];
            var dealer;
            var localPlayer;
            for (var i = 0; i < playersDetails.length; ++i) {
                if (playersDetails[i].name !== window.globals.player_name && playersDetails[i].name !== 'Dealer') {
                    if (playersDetails[i].status !== 'RESIGNED') {
                        remotePlayers.push(playersDetails[i]);
                    }
                }
                else if (playersDetails[i].name === 'Dealer') {
                    dealer = playersDetails[i];
                }
                else if (playersDetails[i].name === window.globals.player_name) {
                    localPlayer = playersDetails[i];
                }
            }
            _view.printRemotePlayers(remotePlayers);
            _view.printDealer(dealer);
            _view.printLocalPlayer(localPlayer);
        });
        
        _model.addEventsCallback(function(events) {
            for (var i = 0; i < events.length; ++i) {
                proccessEvent(events[i]);
            }
            
        });
        
        _model.addGameDetailsCallback(function(gameDetails) {
            _view.printGameDetails(gameDetails);
            if (gameDetails.status === 'FINISHED') {
                clearInterval(serverInterval);
            }
        });

        serverInterval = setInterval(function() {
            _model.pullSessionInfo();
            _model.pullGameDetails();
            _model.pullPlayers();
            _model.pullEvents();
            
        } , 4 * 1000);
        
        _model.pullSessionInfo();
        _model.pullGameDetails();
        _model.pullPlayers();
        _model.pullEvents();
    }

    return {
        init: init
    };
})();