(function() {
    'use strict';

    angular
        .module('iaserversnorkunkingApp')
        .controller('HomeController', HomeController);

    HomeController.$inject = ['$scope', 'Principal', 'LoginService', '$state', 'IAConnectorGame', 'IAConnectorService'];

    function HomeController ($scope, Principal, LoginService, $state, IAConnectorGame, IAConnectorService) {

        $scope.moves = ["Go Up", "Go Down", "Pick Treasure"];
        $scope.oxygenFactor = 2.0
        $scope.caveCount = 3


        $scope.currentPlayers = {};
        function loadCurrentPlayers() {
            var currentPlayerString = localStorage.getItem("currentPlayers");
            if (!currentPlayerString) {
                $scope.currentPlayers = {};
            } else {
                $scope.currentPlayers = JSON.parse(currentPlayerString);
            }
        }
        loadCurrentPlayers();

        console.log("$scope.currentPlayers", $scope.currentPlayers);

        var vm = this;
        $scope.currentGame;
        $scope.currentIdGame;


        $scope.createGame = function() {
            $scope.oxygenFactor = $("#oxygenFactor").val()
            $scope.caveCount = $("#caveCount").val()
            IAConnectorService.createGame($scope.oxygenFactor, $scope.caveCount)
                .then(function(res) {
                    var game = res.data;
                    console.log("Game is : " + JSON.stringify(game));
                    $scope.currentGame = game;
                    $scope.refreshGameList();
                    $scope.currentIdGame = game.idGame;
                })

        }

        $scope.selectGame = function(idGame) {
            console.log(idGame);
            IAConnectorGame
                .get({idGame : idGame})
                .$promise.then(function(currentGame) {
                    console.log(currentGame);
                    $scope.currentIdGame = idGame;
                    $scope.currentGame = currentGame;
                 });;
        }

        /**
         * Refresh List of Games.
         */
        $scope.refreshGameList = function(callback) {
            console.log("get game list");
            IAConnectorService.getGames(function(res) {
                var games = res.data
                if (!$scope.games) {
                    $scope.selectGame(games[0]);
                }
                $scope.games = games;
                console.log($scope.games);
            })
        }
        $scope.refreshGameList();

        function refreshCurrentGame() {
            $scope.selectGame($scope.currentIdGame);
        }

        $scope.startGame = function() {
            var idGame = $scope.currentIdGame;
            IAConnectorService
                    .startGame(idGame)
                    .then(refreshCurrentGame);
        }

        $scope.addPlayer= function() {
            var idGame = $scope.currentIdGame;
            var playerName = "Player " + ($scope.currentGame.players.length + 1);
            IAConnectorService.addPlayer(idGame, playerName, function(response) {
                var playerInstance = response.data
                var idPlayerTurn = playerInstance.idPlayer;
                var key = getKey(idGame, idPlayerTurn);
                $scope.currentPlayers[key] = playerInstance.UUID || playerInstance.uuid;
                localStorage.setItem("currentPlayers", JSON.stringify($scope.currentPlayers));

                refreshCurrentGame();
            })
        }

        $scope.sendMove = function(move, idPlayerTurn) {
            var playerUUID = $scope.currentPlayers[getKey($scope.currentIdGame, idPlayerTurn)];
            IAConnectorService.sendMove(playerUUID, move).then(
                function(response) {
                    refreshCurrentGame();
                }, function(response) {
                    console.log("response", response)
                    alert(JSON.stringify(response.data));
                }
            )
        }

        function getKey(idGame, idPlayerTurn) {
            return idGame + "#" + idPlayerTurn;
        }

        $scope.isCurrentPlayer = function(currentIdGame, idPlayerTurn) {
            return $scope.currentPlayers[getKey(currentIdGame, idPlayerTurn)]
        }

        $scope.isPlayerTurn = function(game, idPlayerTurn) {
            return game.started && game.currentIdPlayerTurn == idPlayerTurn && !game.finished;
        }


    }


})();
