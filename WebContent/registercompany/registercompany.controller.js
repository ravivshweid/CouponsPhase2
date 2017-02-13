﻿(function () {
    'use strict';

    angular
        .module('app')
        .controller('RegisterCompanyController', RegisterCompanyController);

    RegisterCompanyController.$inject = ['AuthenticationService', 'UserService', '$location', '$rootScope', 'FlashService'];
    function RegisterCompanyController(AuthenticationService, UserService, $location, $rootScope, FlashService) {
        var vm = this;

        vm.register = register;

        
        function register() {
            vm.dataLoading = true;
            UserService.CreateCustomer(	vm.user.userName, 
            							vm.user.loginName, 
            							vm.user.loginPassword, 
            							vm.user.customerName, 
            							function (response) 
            {
                if (response.data.success === "true") 
                {
                        $location.path('/login');                	                    
                } 
                else 
                {
                    FlashService.Error(response.data.errorMessage);
                    vm.dataLoading = false;
                }
            });
        };
    }

    
})();