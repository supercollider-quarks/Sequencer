
/*

// WARNING: this is experimental. It is not block accurate, due to OSC communication
// Besides, it will accumulate memory when you don't recompile for a long time.
// But it works reasonaly.

{ Sequencer.kr({ 100.rand }, Impulse.kr(10)).poll }.play;
{ Sequencer.kr({ |mx| mx.rand }, Impulse.kr(10), MouseX.kr(0, 100)).poll }.play;
(
{ Sequencer.kr(
	{ |mx, my| [mx.rand, my] }, 
	Impulse.kr(10), 
	[MouseX.kr(0, 100), MouseY.kr(-100, 0)]
).poll }.play;
)


*/

// todo: 
// remove funcs (avoid memory leak)
// multichannelExpand



Sequencer {
	
	classvar <functions;
	classvar langID, responder, cmd; 
	classvar defLookUp;	
	
	*initClass {
		langID = inf.asInteger.rand; // make sure to look up function in the right client
		functions = IdentityDictionary.new;
		defLookUp = IdentityDictionary.new;
		cmd = "/sequencer" ++ langID;
	}
	
	*kr { |func, trig, args|
		
		var name = UGen.buildSynthDef.name.asSymbol;
		var replyID = UniqueID.next;
		
		functions[replyID] = func;
		//defLookUp[name] = defLookUp[name].add(replyID);
		
		this.makeResponder;
		//this.cleanFunctions;
		
		SendReply.kr(trig, cmd, args, replyID);
		^NamedControl.kr("sequencer_in_" ++ replyID, 0.0 ! args.size.max(1));
	}
	
	*cleanFunctions {
		// unfortunately there is currently no way of knowing that a SynthDef has been removed.
		// otherwise, a responder could look up the ids and remove them.
	}
	
	*makeResponder {
		if(responder.isNil) {
			responder = OSCFunc({ |msg, time, addr| this.respond(msg, addr) }, cmd).fix
		}	
	}
	
	*respond { |msg, addr|
		var res, nodeID, replyID, args, func = functions[msg[2]];
		if(func.notNil) {
			#nodeID, replyID ... args = msg[1..];
			res = func.valueArray(args).asOSCArgArray.asArray;
			if(res.notNil) {
				addr.sendMsg(*["/n_setn", nodeID, "sequencer_in_" ++ replyID, res.size] ++ res)
			}
		}
	}
	
}

/*

// lang test
"top".runInTerminal;

{ Sequencer.kr({ 100.rand }, Impulse.kr(MouseX.kr(0, 1000))) }.play;

Sequencer.functions
*/


