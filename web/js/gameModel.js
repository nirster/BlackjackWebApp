/**
 * game model - responsible for quering the server for new data
 * @author Nir Zarko <nirster@gmail.com>
 */


 var gameModel = (function() {
    
    var eventsCb = $.Callbacks();
    var playersCb = $.Callbacks();
    var localPlayerCb = $.Callbacks();
    var gameDetailsCb = $.Callbacks();
    var errorsCb = $.Callbacks();
    var serverErrorCb = $.Callbacks();
    var sessionInfoCb = $.Callbacks();
    
    function isOperationFailed(obj) {
        if (obj.hasOwnProperty('success')) {
            if (obj.success === false) {
                return true;
            }
        }
        else {
            return false;
        }
    }
    
    function debug(param) {
        if (window.globals.enable_debug === true)
            console.log('%c Debug-Model: ' + param, 'color: #0066FF');
    }
    
    // this will be fired if we got "soft" logic error from the server like
    // invalid action, not logged in and so on.
    // when this happens, the server will return a json object in the format: {success: false, message: val}
    // the message should be human readble
    function onError(msg) {
        debug('### onError(msg) ###');
        errorsCb.fire(msg);
    }

    var updateEvents = function(newEvents) {
        debug('### updateEvents() ###');
        
        if (isOperationFailed(newEvents)) {
            onError(newEvents.message);
        }
        else if ($.isEmptyObject(newEvents)) {
            debug('## no new events ##');
        }
        else {
            debug('## got new events: ##');
            eventsCb.fire(newEvents);
        }
    };
    
    var updatePlayers = function(remotePlayers) {
        debug('### updateRemotePlayers() ###');

        if (isOperationFailed(remotePlayers)) {
            onError(remotePlayers.message);
        }
        else {
            playersCb.fire(remotePlayers);
        }
    };

    var updateLocalPlayer = function(localPlayer) {
        debug('### updateLocalPlayer() ###');
        if (isOperationFailed(localPlayer)) {
            onError(localPlayer.message);
        }
        else {
            localPlayerCb.fire(localPlayer);
        }
    };

    var updateGameDetails = function(gameDetails) {
        debug('### updateGameDetails() ###');
        if (isOperationFailed(gameDetails)) {
            onError(gameDetails.message);
        }
        else {
            gameDetailsCb.fire(gameDetails);
        }
    };
    
    var updateSessionInfo = function(sessionInfo) {
        debug('### updateSessionInfo ###');
        if (isOperationFailed(sessionInfo)) {
            onError(sessionInfo.message);
        }
        else {
            sessionInfoCb.fire(sessionInfo);
            debug(sessionInfo);
        }
    };
    
    function addSessionInfoCallback(f) {
        sessionInfoCb.add(f);
    }
    
    function addEventsCallback(f) {
        eventsCb.add(f);
    }

    function addPlayersCallback(f) {
        playersCb.add(f);
    }

    function addLocalPlayerCallback(f) {
        localPlayerCb.add(f);
    }

    function addGameDetailsCallback(f) {
        gameDetailsCb.add(f);
    }
    
    function addErrorCallback(f) {
        errorsCb.add(f);
    }
    
    function addServerErrorCallback(f) {
        serverErrorCb.add(f);
    }
    
    function pullEvents() {
        debug('### pullEvents() ###');
        $.ajax({url: 'game', type: 'POST', dataType: 'json', data: {gameAction: 'getEvents'}}).done(updateEvents);
    }
    
    function pullGameDetails() {
        debug('### pullGameDetails() ###');
        $.ajax({url: 'game', type: 'POST', dataType: 'json', data: {gameAction: 'getGameDetails'}}).done(updateGameDetails);
    }
    
    function pullLocalPlayer() {
        debug('### pullLocalPlayer() ###');
        $.ajax({url: 'game', type: 'POST', dataType: 'json', data: {gameAction: 'getPlayerDetails'}}).done(updateLocalPlayer);
    }
    
    function pullPlayers() {
        debug('### pullPlayersDetails() ###');
        $.ajax({url: 'game', type: 'POST', dataType: 'json', data: {gameAction: 'getPlayersDetails'}}).done(updatePlayers);
    }
    
    function pullSessionInfo() {
        $.ajax({url: 'game', type: 'POST', dataType: 'json', data: {gameAction: 'getSessionInfo'}}).done(updateSessionInfo);
    }
    
    function init() {
        debug('### init() ###');
        $.ajaxSetup({
            error: function (xhr) {
                alert('something bad happend.');
                serverErrorCb.fire(xhr);
            }
        });
    }

    return {
        init: init,
        pullEvents: pullEvents,
        pullGameDetails: pullGameDetails,
        pullLocalPlayer: pullLocalPlayer,
        pullPlayers: pullPlayers,
        addEventsCallback: addEventsCallback,
        pullSessionInfo: pullSessionInfo,
        addPlayersCallback: addPlayersCallback,
        addLocalPlayerCallback: addLocalPlayerCallback,
        addGameDetailsCallback: addGameDetailsCallback,
        addSessionInfoCallback: addSessionInfoCallback,
        addErrorCallback: addErrorCallback,
        addServerErrorCallback: addServerErrorCallback
    };
})();