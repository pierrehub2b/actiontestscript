let result=[], parent=arguments[0];
const tag=arguments[1], attributes=arguments[2], attributesLen=arguments[3];	
const screenOffsetX=(window.outerWidth-window.innerWidth)/2+window.screenX+0.0001, screenOffsetY=window.outerHeight-window.innerHeight+window.screenY+0.0001;

if(parent == null){
	parent = window.document;
};

const elts=parent.getElementsByTagName(tag);
const eltsLength=elts.length;

let addElement=function(e, a){
	let r = e.getBoundingClientRect();
	result[result.length] = [e, e.tagName, e.getAttribute('inputmode')=='numeric',e.getAttribute('type')=='password', r.x+0.0001, r.y+0.0001, r.width+0.0001, r.height+0.0001, r.left+0.0001, r.top+0.0001, screenOffsetX, screenOffsetY, a];
};

if(attributesLen == 0){

	for(let h = 0; h < eltsLength; h++){
		addElement(elts[h], {});
	}

}else{

	let i=0, loop=function (){

		let e = elts[i], a = {}, j = 0;

		while(j < attributesLen){

			let attributeName = attributes[j];

			if(attributeName == 'text'){
				let textValue = e.textContent;
				if(textValue){
					a['text'] = textValue.replace(/\xA0/g,' ').trim();
				}
			}else{
				if(attributeName == 'checked' && e.tagName == 'INPUT' && (e.type == 'radio' || e.type == 'checkbox')){
					if(e.checked == true){
						a['checked'] = 'true';
					}else{
						a['checked'] = 'false';
					}		
				}else{
					if(e.hasAttribute(attributeName)){
						a[attributeName] = e.getAttribute(attributeName);
					}else{
						let prop = e[attributeName];
						if(prop != null && prop != 'function'){
							a[attributeName] = prop.toString();
						}else{
							a[attributeName] = window.getComputedStyle(e,null).getPropertyValue(attributeName);
						}
					}
				}
			}
			j++;
		}

		if(Object.keys(a).length == attributesLen){
			addElement(e, a);
		}
	};
	
	while (i < eltsLength) {
		loop();
		i++;
	}
};