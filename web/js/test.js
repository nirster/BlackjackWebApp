/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */

'use strict';

var imagesUrl = window.location.protocol + "//" + window.location.host + '/api/images/';
    var ranks = {'ace': 11, 'two': 2, 'three': 3, 'four': 4, 'five': 5,
    'six': 6, 'seven': 7, 'eight': 8, 'nine': 9, 'ten': 10,
    'jack': 10, 'queen': 10, 'king': 10};
    var suits = ['diamonds', 'spades', 'clubs', 'hearts'];

function PlayerView(playerDetails) {
    var _name = playerDetails.name;
    var _money = playerDetails.money;
    var _status = playerDetails.status;
    var _type = playerDetails.type;

    var firstHand = new HandView(playerDetails.firstBetWage);
    if (playerDetails.hasOwnProperty('firstBet')) {
        var fb = playerDetails.firstBet;
        var len = playerDetails.firstBet.length;
        for (var i = 0; i < len; ++i) {
            firstHand.addCard(new CardView(fb[i].suit, fb[i].rank));
        }
    }

    var secondHand = new HandView(playerDetails.secondBetWage);
    if (playerDetails.hasOwnProperty('secondBet')) {
        var sb = playerDetails.secondBet;
        var len2 = playerDetails.secondBet.length;
        for (var i = 0; i < len2; ++i) {
            secondHand.addCard(new CardView(sb[i].suit, sb[i].rank));
        }
    }

    var getHtml = function() {
        var html = $('<p/>').text(_name + ' ' + _money + ' ' + _status).wrapInner('<b/>');
        if (firstHand.isEmpty() === false) {
            firstHand.getHtml().appendTo(html);
        }
        if (secondHand.isEmpty() === false) {
            secondHand.getHtml().appendTo(html);
        }
        return html;
    };

    return {
        name: _name,
        money: _money,
        status: _status,
        type: _type,
        firstHand: firstHand,
        secondHand: secondHand,
        getHtml: getHtml
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

    var getHtml = function() {
        var html = $('<p/>');
        for (var i = 0; i < _cards.length; ++i) {
            _cards[i].getHtml().appendTo(html);
        }
        return html;
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
        getHtml: getHtml
    };
}

function CardView(suit, rank) {
    var _suit, _rank;

    if (suit && rank) {
        _suit = suit.toLowerCase();
        _rank = rank.toLowerCase();
    }

    var toString = function() {
        return _rank + _suit;
    };

    var image = function() {
        return imagesUrl + toString() + '.gif';
    };

    var value = function() {
        return ranks[_rank];
    };

    var getHtml = function() {
        return $('<img/>').attr('src', image()).attr('alt', toString()).addClass('thumbnail');
    };

    return {
        value: value,
        getHtml: getHtml
    };
}

$(document).ready(function() {
    var c1 = new CardView("DIAMONDS", "SEVEN");
    var c2 = new CardView("CLUBS", "EIGHT");
    var c3 = new CardView("HEARTS", "ACE");
    var hv = new HandView(20);
    hv.addCard(c1);
    hv.addCard(c2);
    hv.addCard(c3);
    
    var s1 = {suit: "DIAMONDS", rank: "NINE"};
    var s2 = {suit: "CLUBS", rank: "TEN"};
    var s3 = {suit: "SPADES", rank: "SEVEN"};
    var s4 = {suit: "HEARTS", rank: "THREE"};
    var pd1 = {name: "pd1", money: 100, status: "ACTIVE", type: "HUMAN", firstBetWage: 30, firstBet: [s1, s2, s3, s4]};
    var pd2 = {name: "pd2", money: 200, status: "ACTIVE", type: "COMPUTER"};
});