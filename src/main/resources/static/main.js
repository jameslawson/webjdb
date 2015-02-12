var stompClient = null;

function setConnected(connected) {
    document.getElementById('connect').disabled = connected;
    document.getElementById('disconnect').disabled = !connected;
    document.getElementById('step').disabled = !connected;
    document.getElementById('conversationDiv').style.visibility = connected ? 'visible' : 'hidden';
    document.getElementById('response').innerHTML = '';
}

function connect() {
    var socket = new SockJS('/hello');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/minimessage', function(miniMessage){
            console.log('Minimessage received!');
            console.log(miniMessage);
            showMiniMessage(JSON.parse(miniMessage.body).content);
        });
        stompClient.subscribe('/topic/stackframe', function(frame){
            console.log('Stack frame received!');
            showStackFrame(JSON.parse(frame.body).variableEvaluations);
        });
        stompClient.send("/app/start", {}, JSON.stringify({ 'name': name }));
        socket.onclose = function() {
            disconnect();
        };
    });
}

function step() {
    stompClient.send("/app/step", {}, '');
}
function suspend() {
    stompClient.send("/app/suspend", {}, '');
}
function resume() {
    stompClient.send("/app/resume", {}, '');
}
function printframe() {
    stompClient.send("/app/printframe", {}, '');
}

function disconnect() {
    stompClient.disconnect();
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    var name = document.getElementById('name').value;
    stompClient.send("/app/hello", {}, JSON.stringify({ 'name': name }));
}

function showMiniMessage(message) {
    var response = document.getElementById('response');
    var p = document.createElement('p');
    p.style.wordWrap = 'break-word';
    p.appendChild(document.createTextNode(message));
    response.appendChild(p);
}
function showStackFrame(frame) {
    var f = document.getElementById('stackframe');
    f.innerHTML = '';
    console.log(frame);
    Object.keys(frame).forEach(function(name) {
        var evaluation = frame[name];
        var p = document.createElement('p');
        p.appendChild(document.createTextNode(name + ':' + evaluation));
        f.appendChild(p);
    });
}