var parent=arguments[0], result=[];
const tagName=arguments[1], attributes=arguments[2], attributesLen=arguments[3];	
const screenOffsetX=(window.outerWidth-window.innerWidth)/2+window.screenX+0.0001, screenOffsetY=window.outerHeight-window.innerHeight+window.screenY+0.0001;

if(parent == null){
	parent = window.document;
};

const elts=parent.getElementsByTagName(tagName);
const eltsLength=elts.length;

var addElement=function(e, t, a){
	var rect = e.getBoundingClientRect();
	result[result.length] = [e, t, e.getAttribute('inputmode')=='numeric',e.getAttribute('type')=='password', rect.x+0.0001, rect.y+0.0001, rect.width+0.0001, rect.height+0.0001, rect.left+0.0001, rect.top+0.0001, screenOffsetX, screenOffsetY, a];
};

if(attributesLen == 0){

	for(let h = 0; h < eltsLength; h++){
		var e = elts[h];
		addElement(e, e.tagName, {});
	}

}else{

	let loop=function (e, i, arr){

		let a={};
		const t=e.tagName;
		
		attributes.forEach(function (attributeName) {
			if(attributeName == 'text'){
				if(e.textContent){
					a['text'] = e.textContent.replace(/\xA0/g,' ').trim();
				}
			}else{
				if(attributeName == 'checked' && t == 'INPUT' && (e.type == 'radio' || e.type == 'checkbox')){
					if(e.checked == true){
						a['checked'] = 'true';
					}else{
						a['checked'] = 'false';
					}		
				}else{
					if(e.hasAttribute(attributeName)){
						a[attributeName] = e.getAttribute(attributeName);
					}else{
						var prop = e[attributeName];
						if(prop != null && prop != 'function'){
							a[attributeName] = prop.toString();
						}else{
							a[attributeName] = window.getComputedStyle(e,null).getPropertyValue(attributeName);
						}
					}
				}
			}
		});

		if(Object.keys(a).length == attributesLen){
			addElement(e, t, a);
		}
	};
	
	Array.prototype.slice.call(elts).forEach(loop);
};