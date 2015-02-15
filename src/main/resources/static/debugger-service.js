var DebuggerService =  {
	connect: function connect (onMessage, onFrame, onClose) {
		var socket = new SockJS('/hello');
		stompClient = Stomp.over(socket);
		stompClient.connect({}, function(frame) {
		    console.log('DebuggerService: Connected: ' + frame);

		    stompClient.subscribe('/topic/minimessage', function(miniMessage){
		        console.log('DebuggerService: Minimessage received!', miniMessage);
		        if(typeof onMessage === 'function'){
		        	onMessage(JSON.parse(miniMessage.body).content);
		        }
		    });

		    stompClient.subscribe('/topic/stackframe', function(frame){
		        console.log('DebuggerService: stackframe received!', frame);
		        if(typeof onFrame === 'function'){
		        	onFrame(JSON.parse(frame.body));
		        }
		    });

		    stompClient.send("/app/start", {}, JSON.stringify({ 'name': name }));

		    socket.onclose = function() {
		    	if(typeof onClose === 'function'){
		    		onClose();
		    	}
		        disconnect();
		    };

		});
	},

	step: function step () {
		stompClient.send("/app/step", {}, '');
	},

	suspend: function suspend () {
		stompClient.send("/app/suspend", {}, '');
	},

	resume: function resume () {
		stompClient.send("/app/resume", {}, '');
	},

	printframe: function printframe () {
		stompClient.send("/app/printframe", {}, '');
	},

	disconnect: function disconnect () {
		stompClient.disconnect();
		console.log("Disconnected");
	},

	sendName: function sendName (name) {
		stompClient.send("/app/hello", {}, JSON.stringify({ 'name': name }));
	}
};