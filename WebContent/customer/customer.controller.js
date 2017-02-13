﻿(function () {
    'use strict';

    angular
        .module('app')
        .controller('CustomerController', CustomerController);

    CustomerController.$inject = ['UserService', '$rootScope', 'AuthenticationService'];
    
    function CustomerController(UserService, $rootScope, AuthenticationService) {
        var vm = this;
        
        vm.user = null;
        vm.allUsers = [];
        vm.deleteUser = deleteUser;

        initController();

        function initController() {
        	loadCurrentUser();
            loadAllUsers();
        }

        function loadCurrentUserOld() {
            UserService.GetByUsername($rootScope.globals.currentUser.username)
                .then(function (user) {
                    vm.user = user;
                });
        }

        function loadCurrentUser() {
        	vm.user = $rootScope.globals.currentUser;
        }


        
        function loadAllUsers() {
            UserService.GetAll()
                .then(function (users) {
                    vm.allUsers = users;
                });
        }

        function deleteUser(id) {
            UserService.Delete(id)
            .then(function () {
                loadAllUsers();
            });
        }
    }

})();