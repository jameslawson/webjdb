/*
Debugger
- Controls
- List of Stack Frames / variables
- Code view
*/

var Debugger = React.createClass({
	getInitialState: function(){
		return {
			frames: []
		}
	},

	handleConnect: function(){
		DebuggerService.connect(this.onMessage, this.onFrame);
	},

	onMessage: function onMessage (message) {
		console.info('Got a message', message);
	},

	onFrame: function onFrame (frame) {
		var nextFrames = this.state.frames.concat([frame]); // treat frames as immutable!
		this.setState({frames: nextFrames}); 
	},

	handleStep: function(){
		DebuggerService.step();
	},

	handlePrintFrame: function(){
		DebuggerService.printframe();
	},

	handleDisconnect: function(){
		DebuggerService.disconnect();
	},

	render: function(){
		return (
			<div>
			<button id="connect" onClick={this.handleConnect}>Connect</button>
			<button id="step" onClick={this.handleStep}>Step</button>
			<button id="printframe" onClick={this.handlePrintFrame}>Print Frame</button>
			<button id="disconnect" disabled="disabled" onClick={this.handleDisconnect}>Disconnect</button>

			<DebuggerStackFrames frames={this.state.frames} />
			<DebuggerCodeView />
			</div>
		)
	}
});



var DebuggerStackFrames = React.createClass({
	createFrame: function(frame){
		var variableEvaluations = frame.variableEvaluations;
		// varEvaluations is an object. Turn it into an array of strings.
		var varArray = [];
		Object.keys(variableEvaluations).forEach(function(name) {
		    var evaluation = variableEvaluations[name];
		    varArray.push(<p>{name}: {evaluation}</p>);
		});

		var divStyle = {border: '1px solid black', margin: '2px'};

		return <li style={divStyle}>{varArray}</li>;
	},
	render: function(){
		return (
			<ol>{this.props.frames.map(this.createFrame)}</ol>
		)
	}
});


var DebuggerCodeView = React.createClass({
	render: function(){
		return (
			 <div><p>Todo the code will go here.</p></div>
		)
	}
});

React.render(
  <Debugger />,
  document.getElementById('debugger')
);