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
		parent = parent.parentElement;
	}
};

function addElement(el){
	if(el != null){
		try{
			var rec = el.getBoundingClientRect();
			result.push({value:el,tag:el.tagName,x:rec.left+0.00001,y:rec.top+0.00001,width:rec.width+0.00001,height:rec.height+0.00001});
		}catch(error){
			result.push({value:el,tag:'va',x:0.00001,y:0.00001,width:0.00001,height:0.00001});
		}
	}
}