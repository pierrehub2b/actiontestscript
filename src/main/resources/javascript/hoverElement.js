var e=document.elementFromPoint(arguments[0], arguments[1]);
var result = null;

if(e){
	var rect=e.getBoundingClientRect();
	result = {value:e,tag:e.tagName,x:rect.left+0.00001,y:rect.top+0.00001,width:rect.width+0.00001,height:rect.height+0.00001};
};