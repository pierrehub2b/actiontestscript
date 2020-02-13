var parent=arguments[0], result=[];
const tag=arguments[1], attributes=arguments[2], attributesLen=arguments[3];	
const scX=(window.outerWidth-window.innerWidth)/2+window.screenX+0.0001, scY=window.outerHeight-window.innerHeight+window.screenY+0.0001;

if(parent == null){
	parent = window.document;
};

const elts=parent.getElementsByTagName(tag);
const eltsLength=elts.length;

function addElement(e, t, r, a){
	result[result.length] = [e, t, e.getAttribute('inputmode')=='numeric',e.getAttribute('type')=='password', r.x+0.0001, r.y+0.0001, r.width+0.0001, r.height+0.0001, r.left+0.0001, r.top+0.0001, scX, scY, a];
};

if(attributesLen == 0){

	for(let h = 0; h < eltsLength; h++){
		var e = elts[h];
		addElement(e, e.tagName, e.getBoundingClientRect(), {});
	}

}else{

	function loop(e){

		let a={};
		const t=e.tagName;
		
		attributes.forEach(function (attributeName) {
			if(attributeName == 'text'){
				if(e.textContent){
					a['text'] = e.textContent.replace(/\xA0/g,' ').trim();
				}
			}else{
				const type = e.type;
				if(attributeName == 'checked' && t == 'INPUT' && (type == 'radio' || type == 'checkbox')){
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
			addElement(e, t, e.getBoundingClientRect(), a);
		}
	};
	
	Array.prototype.forEach.call(elts, loop);
};