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
		let rec = e.getBoundingClientRect();
		result[result.length] = [e, e.tagName, e.getAttribute('inputmode')=='numeric', rec.width+0.0001, rec.height+0.0001, rec.left+0.0001, rec.top+0.0001, 0.0001, 0.0001];
	}catch(error){
		result[result.length] = [e, '', false, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001];
	}
}