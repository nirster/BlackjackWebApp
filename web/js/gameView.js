/**
 * methods for drawing the UI and DOM manipulation
 * @author Nir Zarko <nirster@gmail.com>
 */


'use strict';

var gameView = (function() {
    var imagesUrl = window.location.protocol + "//" + window.location.host + '/api/images/';
    var ranks = {'ace': 11, 'two': 2, 'three': 3, 'four': 4, 'five': 5,
        'six': 6, 'seven': 7, 'eight': 8, 'nine': 9, 'ten': 10,
        'jack': 10, 'queen': 10, 'king': 10};

    var hitCallbacks = $.Callbacks(),
            placeBetCallbacks = $.Callbacks(),
            standCallbacks = $.Callbacks(),
            splitCallbacks = $.Callbacks(),
            doubleCallbacks = $.Callbacks(),
            resignCallbacks = $.Callbacks(),
            handChangedCallbacks = $.Callbacks();
            
    var currentHand = 0;
    var messages = [];

    function debug(param) {
        if (window.globals.enable_debug === true)
            console.log('%c Debug-View: ' + param, 'color: #FF9900');
    }

    // TODO: implement
    function getHandNumber() {
        return currentHand;
    }

    function PlayerView(playerDetails) {
        var _name = playerDetails.name;
        var _money = playerDetails.money;
        var _status = playerDetails.status;
        var _type = playerDetails.type;
        var _hands = [];

        if (playerDetails.hasOwnProperty('firstBet') && playerDetails.hasOwnProperty('firstBetWage')) {
            if (playerDetails.firstBet.length !== 0) {
                var firstHand = new HandView(playerDetails.firstBetWage);
                var fb = playerDetails.firstBet;
                var len = playerDetails.firstBet.length;
                for (var i = 0; i < len; ++i) {
                    firstHand.addCard(new CardView(fb[i].suit, fb[i].rank));
                }
                _hands.push(firstHand);
            }
        }

        if (playerDetails.hasOwnProperty('secondBet') && playerDetails.hasOwnProperty('secondBetWage')) {
            if (playerDetails.firstBet.length !== 0) {
                var secondHand = new HandView(playerDetails.secondBetWage);
                var sb = playerDetails.secondBet;
                var len2 = playerDetails.secondBet.length;
                for (var i = 0; i < len2; ++i) {
                    secondHand.addCard(new CardView(sb[i].suit, sb[i].rank));
                }
                _hands.push(secondHand);
            }
        }

        var hasFirstHand = function() {
            return _hands.length >= 1;
        };

        var hasSecondHand = function() {
            return _hands.length >= 2;
        };
        
        var getFirstHand = function() {
          return _hands[0];  
        };
        
        var getSecondHand = function() {
          return _hands[1];  
        };

        return {
            name: _name,
            money: _money,
            status: _status,
            type: _type,
            hands: _hands,
            hasFirstHand: hasFirstHand,
            hasSecondHand: hasSecondHand,
            getFirstHand: getFirstHand,
            getSecondHand: getSecondHand
        };
    }

    function HandView(wage) {
        var _cards = [];
        var _value = 0;
        var _wage = wage;

        var addCard = function(card) {
            _cards.push(card);
            _value += card.value();
            if (_value > 21) {
                normalize();
            }
        };

        var getCards = function() {
            return _cards;
        };

        var getWage = function() {
            return _wage;
        };

        var isEmpty = function() {
            return _cards.length === 0;
        };

        var normalize = function() {
            for (var i = 0; i < _cards.length; ++i) {
                if (_cards[i].value() === 11) {
                    _value -= 10;
                    if (_value <= 21)
                        break;
                }
            }
        };

        var value = function() {
            return _value;
        };

        return {
            addCard: addCard,
            value: value,
            getWage: getWage,
            isEmpty: isEmpty,
            getCards: getCards
        };
    }

    function CardView(suit, rank) {
        var _suit, _rank;

        _suit = suit.toLowerCase();
        _rank = rank.toLowerCase();
        

        var toString = function() {
            return _rank + _suit;
        };

        var image = function() {
            return imagesUrl + toString() + '.gif';
        };

        var value = function() {
            return ranks[_rank];
        };

        return {
            value: value,
            image: image,
            toString: toString
        };
    }
    
    function onHandChanged() {
        handChangedCallbacks.fire(currentHand);
    }

    function onPlaceBet(money) {
        debug('### onPlaceBet(money) ###');
        placeBetCallbacks.fire(Number(money));
    }

    function onHit() {
        debug('### onHit() ###');
        var handNumber = getHandNumber();
        hitCallbacks.fire(handNumber);
    }

    function onStand() {
        debug('### onStand() ###');
        var handNumber = getHandNumber();
        standCallbacks.fire(handNumber);
    }

    function onSplit() {
        debug('### onSplit() ###');
        splitCallbacks.fire();
    }

    function onDouble() {
        debug('### onDouble() ###');
        var handNumber = getHandNumber();
        doubleCallbacks.fire(handNumber);
    }

    function onResign() {
        debug('### onResign() ###');
        resignCallbacks.fire();
    }

    function addPlaceBetCallback(f) {
        placeBetCallbacks.add(f);
    }
    
    function addHandChangedCallback(f) {
        handChangedCallbacks.add(f);
    }

    function addHitCallback(f) {
        hitCallbacks.add(f);
    }

    function addStandCallback(f) {
        standCallbacks.add(f);
    }

    function addDoubleCallback(f) {
        doubleCallbacks.add(f);
    }

    function addSplitCallback(f) {
        splitCallbacks.add(f);
    }
    

    function addResignCallback(f) {
        resignCallbacks.add(f);
    }
    
    function newRound() {
        currentHand = 0;
        $('#player-first').addClass('well');
        $('#player-second').removeClass('well');
        $('#player-second').hide();
    }
    
    function clearMessages() {
        $('#game-messages p').fadeOut(400, function() {
            $(this).remove();
        });
    }

    function printMessage(msg) {
        messages.push(msg);
        $('<p/>').text(msg).wrapInner('<b/>').css('color', 'green').appendTo('#game-messages');
    }

    function printError(msg) {
        messages.push(msg);
        $('<p/>').text(msg).wrapInner('<b/>').css('color', 'red').appendTo('#game-messages');
    }

    function printUrgent(msg) {
        messages.push(msg);
        $('<p/>').text(msg).wrapInner('<b/>').css('color', 'orange').appendTo('#game-messages');
    }

    function printLocalPlayer(playerDetails) {
        $('#player-second, #player-first').removeClass('well');
        var player = new PlayerView(playerDetails);
        var jq = $('<p/>')
                .text('Name: ' + player.name + ' Money: ' + player.money + '$')
                .wrapInner('<strong/>');
        $('#player-info-text').html(jq);
        $('#player-first-bet p').remove();
        $('#player-first-hand img').remove();
        $('#player-second-bet p').remove();
        $('#player-second-hand img').remove();
        if (player.hands.length >= 1) {
            if (player.hasFirstHand()) {
                $('#player-first').addClass('well');
                $('#player-first').show();
                $('<p/>')
                        .text('Bet: ' + player.getFirstHand().getWage() + '$ Value: ' + player.getFirstHand().value())
                        .wrapInner('<b/>')
                        .css('color', player.getFirstHand().value() <= 21 ? 'green' : 'red')
                        .appendTo('#player-first-bet');
                for (var i = 0; i < player.getFirstHand().getCards().length; ++i) {
                    var card = player.getFirstHand().getCards()[i];
                    $('<img/>')
                            .attr('src', card.image())
                            .attr('alt', card.toString())
                            .addClass('img-thumbnail')
                            .appendTo('#player-first-hand');
                }
            }
            if (player.hasSecondHand()) {
                $('#player-second').removeClass('well');
                $('#player-second').show();
                $('<p/>')
                        .text('Bet: ' + player.getSecondHand().getWage() + '$ Value: ' + player.getSecondHand().value())
                        .wrapInner('<b/>')
                        .css('color', player.getSecondHand().value() <= 21 ? 'green' : 'red')
                        .appendTo('#player-second-bet');
                for (var i = 0; i < player.getSecondHand().getCards().length; ++i) {
                    var card = player.getSecondHand().getCards()[i];
                    $('<img/>')
                            .attr('src', card.image())
                            .attr('alt', card.toString())
                            .addClass('img-thumbnail')
                            .appendTo('#player-second-hand');
                }
            }
        }
        // player has no hands
        else {
            $('<p/>').text('Bet: ' + '0$' + ' Value: 0')
                    .wrapInner('<b/>')
                    .appendTo('#player-first-bet');
        }
    }

    function printRemotePlayers(playersArr) {
        debug('### printRemotePlayers(playersArr) ###');
        $('#remote-players p').remove();
        for (var i = 0; i < playersArr.length; ++i) {
            printRemotePlayer(playersArr[i]);
        }
    }

    function printDealer(dealerDetails) {
        debug('playerView: dealer');
        var dealer = new PlayerView(dealerDetails);
        $('#dealer-cards img').remove();
        $('#dealer-text h3').remove();
        if (dealer.hasFirstHand()) {
            for (var i = 0; i < dealer.getFirstHand().getCards().length; ++i) {
                var card = dealer.getFirstHand().getCards()[i];
                $('<img/>')
                        .attr('src', card.image())
                        .attr('alt', card.toString())
                        .addClass('img-thumbnail')
                        .appendTo('#dealer-cards');
            }
            $('<h3/>').text('Dealer: ' + dealer.getFirstHand().value()).appendTo('#dealer-text');
        }
        else {
            $('<h3/>').text('Dealer: 0').appendTo('#dealer-text');
        }
    }

    function printRemotePlayer(playerDetails) {
        var pv = new PlayerView(playerDetails);
        if (pv.hands.length >= 1) {
            if (pv.hasFirstHand()) {
                $('<p/>')
                        .text(pv.name + ' ' + ' Money:' + pv.money + '$ Hand: ' + pv.getFirstHand().value())
                        .wrapInner('<b/>')
                        .attr('id', 'remote-first' + pv.name)
                        .appendTo('#remote-players');
                for (var i = 0; i < pv.getFirstHand().getCards().length; ++i) {
                    var card = pv.getFirstHand().getCards()[i];
                    $('<img/>')
                            .attr('src', card.image())
                            .attr('alt', card.toString())
                            .addClass('img-thumbnail')
                            .css('width', '10%')
                            .css('height', '10%')
                            .appendTo('#remote-first' + pv.name);
                }
            }
            if (pv.hasSecondHand()) {
                $('<p/>')
                        .text(pv.name + ' ' + ' Money:' + pv.money + '$ Hand: ' + pv.getFirstHand().value())
                        .wrapInner('<b/>')
                        .attr('id', 'remote-second' + pv.name)
                        .appendTo('#remote-players');
                for (var i = 0; i < pv.getSecondHand().getCards().length; ++i) {
                    var card = pv.getSecondHand().getCards()[i];
                    $('<img/>')
                            .attr('src', card.image())
                            .attr('alt', card.toString())
                            .addClass('img-thumbnail')
                            .css('width', '10%')
                            .css('height', '10%')
                            .appendTo('#remote-second' + pv.name);
                }
            }
        }
        // no hands
        else {
            $('<p/>').text(pv.name + ' ' + ' Money:' + pv.money + '$').wrapInner('<b/>')
                    .css('color', pv.status === 'ACTIVE' ? 'green' : 'orange')
                    .appendTo('#remote-players');
        }
    }

    function printGameDetails(gameDetails) {
        //gameDetails: computerizedPlayers , humanPlayers, joinedHumanPlayers, loadedFromXML, money, name, status
        $('#room-name').text('Game: ' + gameDetails.name);
        switch (gameDetails.status) {
            case 'ACTIVE':
                $('#game-status').hide();
                break;
            case 'WAITING':
                var numWaiting = gameDetails.humanPlayers - gameDetails.joinedHumanPlayers;
                $('#game-status').css('color', 'orange').text('waiting for ' + numWaiting + ' players to join...');
                break;
            case 'FINISHED':
                $('#game-status').css('color', 'red').text('Game is over :(');
                break;
        }
    }

    function init() {
        $('#hit, #double, #split, #stand, #resign').on('click', null, function(event) {
            event.preventDefault();
            switch (event.target.id) {
                case 'hit':
                    onHit();
                    break;
                case 'stand':
                    onStand();
                    break;
                case 'split':
                    onSplit();
                    break;
                case 'double':
                    onDouble();
                    break;
                case 'resign':
                    onResign();
                    break;
            }
        });

        $('#placeBet li a').on('click', null, function() {
            var money = $(this).text().slice(0, -1);
            onPlaceBet(Number(money));
        });
        
        $('#player-first').on('click', null, function() {
            $(this).addClass('well');
            $('#player-second').removeClass('well');
            currentHand = 0;
            onHandChanged();
        });
        
        $('#player-second').on('click', null, function() {
            $(this).addClass('well');
            $('#player-first').removeClass('well');
            currentHand = 1;
            onHandChanged();
        });
    }

    return {
        init: init,
        printMessage: printMessage,
        printUrgent: printUrgent,
        addHitCallback: addHitCallback,
        addPlaceBetCallback: addPlaceBetCallback,
        addStandCallback: addStandCallback,
        addDoubleCallback: addDoubleCallback,
        addSplitCallback: addSplitCallback,
        addResignCallback: addResignCallback,
        addHandChangedCallback: addHandChangedCallback,
        printLocalPlayer: printLocalPlayer,
        printRemotePlayers: printRemotePlayers,
        printDealer: printDealer,
        printError: printError,
        printGameDetails: printGameDetails,
        clearMessages: clearMessages,
        newRound: newRound
    };
})();