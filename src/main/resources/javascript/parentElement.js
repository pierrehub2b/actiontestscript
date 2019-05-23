var elem=arguments[0].parentElement,result=[];

while (elem != null){
	addElement(elem);
	if(elem.parentElement == null || elem.parentElement.nodeName == 'BODY' || elem.parentElement.nodeName == 'HTML'){
		break;
	}
	elem = elem.parentElement;
};

function addElement(e){
	try{
		let r = e.getBoundingClientRect();
		result[result.length] = [e, e.tagName, e.getAttribute('inputmode')=='numeric',e.getAttribute('type')=='password', r.x+0.0001, r.y+0.0001, r.width+0.0001, r.height+0.0001, r.left+0.0001, r.top+0.0001, 0.0001, 0.0001, {}];
	}catch(error){
		result[result.length] = [e, '', false, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001, {}];
	}
}