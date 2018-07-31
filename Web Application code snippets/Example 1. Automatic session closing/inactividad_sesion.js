//Funcionalidad de inactividad
var modalInstance = null;

// Timeout timer value
var TimeOutTimerValue = 1000000; //50 horas
var TimeOut_Thread = $timeout(function(){ LogoutByTimer() } , TimeOutTimerValue);
var bodyElement = angular.element($document);

$rootScope.$on('eventUpdateTimeOut', function (event, args) {
    TimeOutTimerValue = args.valor;
    $cookieStore.put('timeOut', args.valor);

});

$rootScope.$on("$stateChangeStart", function(event, toState, fromState){

    if (toState.url === "/login") {
        $('#body').addClass('background-login');
    } else {
        $('#body').removeClass('background-login');
    }

});

$rootScope.$state = $state;

angular.forEach(['keydown', 'keyup', 'click', 'mousemove', 'DOMMouseScroll', 'mousewheel', 'mousedown', 'touchstart', 'touchmove', 'scroll', 'focus'],
    function(EventName) {
        bodyElement.bind(EventName, function (e) { TimeOut_Resetter(e) });
    });

function LogoutByTimer(){
    //Se valida el state actual
    if($state.$current.self.parent !== 'account' && $state.$current.self.name !== 'login' ){
        logout();
    }
}

function TimeOut_Resetter(e){
    $timeout.cancel(TimeOut_Thread);
    var sessionTimeOut = $cookieStore.get('timeOut');
    TimeOut_Thread = null;
    if(sessionTimeOut){
        TimeOut_Thread = $timeout(function(){ LogoutByTimer() } , sessionTimeOut);
    }else{
        TimeOut_Thread = $timeout(function(){ LogoutByTimer() } , TimeOutTimerValue);
    }
}

function logout() {
    $uibModalStack.dismissAll();
    BitacoraService.eventSessionExpire(Principal.account(),function () {
        Auth.logout();
        $cookieStore.put('toggle', true);
        $state.go('login');
        $state.reload();
        openDialogInactivity();
    });
}