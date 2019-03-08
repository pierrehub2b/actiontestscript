var elem=arguments[0].parentElement,result=[],ifrm=false;

while (elem != null){
	addElement(elem);
	if (elem.nodeName == 'BODY' || elem.nodeName == 'HTML' || elem.nodeName == '#document' || ifrm){
		ifrm = true;
		elem = elem.ownerDocument.defaultView.frameElement;
	}else{
		elem = elem.parentElement;
	}
};

function addElement(e){
	try{
		let r = e.getBoundingClientRect();
		result[result.length] = [e, e.tagName, e.getAttribute('inputmode')=='numeric', r.x+0.0001, r.y+0.0001, r.width+0.0001, r.height+0.0001, r.left+0.0001, r.top+0.0001, 0.0001, 0.0001, {}];
	}catch(error){
		result[result.length] = [e, '', false, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001, {}];
	}
}