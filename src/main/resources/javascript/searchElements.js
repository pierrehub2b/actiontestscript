let parent=arguments[0];
const tagName=arguments[1], attributes=arguments[2], attributesLen=arguments[3];	
const screenOffsetX=(window.outerWidth-window.innerWidth)/2+window.screenX+0.0001, screenOffsetY=window.outerHeight-window.innerHeight+window.screenY+0.0001;

if(parent == null){
	parent = window.document;
};

const elts=parent.getElementsByTagName(tagName);
const eltsLength=elts.length;

result=[]; 

let addElement=function(e, t, a){
	let rect = e.getBoundingClientRect();
	result[result.length] = [e, t, e.getAttribute('inputmode')=='numeric',e.getAttribute('type')=='password', rect.x+0.0001, rect.y+0.0001, rect.width+0.0001, rect.height+0.0001, rect.left+0.0001, rect.top+0.0001, screenOffsetX, screenOffsetY, a];
};

if(attributesLen == 0){

	for(let h = 0; h < eltsLength; h++){
		let e = elts[h];
		addElement(e, e.tagName, {});
	}

}else{

	let loop=function (e){

		let a={}, j=0;
		const t=e.tagName;

		while(j < attributesLen){

			const attributeName = attributes[j];

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
			addElement(e, t, a);
		}
	};
	
	for (let i=0; i<eltsLength; i++){
		loop(elts[i]);
	}
};