var parent = arguments[0], result = [], iframeOnly = false;

while (parent != null){
	addElement(parent);
	if (parent.nodeName == 'BODY' || parent.nodeName == 'HTML' || parent.nodeName == '#document' || iframeOnly){
		iframeOnly = true;
		parent = parent.ownerDocument.defaultView.frameElement;
	}else{
		parent = parent.parentElement;
	}
};

function addElement(e){
	try{
		let rec = e.getBoundingClientRect();
		result[result.length] = [e, e.tagName, e.getAttribute('inputmode')=='numeric', rec.width+0.0001, rec.height+0.0001, rec.left+0.0001, rec.top+0.0001, 0.0001, 0.0001];
	}catch(error){
		result[result.length] = [e, '', false, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001];
	}
}