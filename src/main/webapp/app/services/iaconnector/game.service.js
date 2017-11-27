(function() {
    'use strict';

    angular
        .module('iaserversnorkunkingApp')
        .factory('IAConnector', IAConnector);

    IAConnector.$inject = ['$resource'];

    function IAConnector ($resource) {
        var service = $resource('api/iaconnector/game/:gameId', {gameId:'@gameId'}, {
            'get': { method: 'GET', params: {}, isArray: false}
        });

        return service;
    }
})();
