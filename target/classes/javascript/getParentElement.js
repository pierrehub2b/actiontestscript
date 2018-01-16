var parent = arguments[0].parentNode;
var result = [];

while (parent != null){
	addElement(parent);
	
	if(parent.nodeName == 'HTML'){
		parent = parent.ownerDocument.defaultView.frameElement;
		while(parent != null){
			addElement(parent);
			parent = parent.ownerDocument.defaultView.frameElement;
		}
	}else{
		parent = parent.parentNode;
	}
};

function addElement(e){
	if(e != null){
		var rect = e.getBoundingClientRect();
		result.push({value:e,tag:e.tagName,x:rect.left+0.00001,y:rect.top+0.00001,width:rect.width+0.00001,height:rect.height+0.00001});
	}
}