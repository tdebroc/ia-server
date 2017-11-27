(function() {
    'use strict';

    angular
        .module('iaserversnorkunkingApp')
        .controller('HomeController', HomeController);

    HomeController.$inject = ['$scope', 'Principal', 'LoginService', '$state', 'IAConnector'];

    function HomeController ($scope, Principal, LoginService, $state, IAConnector) {
        var vm = this;

        function createGame() {
            var game = IAConnector.get();
            console.log("new game is: " + game);
            return game;
        }
    }

})();
